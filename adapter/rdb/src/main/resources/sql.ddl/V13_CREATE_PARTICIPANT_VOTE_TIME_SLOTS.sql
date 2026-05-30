CREATE TABLE IF NOT EXISTS participant_vote_time_slots (
    participant_id BIGINT      NOT NULL,
    vote_date      DATE        NOT NULL,
    time_slots     VARCHAR(48) NOT NULL,
    FOREIGN KEY (participant_id) REFERENCES participants(participant_id) ON DELETE CASCADE,
    PRIMARY KEY (participant_id, vote_date)
);
