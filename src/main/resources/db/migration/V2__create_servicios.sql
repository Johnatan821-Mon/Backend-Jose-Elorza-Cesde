CREATE TABLE servicios (
    id                BIGSERIAL     PRIMARY KEY,
    nombre            VARCHAR(255)  NOT NULL,
    descripcion       VARCHAR(500),
    duracion_minutos  INTEGER       NOT NULL,
    precio            DECIMAL(10,2) NOT NULL,
    activo            BOOLEAN       NOT NULL DEFAULT TRUE
);
