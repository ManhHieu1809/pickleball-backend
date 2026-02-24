-- Add deactivated_by_admin_id column to venues table
-- Note: This will fail if column already exists, which is expected behavior for migrations
ALTER TABLE venues
ADD COLUMN deactivated_by_admin_id INT NULL;

-- Add foreign key constraint
ALTER TABLE venues
ADD CONSTRAINT fk_venues_deactivated_by_admin
FOREIGN KEY (deactivated_by_admin_id) REFERENCES admins(user_id) ON DELETE SET NULL;

-- Add deactivated_by_admin_id column to courts table
ALTER TABLE courts
ADD COLUMN deactivated_by_admin_id INT NULL;
