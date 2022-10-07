# OpenSearchin indeksin manuaalinen päivittäminen

Jos tulee tarve päivittää OpenSearchin indeksoimia opiskeluoikeuksia manuaalisesti, niin se on mahdollista lisäämällä
opiskeluoikeuksien OID:t `perustiedot_manual_sync`-tauluun seuraavalla kyselyllä:

```sql
INSERT INTO perustiedot_manual_sync (opiskeluoikeus_oid, upsert)
VALUES ("1.2.345.678.90.00000000000", true),
       ("1.2.345.678.90.00000000001", true),
       ("1.2.345.678.90.00000000002", true);
```

Manuaalinen päivitys on ajastettu tapahtumaan 5 minuutin välein.
