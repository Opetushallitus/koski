create unique index unique_opiskeluoikeus_idx on opiskeluoikeus(oppija_oid, (data->'oppilaitos'->>'oid'), (data->'tyyppi'->>'koodiarvo'), (data->>'lähdejärjestelmänId')) where mitatoity = false;
