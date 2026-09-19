package sv.edu.udb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.model.ServicioCatering;
import sv.edu.udb.service.CateringService;

@RestController
@RequestMapping("/api/catering")
@RequiredArgsConstructor
public class CateringController {

    private final CateringService cateringService;

    @GetMapping
    public ResponseEntity<Page<ServicioCatering>> listarServicios(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {

        Sort orden = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();

        Pageable paginacion = PageRequest.of(
                pagina,
                tamano,
                orden
        );

        Page<ServicioCatering> servicios =
                cateringService.listarServicios(
                        buscar,
                        paginacion
                );

        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioCatering> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                cateringService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<ServicioCatering> registrarServicio(
            @Valid @RequestBody ServicioCatering servicio) {

        ServicioCatering servicioRegistrado =
                cateringService.registrarServicio(servicio);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicioRegistrado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioCatering> actualizarServicio(
            @PathVariable Integer id,
            @Valid @RequestBody ServicioCatering servicio) {

        return ResponseEntity.ok(
                cateringService.actualizarServicio(
                        id,
                        servicio
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(
            @PathVariable Integer id) {

        cateringService.eliminarServicio(id);

        return ResponseEntity.noContent().build();
    }
}