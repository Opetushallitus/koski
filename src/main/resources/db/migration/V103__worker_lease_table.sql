CREATE TABLE worker_lease (
  name TEXT NOT NULL,
  slot INT NOT NULL,
  holder_id TEXT NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  heartbeat_at TIMESTAMP NOT NULL,
  PRIMARY KEY (name, slot)
);

CREATE INDEX worker_lease_active_idx ON worker_lease(name, expires_at);
