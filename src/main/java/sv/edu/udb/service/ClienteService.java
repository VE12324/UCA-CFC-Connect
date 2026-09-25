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
import sv.edu.udb.model.Usuario;
import sv.edu.udb.repository.ClienteRepository;
/**
 *
 * @author crist
 */
@Service
@RequiredArgsConstructor
public class ClienteService {
        
    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public Page<Cliente> listarClientes(
            String busqueda,
            Pageable pageable) {

        if (busqueda == null
                || busqueda.isBlank()) {

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
                    "No se encontró el cliente con ID: "
                    + id
                ));
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorCorreoUsuario(
            String correo) {

        return clienteRepository
                .findByUsuario_EmailIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontrado(
                    "No se encontró un cliente asociado "
                    + "con la cuenta iniciada"
                ));
    }

    @Transactional
    public Cliente registrarCliente(Cliente cliente) {

        validarDuiDuplicado(
                cliente.getDui(),
                null
        );

        if (Boolean.TRUE.equals(
                cliente.getCrearCuenta())) {

            validarDatosNuevaCuenta(cliente);

            Usuario usuario
                    = usuarioService.crearCuentaCliente(
                            cliente.getNombre(),
                            cliente.getCorreo(),
                            cliente.getPassword()
                    );

            cliente.setUsuario(usuario);
        }

        limpiarDatosTemporales(cliente);

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizarCliente(
            Integer id,
            Cliente datosCliente) {

        Cliente cliente = buscarPorId(id);

        validarDuiDuplicado(
                datosCliente.getDui(),
                id
        );

        cliente.setDui(
                datosCliente.getDui()
        );

        cliente.setNombre(
                datosCliente.getNombre()
        );

        cliente.setEmpresa(
                datosCliente.getEmpresa()
        );

        cliente.setCorreo(
                datosCliente.getCorreo()
        );

        cliente.setTelefono(
                datosCliente.getTelefono()
        );

        cliente.setDireccion(
                datosCliente.getDireccion()
        );

        if (cliente.getUsuario() != null) {

            validarCambioPassword(datosCliente);

            Usuario usuarioActualizado
                    = usuarioService
                            .actualizarCuentaCliente(
                                    cliente.getUsuario(),
                                    datosCliente.getNombre(),
                                    datosCliente.getCorreo(),
                                    datosCliente.getPassword()
                            );

            cliente.setUsuario(usuarioActualizado);

        } else if (Boolean.TRUE.equals(
                datosCliente.getCrearCuenta())) {

            validarDatosNuevaCuenta(datosCliente);

            Usuario nuevoUsuario
                    = usuarioService
                            .crearCuentaCliente(
                                    datosCliente.getNombre(),
                                    datosCliente.getCorreo(),
                                    datosCliente.getPassword()
                            );

            cliente.setUsuario(nuevoUsuario);
        }

        limpiarDatosTemporales(cliente);

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminarCliente(Integer id) {

        Cliente cliente = buscarPorId(id);
        Usuario usuario = cliente.getUsuario();

        clienteRepository.delete(cliente);
        clienteRepository.flush();

        if (usuario != null) {
            usuarioService.eliminar(
                    usuario.getId()
            );
        }
    }

    private void validarDuiDuplicado(
            String dui,
            Integer idClienteActual) {

        if (dui == null || dui.isBlank()) {
            return;
        }

        clienteRepository.findByDui(dui)
                .filter(cliente
                        -> idClienteActual == null
                        || !cliente.getIdCliente()
                                .equals(idClienteActual)
                )
                .ifPresent(cliente -> {
                    throw new IllegalArgumentException(
                            "Ya existe un cliente registrado "
                            + "con el DUI: " + dui
                    );
                });
    }

    private void validarDatosNuevaCuenta(
            Cliente cliente) {

        if (cliente.getCorreo() == null
                || cliente.getCorreo().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar un correo "
                    + "para crear la cuenta"
            );
        }

        if (cliente.getPassword() == null
                || cliente.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar una contraseña "
                    + "para crear la cuenta"
            );
        }

        if (cliente.getConfirmarPassword() == null
                || cliente.getConfirmarPassword()
                        .isBlank()) {

            throw new IllegalArgumentException(
                    "Debe confirmar la contraseña"
            );
        }

        if (!cliente.getPassword().equals(
                cliente.getConfirmarPassword())) {

            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden"
            );
        }
    }

    private void validarCambioPassword(
            Cliente cliente) {

        if (cliente.getPassword() == null
                || cliente.getPassword().isBlank()) {
            return;
        }

        if (cliente.getConfirmarPassword() == null
                || cliente.getConfirmarPassword()
                        .isBlank()) {

            throw new IllegalArgumentException(
                    "Debe confirmar la nueva contraseña"
            );
        }

        if (!cliente.getPassword().equals(
                cliente.getConfirmarPassword())) {

            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden"
            );
        }
    }

    private void limpiarDatosTemporales(
            Cliente cliente) {

        cliente.setCrearCuenta(false);
        cliente.setPassword(null);
        cliente.setConfirmarPassword(null);
    }
}
