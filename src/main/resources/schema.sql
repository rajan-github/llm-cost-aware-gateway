-- src/main/resources/schema.sql
CREATE TABLE IF NOT EXISTS token_ledger (
                                            request_id UUID NOT NULL,
                                            ledger_state VARCHAR(50) NOT NULL,
    org_id VARCHAR(255) NOT NULL,
    tokens BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (request_id, ledger_state, org_id)
    );