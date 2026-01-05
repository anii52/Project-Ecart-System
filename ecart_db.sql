DROP DATABASE IF EXISTS ecart_db;
CREATE DATABASE ecart_db;
USE ecart_db;

CREATE TABLE products (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    price DECIMAL(10,2),
    quantity INT,
    type VARCHAR(50),
    brand VARCHAR(50),
    size VARCHAR(10)
);

INSERT INTO products VALUES 
(101, 'Laptop', 1200.00, 5, 'Electronics', 'Dell', NULL),
(102, 'Smartphone', 800.00, 2, 'Electronics', 'Samsung', NULL),
(103, 'Headphones', 150.00, 10, 'Electronics', 'Sony', NULL),
(201, 'T-Shirt', 25.00, 50, 'Clothing', 'Levis', 'M'),
(202, 'Jeans', 60.00, 30, 'Clothing', NULL, 'L'),
(203, 'Jacket', 120.00, 15, 'Clothing', NULL, 'XL');
UPDATE products SET quantity = 1 WHERE id = 102;

SELECT * FROM products;