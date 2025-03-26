DROP DATABASE IF EXISTS myapp;

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
    name VARCHAR(32) NOT NULL,
    user_img MEDIUMBLOB NULL,
	post MEDIUMTEXT NULL,
    timestamp DATE NOT NULL,
    status VARCHAR(8) NOT NULL,
	PRIMARY KEY (id),
    FOREIGN KEY (user_id)
		REFERENCES users(id)
			ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE media_files (
	id VARCHAR(8) NOT NULL,
    post_id VARCHAR(8) NOT NULL,
    type VARCHAR(32) NOT NULL,
    file MEDIUMBLOB NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (post_id)
		REFERENCES posts(id)
			ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE item_media_files (
	id VARCHAR(8) NOT NULL,
    item_id VARCHAR(8) NOT NULL,
    type VARCHAR(32) NOT NULL,
    file MEDIUMBLOB NOT NULL,
    PRIMARY KEY(id)
);