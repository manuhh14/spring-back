package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.EmpresaResponse;
import com.practica.webAvanzada.dto.EmpresaUpdateRequest;
import com.practica.webAvanzada.dto.RegistroEmpresaRequest;
import com.practica.webAvanzada.exceptions.AccesoDenegadoException;
import com.practica.webAvanzada.exceptions.RecursoNoEncontradoException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.Empresa;
import com.practica.webAvanzada.models.enums.RoleUsuario;
import com.practica.webAvanzada.repositories.EmpresaRepository;
import com.practica.webAvanzada.repositories.ProyectoResidenciaRepository;
import com.practica.webAvanzada.repositories.UsuarioRepository;
import com.practica.webAvanzada.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpresaService {
    private final EmpresaRepository empresaRepository;
    private final ProyectoResidenciaRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public Page<EmpresaResponse> obtenerTodos(Pageable pageable) {
        return empresaRepository.findAll(pageable).map(EmpresaResponse::de);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPorId(Long id) {
        return EmpresaResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPerfil(UsuarioActual actual) {
        return EmpresaResponse.de(buscarPorUsuario(actual.idUsuario()));
    }

    // La empresa queda sin verificar hasta que un administrador la revise
    @Transactional
    public EmpresaResponse registrar(RegistroEmpresaRequest request) {
        Empresa empresa = new Empresa();
        empresa.setUsuario(usuarioService.crearUsuario(request.correo(), request.password(), RoleUsuario.EMPRESA));
        empresa.setRazonSocial(request.razonSocial());
        empresa.setSector(request.sector());
        empresa.setNombreContacto(request.nombreContacto());
        empresa.setTelefono(request.telefono());
        empresa.setDireccion(request.direccion());
        empresa.setEstadoVerificacion(false);
        return EmpresaResponse.de(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaResponse actualizar(Long id, EmpresaUpdateRequest request, UsuarioActual actual) {
        Empresa empresa = buscar(id);
        if (!actual.esAdmin() && !empresa.getUsuario().getIdUsuario().equals(actual.idUsuario())) {
            throw new AccesoDenegadoException("Solo puedes modificar los datos de tu empresa");
        }
        empresa.setRazonSocial(request.razonSocial());
        empresa.setSector(request.sector());
        empresa.setNombreContacto(request.nombreContacto());
        empresa.setTelefono(request.telefono());
        empresa.setDireccion(request.direccion());
        return EmpresaResponse.de(empresa);
    }

    @Transactional
    public EmpresaResponse cambiarVerificacion(Long id, boolean verificada) {
        Empresa empresa = buscar(id);
        empresa.setEstadoVerificacion(verificada);
        return EmpresaResponse.de(empresa);
    }

    @Transactional
    public void eliminar(Long id) {
        Empresa empresa = buscar(id);
        if (proyectoRepository.existsByEmpresaIdEmpresa(id)) {
            throw new ReglaNegocioException("La empresa tiene proyectos registrados; desactiva su usuario en su lugar");
        }
        empresaRepository.delete(empresa);
        usuarioRepository.delete(empresa.getUsuario());
    }

    public Empresa buscarPorUsuario(Long idUsuario) {
        return empresaRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de empresa del usuario", idUsuario));
    }

    private Empresa buscar(Long id) {
        return empresaRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Empresa", id));
    }
}
