-- social_oauth_tokens 테이블 생성
CREATE TABLE IF NOT EXISTS social_oauth_tokens (
    social_oauth_token_id      BIGSERIAL PRIMARY KEY,
    user_id                    BIGINT        NOT NULL,
    provider                   VARCHAR(50)   NOT NULL,
    access_token               VARCHAR(2048) NOT NULL,
    refresh_token              VARCHAR(2048),
    access_token_expires_at    TIMESTAMPTZ   NOT NULL,
    refresh_token_expires_at   TIMESTAMPTZ,
    scope                      VARCHAR(1024),
    created_at                 TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_social_oauth_tokens_user_provider
    ON social_oauth_tokens (user_id, provider);

CREATE TRIGGER update_social_oauth_tokens_updated_at
    BEFORE UPDATE ON social_oauth_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
