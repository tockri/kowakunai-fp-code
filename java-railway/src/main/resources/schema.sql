CREATE TABLE "order" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_key INT,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES "order"(id)
);
