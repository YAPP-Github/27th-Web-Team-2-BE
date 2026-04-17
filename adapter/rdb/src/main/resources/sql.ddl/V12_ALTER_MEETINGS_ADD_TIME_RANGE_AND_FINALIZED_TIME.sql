ALTER TABLE meetings ADD COLUMN IF NOT EXISTS time_range_start     TIME NULL;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS time_range_end       TIME NULL;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS finalized_start_time TIME NULL;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS finalized_end_time   TIME NULL;
