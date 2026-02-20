ALTER TABLE meetings
    ADD COLUMN IF NOT EXISTS max_participant_count INTEGER NULL;
