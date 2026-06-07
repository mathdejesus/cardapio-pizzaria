CREATE TABLE pizzas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(1000) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE pizza_tamanhos (
    pizza_id BIGINT NOT NULL REFERENCES pizzas(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    fatias INTEGER NOT NULL
);

CREATE TABLE pizza_ingredientes (
    pizza_id BIGINT NOT NULL REFERENCES pizzas(id) ON DELETE CASCADE,
    ingrediente VARCHAR(255) NOT NULL
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE INDEX idx_pizzas_categoria ON pizzas(categoria);
CREATE INDEX idx_pizzas_disponivel ON pizzas(disponivel);
CREATE INDEX idx_pizzas_deleted ON pizzas(deleted);
CREATE INDEX idx_pizzas_deleted_disponivel ON pizzas(deleted, disponivel);
CREATE INDEX idx_pizzas_deleted_categoria ON pizzas(deleted, categoria);
