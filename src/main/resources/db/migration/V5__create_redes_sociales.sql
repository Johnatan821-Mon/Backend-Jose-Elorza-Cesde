CREATE TABLE redes_sociales (
    id     BIGSERIAL    PRIMARY KEY,
    tipo   VARCHAR(50)  NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    url    VARCHAR(500) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);
