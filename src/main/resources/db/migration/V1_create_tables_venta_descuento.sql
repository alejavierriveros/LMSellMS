-- 1. Crear tabla descuento primero (Lado "Uno" de la relación)
CREATE TABLE descuento (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(20) NOT NULL, -- Respetando el @Length(max = 20)
                           porcentaje_dcto DOUBLE NOT NULL,
                           fecha_creacion DATE NOT NULL,
                           fecha_modificacion DATE NOT NULL,
                           fecha_expiracion DATE NOT NULL
);

-- 2. Crear tabla venta (Lado "Muchos" de la relación)
CREATE TABLE venta (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       cliente_id BIGINT NOT NULL,
                       cliente_nombre VARCHAR(255) NOT NULL,
                       cliente_run VARCHAR(50) NOT NULL,
                       carrito_id BIGINT NOT NULL,
                       detalles_venta LONGTEXT NULL, -- Soporta el string JSON crudo
                       direccion_id BIGINT NOT NULL,
                       direccion_envio VARCHAR(255) NOT NULL,
                       total DOUBLE NOT NULL,
                       descuento_id BIGINT NOT NULL,
                       total_final DOUBLE NOT NULL,
                       fecha_creacion DATE NULL,
                       fecha_modificacion DATE NULL,
                       fecha_salida DATE NULL,
                       realizada BOOLEAN DEFAULT FALSE,

    -- Restricción de Clave Foránea hacia descuento
                       CONSTRAINT fk_venta_descuento FOREIGN KEY (descuento_id)
                           REFERENCES descuento(id)
);