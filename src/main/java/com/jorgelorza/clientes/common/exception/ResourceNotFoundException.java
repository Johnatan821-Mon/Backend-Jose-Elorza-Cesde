package com.jorgelorza.clientes.common.exception;

/**
 * Excepción lanzada cuando un recurso buscado no existe en la base de datos.
 *
 * {@link GlobalExceptionHandler} la captura y la traduce a HTTP 404 Not Found
 * con el mensaje incluido en la respuesta. Úsala siempre que un {@code findById}
 * o consulta equivalente no encuentre el registro esperado.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
