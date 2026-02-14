-- Meeting 테이블
CREATE TABLE IF NOT EXISTS meetings (
    meet_id VARCHAR(16) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    host_name VARCHAR(100) NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_meetings_created_at ON meetings(created_at);
CREATE INDEX IF NOT EXISTS idx_meetings_updated_at ON meetings(updated_at);

-- Meeting 가능한 날짜들
CREATE TABLE IF NOT EXISTS meeting_dates (
    meet_id VARCHAR(16) NOT NULL,
    available_date DATE NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (meet_id) REFERENCES meetings(meet_id) ON DELETE CASCADE,
    PRIMARY KEY (meet_id, available_date)
);
CREATE INDEX IF NOT EXISTS idx_meeting_dates_created_at ON meeting_dates(created_at);
CREATE INDEX IF NOT EXISTS idx_meeting_dates_updated_at ON meeting_dates(updated_at);

-- Participant 테이블
CREATE TABLE IF NOT EXISTS participants (
    participant_id BIGSERIAL PRIMARY KEY,
    meet_id VARCHAR(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    has_voted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (meet_id) REFERENCES meetings(meet_id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_participants_created_at ON participants(created_at);
CREATE INDEX IF NOT EXISTS idx_participants_updated_at ON participants(updated_at);

-- Participant 투표 날짜들
CREATE TABLE IF NOT EXISTS participant_vote_dates (
    participant_id BIGINT NOT NULL,
    vote_date DATE NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (participant_id) REFERENCES participants(participant_id) ON DELETE CASCADE,
    PRIMARY KEY (participant_id, vote_date)
);
CREATE INDEX IF NOT EXISTS idx_participant_vote_dates_created_at ON participant_vote_dates(created_at);
CREATE INDEX IF NOT EXISTS idx_participant_vote_dates_updated_at ON participant_vote_dates(updated_at);

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_participants_meet_id ON participants(meet_id);
CREATE INDEX IF NOT EXISTS idx_participants_name ON participants(name);

-- 각 테이블에 트리거 적용
CREATE TRIGGER update_meetings_updated_at
    BEFORE UPDATE ON meetings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_meeting_dates_updated_at
    BEFORE UPDATE ON meeting_dates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_participants_updated_at
    BEFORE UPDATE ON participants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_participant_vote_dates_updated_at
    BEFORE UPDATE ON participant_vote_dates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
