package sv.edu.udb.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.repository.ClienteRepository;
import sv.edu.udb.repository.ParticipanteRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecepcionistaDashboardService {

    private final ClienteRepository clienteRepository;
    private final ParticipanteRepository participanteRepository;
    private final CotizacionService cotizacionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public long contarClientes() {
        return clienteRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarParticipantes() {
        return participanteRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarCotizacionesPendientes() {
        return cotizacionService.contarPendientesDeAprobacion();
    }

    // Inscripción, Alquiler y Solicitud de catering todavía no tienen entidad JPA en el proyecto
    // (módulos en desarrollo por Recepcionista). Se cuentan con SQL nativo de solo lectura para no
    // acoplar el panel a clases que aún pueden cambiar; si la tabla no existe se devuelve null.
    // Van sin @Transactional a propósito: un error de SQL dentro de una transacción la marcaría
    // como rollback-only y el panel fallaría igual aunque se capture la excepción.

    public Long contarInscripciones() {
        return contarFilas("inscripcion");
    }

    public Long contarAlquileres() {
        return contarFilas("alquiler");
    }

    public Long contarSolicitudesCatering() {
        return contarFilas("solicitud_catering");
    }

    private Long contarFilas(String tabla) {
        try {
            Number total = (Number) entityManager
                    .createNativeQuery("SELECT COUNT(*) FROM " + tabla)
                    .getSingleResult();
            return total.longValue();
        } catch (RuntimeException excepcion) {
            log.warn("No se pudo contar la tabla {} para el panel de Recepcionista: {}", tabla, excepcion.getMessage());
            return null;
        }
    }
}
