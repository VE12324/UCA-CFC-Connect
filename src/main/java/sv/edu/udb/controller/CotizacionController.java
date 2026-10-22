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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.dto.CotizacionRequestDTO;
import sv.edu.udb.dto.CotizacionRespuestaDTO;
import sv.edu.udb.service.CotizacionService;


@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
@Tag(name = "Cotizaciones", description = "Cotizaciones con IVA 13 %. RECEPCIONISTA y ADMIN gestionan; solo ADMIN aprueba o rechaza.")
public class CotizacionController {




    private final CotizacionService cotizacionService;




    @Operation(
            summary = "Listar cotizaciones",
            description = "Paginado (pagina, tamano). El parámetro buscar filtra por nombre del cliente o nombre del estado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CotizacionRespuestaDTO>> listarCotizaciones(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {




        Pageable pageable = PageRequest.of(pagina, tamano);
        return ResponseEntity.ok(cotizacionService.listarCotizaciones(buscar, pageable));
    }




    @Operation(
            summary = "Listar cotizaciones pendientes de aprobación",
            description = "Solo cotizaciones en estado Pendiente o En proceso. Paginado (pagina, tamano)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping("/pendientes")
    public ResponseEntity<Page<CotizacionRespuestaDTO>> listarPendientes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {




        Pageable pageable = PageRequest.of(pagina, tamano);
        return ResponseEntity.ok(cotizacionService.listarPendientesOEnProceso(pageable));
    }




    @Operation(
            summary = "Consultar la cotización por ID",
            description = "Devuelve la cotización con el ID indicado."
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
    public ResponseEntity<CotizacionRespuestaDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.buscarPorId(id));
    }




    @Operation(
            summary = "Registrar una cotización",
            description = "Siempre se crea en estado Pendiente. Requiere cliente, fecha y al menos un detalle (descripción, cantidad >= 1, precio >= 0). Subtotal de cada línea = cantidad x precio; total = subtotal x 1.13 (IVA 13 %), redondeado a 2 decimales."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio no cumplida",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe el cliente indicado",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping
    public ResponseEntity<CotizacionRespuestaDTO> registrarCotizacion(@Valid @RequestBody CotizacionRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.registrarCotizacion(datos));
    }




    @Operation(
            summary = "Actualizar una cotización",
            description = "Reemplaza todas las líneas de detalle y recalcula subtotal y total. No se puede editar una cotización Aprobada."
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
    public ResponseEntity<CotizacionRespuestaDTO> actualizarCotizacion(
            @PathVariable Integer id,
            @Valid @RequestBody CotizacionRequestDTO datos) {




        return ResponseEntity.ok(cotizacionService.actualizarCotizacion(id, datos));
    }




    @Operation(
            summary = "Eliminar una cotización",
            description = "Elimina la cotización y sus detalles. No se puede eliminar una cotización Aprobada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "400", description = "La cotización está Aprobada",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCotizacion(@PathVariable Integer id) {
        cotizacionService.eliminarCotizacion(id);
        return ResponseEntity.noContent().build();
    }




    @Operation(
            summary = "Aprobar una cotización (solo ADMIN)",
            description = "Solo se puede aprobar si está en estado Pendiente o En proceso."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cotización aprobada"),
            @ApiResponse(responseCode = "400", description = "La cotización no está Pendiente ni En proceso",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<CotizacionRespuestaDTO> aprobarCotizacion(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.aprobarCotizacion(id));
    }




    @Operation(
            summary = "Rechazar una cotización (solo ADMIN)",
            description = "Solo se puede rechazar si está en estado Pendiente o En proceso."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cotización rechazada"),
            @ApiResponse(responseCode = "400", description = "La cotización no está Pendiente ni En proceso",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<CotizacionRespuestaDTO> rechazarCotizacion(@PathVariable Integer id) {
        return ResponseEntity.ok(cotizacionService.rechazarCotizacion(id));
    }
}


