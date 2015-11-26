create table opiskeluoikeus (
    id serial,
    data jsonb,
    primary key (id),
    CONSTRAINT validate_oid
        CHECK (length(data->>'oppijaOid') > 0 AND (data->>'oppijaOid') IS NOT NULL ),
    CONSTRAINT validate_peruste
        CHECK (length(data->>'ePerusteetDiaarinumero') > 0 AND (data->>'ePerusteetDiaarinumero') IS NOT NULL ),
    CONSTRAINT validate_organisaation
        CHECK (length(data->>'oppilaitosOrganisaatio') > 0  AND (data->>'oppilaitosOrganisaatio') IS NOT NULL )
);

CREATE UNIQUE INDEX opinto_oikeus_unique_idx ON opiskeluoikeus((data->>'oppijaOid'), (data->>'ePerusteetDiaarinumero'), (data->>'oppilaitosOrganisaatio'));