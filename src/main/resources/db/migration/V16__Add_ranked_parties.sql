CREATE TABLE ranked_parties (
    id INT PRIMARY KEY AUTO_INCREMENT,
    host_user_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    queued_at DATETIME,
    matched_booking_id INT NULL,
    FOREIGN KEY (host_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (matched_booking_id) REFERENCES bookings(id) ON DELETE SET NULL
);

CREATE TABLE ranked_party_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    party_id INT NOT NULL,
    user_id INT NOT NULL,
    invited_by_user_id INT,
    status VARCHAR(20) NOT NULL DEFAULT 'JOINED',
    is_host BOOLEAN DEFAULT false,
    message VARCHAR(255),
    joined_at DATETIME,
    responded_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (party_id) REFERENCES ranked_parties(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (invited_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uq_ranked_party_member (party_id, user_id)
);

ALTER TABLE matchmaking_queue
    ADD COLUMN party_id INT NULL,
    ADD CONSTRAINT fk_matchmaking_queue_party FOREIGN KEY (party_id) REFERENCES ranked_parties(id) ON DELETE SET NULL;

ALTER TABLE booking_participants
    ADD COLUMN party_id INT NULL,
    ADD CONSTRAINT fk_booking_participants_party FOREIGN KEY (party_id) REFERENCES ranked_parties(id) ON DELETE SET NULL;

CREATE INDEX idx_ranked_parties_host_status ON ranked_parties(host_user_id, status);
CREATE INDEX idx_ranked_party_members_user_status ON ranked_party_members(user_id, status);
CREATE INDEX idx_matchmaking_queue_party ON matchmaking_queue(party_id, is_active);
CREATE INDEX idx_booking_participants_party ON booking_participants(party_id);
