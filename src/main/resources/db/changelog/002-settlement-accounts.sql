--liquibase formatted sql

--changeset ledger:settlement-accounts runAlways:true
INSERT IGNORE INTO account (id, currency, overdraft_limit, balance, version, created_at) VALUES
  ('00000000-0000-0000-0000-000000000001', 'EUR', NULL, 0, 0, NOW(6)),
  ('00000000-0000-0000-0000-000000000002', 'JPY', NULL, 0, 0, NOW(6));