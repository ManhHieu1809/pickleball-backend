ALTER TABLE ranked_matches DROP CONSTRAINT chk_team_scores;
ALTER TABLE ranked_matches ADD CONSTRAINT chk_team_scores CHECK (
    (team_a_score IS NOT NULL AND team_b_score IS NOT NULL) OR
    status IN ('PENDING', 'CANCELLED')
);

