-- Make company evaluation fields nullable
ALTER TABLE traineeship_evaluations MODIFY COLUMN company_facilities INT NULL;
ALTER TABLE traineeship_evaluations MODIFY COLUMN company_guidance INT NULL; 