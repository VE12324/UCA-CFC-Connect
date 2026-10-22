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
import sv.edu.udb.dto.ConceptoPagoDTO;
import sv.edu.udb.dto.PagoRequestDTO;
import sv.edu.udb.dto.PagoRespuestaDTO;
import sv.edu.udb.service.ConceptoPagoService;
import sv.edu.udb.service.PagoService;




import java.util.List;




@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Pagos de clientes (solo CONTABILIDAD)")
public class PagoController {




    private final PagoService pagoService;
    private final ConceptoPagoService conceptoPagoService;




    @Operation(
            summary = "Listar pagos",
            description = "Paginado (pagina, tamano). El parámetro buscar filtra por nombre del cliente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping
    public ResponseEntity<Page<PagoRespuestaDTO>> listarPagos(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {




        Pageable pageable = PageRequest.of(pagina, tamano);
        return ResponseEntity.ok(pagoService.listarPagos(buscar, pageable));
    }




    @Operation(
            summary = "Conceptos pendientes de pago de un cliente",
            description = "Lo que el cliente tiene pendiente de pagar: cotizaciones Aprobadas (total con IVA), alquileres no cancelados (costo por hora × horas), solicitudes de catering no canceladas (precio por persona × asistentes) e inscripciones no canceladas (costo del curso × participantes, mínimo 1). Cada fila trae total, ya pagado (suma de todos sus pagos) y saldo; solo aparecen las de saldo mayor que cero. Usar tipoPago e idReferencia de una fila para registrar el pago."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de conceptos con saldo pendiente (puede estar vacía)"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe el cliente indicado",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping("/conceptos-pendientes/{idCliente}")
    public ResponseEntity<List<ConceptoPagoDTO>> conceptosPendientes(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(conceptoPagoService.listarPendientes(idCliente));
    }




    @Operation(
            summary = "Consultar el pago por ID",
            description = "Devuelve el pago con el ID indicado."
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
    public ResponseEntity<PagoRespuestaDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }




    @Operation(
            summary = "Registrar un pago",
            description = "Siempre se registra en estado Pendiente. tipoPago (INSCRIPCION, COTIZACION, ALQUILER, CATERING) e idReferencia indican qué se paga (ver GET /api/pagos/conceptos-pendientes/{idCliente}). El registro debe existir, pertenecer al cliente indicado y estar vigente (cotización Aprobada; alquiler, catering o inscripción no cancelados). El monto debe ser mayor que cero y no superar el saldo pendiente del concepto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio no cumplida",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe el cliente o el método de pago indicado",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping
    public ResponseEntity<PagoRespuestaDTO> registrarPago(@Valid @RequestBody PagoRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(datos));
    }




    @Operation(
            summary = "Actualizar un pago",
            description = "Solo cambian fecha, monto y método de pago: idCliente, tipoPago e idReferencia deben ser los del pago guardado. El monto no puede superar el saldo pendiente del concepto (sin contar este pago). No se puede modificar un pago en estado Pagado. El estado no se edita aquí."
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
    public ResponseEntity<PagoRespuestaDTO> actualizarPago(@PathVariable Integer id, @Valid @RequestBody PagoRequestDTO datos) {
        return ResponseEntity.ok(pagoService.actualizarPago(id, datos));
    }




    @Operation(
            summary = "Eliminar un pago",
            description = "No se puede eliminar un pago en estado Pagado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "400", description = "El pago está Pagado",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPago(@PathVariable Integer id) {
        pagoService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }




    @Operation(
            summary = "Validar un pago",
            description = "Si el pago es de una cotización, suma todos los pagos de esa cotización: Pagado si cubren el total, Parcial si no. Sin cotización queda Pagado. No se puede validar un pago que ya está Pagado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago validado"),
            @ApiResponse(responseCode = "400", description = "El pago ya está Pagado",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping("/{id}/validar")
    public ResponseEntity<PagoRespuestaDTO> validarPago(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.validarPago(id));
    }




    @Operation(
            summary = "Marcar un pago como parcial",
            description = "No aplica si el pago ya está Parcial o Pagado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago marcado como Parcial"),
            @ApiResponse(responseCode = "400", description = "El pago ya está Parcial o Pagado",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping("/{id}/parcial")
    public ResponseEntity<PagoRespuestaDTO> marcarComoParcial(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.marcarComoParcial(id));
    }




    @Operation(
            summary = "Emitir comprobante de pago",
            description = "Registra la fecha de emisión (hoy) y devuelve el pago. El comprobante imprimible está en la interfaz web."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comprobante emitido"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @PostMapping("/{id}/comprobante")
    public ResponseEntity<PagoRespuestaDTO> emitirComprobante(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.emitirComprobante(id));
    }
}


