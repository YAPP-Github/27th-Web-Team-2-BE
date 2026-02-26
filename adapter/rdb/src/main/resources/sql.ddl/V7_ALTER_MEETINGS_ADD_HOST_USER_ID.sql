ALTER TABLE meetings
    ADD COLUMN IF NOT EXISTS host_user_id BIGINT NULL;

CREATE INDEX IF NOT EXISTS idx_meetings_host_user_id ON meetings(host_user_id);
