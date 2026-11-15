-- Run this script while connected as a PostgreSQL administrator.
-- psql: psql -U postgres -f database/create-database.sql

CREATE USER eventhub_user WITH PASSWORD 'eventhub_password';
CREATE DATABASE eventhub OWNER eventhub_user;
