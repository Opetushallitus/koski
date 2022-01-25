# Migraatiot

## Yleistä

Kosken päätietokannassa on melko paljon rivejä ja näitä rivejä myös haetaan ja päivitetään melko arvaamattomasti. Migraatiot voivat lukita rivien päivityksen tai teoriassa aiheuttaa jonkinlaisen synkronisointiongelman. Koski ei käsittele opiskeluoikeuksien validaatioita ja uuden opiskeluoikeuden kirjoitusta yhdessä transaktiossa, eikä Koski myöskään päivitä historiatietoja transaktionaalisesti opiskeluoikeuden kanssa.

## Migraation valmistelu

Migraatiot tulisi testata tuotantotietokannan replikaa vasten, jotta saadaan hyvä arvio migraation kestosta. Tällöinkin tuotannon kuorma voi kuitenkin aiheuttaa keston kasvavan.

AWS:n konsolista on helppo pystyttää replika tuotannon edellisestä backupista.

Valitse AWS:ssä tuotantoympäristö, mene RDS-palveluun, sieltä Kosken päätietokantaan ja sieltä backuppeihin. Täältä voi tehdä point in time -restoren.

Anna tietokannalle nimi `DB instance identifier` -kohdassa. Muiden kenttien pitäisi tulla replikoitavan tietokannan kenttien mukaan, mutta silti voi olla hyvä tarkistaa `DB instance class`, `Availability & durability`, `Storage`, `Connectivity`, `Database authentication` ja `Additional configuration`.

Salasana on sama kuin replikoinnin lähteellä.

## Migraatiotiedostojen käyttö

Kosken automaattisesti ajettavat migraatiotiedostot (`src/main/resources/db.migration`) tulisi pitää ajantasalla.

Näiden tiedostojen kautta ei kuitenkaan kannattane ajaa migraatioita tuotantoon. Jos migraatio on pitkä, niitä ajava kontti ei pääse pystyyn ja hälyt reagoivat tähän. Toisaalta on myös epäselvää, miten Kosken usea ajossa oleva kontti reagoi näihin automaattisiin migraatioihin.

Migraatiot on parempi ajaa manuaalisesti käyttäen pääasiassa samoja komentoja, jotka löytyvät myös migraatiotiedostoista. Tämä koskee myös indeksien luontia, koska niissä voi kestää melko pitkään indeksistä riippuen.

## PostgreSQL:n lukoista ja migraation tekeminen batcheissa

Jos migraatio vaatii koko kannan läpikäymistä ja jokaisen rivin päivitystä, saadaan tästä helposti aikaan tilanne, jossa tietosiirtoja ei voi migraation aikana tehdä.

Esimerkiksi seuraava query käy läpi koko tietokannan ja päivittää jokaisen rivin kohdalla yhden kentän data-kentän sisällön perusteella:

```sql
update opiskeluoikeus
set suoritustyypit = array(select distinct jsonb_array_elements(data -> 'suoritukset')->'tyyppi'->>'koodiarvo')
```
PSQL tässä tapauksessa käy tietokantaa läpi rivi kerrallaan ja jokaisen rivin kohdalla luo eksklusiivisen lukon riville. *Nämä lukot eivät vapaudu ennen kuin koko transaktio on suoritettu loppuun, eli ennen kuin jokainen rivi on päivitetty*. Täten tämän migraation aikana tiedonsiirrot voivat jäädä jumittamaan. 

Päivityksen voi ajaa batcheissa, jolloin tätä ongelmaa ei ole. Esimerkki saman päivityksen ajamisesta tuhannen rivin batcheissa:

```sql
DO $$ 
DECLARE 
  page int := 1000;
  min_id bigint; max_id bigint;
BEGIN
  SELECT max(id),min(id) INTO max_id,min_id FROM opiskeluoikeus;
  FOR j IN min_id..max_id BY page LOOP 
    UPDATE opiskeluoikeus SET suoritustyypit = array(select distinct jsonb_array_elements(data -> 'suoritukset')->'tyyppi'->>'koodiarvo')
    WHERE id >= j AND id < j+page;
    COMMIT;            
  END LOOP;
END; $$;
```

## Timestamp-sarakkeen automaattinen päivittyminen

Kosken tietokannoille on säädetty trigger, joka päivittää rivin `timestamp`-sarakkeen aina kun riviä päivitetään. Muita triggereitä ei kirjoitushetkellä Koskella ole.

Timestampin päivitys ei välttämättä ole suotavaa migraatioita tehdessä, koska kyseistä saraketta käytetään päättelemään koska opiskeluoikeuden tietoja on viimeksi muokattu.

PSQL:ssä voi disabloida triggerit sessiokohtaisesti komennolla:
`SET session_replication_role = replica;`

Ja enabloida saman session sisällä komennolla:
`SET session_replication_role = DEFAULT;`
