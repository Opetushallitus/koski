-- Nämä alterit kestivät extralla reilut puoli tuntia, eli niille pitää erikseen sopia huoltokatko tuotannossa.

alter table opiskeluoikeus alter column aikaleima set data type timestamptz;

alter table opiskeluoikeushistoria alter column aikaleima set data type timestamptz;
