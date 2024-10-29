-- V2__insert_super_user.sql

-- Insert a SUPER user into the 'users' table
INSERT INTO users (username, email, password, type, profile_image, version) VALUES (
    'superuser',
    'publixoapagar@gmail.com',
    '$2a$10$Dre7yM16nlHIloWftfw2Kuk90nVsrh4tnqN/1MSrIoqlp3bHdquX6', -- Ensure this password is hashed
    'SUPER',
    NULL, -- Profile image can be NULL or specify a URL
    0     -- Version
);

