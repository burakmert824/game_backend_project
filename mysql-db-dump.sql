
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


-- INSERT INTO users (username, level, coins, country) VALUES ('user1', 20, 5000, 'Turkey');
-- INSERT INTO users (username, level, coins, country) VALUES ('user2', 20, 5000, 'United States');
-- INSERT INTO users (username, level, coins, country) VALUES ('user3', 20, 5000, 'United Kingdom');
-- INSERT INTO users (username, level, coins, country) VALUES ('user4', 20, 5000, 'France');
-- INSERT INTO users (username, level, coins, country) VALUES ('user5', 20, 5000, 'Germany');

-- INSERT INTO users (username, level, coins, country) VALUES ('user6', 120, 5000, 'Turkey');
-- INSERT INTO users (username, level, coins, country) VALUES ('user7', 120, 5000, 'United States');
-- INSERT INTO users (username, level, coins, country) VALUES ('user8', 120, 5000, 'United Kingdom');
-- INSERT INTO users (username, level, coins, country) VALUES ('user9', 120, 5000, 'France');
-- INSERT INTO users (username, level, coins, country) VALUES ('user10', 120, 5000, 'Germany');

-- INSERT INTO users (username, level, coins, country) VALUES ('user11', 120, 5000, 'Turkey');
-- INSERT INTO users (username, level, coins, country) VALUES ('user12', 120, 5000, 'United States');
-- INSERT INTO users (username, level, coins, country) VALUES ('user13', 120, 5000, 'United Kingdom');
-- INSERT INTO users (username, level, coins, country) VALUES ('user14', 120, 5000, 'France');
-- INSERT INTO users (username, level, coins, country) VALUES ('user15', 120, 5000, 'Germany');


-- Insert two tournaments with today's local date
-- INSERT INTO tournaments (is_started, date) VALUES (FALSE, CURDATE() - INTERVAL 1 DAY);
-- INSERT INTO tournaments (is_started, date) VALUES (True, CURDATE());
-- INSERT INTO tournaments (is_started, date) VALUES (True, CURDATE());



-- Ensure the first 5 users attend the first tournament (old)
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (1, 1, FALSE, 5);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (2, 1, FALSE, 4);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (3, 1, FALSE, 3);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (4, 1, FALSE, 2);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (5, 1, FALSE, 1);

-- Ensure the last 5 users attend the second tournament
-- Assuming the last 5 users have IDs from 6 to 10
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (6, 2, FALSE, 10);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (7, 2, FALSE, 11);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (8, 2, FALSE, 12);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (9, 2, FALSE, 13);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (10, 2, FALSE, 14);

-- Ensure the last 5 users attend the third tournament
-- Assuming the last 5 users have IDs from 11 to 15
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (11, 3, FALSE, 5);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (12, 3, FALSE, 4);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (13, 3, FALSE, 3);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (14, 3, FALSE, 2);
-- INSERT INTO user_tournament (user_id, tournament_id, is_claimed, score) VALUES (15, 3, FALSE, 1);

-- SELECT * from users;

-- SELECT * from tournaments;

-- SELECT * from user_tournament;


-- Disable foreign key checks
-- SET FOREIGN_KEY_CHECKS = 0;
--  SET FOREIGN_KEY_CHECKS = 1;
-- Truncate Table users;Truncate Table tournaments;Truncate Table user_tournament;


