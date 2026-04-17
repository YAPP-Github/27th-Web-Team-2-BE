ALTER TABLE meetings
    ADD COLUMN time_range_start     TIME NULL,
    ADD COLUMN time_range_end       TIME NULL,
    ADD COLUMN finalized_start_time TIME NULL,
    ADD COLUMN finalized_end_time   TIME NULL;
