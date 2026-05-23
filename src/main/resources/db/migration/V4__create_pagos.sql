CREATE TABLE pagos (
    id             BIGSERIAL     PRIMARY KEY,
    cita_id        BIGINT        NOT NULL UNIQUE,
    monto          DECIMAL(10,2) NOT NULL,
    estado         VARCHAR(50)   NOT NULL DEFAULT 'PENDIENTE',
    metodo_pago    VARCHAR(50),
    referencia     VARCHAR(255),
    fecha_pago     TIMESTAMP,
    fecha_creacion TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pagos_cita FOREIGN KEY (cita_id) REFERENCES citas(id)
);
