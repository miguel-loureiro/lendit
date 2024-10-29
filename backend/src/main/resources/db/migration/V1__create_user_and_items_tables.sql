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
    id SERIAL PRIMARY KEY,
    designation VARCHAR(255) UNIQUE NOT NULL,
    brand VARCHAR(70) NOT NULL,          -- Removed UNIQUE as it's not in the entity
    barcode VARCHAR(13) UNIQUE NOT NULL,
    price VARCHAR(10),
    lend_start TIMESTAMP,                     -- Changed to match entity field name
    item_image_url TEXT,                 -- Changed to match entity field name
    version BIGINT
);

-- Create the many-to-many relationship table between users and items
CREATE TABLE IF NOT EXISTS user_items (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    item_id INTEGER REFERENCES items(id) ON DELETE CASCADE,  -- Changed book_id to item_id
    PRIMARY KEY (user_id, item_id)                          -- Changed book_id to item_id in PRIMARY KEY
);