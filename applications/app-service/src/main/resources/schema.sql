-- Crear tabla de capacidades
CREATE TABLE IF NOT EXISTS capacities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(90) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla intermedia para la relación capacidad-tecnología
CREATE TABLE IF NOT EXISTS capacity_technologies (
    id BIGSERIAL PRIMARY KEY,
    capacity_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    CONSTRAINT fk_capacity FOREIGN KEY (capacity_id) REFERENCES capacities(id) ON DELETE CASCADE,
    CONSTRAINT uq_capacity_technology UNIQUE (capacity_id, technology_id)
);

-- Índices para optimización
CREATE INDEX IF NOT EXISTS idx_capacity_name ON capacities(name);
CREATE INDEX IF NOT EXISTS idx_capacity_technologies_capacity_id ON capacity_technologies(capacity_id);
CREATE INDEX IF NOT EXISTS idx_capacity_technologies_technology_id ON capacity_technologies(technology_id);
