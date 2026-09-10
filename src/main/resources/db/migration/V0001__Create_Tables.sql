-- 1. Create Lookup Table for Roles
CREATE TABLE role_types (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Create Users Table (Handles permanent users and null-email guests)
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE CHECK (email IS NULL OR email LIKE '%@%'),
    password VARCHAR(255) -- hashed password (nullable for guest users)
);

-- 3. Junction Table: Many-to-Many relationship between Users and Roles
CREATE TABLE user_roles (
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    role_id INT REFERENCES role_types(role_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 4. Create Games Table (Removes repeating player/score columns)
CREATE TABLE games (
    game_id SERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    game_date DATE DEFAULT CURRENT_DATE,
    start_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. NEW Normalized Table: Links players to games and stores final scores
CREATE TABLE game_players (
    game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
    user_id INT REFERENCES users(user_id) ON DELETE RESTRICT,
    final_score INT DEFAULT 0,
    PRIMARY KEY (game_id, user_id)
);

-- 6. Create Rounds Table (Removes repeating call/points columns)
CREATE TABLE rounds (
    game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
    round_number INT CHECK (round_number BETWEEN 1 AND 13),
    PRIMARY KEY (game_id, round_number)
);

-- 7. NEW Normalized Table: Stores individual player statistics per round
CREATE TABLE round_player_stats (
    game_id INT,
    round_number INT,
    user_id INT,
    player_call INT NOT NULL CHECK (player_call >= 0),
    points_earned INT NOT NULL, -- points without bonus (+10) calculated or stored here
    FOREIGN KEY (game_id, round_number) REFERENCES rounds(game_id, round_number) ON DELETE CASCADE,
    FOREIGN KEY (game_id, user_id) REFERENCES game_players(game_id, user_id) ON DELETE CASCADE,
    PRIMARY KEY (game_id, round_number, user_id)
);


INSERT INTO role_types (role_name)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM role_types WHERE role_name = 'ROLE_USER');

INSERT INTO role_types (role_name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM role_types WHERE role_name = 'ROLE_ADMIN');

INSERT INTO role_types (role_name)
SELECT 'ROLE_MODERATOR'
WHERE NOT EXISTS (SELECT 1 FROM role_types WHERE role_name = 'ROLE_MODERATOR');

