-- V1__create_user_and_items_tables.sql

-- Drop the 'users' and 'items' tables if they exist
DROP TABLE IF EXISTS user_items CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create the 'users' table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    profile_image TEXT,
    version BIGINT,
    CONSTRAINT users_unique_email UNIQUE (email)
);

-- Create the 'items' table
CREATE TABLE IF NOT EXISTS items (
    id SERIAL PRIMARY KEY,  -- Auto-incrementing primary key
    designation VARCHAR(255) NOT NULL UNIQUE,  -- Designation must be unique and not null
    barcode VARCHAR(13) NOT NULL UNIQUE,  -- Barcode must be unique and not null
    brand VARCHAR(255) NOT NULL,  -- Brand cannot be null
    category VARCHAR(50) NOT NULL,  -- Category cannot be null (consider using ENUM if applicable)
    purchase_price DECIMAL(10, 2) NOT NULL,  -- Purchase price with precision and scale
    stock_quantity INTEGER NOT NULL,  -- Stock quantity cannot be null
    version BIGINT,  -- Version column for optimistic locking
    CHECK (stock_quantity >= 0),  -- Constraint to ensure stock quantity is non-negative
    CHECK (purchase_price >= 0)  -- Constraint to ensure purchase price is non-negative
);

-- Create the many-to-many relationship table between users and items
CREATE TABLE IF NOT EXISTS user_items (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    item_id INTEGER REFERENCES items(id) ON DELETE CASCADE,  -- Changed book_id to item_id
    PRIMARY KEY (user_id, item_id)                          -- Changed book_id to item_id in PRIMARY KEY
);