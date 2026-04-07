-- Target DB: customers table WITHOUT email column
-- Liquibase diff will detect email as missing on this side
CREATE TABLE IF NOT EXISTS customers (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(100)
);
