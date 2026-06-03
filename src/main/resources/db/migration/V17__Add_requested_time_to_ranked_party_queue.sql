ALTER TABLE ranked_parties
    ADD COLUMN requested_start_time DATETIME NULL,
    ADD COLUMN requested_end_time DATETIME NULL;

ALTER TABLE matchmaking_queue
    ADD COLUMN requested_start_time DATETIME NULL,
    ADD COLUMN requested_end_time DATETIME NULL;

CREATE INDEX idx_ranked_parties_requested_time ON ranked_parties(requested_start_time, requested_end_time, status);
CREATE INDEX idx_matchmaking_queue_requested_time ON matchmaking_queue(requested_start_time, requested_end_time, is_active);
