CREATE DATABASE myapp;
USE myapp;

CREATE TABLE users (
    id VARCHAR(8) NOT NULL,
    name VARCHAR(32) NOT NULL,
    email VARCHAR(256) NOT NULL UNIQUE,
    picture MEDIUMBLOB NULL,
    password VARCHAR(64) NULL,
    google_login BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE posts (
	id VARCHAR(8) NOT NULL,
    user_id VARCHAR(8) NOT NULL,
	post MEDIUMTEXT NULL,
	picture MEDIUMBLOB NULL,
    timestamp DATE NOT NULL,
	PRIMARY KEY (id),
    FOREIGN KEY (user_id)
		REFERENCES users(id)
)