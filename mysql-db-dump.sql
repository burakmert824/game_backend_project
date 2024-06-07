CREATE DATABASE IF NOT EXISTS `mysql-db`;
USE `mysql-db`;

CREATE TABLE IF NOT EXISTS users (
     id BIGINT AUTO_INCREMENT PRIMARY KEY, 
     username VARCHAR(255) NOT NULL,  
     level INT DEFAULT 1, 
     coins INT DEFAULT 5000, 
     country VARCHAR(255) NOT NULL );

CREATE TABLE IF NOT EXISTS tournaments (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     is_started BOOLEAN DEFAULT FALSE,
     date DATE NOT NULL );


CREATE TABLE IF NOT EXISTS user_tournament (    
 user_id BIGINT NOT NULL, 
    tournament_id BIGINT NOT NULL,
    is_claimed BOOLEAN DEFAULT FALSE,
    score INT DEFAULT 0,
    PRIMARY KEY (user_id, tournament_id), 
    FOREIGN KEY (user_id) REFERENCES users(id), 
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) );


