# Supa-replikoinnin vaatimusanalyysi

Tausta tämän dokumentin laatimiseen: Supa rakentaa Koskesta minuuttitasolla
jäljessä olevaa loogista replikaa käyttäen massaluovutusrajapintaa ja
aikaleimoja. Nykytoteutus on osoittautunut sekä tehottomaksi että
luotettavuudeltaan riittämättömäksi. Tämä dokumentti kokoaa ongelman juurisyyn,
johtaa siitä vaatimukset uudelle integraatiolle, vertailee päävaihtoehdot ja
suosittaa yhden niistä jatkosuunnittelun pohjaksi.

## 1. Nykytila ja sen ongelmat

Supa pollaa massaluovutusrajapintaa aikaleimaehdolla ja muodostaa siitä oman
replikansa. Käytännössä tämä tarkoittaa:

- **Sama data luetaan n. 3 kertaa**, jotta aikaleimoihin perustuva poiminta
  pysyy edes kohtuullisen luotettavana.
- **Tämäkään ei riitä**: tarvitaan säännöllisiä massakorjausajoja
  (eli neljäs kierros).
- **Supa fallbackaa Kosken masteriin**, jos replica on liikaa jäljessä —
  ulkoinen kuluttaja pyytää siis tiukempaa konsistenssia kuin mitä Koski
  itse sisäisesti tarjoaa.

### Juurisyy

Massaluovutusrajapinta on **query-time-API**, jota käytetään **logical
replicationin** välineenä. Näillä on rakenteellisesti erilaiset takuut:

| Ominaisuus            | Query-time-API                     | Logical replication                    |
| --------------------- | ---------------------------------- | -------------------------------------- |
| Snapshot              | Saa olla epäkonsistentti           | Pitää olla aukoton                     |
| Järjestys             | Ei taata                           | Commit-järjestys olennainen            |
| Kertaalleen toimitus  | Kuluttajan vastuulla / ei pakko    | Pakollinen takuu                       |
| Aikaleimat avaimena   | OK approksimaatio                  | Rikki rinnakkaisilla transaktioilla\*  |

\* Rinnakkainen transaktio voi commit:ata aiemmalla `updated_at`-arvolla kuin
mitä Supa on jo "nähnyt myöhempänä" — eli aikaleimakursori voi liukua
kirjoituksen yli. Tämä on perustavanlaatuinen, ei viritettävissä oleva
ongelma.

## 2. Vaatimukset

### Toiminnalliset

- **F1**: Jokainen Oo-muutos välittyy Supaan vähintään kerran, idempotentisti
  käsiteltävissä.
- **F2**: Pitkäkin (tunteja, päivä) katkos Supan puolella ei tuhoa muutoksia
  — kun kuluttaja palaa, kaikki välissä olleet muutokset löytyvät.
- **F3**: Tuetaan bootstrap-skenaario (uusi Supa-instanssi tai täystäyttö).
- **F4**: Skeemamuutoksia voidaan tehdä Oo-JSONiin hallitusti, vanhojen
  ja uusien rivien rinnakkaiselo on tuettu.

### Ei-toiminnalliset

- **NF1**: Latenssi minuuttitasoa (ei reaaliaikainen).
- **NF2**: Ei merkittävää write-throughput-haittaa Koskelle; ei lukoilla
  pidättele transaktioita.
- **NF3**: Ei tarvitse multiple-pass-skannauksia samasta datasta.
- **NF4**: Operatiivisesti seurattavissa: havaitaan viive, kuluttajan
  kaatuminen, outboxin paisuminen.
- **NF5**: Selvä omistajuus — Koski päättää mitä julkaistaan, milloin se
  saa hävitä, ja millä skeemalla.

### Reunaehdot

- **R1**: Supa on ulkoinen kuluttaja (eri tiimi), joten rajapintamuutos
  vaatii koordinointia ja datan rakenteen määrittelyn.
- **R2**: Koski on ainoa kirjoittaja ja totuuden lähde.
- **R3**: Ympäristö on AWS/RDS PostgreSQL — vaikuttaa logical replication
  slottien operatiiviseen kestoon (slot voi paisuttaa WALia jos kuluttaja
  jää jälkeen).
- **R4**: Henkilötiedot — kaikki ratkaisut ovat audit-lokitettavia ja
  pääsy on rajoitettua.

## 3. Vaihtoehdot

### A. Transactional outbox + S3-objektit

Triggeri kirjoittaa muutosrivin outbox-tauluun samassa transaktiossa.
Worker batchaa rivit S3-objekteiksi. Supa kuuntelee S3-eventtejä tai pollaa
bucketia.

- **+** Supa irti Kosken DB:stä, hyvä jos kuluttajia tulee lisää.
- **+** S3:n eventit / objektien immutability tekevät retry-semantiikasta
  selkeää.
- **−** Lisää komponentteja (worker, S3, bucket-policy, eventbridge tms.),
  enemmän operatiivista pintaa.
- **−** Batchaus tuo lisälatenssia; debugaaminen vaatii hyppäämistä useaan
  paikkaan.

### B. Outbox + sekvenssikursori (pull-API)

Outbox-taulu ja API jossa Supa pyytää `?after_seq=N&limit=K`. Vastauksessa
viimeisin seq. Supa muistaa kursorin.

- **+** Yksinkertainen, suoraviivainen, yksi liikkuva osa.
- **−** Tavallinen sekvenssi ei takaa, etteivätkö rinnakkaiset transaktiot
  commit:aisi myöhemmin pienemmällä seq-arvolla → kursori voi liukua
  kirjoituksen yli (sama gap-ongelma kuin aikaleimoilla, kevyemmässä
  muodossa).
- Ratkaistavissa esim. `pg_xact_commit_timestamp` + watermark "kaikki seq:t
  ennen X on takuulla commit:ttu, vasta nämä saa palauttaa", mutta
  watermarkin pitäminen tarkkana vaatii varovaisuutta.

### C. Outbox + ACK-protokolla

Kuten B, mutta Supa palauttaa batchin käsiteltyään ACK:n, jonka perusteella
Koski poistaa (tai merkitsee käsitellyiksi) outboxin rivit.

- **+** Gap-ongelma katoaa rakenteellisesti: rivi pysyy outboxissa kunnes
  ack:ttu, joten "kursorin liukuminen" ei voi pudottaa riviä.
- **+** Retry on luonnollinen (ack puuttuu → rivi tulee uudestaan).
- **+** Ei tarvitse lukoilla pidätellä kirjoituksia eikä monimutkaisia
  watermark-laskelmia.
- **−** Outbox kasvaa rajatta, jos Supa katoaa pitkäksi aikaa →
  tarvitaan hälytys ja päätös retentio-leikkurista.
- **−** Jos kuluttajia tulee monta, per-kuluttaja-tila siirtyy Koskin
  puolelle (joko per-kuluttaja outbox tai erillinen ack-taulu kuluttajaa
  kohti).

### D. Postgres logical replication / WAL (esim. pgoutput, Debezium)

Supa kuluttaa suoraan replication-slottia.

- **+** Postgres takaa kanonisesti aukottoman, järjestetyn muutoslokin.
- **−** RDS:llä toimii, mutta slot voi paisuttaa WALin niin että kanta
  kaatuu, jos kuluttaja jää jumiin → operatiivinen riski isompi.
- **−** Supa ei käytännössä halua kuluttaa WALia raakana, vaan jonkin
  sovellustason rajapinnan kautta → tämä siirtää kompleksisuutta vain,
  ei poista sitä.
- **−** Skeemamuutosten hallinta WAL-tasolla raskaampaa kuin sovellustason
  outboxissa, jossa voimme tallentaa version mukana.

## 4. Suositus

**Yhdistetään B + C: outbox + sekvenssikursori + ACK-pohjainen poisto.**

Konkreettisesti:

1. **Triggeri** Oo-muutoksista (insert/update/delete) kirjoittaa rivin
   `oo_outbox`-tauluun samassa transaktiossa kuin itse muutos. Rivillä
   `oppija_oid`, `opiskeluoikeus_oid`, `payload` (koko Oo-JSON tai
   referenssi versioon), `seq` (BIGSERIAL), `created_at`, sekä mahdollinen
   `schema_version`.
2. **Pull-API**: `GET /supa-outbox?after_seq=N&limit=K` palauttaa
   järjestyksessä rivit, joiden `seq > N` ja jotka eivät ole vielä ack:ttu,
   sekä `batch_id` ja viimeisin seq.
3. **ACK**: `POST /supa-outbox/ack` `batch_id`:llä → Koski poistaa
   (tai merkitsee `acked_at`-leimalla) batchin rivit. Audit-jälki säilyy
   erillisessä taulussa jos halutaan jäljitettävyys.
4. **Gap-ongelma katoaa**: koska rivi pysyy outboxissa ack:in saakka, ei
   ole väliä missä järjestyksessä rinnakkaiset transaktiot commit:taavat
   seq-arvojaan — Supa joka tapauksessa hakee aina kaikki ei-ack:tut.
   `seq`-kursori on pelkkä pagination-apu, ei korrektiuden perusta.
5. **Bootstrap**: alkutäyttö joko (a) erillisellä snapshot-rajapinnalla,
   joka palauttaa kaikki Oo:t kerralla, jonka jälkeen siirrytään outboxiin,
   tai (b) seedaamalla outbox kerran alkuun ennen ensimmäistä kuluttamista.

### Miksi ei muut

- **Ei A (S3)**: tarpeeton kompleksisuus tähän vaiheeseen. Jos kuluttajia
  tulee lisää tai DB-pohjainen API muodostuu kuormahaitaksi, S3-välivarasto
  voidaan rakentaa myöhemmin samaa outboxia lähteenä käyttäen — eli A on
  evoluutiopolulla, ei kilpaileva vaihtoehto.
- **Ei pelkkä B**: gap-ongelma jää, vaikkakin kevyempänä kuin aikaleimoilla.
  ACK-mekanismi on niin pieni lisä, että sen jättäminen pois ei oikeasti
  yksinkertaista.
- **Ei D (WAL)**: operatiivinen riski (slot vs. RDS WAL-tila) suurempi
  kuin etu, ja Supa joka tapauksessa haluaa sovellustason kontraktin.

## 5. Avoimet kysymykset

- **Skooppi**: vain `opiskeluoikeus`, vai myös päättelyperusteet ja
  cron-päättelyt (jotka voivat muuttaa Oo:n näkymää ilman varsinaista
  kirjoitusta)? Vaikuttaa siihen, mistä triggerit lähtevät.
- **Useat kuluttajat**: tarvitaanko alusta lähtien per-kuluttaja-tila vai
  riittääkö yksi (Supa)? Per-kuluttaja-malli kannattaa harkita heti, jos
  toinen kuluttaja on horisontissa.
- **Outboxin retentio**: mikä on sallittu maksimikoko / -ikä ennen
  hälytystä ja mahdollista pakkoleikkausta? Liittyy R3:een (RDS-kapasiteetti).
- **Payloadin paino**: tallennetaanko koko Oo-JSON outboxiin (yksinkertainen
  mutta paksu) vai vain id + versio (kevyt mutta vaatii lisähaun
  toimituksessa)? Vaikuttaa I/O-kuormaan ja konsistenssiin
  ([[per_oppija_consistency_invariant]] kannattaa pitää mielessä).
- **Skeemaversiointi**: miten breaking-muutokset Oo-JSONiin välitetään
  ja koordinoidaan Supa-tiimin kanssa?
- **Audit-loki**: riittääkö ack-poisto, vai pidetäänkö pysyvä lähetysloki
  erillisessä taulussa (henkilötietojen luovutuksen jäljitettävyys).
