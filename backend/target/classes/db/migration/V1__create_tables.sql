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
    brand VARCHAR(100),
    description TEXT,
    barcode VARCHAR(13) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    stock_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL,
    available_for_direct_loan BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create the loans table
CREATE TABLE loans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    user_id INT NOT NULL,
    requested_quantity INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    initial_end_date DATE NOT NULL,  -- New column to track initial end date
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    previous_extended_date DATE,
    return_date DATE,
    extension_count INT NOT NULL,
    version BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT version_constraint_loans CHECK (version >= 0)  -- Unique constraint name
);

-- Create the item_requests table
CREATE TABLE item_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    user_id INT NOT NULL,
    requested_quantity INT NOT NULL,
    request_date DATE NOT NULL,
    queue_position INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    return_date DATE NOT NULL,
    version BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT version_constraint_item_requests CHECK (version >= 0)  -- Unique constraint name
);
