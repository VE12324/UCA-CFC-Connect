/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.udb.exception;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sv.edu.udb.dto.RespuestaError;


@RestControllerAdvice
public class ManejoErrores {


    @ExceptionHandler(RecursoNoEncontrado.class)
    public ResponseEntity<RespuestaError> manejarRecursoNoEncontrado(
            RecursoNoEncontrado excepcion) {


        RespuestaError error = new RespuestaError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                excepcion.getMessage(),
                null
        );


        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }


    @ExceptionHandler(EspacioOcupadoException.class)
    public ResponseEntity<RespuestaError> manejarEspacioOcupado(
            EspacioOcupadoException excepcion) {


        RespuestaError error = new RespuestaError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Espacio ocupado",
                excepcion.getMessage(),
                null
        );


        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RespuestaError> manejarArgumentoInvalido(
            IllegalArgumentException excepcion) {


        RespuestaError error = new RespuestaError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud incorrecta",
                excepcion.getMessage(),
                null
        );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }


    @ExceptionHandler(CorreoEnUsoException.class)
    public ResponseEntity<RespuestaError> manejarCorreoEnUso(
            CorreoEnUsoException excepcion) {


        RespuestaError error = new RespuestaError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Correo en uso",
                excepcion.getMessage(),
                null
        );


        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidaciones(
            MethodArgumentNotValidException excepcion) {


        Map<String, String> validaciones = new HashMap<>();


        excepcion.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validaciones.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));


        RespuestaError error = new RespuestaError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                "Algunos datos enviados no son válidos",
                validaciones
        );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }


    @ExceptionHandler(RegistroEnUsoException.class)
    public ResponseEntity<RespuestaError> manejarRegistroEnUso(
            RegistroEnUsoException excepcion) {


        RespuestaError error = new RespuestaError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Registro en uso",
                excepcion.getMessage(),
                null
        );


        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorGeneral(
            Exception excepcion) {


        RespuestaError error = new RespuestaError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno",
                "Ocurrió un error inesperado en el servidor",
                null
        );


        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}


