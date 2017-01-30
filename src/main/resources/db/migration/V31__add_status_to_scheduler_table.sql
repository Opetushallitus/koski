ALTER TABLE scheduler ADD status INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN scheduler.status is '0=idle, 1=running';