ALTER TABLE meetings
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'VOTING';

ALTER TABLE meetings
    ADD COLUMN IF NOT EXISTS finalized_date DATE NULL;

CREATE INDEX IF NOT EXISTS idx_meetings_status ON meetings(status);
