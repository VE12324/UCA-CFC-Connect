/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.repository.ClienteRepository;
/**
 *
 * @author crist
 */
@Service
@RequiredArgsConstructor
public class ClienteService {
        
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public Page<Cliente> listarClientes(String busqueda, Pageable pageable) {

        if (busqueda == null || busqueda.isBlank()) {
            return clienteRepository.findAll(pageable);
        }

        return clienteRepository
                .findByNombreContainingIgnoreCaseOrDuiContainingIgnoreCaseOrEmpresaContainingIgnoreCase(
                        busqueda,
                        busqueda,
                        busqueda,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el cliente con ID: " + id
                ));
    }

    @Transactional
    public Cliente registrarCliente(Cliente cliente) {

        validarDuiDuplicado(cliente.getDui(), null);

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizarCliente(Integer id, Cliente datosCliente) {

        Cliente cliente = buscarPorId(id);

        validarDuiDuplicado(datosCliente.getDui(), id);

        cliente.setDui(datosCliente.getDui());
        cliente.setNombre(datosCliente.getNombre());
        cliente.setEmpresa(datosCliente.getEmpresa());
        cliente.setCorreo(datosCliente.getCorreo());
        cliente.setTelefono(datosCliente.getTelefono());
        cliente.setDireccion(datosCliente.getDireccion());

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminarCliente(Integer id) {

        Cliente cliente = buscarPorId(id);

        clienteRepository.delete(cliente);
    }

    private void validarDuiDuplicado(String dui, Integer idClienteActual) {

        if (dui == null || dui.isBlank()) {
            return;
        }

        clienteRepository.findByDui(dui)
                .filter(cliente -> idClienteActual == null
                        || !cliente.getIdCliente().equals(idClienteActual))
                .ifPresent(cliente -> {
                    throw new IllegalArgumentException(
                            "Ya existe un cliente registrado con el DUI: " + dui
                    );
                });
    }
}
