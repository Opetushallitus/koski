CREATE TABLE failed_login_attempt (
  username TEXT NOT NULL,
  time TIMESTAMP NOT NULL,
  count INT NOT NULL,
  primary key (username)
);