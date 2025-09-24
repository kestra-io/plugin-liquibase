docker compose -f docker-compose-ci.yml up -d

echo "Waiting for Postgres containers..."
until docker exec pg1 pg_isready -U user1 -d demo1 >/dev/null 2>&1; do sleep 2; done
until docker exec pg2 pg_isready -U user2 -d demo2 >/dev/null 2>&1; do sleep 2; done

# pg1
docker exec -i pg1 psql -U user1 -d demo1 <<EOF
DROP TABLE IF EXISTS customers;
CREATE TABLE customers (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100)
);
EOF

# pg2 (different: has email column)
docker exec -i pg2 psql -U user2 -d demo2 <<EOF
DROP TABLE IF EXISTS customers;
CREATE TABLE customers (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100),
  email VARCHAR(100)
);
EOF

echo "Schemas created successfully."
