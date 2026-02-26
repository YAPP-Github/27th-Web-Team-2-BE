-- social_logins 테이블 생성
-- user_id가 NULL이면 soft delete된 상태
CREATE TABLE IF NOT EXISTS social_logins (
    social_login_id   BIGSERIAL PRIMARY KEY,
    user_id           BIGINT,
    social_provider   VARCHAR(50)  NOT NULL,
    social_id         VARCHAR(255) NOT NULL,
    email             VARCHAR(255),
    name              VARCHAR(100),
    profile_image_url VARCHAR(512),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- social_provider + social_id 전체 조회를 위한 인덱스 (soft delete 포함)
CREATE INDEX IF NOT EXISTS idx_social_logins_provider_social_id
    ON social_logins (social_id, social_provider);

CREATE INDEX IF NOT EXISTS idx_social_logins_user_id ON social_logins (user_id);
CREATE INDEX IF NOT EXISTS idx_social_logins_created_at ON social_logins (created_at);
CREATE INDEX IF NOT EXISTS idx_social_logins_updated_at ON social_logins (updated_at);

-- 트리거 적용
CREATE TRIGGER update_social_logins_updated_at
    BEFORE UPDATE ON social_logins
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
