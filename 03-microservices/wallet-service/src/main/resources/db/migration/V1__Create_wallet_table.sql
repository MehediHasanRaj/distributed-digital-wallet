CREATE TABLE wallets (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         user_id CHAR(36),
                         balance DECIMAL(19,2),
                         currency VARCHAR(10),
                         status VARCHAR(20),
                         version BIGINT
);