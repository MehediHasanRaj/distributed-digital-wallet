CREATE TABLE wallets
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    wallet_id BINARY(16) NOT NULL UNIQUE,

    user_id BINARY(16) NOT NULL,

    balance DECIMAL(19,2) NOT NULL,

    currency VARCHAR(10) NOT NULL,

    status VARCHAR(20) NOT NULL,

    version BIGINT,

    created_at DATETIME NOT NULL,

    updated_at DATETIME NOT NULL
);

CREATE UNIQUE INDEX idx_wallet_id
    ON wallets(wallet_id);

CREATE INDEX idx_user_id
    ON wallets(user_id);