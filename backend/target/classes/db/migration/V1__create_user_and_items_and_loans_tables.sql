-- V1__create_user_and_items_and_loans_tables.sql

-- Drop the 'users', 'items', and 'loans' tables if they exist
DROP TABLE IF EXISTS loans CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create the 'users' table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    role VARCHAR(50) NOT NULL,
    profile_image VARCHAR(255),
    version BIGINT,
    CHECK (char_length(password) >= 8)
);

-- Create the 'items' table
CREATE TABLE IF NOT EXISTS items (
    id SERIAL PRIMARY KEY,
    designation VARCHAR(255) NOT NULL UNIQUE,
    barcode VARCHAR(13) NOT NULL UNIQUE,
    brand VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    state VARCHAR(10) NOT NULL DEFAULT 'FREE',
    version BIGINT,
    CHECK (stock_quantity >= 0),
    CHECK (purchase_price >= 0)
);

-- Create the 'loans' table
CREATE TABLE IF NOT EXISTS loans (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL
);
