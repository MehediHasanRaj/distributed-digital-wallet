CREATE TABLE users
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BINARY(16) NOT NULL UNIQUE,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    status VARCHAR(20) NOT NULL,

    version BIGINT,

    created_at DATETIME NOT NULL,

    updated_at DATETIME NOT NULL
);

CREATE UNIQUE INDEX idx_user_id
    ON users(user_id);

CREATE UNIQUE INDEX idx_email
    ON users(email);