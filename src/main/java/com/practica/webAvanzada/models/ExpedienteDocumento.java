package com.practica.webAvanzada.models;

import com.practica.webAvanzada.models.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "expedientes_documentos")
public class ExpedienteDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocumento;

    @ManyToOne
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    // Nombre con el que se guardó el archivo en el directorio de uploads
    @Column(nullable = false)
    private String rutaArchivoUrl;

    private String nombreArchivoOriginal;

    @Column(nullable = false)
    private LocalDate fechaSubida;

    // null = pendiente de revisión, true = aprobado, false = rechazado
    private Boolean estadoAprobacion;
}
