INSERT INTO usuario (nombre, email, rol, password_hash) VALUES
('Ana Estudiante',  'ana@uv.mx',  'ESTUDIANTE', 'demo123'),
('Luis Asesor',     'luis@uv.mx', 'ASESOR',     'demo123'),
('Coord. Escolar',  'coord@uv.mx','COORDINACION','demo123');

INSERT INTO motivo (nombre, descripcion) VALUES
('Asesoría de proyecto', 'Revisión de avance de proyecto integrador'),
('Duda académica',       'Consulta sobre temas de clase'),
('Revisión de tarea',    'Aclaración sobre entrega');

INSERT INTO disponibilidad (id_asesor, fecha, hora_inicio, hora_fin) VALUES
(2, CURRENT_DATE + INTERVAL '1 day', '09:00', '09:30'),
(2, CURRENT_DATE + INTERVAL '1 day', '09:30', '10:00'),
(2, CURRENT_DATE + INTERVAL '2 day', '11:00', '11:30');