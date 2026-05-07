-- Datos iniciales para el entorno de desarrollo (H2)
INSERT INTO shop_items (id, name, description, price, image_url, stock, category, created_at) VALUES
(1, 'Standard Sleeves - Red', 'Paquete de 60 fundas rojas estándar para tus cartas.', 4.99, 'https://example.com/sleeves-red.png', 100, 'SLEEVES', CURRENT_TIMESTAMP),
(2, 'Standard Sleeves - Blue', 'Paquete de 60 fundas azules estándar para tus cartas.', 4.99, 'https://example.com/sleeves-blue.png', 100, 'SLEEVES', CURRENT_TIMESTAMP),
(3, 'Luffy Playmat', 'Tapete de juego oficial con diseño de Monkey D. Luffy.', 24.99, 'https://example.com/playmat-luffy.png', 50, 'PLAYMAT', CURRENT_TIMESTAMP),
(4, 'Premium Deck Box', 'Caja para mazos premium con compartimento para dados.', 14.99, 'https://example.com/deckbox-premium.png', 30, 'DECK_BOX', CURRENT_TIMESTAMP),
(5, 'Zoro Playmat', 'Tapete de juego oficial con diseño de Roronoa Zoro.', 24.99, 'https://example.com/playmat-zoro.png', 50, 'PLAYMAT', CURRENT_TIMESTAMP);

