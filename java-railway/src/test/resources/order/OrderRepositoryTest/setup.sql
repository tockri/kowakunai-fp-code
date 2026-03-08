INSERT INTO "order" (id, customer_name, order_date, total_amount) VALUES
    (1, '山田太郎', '2026-03-01 10:00:00', 1980),
    (2, '鈴木花子', '2026-03-02 14:30:00', 3500);

INSERT INTO order_detail (id, order_id, order_key, product_name, quantity, unit_price) VALUES
    (1, 1, 0, 'コーヒー豆 200g', 2, 800),
    (2, 1, 1, 'ドリップフィルター', 1, 380),
    (3, 2, 0, '抹茶パウダー 100g', 1, 1500),
    (4, 2, 1, 'チョコレート詰合せ', 2, 1000);
