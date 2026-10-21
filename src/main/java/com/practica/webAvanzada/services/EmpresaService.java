package com.practica.webAvanzada.services;

import com.practica.webAvanzada.models.Empresa;
import com.practica.webAvanzada.repositories.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpresaService {
    private final EmpresaRepository empresaRepository;

    public Empresa guardar(Empresa empresa) { return empresaRepository.save(empresa); }
    public List<Empresa> obtenerTodos() { return empresaRepository.findAll(); }
    public Optional<Empresa> obtenerPorId(Long id) { return empresaRepository.findById(id); }
    public void eliminar(Long id) { empresaRepository.deleteById(id); }

    public Empresa actualizar(Long id, Empresa empresaActualizada) {
        if (empresaRepository.existsById(id)) {
            empresaActualizada.setIdEmpresa(id);
            return empresaRepository.save(empresaActualizada);
        }
        return null;
    }
}