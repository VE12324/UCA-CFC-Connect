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
import sv.edu.udb.model.Participante;
import sv.edu.udb.service.ParticipanteService;
/**
 *
 * @author crist
 */
@RestController
@RequestMapping("/api/participantes")
@RequiredArgsConstructor
@Tag(name = "Participantes", description = "Participantes de las inscripciones (ADMIN y RECEPCIONISTA)")
public class ParticipanteController {

    private final ParticipanteService participanteServicio;


    @Operation(
            summary = "Listar los participantes",
            description = "Paginado con los parámetros pagina (desde 0), tamano, ordenarPor y direccion (asc/desc). El parámetro buscar filtra por nombre, apellido o DUI. Orden por defecto: nombre ascendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @GetMapping
    public ResponseEntity<Page<Participante>> listarParticipantes(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {


        Sort orden;


        if (direccion.equalsIgnoreCase("desc")) {
            orden = Sort.by(ordenarPor).descending();
        } else {
            orden = Sort.by(ordenarPor).ascending();
        }


        Pageable paginacion = PageRequest.of(
                pagina,
                tamano,
                orden
        );


        Page<Participante> participantes =
                participanteServicio.listarParticipantes(
                        buscar,
                        paginacion
                );


        return ResponseEntity.ok(participantes);
    }


    @Operation(
            summary = "Consultar el participante por ID",
            description = "Devuelve el participante con el ID indicado."
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
    public ResponseEntity<Participante> buscarParticipantePorId(
            @PathVariable Integer id) {


        Participante participante =
                participanteServicio.buscarPorId(id);


        return ResponseEntity.ok(participante);
    }


    @Operation(
            summary = "Registrar un participante",
            description = "El DUI es obligatorio, con formato 00000000-0, y no puede repetirse."
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
    public ResponseEntity<Participante> registrarParticipante(
            @Valid @RequestBody Participante participante) {


        Participante participanteRegistrado =
                participanteServicio.registrarParticipante(
                        participante
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(participanteRegistrado);
    }


    @Operation(
            summary = "Actualizar un participante",
            description = "El DUI no puede repetirse con otro participante."
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
    public ResponseEntity<Participante> actualizarParticipante(
            @PathVariable Integer id,
            @Valid @RequestBody Participante participante) {


        Participante participanteActualizado =
                participanteServicio.actualizarParticipante(
                        id,
                        participante
                );


        return ResponseEntity.ok(participanteActualizado);
    }


    @Operation(
            summary = "Eliminar el participante",
            description = "Elimina el participante. No se permite si tiene registros relacionados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar: tiene registros relacionados",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta el token JWT o no es válido",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso para esta operación",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID",
                    content = @Content(schema = @Schema(implementation = RespuestaError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarParticipante(
            @PathVariable Integer id) {


        participanteServicio.eliminarParticipante(id);


        return ResponseEntity.noContent().build();
    }

}


