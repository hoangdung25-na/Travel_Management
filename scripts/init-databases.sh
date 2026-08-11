#!/bin/bash
set -e

echo "=========================================================="
echo "Initializing Travel Microservices Databases & Extensions..."
echo "=========================================================="

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE db_travel_auth;
    CREATE DATABASE db_travel_tour;
    CREATE DATABASE db_travel_booking;
    CREATE DATABASE db_travel_payment;
    CREATE DATABASE db_travel_ai;
EOSQL

echo "Activating pgvector extension on db_travel_ai..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "db_travel_ai" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS vector;
EOSQL

echo "=========================================================="
echo "All Databases & Extensions Created Successfully!"
echo "=========================================================="
