CREATE DATABASE myapp;
USE myapp;

CREATE TABLE users (
    id varchar(8) not null,
    name varchar(32) not null,
    email varchar(256) not null unique,
    picture varchar(256) null,
    password varchar(64) null,
    google_login boolean not null,
    primary key(id)
);