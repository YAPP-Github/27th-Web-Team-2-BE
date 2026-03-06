CREATE INDEX IF NOT EXISTS idx_meetings_host_status_finalized_date
    ON meetings(host_user_id, status, finalized_date);
