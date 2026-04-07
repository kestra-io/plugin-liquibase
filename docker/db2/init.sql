-- Reference DB: customers table WITH email column
-- Liquibase diff will report public.customers.email as a difference
CREATE TABLE IF NOT EXISTS customers (
  id    SERIAL PRIMARY KEY,
  name  VARCHAR(100),
  email VARCHAR(100)
);
