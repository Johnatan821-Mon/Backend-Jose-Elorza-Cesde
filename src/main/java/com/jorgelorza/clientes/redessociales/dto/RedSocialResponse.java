package com.jorgelorza.clientes.redessociales.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Proyección de red social devuelta al cliente.
 * {@code tipo} se serializa como String para desacoplar el cliente del enum interno.
 */
@Data
@Builder
public class RedSocialResponse {

    private Long id;
    private String tipo;
    private String nombre;
    private String url;
    private boolean activo;
}
