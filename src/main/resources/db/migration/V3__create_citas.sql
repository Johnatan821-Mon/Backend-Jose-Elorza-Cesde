CREATE TABLE citas (
    id                        BIGSERIAL    PRIMARY KEY,
    usuario_id                BIGINT       NOT NULL,
    servicio_id               BIGINT       NOT NULL,
    fecha_hora                TIMESTAMP    NOT NULL,
    estado                    VARCHAR(50)  NOT NULL DEFAULT 'PENDIENTE',
    notas                     VARCHAR(500),
    fecha_creacion            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    google_calendar_event_id  VARCHAR(255),

    CONSTRAINT fk_citas_usuario  FOREIGN KEY (usuario_id)  REFERENCES users(id),
    CONSTRAINT fk_citas_servicio FOREIGN KEY (servicio_id) REFERENCES servicios(id)
);
