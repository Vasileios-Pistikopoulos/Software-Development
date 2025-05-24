-- First, try to drop the role column if it exists
ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Make sure the user_roles table exists
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    roles VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
); 