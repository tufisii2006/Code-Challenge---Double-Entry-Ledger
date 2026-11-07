--liquibase formatted sql

--changeset ledger:001-create-account
CREATE TABLE account (
                         id               CHAR(36)     PRIMARY KEY,
                         currency         CHAR(3)      NOT NULL,
                         overdraft_limit  BIGINT       NULL,
                         balance          BIGINT       NOT NULL DEFAULT 0,
                         version          BIGINT       NOT NULL DEFAULT 0,
                         created_at       TIMESTAMP(6) NOT NULL,
                         CONSTRAINT chk_overdraft_non_negative CHECK (overdraft_limit IS NULL OR overdraft_limit >= 0)
);

CREATE TABLE transaction (
                             id               CHAR(36)     PRIMARY KEY,
                             description      VARCHAR(255) NOT NULL,
                             reverses_id      CHAR(36)     NULL UNIQUE,
                             idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
                             recorded_at      TIMESTAMP(6) NOT NULL,
                             CONSTRAINT fk_transaction_reverses FOREIGN KEY (reverses_id) REFERENCES transaction (id),
                             CONSTRAINT chk_not_self_reversal CHECK (reverses_id IS NULL OR reverses_id <> id)
);

CREATE TABLE entry (
                       id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
                       transaction_id  CHAR(36)     NOT NULL,
                       account_id      CHAR(36)     NOT NULL,
                       direction       ENUM('DEBIT', 'CREDIT') NOT NULL,
                       amount          BIGINT       NOT NULL,
                       created_at      TIMESTAMP(6) NOT NULL,
                       CONSTRAINT fk_entry_transaction FOREIGN KEY (transaction_id) REFERENCES transaction (id),
                       CONSTRAINT fk_entry_account     FOREIGN KEY (account_id)     REFERENCES account (id),
                       CONSTRAINT chk_entry_amount_positive CHECK (amount > 0),
                       INDEX idx_entry_account (account_id, id),
                       INDEX idx_entry_transaction (transaction_id)
);