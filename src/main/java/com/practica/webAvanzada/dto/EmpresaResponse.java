package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.Empresa;

public record EmpresaResponse(
        Long idEmpresa,
        Long idUsuario,
        String correo,
        String razonSocial,
        String sector,
        String nombreContacto,
        String telefono,
        String direccion,
        Boolean estadoVerificacion
) {
    public static EmpresaResponse de(Empresa empresa) {
        return new EmpresaResponse(empresa.getIdEmpresa(), empresa.getUsuario().getIdUsuario(),
                empresa.getUsuario().getCorreo(), empresa.getRazonSocial(), empresa.getSector(),
                empresa.getNombreContacto(), empresa.getTelefono(), empresa.getDireccion(),
                empresa.getEstadoVerificacion());
    }
}
