-- V1__create_user_and_items_and_loans_tables.sql

-- Drop the 'users', 'items', 'loans', and 'item_requests' tables if they exist
DROP TABLE IF EXISTS item_requests CASCADE;
DROP TABLE IF EXISTS loans CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create the users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    role VARCHAR(50),
    profile_image VARCHAR(255),
    version BIGINT NOT NULL,
    CONSTRAINT version_constraint_users CHECK (version >= 0)  -- Unique constraint name
);

-- Create the items table
CREATE TABLE items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    designation VARCHAR(255) NOT NULL UNIQUE,
    barcode VARCHAR(13) NOT NULL UNIQUE,
    brand VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    stock_quantity INT NOT NULL DEFAULT 0,
    item_request VARCHAR(50),
    version BIGINT NOT NULL,
    CONSTRAINT version_constraint_items CHECK (version >= 0)  -- Unique constraint name
);

-- Create the loans table
CREATE TABLE loans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL,
    return_date DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT version_constraint_loans CHECK (version >= 0)  -- Unique constraint name
);

-- Create the item_requests table
CREATE TABLE item_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    request_date DATETIME NOT NULL,
    queue_position INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    return_date DATETIME NOT NULL,
    version BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT version_constraint_item_requests CHECK (version >= 0)  -- Unique constraint name
);
