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
import sv.edu.udb.model.Inscripcion;
import sv.edu.udb.service.InscripcionService;
/**
 *
 * @author crist
 */
@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "Inscripciones de clientes a cursos (ADMIN y RECEPCIONISTA)")
public class InscripcionController {

    private final InscripcionService inscripcionService;


    @Operation(
            summary = "Listar las inscripciones",
            description = "Paginado con los parámetros pagina (desde 0), tamano, ordenarPor y direccion (asc/desc). El parámetro buscar filtra por nombre del cliente o del curso. Orden por defecto: fecha descendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping
    public ResponseEntity<Page<Inscripcion>> listarInscripciones(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
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
                inscripcionService.listarInscripciones(
                        buscar,
                        paginacion
                )
        );
    }


    @Operation(
            summary = "Consultar la inscripción por ID",
            description = "Devuelve la inscripción con el ID indicado."
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
    public ResponseEntity<Inscripcion> buscarPorId(
            @PathVariable Integer id) {


        return ResponseEntity.ok(
                inscripcionService.buscarPorId(id)
        );
    }


    @Operation(
            summary = "Registrar una inscripción",
            description = "Requiere cliente, curso, estado y al menos un participante; todos deben existir. Los participantes repetidos se descartan."
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
    public ResponseEntity<Inscripcion> registrarInscripcion(
            @Valid @RequestBody Inscripcion inscripcion) {


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        inscripcionService.registrarInscripcion(
                                inscripcion
                        )
                );
    }


    @Operation(
            summary = "Actualizar una inscripción",
            description = "Aplica las mismas reglas que el registro."
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
    public ResponseEntity<Inscripcion> actualizarInscripcion(
            @PathVariable Integer id,
            @Valid @RequestBody Inscripcion inscripcion) {


        return ResponseEntity.ok(
                inscripcionService.actualizarInscripcion(
                        id,
                        inscripcion
                )
        );
    }


    @Operation(
            summary = "Eliminar la inscripción",
            description = "Elimina la inscripción. No se permite si tiene pagos u otros registros asociados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar: tiene pagos u otros registros asociados",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInscripcion(
            @PathVariable Integer id) {


        inscripcionService.eliminarInscripcion(id);


        return ResponseEntity.noContent().build();
    }

}


