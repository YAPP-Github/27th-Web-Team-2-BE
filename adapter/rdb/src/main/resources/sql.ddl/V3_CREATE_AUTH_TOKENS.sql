-- Auth Token 테이블
CREATE TABLE IF NOT EXISTS auth_tokens (
    token_id BIGSERIAL PRIMARY KEY,
    token_value VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_token_value ON auth_tokens(token_value);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_created_at ON auth_tokens(created_at);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_updated_at ON auth_tokens(updated_at);

-- Refresh Token 테이블
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              refresh_token_id BIGSERIAL PRIMARY KEY,
                                              token_value VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_value ON refresh_tokens(token_value);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_created_at ON refresh_tokens(created_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_updated_at ON refresh_tokens(updated_at);

-- 트리거 적용
CREATE TRIGGER update_auth_tokens_updated_at
    BEFORE UPDATE ON auth_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_refresh_tokens_updated_at
    BEFORE UPDATE ON refresh_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
