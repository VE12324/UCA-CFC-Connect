package sv.edu.udb.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.ProgramaPortalDTO;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Categoria;
import sv.edu.udb.model.Espacio;
import sv.edu.udb.model.Modalidad;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.repository.CategoriaRepository;
import sv.edu.udb.repository.CateringRepository;
import sv.edu.udb.repository.CursoRepository;
import sv.edu.udb.repository.DiplomadoRepository;
import sv.edu.udb.repository.EspacioRepository;
import sv.edu.udb.repository.ModalidadRepository;

@Service
@RequiredArgsConstructor
public class PortalPublicoService {

    public static final int TAMANO_PAGINA_OFERTA = 12;

    private static final String ACTIVO = "ACTIVO";

    private final CursoRepository cursoRepository;
    private final DiplomadoRepository diplomadoRepository;
    private final EspacioRepository espacioRepository;
    private final CateringRepository cateringRepository;
    private final CategoriaRepository categoriaRepository;
    private final ModalidadRepository modalidadRepository;
    private final Clock reloj;

    /** Cursos y diplomados ACTIVO ordenados por fecha de inicio. */
    @Transactional(readOnly = true)
    public List<ProgramaPortalDTO> listarProgramasActivos() {

        Stream<ProgramaPortalDTO> cursos = cursoRepository.findAll().stream()
                .filter(curso -> ACTIVO.equalsIgnoreCase(curso.getEstado()))
                .map(ProgramaPortalDTO::desde);

        Stream<ProgramaPortalDTO> diplomados = diplomadoRepository.findAll().stream()
                .filter(diplomado -> ACTIVO.equalsIgnoreCase(diplomado.getEstado()))
                .map(ProgramaPortalDTO::desde);

        return Stream.concat(cursos, diplomados)
                .sorted(Comparator
                        .comparing(ProgramaPortalDTO::fechaInicio,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ProgramaPortalDTO::nombre))
                .toList();
    }

    /**
     * Programas activos con la fecha de inicio más cercana a hoy.
     */
    @Transactional(readOnly = true)
    public List<ProgramaPortalDTO> listarDestacados(int cantidad) {

        LocalDate hoy = LocalDate.now(reloj);

        return listarProgramasActivos().stream()
                .filter(programa -> programa.fechaInicio() != null)
                .sorted(Comparator
                        .comparingLong((ProgramaPortalDTO programa) -> Math.abs(
                                ChronoUnit.DAYS.between(hoy, programa.fechaInicio())))
                        .thenComparing(programa -> programa.fechaInicio().isBefore(hoy)))
                .limit(cantidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProgramaPortalDTO> buscarProximoEvento() {
        return listarDestacados(1).stream().findFirst();
    }

    /** "PRÓXIMO EVENTO", "EN CURSO" o "EVENTO RECIENTE". */
    public String etiquetaEvento(ProgramaPortalDTO programa) {

        LocalDate hoy = LocalDate.now(reloj);

        if (!programa.fechaInicio().isBefore(hoy)) {
            return "PRÓXIMO EVENTO";
        }

        if (programa.fechaFin() != null && !programa.fechaFin().isBefore(hoy)) {
            return "EN CURSO";
        }

        return "EVENTO RECIENTE";
    }

    /**
     * Oferta filtrada. tipo: "cursos", "diplomados" o vacío (todos).
     */
    @Transactional(readOnly = true)
    public Page<ProgramaPortalDTO> buscarOferta(
            String tipo,
            Integer idCategoria,
            Integer idModalidad,
            String texto,
            int pagina) {

        String busqueda = texto == null
                ? ""
                : texto.trim().toLowerCase(Locale.ROOT);

        List<ProgramaPortalDTO> filtrados = listarProgramasActivos().stream()
                .filter(programa -> !"cursos".equals(tipo) || programa.esCurso())
                .filter(programa -> !"diplomados".equals(tipo) || !programa.esCurso())
                .filter(programa -> idCategoria == null
                        || idCategoria.equals(programa.idCategoria()))
                .filter(programa -> idModalidad == null
                        || idModalidad.equals(programa.idModalidad()))
                .filter(programa -> busqueda.isEmpty()
                        || programa.nombre().toLowerCase(Locale.ROOT).contains(busqueda))
                .toList();

        Pageable paginacion = PageRequest.of(
                Math.max(pagina, 0),
                TAMANO_PAGINA_OFERTA
        );

        int desde = (int) Math.min(paginacion.getOffset(), filtrados.size());
        int hasta = Math.min(desde + TAMANO_PAGINA_OFERTA, filtrados.size());

        return new PageImpl<>(
                new ArrayList<>(filtrados.subList(desde, hasta)),
                paginacion,
                filtrados.size()
        );
    }

    @Transactional(readOnly = true)
    public ProgramaPortalDTO buscarCursoActivo(Integer id) {

        return cursoRepository.findById(id)
                .filter(curso -> ACTIVO.equalsIgnoreCase(curso.getEstado()))
                .map(ProgramaPortalDTO::desde)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "El curso solicitado no existe o no está disponible."
                ));
    }

    @Transactional(readOnly = true)
    public ProgramaPortalDTO buscarDiplomadoActivo(Integer id) {

        return diplomadoRepository.findById(id)
                .filter(diplomado -> ACTIVO.equalsIgnoreCase(diplomado.getEstado()))
                .map(ProgramaPortalDTO::desde)
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "El diplomado solicitado no existe o no está disponible."
                ));
    }

    @Transactional(readOnly = true)
    public List<Espacio> listarEspacios() {
        return espacioRepository.findAll(Sort.by("nombre").ascending());
    }

    @Transactional(readOnly = true)
    public List<ServicioCatering> listarServiciosCatering() {
        return cateringRepository.findAll(Sort.by("nombre").ascending());
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll(Sort.by("nombre").ascending());
    }

    @Transactional(readOnly = true)
    public List<Modalidad> listarModalidades() {
        return modalidadRepository.findAll(Sort.by("nombre").ascending());
    }

    public static boolean espacioReservable(Espacio espacio) {
        return !"MANTENIMIENTO".equalsIgnoreCase(espacio.getEstado());
    }

    public static boolean cateringDisponible(ServicioCatering servicio) {
        return "DISPONIBLE".equalsIgnoreCase(servicio.getEstado());
    }
}
