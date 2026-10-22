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
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.model.Diplomado;
import sv.edu.udb.service.DiplomadoService;

@RestController
@RequestMapping("/api/diplomados")
@RequiredArgsConstructor
@Tag(name = "Diplomados", description = "Oferta de diplomados. Consultar: cualquier usuario autenticado; crear/editar/eliminar: solo ADMIN.")
public class DiplomadoController {


    private final DiplomadoService diplomadoService;


    @Operation(
            summary = "Listar los diplomados",
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
    public ResponseEntity<Page<Diplomado>> listarDiplomados(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {


        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();


        Pageable pageable = PageRequest.of(pagina, tamano, sort);
        return ResponseEntity.ok(diplomadoService.listarDiplomados(buscar, pageable));
    }


    @Operation(
            summary = "Consultar el diplomado por ID",
            description = "Devuelve el diplomado con el ID indicado."
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
    public ResponseEntity<Diplomado> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(diplomadoService.buscarPorId(id));
    }


    @Operation(
            summary = "Registrar un diplomado",
            description = "La fecha de inicio no puede ser anterior a hoy y la de fin debe ser posterior a la de inicio. Requiere categoría, modalidad y docente."
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
    public ResponseEntity<Diplomado> registrarDiplomado(@Valid @RequestBody Diplomado diplomado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diplomadoService.registrarDiplomado(diplomado));
    }


    @Operation(
            summary = "Actualizar un diplomado",
            description = "Aplica las mismas reglas de fechas que el registro."
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
    public ResponseEntity<Diplomado> actualizarDiplomado(
            @PathVariable Integer id, @Valid @RequestBody Diplomado diplomado) {
        return ResponseEntity.ok(diplomadoService.actualizarDiplomado(id, diplomado));
    }


    @Operation(
            summary = "Eliminar el diplomado",
            description = "Elimina el diplomado. No se permite si tiene actividades de agenda u otros registros."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar: tiene actividades de agenda u otros registros",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDiplomado(@PathVariable Integer id) {
        diplomadoService.eliminarDiplomado(id);
        return ResponseEntity.noContent().build();
    }
}


