-- Create global sequence (used by JPA entities)
CREATE SEQUENCE IF NOT EXISTS global_sequence START WITH 100 INCREMENT BY 1;

-- Create Authorities table (matches Authority.java)
CREATE TABLE IF NOT EXISTS authorities (
    authority_id BIGINT PRIMARY KEY DEFAULT nextval('global_sequence'),
    authority VARCHAR(255)
);

-- Create Roles table (matches Role.java)
CREATE TABLE IF NOT EXISTS roles (
    role_id BIGINT PRIMARY KEY DEFAULT nextval('global_sequence'),
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Create Role Authorities junction table (matches @JoinTable in Role.java)
CREATE TABLE IF NOT EXISTS role_authorities (
    role_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, authority_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    FOREIGN KEY (authority_id) REFERENCES authorities(authority_id) ON DELETE CASCADE
);

-- Create Users table (matches UserEntity.java)
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY DEFAULT nextval('global_sequence'),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    phonenumber VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    creationtime TIMESTAMP WITH TIME ZONE,
    is_affiliate BOOLEAN DEFAULT false
);

-- Insert Authorities
INSERT INTO authorities (authority_id, authority) VALUES
(1, 'USER_READ'),
(2, 'USER_WRITE'),
(3, 'USER_DELETE'),
(4, 'PRODUCT_READ'),
(5, 'PRODUCT_WRITE'),
(6, 'PRODUCT_DELETE'),
(7, 'ORDER_READ'),
(8, 'ORDER_WRITE'),
(9, 'ORDER_DELETE'),
(10, 'ADMIN_DASHBOARD_ACCESS'),
(11, 'SYSTEM_CONFIGURATION');


-- Insert Roles
INSERT INTO roles (role_id, name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN'),
(3, 'ROLE_MANAGER');


-- Insert Role Authorities
INSERT INTO role_authorities (role_id, authority_id) VALUES
(1, 1),
(1, 4),
(1, 7),
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 9),
(2, 10),
(2, 11),
(3, 1),
(3, 4),
(3, 7),
(3, 10),
(3, 11);

-- Insert Admin User (password is BCrypt encoded)
INSERT INTO users (user_id, username, email, password, role, phonenumber, enabled, creationtime, is_affiliate) VALUES
(1, 'adminUser', 'admin@gmail.com', '$2a$14$fXyugqLmNg0qUWK5kDmNwOr0EG3e5JtM0NcBrNzNNEwahYbwweWmG', 'ROLE_ADMIN', '0096658844', true, NOW(), false);

-- Insert Loyal Customer (created over 2 years ago)
INSERT INTO users (user_id, username, email, password, role, phonenumber, enabled, creationtime, is_affiliate) VALUES
(2, 'loyalCustomer', 'loyalCustomer@gmail.com', '$2a$14$fXyugqLmNg0qUWK5kDmNwOr0EG3e5JtM0NcBrNzNNEwahYbwweWmG', 'ROLE_USER', '0096658844', true, '2023-01-15 10:30:00+00', false);

-- Insert Store Manager
INSERT INTO users (user_id, username, email, password, role, phonenumber, enabled, creationtime, is_affiliate) VALUES
(3, 'storeManager', 'storeManager@gmail.com', '$2a$14$fXyugqLmNg0qUWK5kDmNwOr0EG3e5JtM0NcBrNzNNEwahYbwweWmG', 'ROLE_MANAGER', '0096658844', true, NOW(), false);