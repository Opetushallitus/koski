delete from casserviceticket;
alter table casserviceticket add started TIMESTAMP NOT NULL DEFAULT current_timestamp;
alter table casserviceticket add updated TIMESTAMP NOT NULL DEFAULT current_timestamp;
alter table casserviceticket drop sessionid;
