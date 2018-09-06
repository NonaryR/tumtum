#!/bin/sh -e

psql --variable=ON_ERROR_STOP=1 --username "postgres" <<-EOSQL
    CREATE ROLE nonaryr WITH LOGIN PASSWORD '123';
    CREATE DATABASE chat_db;
    GRANT ALL PRIVILEGES ON DATABASE chat_db TO nonaryr;
EOSQL
