-- V14__Sync_Entities_With_Schema.sql
-- This migration synchronizes the database schema with the current JPA entity definitions. (Revision 2)

-- =================================================================
-- 1. USER Table Synchronization
-- =================================================================
ALTER TABLE users CHANGE COLUMN password password_hash VARCHAR(255) NOT NULL;
ALTER TABLE users CHANGE COLUMN value address_zip_code VARCHAR(255);
ALTER TABLE users CHANGE COLUMN phone_number phone_number_value VARCHAR(255);
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) NOT NULL;

-- =================================================================
-- 2. CATEGORY Table Synchronization
-- =================================================================
ALTER TABLE category RENAME TO categories;

-- =================================================================
-- 3. PRODUCT Table Synchronization
-- =================================================================
ALTER TABLE product RENAME TO products;
ALTER TABLE products
    ADD COLUMN price_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN price_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    CHANGE COLUMN quantity quantity_in_stock INT NOT NULL DEFAULT 0,
    DROP COLUMN value,
    DROP COLUMN code;

-- =================================================================
-- 4. IMAGE Table Synchronization
-- =================================================================
ALTER TABLE image RENAME TO images;
ALTER TABLE images
    CHANGE COLUMN path file_identifier VARCHAR(255) NOT NULL,
    ADD COLUMN webp_file_identifier VARCHAR(255) UNIQUE,
    MODIFY COLUMN alt_text VARCHAR(255) NOT NULL,
    MODIFY COLUMN mime_type VARCHAR(255) NOT NULL,
    MODIFY COLUMN product_id BIGINT NOT NULL,
    ADD CONSTRAINT UK_file_identifier UNIQUE (file_identifier);

-- =================================================================
-- 5. BLACKLISTED_TOKENS Table Synchronization (CORRECTED SECTION)
-- Renames columns from the original V1 schema to match the new entity properties.
-- =================================================================
ALTER TABLE blacklisted_tokens
    CHANGE COLUMN blacklisted_at token_blacklisted_timestamp datetime(6) NOT NULL,
    CHANGE COLUMN expires_at token_expires_at datetime(6) NOT NULL,
    CHANGE COLUMN user_id user_id_associated_token bigint,
    CHANGE COLUMN blacklisted_by blacklisting_agent varchar(255),
    ADD COLUMN record_created_at datetime(6),
    ADD COLUMN record_created_by varchar(255),
    ADD COLUMN record_last_modified_by varchar(255),
    ADD COLUMN record_updated_at datetime(6);

-- Populate new auditing columns for any existing rows.
UPDATE blacklisted_tokens SET record_created_at = NOW(), record_updated_at = NOW() WHERE record_created_at IS NULL;

-- Now, enforce NOT NULL constraint after populating.
ALTER TABLE blacklisted_tokens
    MODIFY COLUMN record_created_at datetime(6) NOT NULL,
    MODIFY COLUMN record_updated_at datetime(6) NOT NULL;

-- =================================================================
-- 6. REFRESH_TOKENS Table Synchronization
-- =================================================================
ALTER TABLE refresh_tokens
    MODIFY COLUMN token VARCHAR(512) NOT NULL,
    MODIFY COLUMN user_id BIGINT NOT NULL,
    ADD COLUMN updated_at DATETIME(6),
    ADD COLUMN last_modified_by VARCHAR(255);

UPDATE refresh_tokens SET updated_at = NOW() WHERE updated_at IS NULL;

ALTER TABLE refresh_tokens
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL;

-- =================================================================
-- 7. NOTIFICATION Table Synchronization
-- =================================================================
ALTER TABLE email_notifications RENAME TO notifications;