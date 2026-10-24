package com.practica.webAvanzada.services;

import com.practica.webAvanzada.exceptions.RecursoNoEncontradoException;
import com.practica.webAvanzada.exceptions.SolicitudInvalidaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Service
public class AlmacenamientoArchivosService {

    private static final byte[] CABECERA_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final Path directorio;

    public AlmacenamientoArchivosService(@Value("${app.uploads.dir}") String directorio) {
        this.directorio = Paths.get(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de archivos " + this.directorio, e);
        }
    }

    // Solo se aceptan PDF; se comprueba la cabecera real del archivo, no solo la extensión
    public String guardarPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("El archivo está vacío");
        }
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".pdf")) {
            throw new SolicitudInvalidaException("Solo se permiten archivos PDF");
        }
        try {
            try (InputStream entrada = archivo.getInputStream()) {
                if (!Arrays.equals(entrada.readNBytes(CABECERA_PDF.length), CABECERA_PDF)) {
                    throw new SolicitudInvalidaException("El archivo no es un PDF válido");
                }
            }
            String nombreAlmacenado = UUID.randomUUID() + ".pdf";
            try (InputStream entrada = archivo.getInputStream()) {
                Files.copy(entrada, directorio.resolve(nombreAlmacenado));
            }
            return nombreAlmacenado;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
    }

    public Resource cargar(String nombreAlmacenado) {
        Path ruta = directorio.resolve(nombreAlmacenado).normalize();
        if (!ruta.startsWith(directorio) || !Files.isRegularFile(ruta)) {
            throw new RecursoNoEncontradoException("Archivo", nombreAlmacenado);
        }
        return new FileSystemResource(ruta);
    }

    public void eliminar(String nombreAlmacenado) {
        Path ruta = directorio.resolve(nombreAlmacenado).normalize();
        if (!ruta.startsWith(directorio)) {
            return;
        }
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo eliminar el archivo", e);
        }
    }
}
