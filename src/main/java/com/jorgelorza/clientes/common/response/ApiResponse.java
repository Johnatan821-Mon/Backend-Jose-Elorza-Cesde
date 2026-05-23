package com.jorgelorza.clientes.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Envoltorio genérico para todas las respuestas REST de la API.
 *
 * Todos los endpoints devuelven {@code ApiResponse<T>} para que el cliente
 * siempre reciba la misma estructura: {@code success}, {@code message} y {@code data}.
 * Usa los métodos de fábrica estáticos en lugar del constructor directo.
 *
 * @param <T> tipo del objeto devuelto en {@code data}; usa {@code Void} cuando no hay cuerpo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /** Respuesta exitosa que solo devuelve datos, sin mensaje. */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    /** Respuesta exitosa con mensaje descriptivo y datos. */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    /** Respuesta exitosa solo con mensaje (p.ej. confirmaciones de borrado o desactivación). */
    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder().success(true).message(message).build();
    }

    /** Respuesta de error con mensaje legible; {@code success} siempre es {@code false}. */
    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder().success(false).message(message).build();
    }
}
