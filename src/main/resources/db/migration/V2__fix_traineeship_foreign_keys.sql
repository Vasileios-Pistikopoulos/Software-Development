-- Drop the existing foreign key constraint if it exists
ALTER TABLE traineeships DROP FOREIGN KEY IF EXISTS FKbn5r90vsgoqv3ejoaxms0qy7w;
ALTER TABLE traineeships DROP FOREIGN KEY IF EXISTS FKqjf39gu41cpkp74md2hmgyua4;

-- Set any invalid assigned_trainee_id values to NULL
UPDATE traineeships SET assigned_trainee_id = NULL 
WHERE assigned_trainee_id IS NOT NULL 
AND assigned_trainee_id NOT IN (SELECT id FROM trainee_profiles);

-- Set any invalid supervisor_id values to NULL
UPDATE traineeships SET supervisor_id = NULL 
WHERE supervisor_id IS NOT NULL 
AND supervisor_id NOT IN (SELECT id FROM professor_profiles); 