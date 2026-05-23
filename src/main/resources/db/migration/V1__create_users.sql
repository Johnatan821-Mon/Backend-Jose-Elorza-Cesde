CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(50),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    role       VARCHAR(50)  NOT NULL
);
