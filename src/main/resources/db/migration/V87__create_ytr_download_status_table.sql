CREATE TABLE ytr_download_status (
    nimi TEXT NOT NULL,
    aikaleima TIME WITH TIME ZONE NOT NULL DEFAULT now(),
    data JSONB,
    primary key (nimi)
)
