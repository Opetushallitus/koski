create unique index if not exists unique_opiskeluoikeus_idx on opiskeluoikeus(oppija_oid, (data->'oppilaitos'->>'oid'), (data->'tyyppi'->>'koodiarvo'), (data->>'lähdejärjestelmänId')) where mitatoity = false;
drop index opiskeluoikeus_oppija_oppilaitos_tyyppi_lähdejärjestelmänid_idx;
