package com.practica.webAvanzada;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Recorre el ciclo completo de una residencia contra la API real (H2 en memoria)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlujoResidenciaIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void cicloCompletoDeUnaResidencia() throws Exception {
        String admin = login("admin@test.com", "Admin123!");

        // Registro público de empresa y alumno
        String idEmpresa = leer(enviar(post("/api/auth/registro/empresa"), null, """
                {"correo":"rh@acme.com","password":"Empresa123","razonSocial":"ACME SA","nombreContacto":"Ana"}""")
                .andExpect(status().isCreated()), "$.idEmpresa");
        enviar(post("/api/auth/registro/alumno"), null, """
                {"correo":"alumno@test.com","password":"Alumno123","numeroControl":"20240001",
                 "nombreCompleto":"Luis Pérez","carrera":"ISC","semestre":9}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoResidencia").value("PENDIENTE"));

        String empresa = login("rh@acme.com", "Empresa123");
        String alumno = login("alumno@test.com", "Alumno123");
        String proyectoJson = """
                {"titulo":"Sistema de inventarios","tecnologiasRequeridas":"Java, Spring","cupoAlumnos":1}""";

        // Sin verificar, la empresa no puede publicar
        enviar(post("/api/proyectos"), empresa, proyectoJson).andExpect(status().isConflict());
        enviar(patch("/api/empresas/" + idEmpresa + "/verificacion"), admin, "{\"verificada\":true}")
                .andExpect(status().isOk());
        String idProyecto = leer(enviar(post("/api/proyectos"), empresa, proyectoJson)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE")), "$.idProyecto");

        // Postulación (no se permite duplicarla)
        String postulacionJson = "{\"idProyecto\":" + idProyecto + "}";
        String idPostulacion = leer(enviar(post("/api/postulaciones"), alumno, postulacionJson)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_REVISION")), "$.idPostulacion");
        enviar(post("/api/postulaciones"), alumno, postulacionJson).andExpect(status().isConflict());

        // La empresa acepta: el alumno pasa a EN_PROCESO y el proyecto (cupo 1) queda ASIGNADO
        enviar(patch("/api/postulaciones/" + idPostulacion + "/estado"), empresa, "{\"estado\":\"ACEPTADO\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADO"))
                .andExpect(jsonPath("$.estadoResidencia").value("EN_PROCESO"));
        mvc.perform(get("/api/proyectos/" + idProyecto).header("Authorization", "Bearer " + alumno))
                .andExpect(jsonPath("$.estado").value("ASIGNADO"));

        // Alta de asesor y asignación
        String idAsesor = leer(enviar(post("/api/asesores"), admin, """
                {"correo":"asesor@test.com","password":"Asesor123","nombreCompleto":"Dra. Ruiz","maximoAlumnos":5}""")
                .andExpect(status().isCreated()), "$.idAsesor");
        enviar(patch("/api/postulaciones/" + idPostulacion + "/asesor"), admin, "{\"idAsesor\":" + idAsesor + "}")
                .andExpect(status().isOk());
        String asesor = login("asesor@test.com", "Asesor123");

        // No se puede finalizar sin reporte final aprobado
        mvc.perform(patch("/api/postulaciones/" + idPostulacion + "/finalizar").header("Authorization", "Bearer " + asesor))
                .andExpect(status().isConflict());

        // El alumno sube el reporte final y el asesor lo aprueba
        MockMultipartFile pdf = new MockMultipartFile("archivo", "reporte.pdf", "application/pdf", "%PDF-1.4 contenido".getBytes());
        String idDocumento = leer(mvc.perform(multipart("/api/documentos").file(pdf)
                        .param("idPostulacion", idPostulacion)
                        .param("tipoDocumento", "REPORTE_FINAL")
                        .header("Authorization", "Bearer " + alumno))
                .andExpect(status().isCreated()), "$.idDocumento");
        enviar(patch("/api/documentos/" + idDocumento + "/revision"), asesor, "{\"aprobado\":true}")
                .andExpect(status().isOk());
        mvc.perform(get("/api/documentos/" + idDocumento + "/archivo").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isOk());

        mvc.perform(patch("/api/postulaciones/" + idPostulacion + "/finalizar").header("Authorization", "Bearer " + asesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoResidencia").value("FINALIZADA"));
    }

    @Test
    void seguridadYValidaciones() throws Exception {
        mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());

        enviar(post("/api/auth/registro/alumno"), null, """
                {"correo":"seguridad@test.com","password":"Alumno123","numeroControl":"20249999",
                 "nombreCompleto":"Eva","carrera":"ISC","semestre":8}""")
                .andExpect(status().isCreated());
        String alumno = login("seguridad@test.com", "Alumno123");

        // La contraseña nunca se expone y un alumno no accede a endpoints de admin
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isForbidden());

        enviar(post("/api/auth/login"), null, "{\"correo\":\"seguridad@test.com\",\"password\":\"incorrecta\"}")
                .andExpect(status().isUnauthorized());

        enviar(post("/api/auth/registro/alumno"), null, "{\"correo\":\"no-es-correo\",\"password\":\"123\",\"semestre\":30}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.password").exists())
                .andExpect(jsonPath("$.errores.semestre").exists());
    }

    @Test
    void documentacionOpenApiEsPublica() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists());
    }

    private String login(String correo, String password) throws Exception {
        String json = "{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}";
        return leer(enviar(post("/api/auth/login"), null, json).andExpect(status().isOk()), "$.token");
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String token, String json) throws Exception {
        peticion.contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(peticion);
    }

    private String leer(ResultActions resultado, String ruta) throws Exception {
        Object valor = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), ruta);
        return String.valueOf(valor);
    }
}
