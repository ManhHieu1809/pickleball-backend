CREATE TABLE matchmaking_queue (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    role ENUM('PLAYER', 'REFEREE') NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    elo INT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
