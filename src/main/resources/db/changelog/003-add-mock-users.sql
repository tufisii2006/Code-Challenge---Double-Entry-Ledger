--liquibase formatted sql

--changeset ledger:003-add-users
INSERT IGNORE INTO account (id, currency, overdraft_limit, balance, version, created_at) VALUES
  ('7f3c9a2e-4b1d-4e8a-9c63-0d5b7e214a98', 'EUR', 0, 0, 0, NOW(6)),
  ('c2a81f6d-93e0-4b47-8a15-6f2d9e7c3b10', 'EUR', 0, 0, 0, NOW(6)),
  ('5e9d4b78-1c6a-4f23-b8e0-a47c2d91f6e5', 'EUR', 0, 0, 0, NOW(6)),
  ('a14f6c2b-8d97-4e30-9b5a-2e8f1c6d4a73', 'JPY', 0, 0, 0, NOW(6)),
  ('3b7e0d9f-62a4-4c18-a9f3-8d1e5b2c7f06', 'JPY', 0, 0, 0, NOW(6));