/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.model.Cliente;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.repository.AlquilerRepository;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.EspacioRepository;
/**
 *
 * @author crist
 */
@Service
@RequiredArgsConstructor
public class AlquilerService {
    
     private static final Set<String> ESTADOS_PERMITIDOS = Set.of(
            "PENDIENTE",
            "CONFIRMADO",
            "CANCELADO",
            "FINALIZADO"
    );

    private final AlquilerRepository alquilerRepository;
    private final ClienteRepository clienteRepository;
    private final EspacioRepository espacioRepository;

    @Transactional(readOnly = true)
    public Page<Alquiler> listarAlquileres(
            String busqueda,
            Pageable paginacion) {

        if (busqueda == null || busqueda.isBlank()) {
            return alquilerRepository.findAll(paginacion);
        }

        return alquilerRepository
                .findByClienteNombreContainingIgnoreCaseOrEspacioNombreContainingIgnoreCase(
                        busqueda,
                        busqueda,
                        paginacion
                );
    }

    @Transactional(readOnly = true)
    public Alquiler buscarPorId(Integer id) {

        return alquilerRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No se encontró el alquiler con ID: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {

        return clienteRepository.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional(readOnly = true)
    public List<Espacio> listarEspacios() {

        return espacioRepository.findAll(
                Sort.by("nombre").ascending()
        );
    }

    @Transactional
    public Alquiler registrarAlquiler(
            Alquiler alquiler) {

        asignarClienteYEspacio(alquiler);
        validarAlquiler(alquiler, null);

        return alquilerRepository.save(alquiler);
    }

    @Transactional
    public Alquiler actualizarAlquiler(
            Integer id,
            Alquiler datosAlquiler) {

        Alquiler alquiler = buscarPorId(id);

        alquiler.setFecha(
                datosAlquiler.getFecha()
        );

        alquiler.setHoraInicio(
                datosAlquiler.getHoraInicio()
        );

        alquiler.setHoraFin(
                datosAlquiler.getHoraFin()
        );

        alquiler.setCantidadPersonas(
                datosAlquiler.getCantidadPersonas()
        );

        alquiler.setEstado(
                datosAlquiler.getEstado()
        );

        alquiler.setCliente(
                datosAlquiler.getCliente()
        );

        alquiler.setEspacio(
                datosAlquiler.getEspacio()
        );

        asignarClienteYEspacio(alquiler);
        validarAlquiler(alquiler, id);

        return alquilerRepository.save(alquiler);
    }

    @Transactional
    public void eliminarAlquiler(Integer id) {

        Alquiler alquiler = buscarPorId(id);

        try {
            alquilerRepository.delete(alquiler);
            alquilerRepository.flush();

        } catch (DataIntegrityViolationException excepcion) {

            throw new IllegalArgumentException(
                    "No se puede eliminar el alquiler porque tiene pagos, actividades u otros registros asociados."
            );
        }
    }

    private void asignarClienteYEspacio(
            Alquiler alquiler) {

        if (alquiler.getCliente() == null
                || alquiler.getCliente()
                        .getIdCliente() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un cliente"
            );
        }

        if (alquiler.getEspacio() == null
                || alquiler.getEspacio()
                        .getIdEspacio() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un espacio"
            );
        }

        Cliente cliente = clienteRepository.findById(
                alquiler.getCliente().getIdCliente()
        ).orElseThrow(() -> new IllegalArgumentException(
                "El cliente seleccionado no existe"
        ));

        Espacio espacio = espacioRepository.findById(
                alquiler.getEspacio().getIdEspacio()
        ).orElseThrow(() -> new IllegalArgumentException(
                "El espacio seleccionado no existe"
        ));

        alquiler.setCliente(cliente);
        alquiler.setEspacio(espacio);
    }

    private void validarAlquiler(
            Alquiler alquiler,
            Integer idAlquilerActual) {

        if (alquiler.getFecha() == null) {

            throw new IllegalArgumentException(
                    "La fecha del alquiler es obligatoria"
            );
        }

        if (alquiler.getHoraInicio() == null) {

            throw new IllegalArgumentException(
                    "La fecha y hora de inicio son obligatorias"
            );
        }

        if (alquiler.getHoraFin() == null) {

            throw new IllegalArgumentException(
                    "La fecha y hora de finalización son obligatorias"
            );
        }

        if (!alquiler.getHoraFin()
                .isAfter(alquiler.getHoraInicio())) {

            throw new IllegalArgumentException(
                    "La hora de finalización debe ser posterior a la hora de inicio"
            );
        }

        if (!alquiler.getFecha().equals(
                alquiler.getHoraInicio().toLocalDate()
        )) {

            throw new IllegalArgumentException(
                    "La fecha del alquiler debe coincidir con la fecha de inicio"
            );
        }

        if (alquiler.getCantidadPersonas() == null
                || alquiler.getCantidadPersonas() < 1) {

            throw new IllegalArgumentException(
                    "La cantidad de personas debe ser al menos 1"
            );
        }

        if (alquiler.getCantidadPersonas()
                > alquiler.getEspacio().getCapacidad()) {

            throw new IllegalArgumentException(
                    "La cantidad de personas supera la capacidad del espacio seleccionado"
            );
        }

        if ("MANTENIMIENTO".equalsIgnoreCase(
                alquiler.getEspacio().getEstado()
        )) {

            throw new IllegalArgumentException(
                    "El espacio seleccionado se encuentra en mantenimiento"
            );
        }

        if (alquiler.getEstado() == null
                || alquiler.getEstado().isBlank()) {

            alquiler.setEstado("PENDIENTE");
        }

        String estadoNormalizado = alquiler.getEstado()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!ESTADOS_PERMITIDOS.contains(
                estadoNormalizado
        )) {

            throw new IllegalArgumentException(
                    "El estado del alquiler no es válido"
            );
        }

        alquiler.setEstado(estadoNormalizado);

        if (!"CANCELADO".equals(estadoNormalizado)) {

            boolean existeConflicto
                    = alquilerRepository.existeConflictoHorario(
                            alquiler.getEspacio()
                                    .getIdEspacio(),
                            alquiler.getHoraInicio(),
                            alquiler.getHoraFin(),
                            idAlquilerActual
                    );

            if (existeConflicto) {

                throw new IllegalArgumentException(
                        "El espacio ya está reservado durante el horario seleccionado"
                );
            }
        }
    }
}
