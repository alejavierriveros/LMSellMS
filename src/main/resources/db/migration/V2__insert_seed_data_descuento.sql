-- 1. Insertar descuentos iniciales
INSERT INTO descuento (nombre, porcentaje_dcto, fecha_creacion, fecha_modificacion, fecha_expiracion) VALUES
                                                                                                          (
                                                                                                              'SIN_DCTO',
                                                                                                              0.0,
                                                                                                              CURDATE(),
                                                                                                              CURDATE(),
                                                                                                              '2030-12-31'
                                                                                                          ),
                                                                                                          (
                                                                                                              'CYBER_LMS',
                                                                                                              0.25, -- 15% de descuento
                                                                                                              CURDATE(),
                                                                                                              CURDATE(),
                                                                                                              '2026-12-31'
                                                                                                          );