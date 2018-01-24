drop index opiskeluoikeus_oppija_oppilaitos_tyyppi_lähdejärjestelmänid_idx;
create unique index opiskeluoikeus_oppija_oppilaitos_tyyppi_lähdejärjestelmänid_idx on opiskeluoikeus(oppija_oid, (data->'oppilaitos'->>'oid'), (data->'tyyppi'->>'koodiarvo'), (data->>'lähdejärjestelmänId'));
