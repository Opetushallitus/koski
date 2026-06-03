# Loogisen replikoinnin vaatimusanalyysi: Supa + raportointikanta

Tausta tämän dokumentin laatimiseen: Koskesta halutaan kahteen kohteeseen
minuuttitasolla jäljessä oleva looginen replika.

- **Supa** käyttää nykyään aikaleimoihin perustuvaa massaluovutusrajapintaa.
  Toteutus on osoittautunut sekä tehottomaksi että luotettavuudeltaan
  riittämättömäksi.
- **Raportointikanta** rakennetaan nykyään säännöllisin täysgeneraatioin, jota
  on optimoitu nopeammaksi tekemällä välillä vain inkrementaalinen generointi.
  Halutaan siirtyä jatkuvaan, minuuttitasolla ajan tasalla pysyvään malliin.

Molempia yhdistää sama ydinongelma: tarvitaan luotettava ja aukoton
muutosvirta Koskesta kuluttajalle. Tämä dokumentti kokoaa ongelman juurisyyn,
johtaa siitä molempien skenaarioiden vaatimukset, ja vertailee
ratkaisuvaihtoehdot. Konkreettista suositusta ei tässä vaiheessa anneta —
tarkoitus on tuottaa pohja arkkitehtuurikeskustelulle.

## 1. Nykytila ja sen ongelmat

### Supa

Supa pollaa massaluovutusrajapintaa aikaleimaehdolla ja muodostaa siitä oman
replikansa. Käytännössä tämä tarkoittaa:

- **Sama data luetaan n. 3 kertaa** (minuutin välein 3 min ikkunalla), jotta
  aikaleimoihin perustuva poiminta pysyy edes kohtuullisen luotettavana.
- **Tämäkään ei riitä**: tarvitaan säännöllisiä massakorjausajoja
  (eli neljäs kierros).
- **Supa fallbackaa Kosken masteriin**, jos replica on liikaa jäljessä —
  ulkoinen kuluttaja pyytää siis tiukempaa konsistenssia kuin mitä Koski
  itse sisäisesti tarjoaa.

### Raportointikanta

Raportointikanta on Kosken oma erillinen RDS-PostgreSQL-tietokanta, johon Oo-data
puretaan normalisoituihin tauluihin. Se sisältää myös Kosken ulkopuolisten
lähteiden dataa (lähinnä: henkilötietoja ONR:stä, organisaatiot ja koodistot omista
palveluistaan).

Osa raportointikannan tauluista on **konsolidointi- tai johdannaistauluja**,
jotka yhdistelevät dataa useasta opiskeluoikeudesta tai useasta lähteestä —
esim. oppivelvollisuustiedot ja rinnakkaisten opiskeluoikeuksien
yhdistelmänäkymät. Yhden Oo-muutoksen käsittely voi siis vaatia useamman
raportointikantarivin uudelleenlaskennan, myös sellaisten, jotka eivät suoraan
koske muuttunutta opiskeluoikeutta (esim. saman oppijan toinen Oo voi muuttua
"rinnakkainen aktiivinen Oo"-tilastaan).

Nykyinen täysgeneraatiomalli tarkoittaa:

- Pitkä viive: tuoreus mitataan tunneissa/päivissä, ei minuuteissa.
- Iso kuormahuippu ajon aikana.
- Bootstrap on koko prosessi joka kerta — inkrementaalisuutta ei ole.

### Yhteinen juurisyy

Massaluovutusrajapinta on **query-time-API**, jota käytetään tai joudutaan
käyttämään **logical replicationin** välineenä. Näillä on rakenteellisesti
erilaiset takuut:

| Ominaisuus            | Query-time-API                     | Logical replication                    |
| --------------------- | ---------------------------------- | -------------------------------------- |
| Snapshot              | Saa olla epäkonsistentti           | Pitää olla aukoton                     |
| Järjestys             | Ei taata                           | Commit-järjestys olennainen            |
| Kertaalleen toimitus  | Kuluttajan vastuulla / ei pakko    | Pakollinen takuu                       |
| Aikaleimat avaimena   | OK approksimaatio                  | Rikki rinnakkaisilla transaktioilla\*  |

\* Rinnakkainen transaktio voi commit:ata aiemmalla `updated_at`-arvolla kuin
mitä kuluttaja on jo "nähnyt myöhempänä" — eli aikaleimakursori voi liukua
kirjoituksen yli. Tämä on perustavanlaatuinen, ei viritettävissä oleva
ongelma.

## 2. Vaatimukset

### Yhteiset toiminnalliset

- **F1**: Jokainen Oo-muutos välittyy kuluttajalle vähintään kerran,
  idempotentisti käsiteltävissä.
- **F2**: Pitkäkin (tunteja, päivä) katkos kuluttajan puolella ei tuhoa
  muutoksia — kun kuluttaja palaa, kaikki välissä olleet muutokset löytyvät.
- **F3**: Tuetaan bootstrap-skenaario (uusi instanssi tai täystäyttö).
  Myös jo tarjotun datan jälkikäteen päivittäminen on tuettava (esim.
  ohjelmavirheen jälkikorjaus).
- **F4**: Skeemamuutoksia voidaan tehdä hallitusti; vanhojen ja uusien
  rivien rinnakkaiselo on tuettu.

### Supa-spesifiset

- **F5**: Toimitettava data on Oo-JSON, semanttisesti versioidulla supalle räätälöidyllä
  skeemalla.
- **F6**: Kuluttaja on ulkoinen organisaatio → vaatii eksplisiittisen,
  versionhallitun kontraktin.

### Raportointikanta-spesifiset

- **F7**: Oo-data puretaan normalisoituihin tauluihin Kosken
  raportointi-RDS-instanssiin
- **F8**: Per-oppija-rivien on heijastettava samaa Oo-generaatiota
  ([[per_oppija_consistency_invariant]]).
- **F9 (tuoreusinvariantti)**: Ulkoisten lähteiden data raportointikannassa
  **ei saa olla vanhempaa kuin samaa oppijaa/Oo:ta koskeva Oo-data**.
  - Tiukasti: ONR-data.
  - Mielellään: organisaatio- ja koodistodata. Näiden muutosfrekvenssi on
    matalampi ja vaatimus voi olla heikompi, jos toteutus sitä vaatii.
- **F10**: Bootstrap ulkoisille tiedoille tapahtuu Lampi-data-laken
  dumpeista, ei suoraan lähdejärjestelmien tuotantopalvelusta — tämä on
  lähdejärjestelmien ylläpitäjien toivomus.
- **F11**: Minuuttitason inkrementtipäivitykset ulkoisista lähteistä on
  haettava jostain tuoreemmasta kanavasta kuin Lampi-dumpit (jotka ovat
  ONR:llä n. tunti jäljessä).
- **F12 (konsolidointitaulut)**: Yhden Oo-muutoksen käsittely voi vaatia
  useamman riippuvan rivin uudelleenlaskennan (esim. saman oppijan muiden
  Oo:iden rinnakkaisuustiedot, oppivelvollisuustiedot). Toteutuksessa on
  oltava tapa määritellä ja ylläpitää nämä riippuvuudet niin, että
  konsolidointitaulut pysyvät F8:n mukaisesti samassa generaatiossa kuin
  niitä syöttävät pohjadatat.

### Yhteiset ei-toiminnalliset

- **NF1**: Latenssi minuuttitasoa (ei reaaliaikainen).
- **NF2**: Ei merkittävää write-throughput-haittaa Koskelle; ei lukoilla
  pidätellä transaktioita.
- **NF3**: Ei tarvitse multiple-pass-skannauksia samasta datasta.
- **NF4**: Operatiivisesti seurattavissa: havaitaan viive, kuluttajan
  kaatuminen, outboxin paisuminen.
- **NF5**: Selvä omistajuus — Koski päättää mitä julkaistaan, milloin se
  saa hävitä, ja millä skeemalla.
- **NF6**: Ulkoisten lähteiden tuotantopalveluiden kuormitus minimoitava
  (ei N+1-hakuja per outbox-rivi, jos vältettävissä).

### Reunaehdot

- **R1**: Supa on ulkoinen kuluttaja (eri tiimi), joten rajapintamuutos
  vaatii koordinointia ja datan rakenteen määrittelyn.
- **R2**: Koski on ainoa kirjoittaja Kosken omaan dataan, mutta
  raportointikantaan virtaa myös ONR/organisaatio/koodisto-dataa.
- **R3**: Ympäristö on AWS/RDS PostgreSQL — vaikuttaa logical replication
  slottien operatiiviseen kestoon (slot voi paisuttaa WALia jos kuluttaja
  jää jälkeen).
- **R4**: Henkilötiedot — kaikki ratkaisut ovat audit-lokitettavia ja
  pääsy on rajoitettua.
- **R5**: ONR:n Lampi-muutosdumpit ovat noin tunti jäljessä → eivät
  riitä F11:n täyttämiseen, mutta soveltuvat F10-bootstrappiin.
- **R6**: ONR, organisaatio- ja koodistopalvelun ylläpitäjät pyytävät, että
  isot bootstrap-haut tehdään Lampi-dumpeista eikä tuotantopalveluista.

## 3. Vaihtoehdot Kosken omille Oo-muutoksille

Kaikissa vaihtoehdoissa lähdetään samasta pohjasta: jokainen Oo-muutos
kirjataan transaktiossa muutostapahtumana. Erot ovat siinä, miten
kuluttaja saa sen luettavaksi.

### A. Transactional outbox + S3-objektit

Triggeri kirjoittaa muutosrivin outbox-tauluun samassa transaktiossa.
Worker batchaa rivit S3-objekteiksi. Kuluttajat lukevat S3:sta tai
kuuntelevat S3-eventtejä.

- **+** Kuluttajat irti Kosken DB:stä, hyvä jos kuluttajia tulee paljon
  lisää tai jos halutaan eristää kuormaa.
- **+** S3-objektien immutability tekee retry-semantiikasta selkeää.
- **+** Supan nyt käyttämä massaluovutusrajapinta perustuu jo S3-tiedostoihin
- **−** Lisää komponentteja (worker, S3, bucket-policy, eventbridge tms.),
  enemmän operatiivista pintaa.
- **−** Batchaus tuo lisälatenssia; debugaaminen vaatii hyppäämistä useaan
  paikkaan.

**Sopivuus Supalle**: hyvä — ulkoinen kuluttaja voi lukea S3:sta omilla
prosesseillaan ilman pääsyä Koskin kantaan.

**Sopivuus raportointikannalle**: huono — Kosken oma worker, joka kirjoittaa
Kosken omaan RDS-instanssiin, ei tarvitse S3-välivarastoa. Tämä lisäisi
turhaia liikkuvia osia sisäiseen integraatioon.

### B. Outbox + sekvenssikursori (pull-API tai suora kantaluku)

Outbox-taulu Koskissa, johon triggeri kirjoittaa Oo-muutoksesta rivin.
Kuluttaja pyytää `?after_seq=N&limit=K` ja saa rivit + viimeisen seq:n.

- **+** Yksinkertainen, suoraviivainen, yksi liikkuva osa.
- **−** Tavallinen sekvenssi ei takaa, etteivätkö rinnakkaiset transaktiot
  commit:aisi myöhemmin pienemmällä seq-arvolla → kursori voi liukua
  kirjoituksen yli (sama gap-ongelma kuin aikaleimoilla, kevyemmässä
  muodossa).
- Ratkaistavissa esim. `pg_xact_commit_timestamp` + watermark "kaikki seq:t
  ennen X on takuulla commit:ttu, vasta nämä saa palauttaa", mutta
  watermarkin pitäminen tarkkana vaatii varovaisuutta.

**Sopivuus Supalle**: hyvä — luonnollinen HTTP-pull-rajapinta.

**Sopivuus raportointikannalle**: hyvä, mutta kuluttaja voi olla Kosken
sisäinen worker, joka lukee outboxia suoraan kannasta — ei tarvita
HTTP-API:a sisäiseen kommunikaatioon.

### C. Outbox + ACK-protokolla

Kuten B, mutta kuluttaja palauttaa batchin käsiteltyään ACK:n, jonka
perusteella rivit poistetaan (tai merkitään käsitellyiksi).

- **+** Gap-ongelma katoaa rakenteellisesti: rivi pysyy outboxissa kunnes
  ack:ttu, joten "kursorin liukuminen" ei voi pudottaa riviä.
- **+** Retry on luonnollinen (ack puuttuu → rivi tulee uudestaan).
- **+** Ei tarvitse lukoilla pidätellä kirjoituksia eikä monimutkaisia
  watermark-laskelmia.
- **−** Outbox kasvaa rajatta, jos kuluttaja katoaa pitkäksi aikaa →
  tarvitaan hälytys ja päätös retentio-leikkurista.
- **−** Useamman kuluttajan tapauksessa per-kuluttaja-tila siirtyy Kosken
  puolelle.

**Sopivuus Supalle**: hyvä.

**Sopivuus raportointikannalle**: hyvä.

**Erityishuomio kahden kuluttajan tilanteessa**: jos sama outbox palvelee
sekä Supaa että raportointikantaa, **per-kuluttaja-ACK on välttämätön**,
koska kuluttajat etenevät eri tahtia ja toinen voi olla pitkään alhaalla
toisen pyöriessä. Toteutusvaihtoehtoja:

- **C1**: Yhteinen outbox-taulu + erillinen `outbox_ack` -taulu, jossa
  `(consumer, outbox_row)` ja `acked_at`. Rivi poistettavissa kun kaikki
  konfiguroidut kuluttajat ovat ackanneet. Selkein, mutta vaatii hieman
  enemmän logiikkaa.
- **C2**: Per-kuluttaja outbox-taulu (fyysisesti erilliset taulut, sama
  triggeri kirjoittaa molempiin). Yksinkertaisempi tilaltaan, mutta
  tuplaa kirjoituksen.
- **C3**: Yhteinen outbox + per-kuluttaja-ack-watermark-rivi
  (yksinkertaisempi kuin C1 jos ACK aina järjestyksessä).

### D. Postgres logical replication / WAL (esim. pgoutput, Debezium)

Kuluttaja kuluttaa suoraan replication-slottia.

- **+** Postgres takaa kanonisesti aukottoman, järjestetyn muutoslokin.
- **−** RDS:llä toimii, mutta slot voi paisuttaa WALin niin että kanta
  kaatuu, jos kuluttaja jää jumiin → operatiivinen riski isompi.
- **−** Skeemamuutosten hallinta WAL-tasolla raskaampaa kuin sovellustason
  outboxissa, jossa voimme tallentaa version mukana.

**Sopivuus Supalle**: huono — ulkoinen kuluttaja ja WAL ei ole järkevä
ulkoinen kontrakti. Tarvittaisiin joka tapauksessa sovellustasolla
välittävä komponentti.

**Sopivuus raportointikannalle**: parempi kuin Supan tapauksessa
(sisäinen kuluttaja, Postgres natiivi), mutta slot-paisutusriski jää
yhtä realistiseksi.

### Payloadin sisältö outboxissa (kohtisuora valinta)

Riippumatta valitusta A/B/C/D-pohjasta, outboxin riveille on kaksi
vaihtoehtoa:

- **Lihava**: rivi sisältää koko Oo-JSONin (esim. `paivitetty_opiskeluoikeus`
  -tyyppisesti). Helppo kuluttajalle, mutta paksumpi I/O ja tilankäyttö.
  Supa hyötyy tästä.
- **Kevyt**: rivi sisältää vain `(oppija_oid, opiskeluoikeus_oid, versio)`,
  ja kuluttaja hakee koko Oo:n erillisellä haulla. Vähemmän tilaa, mutta
  kuluttajan on tehtävä lisähaku. Raportointikannan workerille
  järkevämpi koska se on joka tapauksessa Kosken sisällä.

Voi olla, että kahdelle kuluttajalle on optimaalista käyttää eri muotoa,
mikä puoltaa C2-mallia (per-kuluttaja-outbox eri payloadeilla) tai
yhteistä lihavaa outboxia jossa raportointikanta vain ignoraa ylimääräistä.

Raportointikannan workerin osalta on syytä huomata, että F12:n takia
worker tarvitsee joka tapauksessa pääsyn muuhunkin dataan kuin pelkkään
outbox-rivin payloadiin (esim. saman oppijan muiden Oo:iden tila
rinnakkaisuuslaskentaa varten). Eli kevyt payload + Kosken
kantapääsy on tähän käytännössä se mitä tarvitaan; lihava payload säästää
korkeintaan yhden haun.

## 4. Vaihtoehdot ulkoisten lähteiden tuoreuden hallintaan

Tämä koskee vain raportointikantaa. Vaatimus F9: ulkoisten lähteiden data
raportointikannassa ei saa olla vanhempaa kuin samaa oppijaa koskeva
Oo-data. Vaatimus F10/F11/R5/R6: bootstrap Lampi-dumpeista, inkrementti
jostain tuoreemmasta kanavasta.

Kaikissa vaihtoehdoissa oletetaan, että Lampi-dumpit hoitavat
bootstrap-osuuden (kerralla iso lataus → täystäytetty paikallinen
ONR/org/koodisto-cache Kosken RDS:ssä raportointikannan rinnalla). Erot
ovat inkrementtimekanismissa.

### X. Live pull-when-needed

Raportointikanta-worker hakee ONR:ltä tuoreen henkilödatan joka kerta kun
käsittelee Oo-outbox-rivin. Org- ja koodistodataa voidaan käsitellä
samalla logiikalla tai jättää harvempaan päivitysrytmiin.

- **+** Invariantti F9 toteutuu yksinkertaisesti: ONR-haku tehdään juuri
  ennen normalisointia.
- **+** Ei tarvitse erillistä cachea (tai cache on opportunistinen).
- **−** N+1-kuorma ONR:lle (rikkoo NF6 ja R6:n hengen, koska tuotantopalvelu
  saa paljon liikennettä).
- **−** Worker hidastuu — jokainen Oo-rivi vaatii synkronisen ONR-haun.
- Mahdollinen variantti: batch-haku ONR:lle per outbox-batch
  (jos ONR tarjoaa multi-get-rajapinnan), lievittää N+1:tä mutta ei poista
  perusongelmaa.

### Y. Lokaali cache + minuuttitason inkrementti ONR:stä

Kosken puolella pidetään yllä lokaalia ONR-cache-taulua. Inkrementti
hoidetaan jollain seuraavista:

- **Y1**: ONR tarjoaa push-/pull-feedin minuuttitason muutoksista (ei vielä
  ole, vaatisi ONR-puolen kehitystyötä).
- **Y2**: Koski pollaa ONR:n live-rajapintaa esim. minuutin välein "anna
  kaikki sitten X muuttuneet" -kyselyllä, jos ONR sellaisen tarjoaa.
- **Y3**: Tunnin jäljessä olevat Lampi-muutosdumpit täydennetään yksittäisillä
  live-hauilla niille oppijoille, joiden Oo on muuttunut ja joiden ONR-data
  on yli X minuuttia vanha — hybridi X:n ja Y:n välillä.

Riippumatta mekanismista, raportointikanta-worker tarkistaa
ONR-cache-rivin freshness-leiman ennen normalisointia, ja jos
`onr_cached_at < oo_outbox_row.created_at`, joko odottaa cachen päivittymistä
tai laukaisee pakotetun haun.

- **+** ONR-kuorma minimissä (jos Y1/Y2 toimii).
- **+** Cache palvelee myös muita Kosken käyttötapauksia.
- **−** Inkrementtimekanismin pitää olla olemassa — Y1 vaatii ONR:n
  kehitystyötä, Y2 vaatii sopivan rajapinnan, Y3 on toiminnallisesti
  monimutkainen.

### Z. ONR-muutosfeed Kosken outboxiin

Symmetrinen Oo-outboxin kanssa: ONR pushaa (tai Koski pollaa) muutoksia,
jotka kirjataan Kosken puolelle erilliseen `henkilo_outbox` -tauluun. Sekä
Supa että raportointikanta voivat kuluttaa myös tätä jonoa.

- **+** Sama mentaalimalli ja sama mekaniikka kuin Oo-muutoksille.
- **+** Cross-source consistency on ratkaistavissa per-oppija-järjestyksessä
  raportointikanta-workerissa (esim. per-oppija-lukko: prosessoi yhden
  oppijan kaikki päällekkäiset Oo- ja henkilömuutokset järjestyksessä).
- **−** Vaatii ONR:n puolelta sopivaa feedmekanismia (sama kysymys kuin
  Y1).
- **−** ONR-puolen muutoksesta voi seurata invalidointiaalto — yksi
  henkilömuutos voi vaatia useamman Oo-rivin re-derivaation
  raportointikantaan.

### W. Pelkkä snapshot-pohjainen päivitys (huonompi vaihtoehto)

Lampi-dumpit ladataan paikallisesti tunnin tahdissa, ONR-tuoreus
raportointikannassa on parhaimmillaan tunti jäljessä. Tämä rikkoo F9:ää,
mutta on yksinkertaisin malli ja voisi riittää organisaatio- ja
koodistodatalle (joiden F9 oli pehmeämpi).

- Listattu täydellisyyden vuoksi — selvästi alle vaatimustason
  ONR:lle, mutta voi olla hyväksyttävä org/koodisto-datalle.

### Yhteinen havainto

Vaihtoehdot X, Y, Z ja W eivät ole keskenään poissulkevia eri lähteille:
- ONR voisi olla X tai Z (tiukin tuoreus tarvitaan).
- Organisaatio voisi olla Y2 tai jopa W (snapshot).
- Koodisto on käytännössä lähes staattinen → W riittänee.

Tämä antaisi malliksi "tiukin tuoreusvaatimus määrittää inkrementtikanavan
per lähde", mutta mekanismit ovat kuitenkin samankaltaisia (taulun
cachella + invariantin tarkistus workerissa).

## 5. Vertailutaulukko

Tiivistys vaihtoehdoista molempien skenaarioiden kannalta:

| Vaihtoehto              | Supa            | Raportointikanta  | Operatiivinen kuorma | Gap-riski |
| ----------------------- | --------------- | ----------------- | -------------------- | --------- |
| A. Outbox + S3          | ✅              | ➖ (tarpeeton)    | Korkea               | Matala    |
| B. Outbox + seq         | ✅              | ✅                | Matala               | Keskitaso |
| C. Outbox + ACK         | ✅              | ✅                | Matala               | Matala    |
| D. WAL / logical repl.  | ➖ (ulkoinen)   | ⚠️ (slot-riski)   | Korkea               | Matala    |

Ulkoisten lähteiden inkrementti (vain raportointikanta):

| Vaihtoehto                  | F9-tuoreus | ONR-kuorma | Riippuvuus ONR:stä |
| --------------------------- | ---------- | ---------- | ------------------ |
| X. Live pull-when-needed    | ✅          | Korkea     | Live-API-haku      |
| Y. Cache + inkrementti      | ✅          | Matala     | Feed/poll-API      |
| Z. ONR-outbox               | ✅          | Matala     | Push-feed          |
| W. Pelkkä Lampi-snapshot    | ❌ ONR:lle  | Matala     | Lampi-dump         |

## 6. Avoimet kysymykset

- **Skooppi**: vain `opiskeluoikeus`, vai myös päättelyperusteet ja
  cron-päättelyt (jotka voivat muuttaa Oo:n näkymää ilman varsinaista
  kirjoitusta)? Vaikuttaa siihen, mistä triggerit lähtevät.
- **Per-kuluttaja-ACK:n tietorakenne**: C1, C2 vai C3 (ks. §3 C-osio).
  Liittyy siihen, halutaanko Supalle ja raportointikannalle eri payload-muoto.
- **Outboxin retentio**: mikä on sallittu maksimikoko / -ikä ennen
  hälytystä ja mahdollista pakkoleikkausta? Liittyy R3:een (RDS-kapasiteetti).
- **Payloadin paino**: lihava vs. kevyt outbox (§3). Mahdollisesti
  kuluttajakohtainen valinta.
- **ONR:n minuuttitason feed**: onko tällainen olemassa tai saatavissa?
  Tämän vastaus määrittää, mikä §4-vaihtoehdoista on edes mahdollinen.
- **Cross-source järjestys**: jos sekä Oo että ONR muuttuvat samaa oppijaa
  koskien, miten raportointikanta-worker säilyttää järjestyksen ja
  per-oppija-konsistenssin?
- **Konsolidointitaulujen riippuvuusgraafi (F12)**: miten kuvataan ja
  ylläpidetään tieto siitä, mitkä raportointikannan rivit pitää
  uudelleenlaskea, kun yksi Oo muuttuu? Vaihtoehtoja:
  - kovakoodattu workerin logiikkaan per konsolidointitaulu,
  - eksplisiittinen riippuvuustaulu (Oo → riippuvat rivit),
  - "laske uudelleen koko oppijan kaikki johdannaisrivit" -karkea sääntö.
  Lisäksi: milloin ACK voidaan tehdä outbox-rivistä — vasta kun kaikki
  riippuvat rivit on päivitetty, vai erillinen vaihe?
- **Bootstrapin uudelleenajo**: jos jälkikäteen huomataan että jokin
  data on virheellistä raportointikannassa tai Supassa, mikä on
  uudelleenladon mekanismi? Onko se sama outbox-tie (seedaa outboxiin),
  vai erillinen "force-refresh"-rajapinta?
- **Skeemaversiointi**: miten breaking-muutokset Oo-JSONiin välitetään
  ja koordinoidaan Supa-tiimin kanssa? Raportointikannassa Kosken oma
  worker voi mukautua skeemamuutoksiin yhdessä Oo-skeeman päivityksen
  kanssa.
- **Audit-loki**: riittääkö ack-poisto, vai pidetäänkö pysyvä lähetysloki
  erillisessä taulussa (henkilötietojen luovutuksen jäljitettävyys —
  erityisesti Supan tapauksessa).
- **Organisaatio- ja koodistodatan tuoreusrajat**: kuinka tiukka F9 on
  näiden osalta? Tämä vaikuttaa siihen, voiko valita W:n vai vaatiiko
  Y/Z-tyyppistä mekanismia.
