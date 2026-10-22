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
import sv.edu.udb.model.Alquiler;
import sv.edu.udb.service.AlquilerService;
/**
 *
 * @author crist
 */
@RestController
@RequestMapping("/api/alquileres")
@RequiredArgsConstructor
@Tag(name = "Alquiler de espacios", description = "Reservas de espacios (ADMIN y RECEPCIONISTA)")
public class AlquilerController {

    private final AlquilerService alquilerService;


    @Operation(
            summary = "Listar los alquileres",
            description = "Paginado con los parámetros pagina (desde 0), tamano, ordenarPor y direccion (asc/desc). El parámetro buscar filtra por nombre del cliente o del espacio. Orden por defecto: horaInicio descendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping
    public ResponseEntity<Page<Alquiler>> listarAlquileres(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "horaInicio") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direccion) {


        Sort orden = direccion.equalsIgnoreCase("asc")
                ? Sort.by(ordenarPor).ascending()
                : Sort.by(ordenarPor).descending();


        Pageable paginacion = PageRequest.of(
                pagina,
                tamano,
                orden
        );


        return ResponseEntity.ok(
                alquilerService.listarAlquileres(
                        buscar,
                        paginacion
                )
        );
    }


    @Operation(
            summary = "Consultar el alquiler por ID",
            description = "Devuelve el alquiler con el ID indicado."
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
    public ResponseEntity<Alquiler> buscarPorId(
            @PathVariable Integer id) {


        return ResponseEntity.ok(
                alquilerService.buscarPorId(id)
        );
    }


    @Operation(
            summary = "Registrar un alquiler",
            description = "La hora de fin debe ser posterior a la de inicio y la fecha debe coincidir con la de inicio. La cantidad de personas no puede superar la capacidad del espacio y el espacio no puede estar en MANTENIMIENTO. Estados: PENDIENTE (por defecto), CONFIRMADO, CANCELADO, FINALIZADO. Si no está CANCELADO, no puede traslaparse con otro alquiler no cancelado del mismo espacio (409)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio no cumplida",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "409", description = "El espacio ya está reservado en ese horario",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping
    public ResponseEntity<Alquiler> registrarAlquiler(
            @Valid @RequestBody Alquiler alquiler) {


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        alquilerService.registrarAlquiler(
                                alquiler
                        )
                );
    }


    @Operation(
            summary = "Actualizar un alquiler",
            description = "Aplica las mismas reglas que el registro; el alquiler no choca consigo mismo."
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
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "409", description = "El espacio ya está reservado en ese horario",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Alquiler> actualizarAlquiler(
            @PathVariable Integer id,
            @Valid @RequestBody Alquiler alquiler) {


        return ResponseEntity.ok(
                alquilerService.actualizarAlquiler(
                        id,
                        alquiler
                )
        );
    }


    @Operation(
            summary = "Eliminar el alquiler",
            description = "Elimina el alquiler. No se permite si tiene pagos, actividades u otros registros asociados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar: tiene pagos, actividades u otros registros asociados",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAlquiler(
            @PathVariable Integer id) {


        alquilerService.eliminarAlquiler(id);


        return ResponseEntity.noContent().build();
    }
}


