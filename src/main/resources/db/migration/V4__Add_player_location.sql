-- Add GPS location fields to players table
-- Used by matchmaking to filter players within radius of venue

ALTER TABLE players
    ADD COLUMN last_latitude DECIMAL(10, 8) NULL,
    ADD COLUMN last_longitude DECIMAL(11, 8) NULL,
    ADD COLUMN location_updated_at DATETIME NULL;

-- Index for location-based queries
CREATE INDEX idx_players_location ON players(last_latitude, last_longitude);
