-- ======== RANKED MATCHMAKING: Allow referee_id to be nullable ========
-- When a ranked match is just created, referee hasn't joined yet.
-- referee_id will be populated when a referee joins the match.

ALTER TABLE ranked_matches MODIFY COLUMN referee_id INT NULL;
