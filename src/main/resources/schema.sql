CREATE SCHEMA IF NOT EXISTS public;

DROP TABLE IF EXISTS files;

CREATE TABLE files (
                       id SERIAL PRIMARY KEY,
                       file_name VARCHAR(255) NOT NULL,
                       digest VARCHAR(255) NOT NULL,
                       size BIGINT NOT NULL
);