DROP TABLE ytr_download_status;

CREATE TABLE ytr_download_status (
    id SERIAL,
    aikaleima TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    initialized TIMESTAMP WITH TIME ZONE,
    completed TIMESTAMP WITH TIME ZONE,
    modified_since_param DATE,
    data JSONB,
    primary key (id)
)
