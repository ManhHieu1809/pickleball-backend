CREATE TABLE match_confirmations (
    ranked_match_id INT NOT NULL,
    player_id INT NOT NULL,
    PRIMARY KEY (ranked_match_id, player_id),
    CONSTRAINT fk_match_confirmations_match FOREIGN KEY (ranked_match_id) REFERENCES ranked_matches (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_confirmations_player FOREIGN KEY (player_id) REFERENCES users (id) ON DELETE CASCADE
);

