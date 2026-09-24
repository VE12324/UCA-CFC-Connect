package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.exception.RecursoNoEncontrado;
import sv.edu.udb.model.Diplomado;
import sv.edu.udb.repository.DiplomadoRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DiplomadoService {

    private final DiplomadoRepository diplomadoRepository;

    @Transactional(readOnly = true)
    public Page<Diplomado> listarDiplomados(String busqueda, Pageable pageable) {
        if (busqueda == null || busqueda.isBlank()) {
            return diplomadoRepository.findAll(pageable);
        }
        return diplomadoRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                busqueda, busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public Diplomado buscarPorId(Integer id) {
        return diplomadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el diplomado con ID: " + id));
    }

    @Transactional
    public Diplomado registrarDiplomado(Diplomado diplomado) {
        validarFechas(diplomado);
        return diplomadoRepository.save(diplomado);
    }

    @Transactional
    public Diplomado actualizarDiplomado(Integer id, Diplomado datosDiplomado) {
        Diplomado diplomado = buscarPorId(id);
        validarFechas(datosDiplomado);

        diplomado.setNombre(datosDiplomado.getNombre());
        diplomado.setDescripcion(datosDiplomado.getDescripcion());
        diplomado.setCupoMaximo(datosDiplomado.getCupoMaximo());
        diplomado.setFechaInicio(datosDiplomado.getFechaInicio());
        diplomado.setFechaFin(datosDiplomado.getFechaFin());
        diplomado.setCosto(datosDiplomado.getCosto());
        diplomado.setEstado(datosDiplomado.getEstado());
        diplomado.setCategoria(datosDiplomado.getCategoria());
        diplomado.setModalidad(datosDiplomado.getModalidad());
        diplomado.setDocente(datosDiplomado.getDocente());

        return diplomadoRepository.save(diplomado);
    }

    @Transactional
    public void eliminarDiplomado(Integer id) {
        Diplomado diplomado = buscarPorId(id);
        diplomadoRepository.delete(diplomado);
    }

    private void validarFechas(Diplomado diplomado) {
        if (diplomado.getFechaInicio() == null || diplomado.getFechaFin() == null) return;
        if (diplomado.getFechaInicio().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser anterior a la fecha actual");
        }
        if (!diplomado.getFechaFin().isAfter(diplomado.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser estrictamente posterior a la fecha de inicio");
        }
    }
}