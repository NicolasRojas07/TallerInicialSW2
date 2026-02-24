package co.edu.uptc.taller.dto;

import lombok.*;

import java.time.Instant;

/**
 * DTO para respuestas de error estandarizadas en el sistema.
 * Facilita el manejo de errores y proporciona informacion util al cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private Instant timestamp;
}
