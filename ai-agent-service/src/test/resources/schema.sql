-- H2 doesn't support all PostgreSQL features, so we need to create some functions
-- that might be used in your application

-- Example: Create a function to simulate PostgreSQL's uuid_generate_v4()
CREATE ALIAS IF NOT EXISTS uuid_generate_v4 FOR "java.util.UUID.randomUUID";

-- Add any other required schema setup for tests