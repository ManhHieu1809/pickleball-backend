CREATE TABLE referee_trust_score_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    referee_id INT NOT NULL,
    old_score DECIMAL(5,2) NOT NULL,
    new_score DECIMAL(5,2) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    associated_match_id INT,
    FOREIGN KEY (referee_id) REFERENCES referees(user_id)
);
