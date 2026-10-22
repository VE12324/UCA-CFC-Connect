package sv.edu.udb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sv.edu.udb.dto.RespuestaError;

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
@Tag(name = "Servicios de catering", description = "Catálogo de servicios de catering. Consultar: cualquier usuario autenticado; crear/editar/eliminar: solo ADMIN.")
public class CateringController {


    private final CateringService cateringService;


    @Operation(
            summary = "Listar los servicios de catering",
            description = "Paginado con los parámetros pagina (desde 0), tamano, ordenarPor y direccion (asc/desc). El parámetro buscar filtra por nombre o descripción. Orden por defecto: nombre ascendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
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


    @Operation(
            summary = "Consultar el servicio de catering por ID",
            description = "Devuelve el servicio de catering con el ID indicado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServicioCatering> buscarPorId(
            @PathVariable Integer id) {


        return ResponseEntity.ok(
                cateringService.buscarPorId(id)
        );
    }


    @Operation(
            summary = "Registrar un servicio de catering",
            description = "La capacidad máxima no puede ser menor que la mínima. Estado por defecto: DISPONIBLE (otro valor: NO DISPONIBLE)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio no cumplida",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping
    public ResponseEntity<ServicioCatering> registrarServicio(
            @Valid @RequestBody ServicioCatering servicio) {


        ServicioCatering servicioRegistrado =
                cateringService.registrarServicio(servicio);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicioRegistrado);
    }


    @Operation(
            summary = "Actualizar un servicio de catering",
            description = "La capacidad máxima no puede ser menor que la mínima."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio no cumplida",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
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


    @Operation(
            summary = "Eliminar el servicio de catering",
            description = "Elimina el servicio de catering. No se permite si tiene solicitudes u otros registros asociados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar: tiene solicitudes u otros registros asociados",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(
            @PathVariable Integer id) {


        cateringService.eliminarServicio(id);


        return ResponseEntity.noContent().build();
    }
}


