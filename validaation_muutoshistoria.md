# Koskeen tallennettavien tietojen validaatiosäännöt

## 15.9.2023

- LOPS 2019 -mukaista lukion oppimäärän suorituksen kokonaislaajuutta ei validoida, jos oppimäärän suorituksesta löytyy oman äidinkielen suorituksia.

## 25.8.2023

- Helsingin Eurooppalaisen koulun vuosiluokalle S5 sallitaan maksuttomuustiedon lisääminen

## 17.8.2023

- Pidennetyn oppivelvollisuuden ja erityisen tuen ajanjaksojen validaatiota tarkennettu terminaalitilaisen opiskeluoikeuden osalta.

## 30.6.2023

- IB-oppiaineiden arvioinneissa kaikki arviointi-haaraan siirretyt arvioinnit katsotaan päättöarvioinneiksi, vaikka niissä olisi predicted-kentän arvona true.

## 21.06.2023

- TUVA-validointiin muutoksia 1.8.2023 alkaen:
  - Valmistunut-tilan voi jatkossa merkitä, kun opiskeluoikeudella on Opiskelu- ja urasuunnittelutaidot - koulutuksen osan suoritus, jonka laajuus on vähintään kaksi viikkoa.
  - Kaikilla muilla koulutuksen osilla poislukien Opiskelu- ja urasuunnittelutaidot ei ole enää määriteltyä vähimmäislaajuutta. Enimmäislaajuudet säilyvät ennallaan.

## 16.5.2023

- Lukion oppimäärän laajuuden validointi jätetään tekemättä aikuisten opetussuunnitelman mukaisissa suorituksissa tietyissä oppilaitoksissa.
  - Sallitut oppilaitokset: `1.2.246.562.10.73692574509` ja `1.2.246.562.10.43886782945`

## 25.4.2023

- Tutkintotavoitteiset ammattikoulun opiskeluoikeuksien duplikaatit estetty validaatiolla. Duplikaattius päätellään oppilaitoksen ja perusteen diaarinumeron perusteella.

## 24.4.2023

- Aikuisten lukiokoulutuksen uudessa opintosuunnitelmassa (2019) Historian kurssien HI2 ja HI3 nimet on korjattu
  vastaamaan Opetushallituksen laatimaa opetussuunnitelmaa myös oppiaineiden oppimäärien suorituksille.
- Tutkintotavoitteiset ammattikoulun opiskeluoikeuksien duplikaatit estetty validaatiolla. Duplikaattius päätellään oppilaitoksen ja peruteen diaarinumeron perusteella. Päällekkäinen opiskeluoikeus sallitaan kuitenkin siinä tapauksessa, että sen aikajakso ei leikkaa aiemman opiskeluoikeuden aikajakson kanssa.

## 14.3.2023

- Taiteen perusopetuksen opiskeluoikeutta ei voi merkitä hyväksytysti suoritetuksi ellei molempia päätason suorituksia ole vahvistettu.

## 13.3.2023

- Lisätty validaatioita ammatillisen opiskeluoikeuden korotuksen suoritukselle:
  - Korotuksen opiskeluoikeuden alkamispäivä on oltava 1.7.2023 tai sen jälkeen.
  - Vain yksi päätason suoritus korotuksen opiskeluoikeudella.
  - Korotetulla suorituksella on oltava linkitys alkuperäiseen opiskeluoikeuteen.
  - Korotettua keskiarvoa ei voi siirtää jos kyseessä ei ole korotuksen suoritus.
  - Korotettu-tietoa ei voi siirtää osasuoritukselle tai aliosasuoritukselle jos kyseessä ei ole korotuksen suoritus.
  - Kaikkien korotuksen suorituksen osasuoritusten tulee olla korotuksen yrityksiä tai tunnustettuja.
  - Muun tutkinnon osaa tai yhteisen tutkinnon osaa ei voi siirtää tunnustettuna korotuksen suoritukselle.
  - Korotettua keskiarvoa ei voi siirtää valmistuneelle korotuksen suoritukselle, jos kaikki korotuksen yritykset epäonnistuivat.
  - Jos korotuksen suoritus päättyy katsotaan eronneeksi -tilaan, ei suoritukselle voi siirtää osasuorituksia.
  - Jos korotuksen suoritus päättyy katsotaan eronneeksi -tilaan, ei suoritukselle voi siirtää korotettua keskiarvoa.
- Korotetun suorituksen linkitystä alkuperäiseen opiskeluoikeuteen ei voi muuttaa sen jälkeen kun linkitys on tehty.
- Alkuperäisen opiskeluoikeuden suoritukselle lisätty validaatiot:
  - Alkuperäinen linkitetty opiskeluoikeus löytyy oppijalle.
  - Alkuperäinen opiskeluoikeus on valmistunut-tilassa.
  - Alkuperäisen opiskeluoikeuden suoritus on ammatillisen perustutkinnon suoritus.
  - Alkuperäisellä ja korotuksen suorituksella on sama koulutusmoduulin tunniste, diaarinumero sekä koulutustyyppi.
- Lisätty validaatioita korotetun suorituksen osasuorituksille ja aliosasuorituksille.
  - Alkuperäiseltä opiskeluoikeudelta on löydyttävä korotuksia vastaavat osasuoritukset ja aliosasuoritukset.
    - Vastaavuudessa huomoidaan osasuorituksen koulutusmoduulin tunniste, laajuus sekä mahdollinen pakollisuus.
    - Samalla tutkinnon osan koodilla ei voi olla enempää korotuksia kuin mitä alkuperäiseltä opiskeluoikeudelta löytyy.
  - Korotetun opiskeluoikeuden osasuorituksen laajuus täytyy vastata sen aliosasuoritusten yhteenlaskettua laajuutta.

## 28.2.2023

- Taiteen perusopetuksen koulutuksen toteutustapaa ei voi muuttaa opiskeluoikeuden luonnin jälkeen.

## 15.2.2023

- Taiteen perusopetuksen päätason suorituksille saa merkitä vahvistuksen, kun sillä on ainakin yksi arvioitu osasuoritus jolla on myös riittävä laajuus.

## 10.2.2023

- Sallittu väliaikaisesti oppilaitoksen vaihto tilanteessa, jossa vaihdetaan oppilaitoksesta 1.2.246.562.10.13857290038 oppilaitokseen 1.2.246.562.10.38485743660

## 25.1.2023

- Tutkintoon valmentavan koulutuksen (TUVA) jaksolta vaaditaan rahoitustieto kun jakson tila on `valmistunut`, `lasna` tai `loma`.

## 24.1.2023

- Taiteen perusopetuksen päätason suoritusten laajuuksien minimitasot muutettu yhden desimaalin tarkkuudelle:
  - Yleisen oppimäärän yhteiset opinnot: 11,1op.
  - Yleisen oppimäärän teemaopinnot: 7,4op.
  - Laajan oppimäärän perusopinnot: 29,6op.
  - Laajan oppimäärän syventävät opinnot: 18,5op.

## 19.1.2023

- Lisätty validaatio että vahvistetulta jatkuvaan oppimiseen suunnatulta vapaan sivistystyön koulutuksen suoritukselta löytyy laajuus.
  - Myös vahvistetun suorituksen osasuorituksilta ja niiden aliosasuorituksilta validoidaan että laajuudet löytyvät.

## 3.1.2023

- Sallittu väliaikaisesti oppilaitoksen vaihto tilanteessa, jossa vaihdetaan oppilaitoksesta 1.2.246.562.10.21744269164 oppilaitokseen 1.2.246.562.10.70112627842

## 21.12.2022

- VALMA- ja perusopetuksen lisäopetuksen opiskeluoikeuksille ei sallita uusia tiloja, joiden alkupäivämäärä on 1.6.2023 tai sen jälkeen.

## 19.12.2022

- Taiteen perusopetuksen opiskeluoikeuksille lisätty validaatiot:
  - Suoritusta ei voi vahvistaa jos sillä on arvioimattomia osasuorituksia.
  - Suorituksen voi vahvistaa jos sillä ei ole yhtään osasuoritusta.
  - Vahvistetun suorituksen laajuuden on oltava riittävä opintotason minimitasoon nähden.
    - Yleisen oppimäärän yhteiset opinnot: 11,11op.
    - Yleisen oppimäärän teemaopinnot: 7,41op.
    - Laajan oppimäärän perusopinnot: 29,63op.
    - Laajan oppimäärän syventävät opinnot: 18,52op.
  - Opiskeluoikeudella voi olla vain yleisen tai laajan oppimäärän suorituksia, mutta ei molempia.
  - Opiskeluoikeudella voi olla vain yhden taiteenalan suorituksia.
  - Suorituksella on oltava kelvollinen perusteen diaarinumero.

## 14.12.2022

- Siirretään TOR-1834:ssä lisätty validaatio (suorituksen alkamispäivämäärä ei saa olla ennen opiskeluoikeuden alkua) tulemaan voimaan myöhemmin 1.2.2023.

## 9.12.2022

- Lukion nuorten oppimäärän (LOPS2015) voi merkitä valmiiksi vasta kun kursseja on suoritettu vähintään 75 kpl.
- Lukion aikuisten oppimäärän (LOPS2015) voi merkitä valmiiksi vasta kun kursseja on suoritettu vähintään 44 kpl.

## 30.11.2022

- Tarkistetaan tietyissä palvelukäyttäjän siirroissa, että suoritukset eivät ala ennen opiskeluoikeuden alkua.

## 21.11.2022

- Nuorten perusopetuksen vuosiluokan päätason suoritus sallitaan vahvistaa ilman osasuorituksia,
  kun oppija on vuosiluokkiin sitomattomassa opetuksessa.

## 15.11.2022

- Ammatillisen perustutkinnon osatutkinnosta valmistuneilta vaaditaan keskiarvo, kun valmistumispäivä on 1.1.2022 tai sen jälkeen.

## 11.11.2022

- Jatkuvaan oppimiseen suunnatulle vapaan sivistystyön koulutukselle lisätty seuraavat validaatiot:
  - Opiskeluoikeuksien tallentaminen testiympäristöihin sallittu.
  - Ko. opiskeluoikeutta ei voi merkitä alkavaksi kuin aikaisintaan 1.1.2023.
  - Rahoitusmuoto valitaan opiskeluoikeusjaksoille, joiden tila on läsnä tai hyväksytysti suoritettu.
  - Jos päätason suoritus on vahvistettu, opiskeluoikeuden päättävänä tilana ei voi olla keskeytynyt.

## 9.11.2022

- TUVAn valinnaisen suorituksen laajuus voidaan syöttää ilman osasuorituksia, mutta jos osasuorituksia on, niiden laajuksien summan pitää vastata suorituksen laajuutta.
- Vapaan sivistystyön koulutuksen opiskeluoikeuksien sallitut jakson tilat on määritelty tietomallitasolla erillisen validaation sijaan, minkä johdosta virhekoodit ei-sallitusta tiloista ovat muuttuneet.
- Jatkuvaan oppimiseen suunnatun koulutuksen rahoitusmuotoa ei voi käyttää ennen ympäristöön määriteltyä rajapäivää.

## 28.10.2022

- Palauta aiemmin välikaikaisesti käytöstä poistettu ammatillisen tutkinnon osan ryhmän validaatio.
  Jos koulutuksen tyyppi on ammatillinen perustutkinto, jonka suoritustapa on ops tai reformi, rakenteessa tulee olla tutkinnon osan ryhmä määriteltynä.

## 26.10.2022

- JOTPA-rahoitusmuoto (14, 15) ei voi vaihtua kesken opiskeluoikeuden, eikä sitä voi käyttää opiskeluoikeuksissa, joissa on käytössä muita rahoitusmuotoja.
- JOTPA-rahoitusmuodon voi lisätä ammatillisen koulutuksen lisäksi myös jatkuvaan oppimiseen suunnatulle vapaan sivistyön koulutuksen opiskeluoikeudelle.

## 25.10.2022

- TUVA-opiskeluoikeuksissa ei enää sallita tiloja eronnut ja peruutettu.

## 24.10.2022

- Sallittu väliaikaisesti oppilaitoksen vaihto tilanteessa, jossa vaihdetaan oppilaitoksesta 1.2.246.562.10.93428463247 oppilaitokseen 1.2.246.562.10.77609835432
- Lisätty TUVA- ja VST-suorituksille perusteiden koulutustyypin validaatio. Estetään näin koulutuksien syöttäminen väärällä diaarinumerolla.

## 19.10.2022

- Sallitaan uudet JOTPA-rahoitusmuodot ainoastaan ammatillisen koulutuksen opiskeluoikeuksille.
- Tunnustetun ammatillisen tutkinnon tai osittaisen ammatillisen tutkinnon osan tutkinnon perusteen voimassaoloa ei enää validoida.

## 18.10.2022

- Lisätty validaatio siitä, että diaarinumeron määrittelemä peruste on voimassa, vaikka opiskeluoikeus ei
  olisikaan vielä päättynyt. Tässä tapauksessa perusteen pitää olla parhaillaan voimassa.
- Jos keskeneräinen opiskeluoikeus on asetettu alkamaan tulevaisuudessa,
  diaarinumeron määrittelemän perusteen tulee olla voimassa opiskeluoikeuden alkamispäivänä.

## 12.10.2022

- Ammatillisen suorituksen voi tallentaa valmis-tilaisena ilman keskiarvoa mikäli kyseessä on linkitetyn opiskeluoikeuden suoritus.

## 5.10.2022

- Validaatioissa tarkastetaan, että opiskeluoikeuden päättymispäivä osuu perusteen voimassaoloaikaan,
  tai viimeistään perusteen siirtymäaikaan. Tämä validaatio oli aiemmin toteutettu vain ammatillisen
  koulutuksen opiskeluoikeuksille, ja se oli väliaikaisesti 15.8.2022 alkaen pois päältä.

## 4.10.2022

- Poistettu laajuuden validointi VST-KOTO-opintojen kieli- ja viestintäosaamisen osasuoritukselta.

## 30.9.2022

- Korjaus ammatillisen validaatioon: keskiarvo vaaditaan vain perustutkinnolta, joka on päättynyt 15.1.2018 tai myöhemmin ja suoritustapana on reformi/ops.

## 29.9.2022

- Ammatillisen suoritukselle pitää lisätä keskiarvo kun suoritus on valmis
- Ammatillisen suoritukselle sallitaan keskiarvo, kun opiskeluoikeuden on tilassa "katsotaan eronneeksi" ja suorituksissa on vähintään yksi tutkinnon osa

## 19.9.2022

- Ammatillisen tutkinnolle tai osatutkinnolle ei voi lisätä keskiarvoa ellei suoritus ole valmis.
- 15.9. käyttöönotetut oppivelvollisuuden pidennyksen, vammaisuuden, vaikeasti vammaisuuden ja erityisen tuen jaksojen
  validaatiot on poistettu käytöstä 1.10.2022 asti
- Pidennetyn oppivelvollisuuden validaatioista on palautettu tuotantoympäristössä käyttöön vanhat aiemmin käytössä
  olleet versiot 1.10. asti. Testiopintopolussa käytössä ovat uudet validaatiot samantien.

## 15.9.2022

- 19.8. testiopintopolussa jo käytetyt oppivelvollisuuden pidennyksen, vammaisuuden, vaikeasti vammaisuuden ja erityisen
  tuen jaksojen validaatiomuutokset on otettu käyttöön. Poikkeuksena tutkintokoulutukseen valmentavan koulutuksen
  perusopetuksen järjestämisluvalla järjestettävä opiskeluoikeus, josta on poistettu pidennetyn oppivelvollisuuden
  tallennusmahdollisuus. Siinä validoidaan:
  - ei sallita tiedon vaikeasti kehitysvammainen tai muu kuin vaikeasti kehitysvammainen siirtämistä, elleivät ne osu jollekin siirretylle erityisen tuen jaksolle
  - ei sallita päällekkäisiä vaikeasti kehitysvammainen- ja muu kuin vaikeasti kehitysvammainen-jaksoja

## 13.9.2022

- VALMA- ja perusopetuksen lisäopetuksen opiskeluoikeuksille ei sallita uusia tiloja, joiden alkupäivämäärä on 2.10.2022 tai sen jälkeen.

## 19.8.2022

- Seuraavat validaatiomuutokset ovat toistaiseksi vain testiopintopolussa, ne viedään tuotantoon myöhemmin:
  - Oppivelvollisuuden laajennuksen, vammaisuuden, ja vaikeasti vammaisuuden aikajaksojen validointia on korjattu. Säännöt ovat nyt:
    - pidennetyn oppivelvollisuuden jakso ei saa päättyä ennen kuin opiskeluoikeus alkaa
    - Jokin vammaisuuspäivä vaaditaan jokaiselle pidennetyn oppivelvollisuuden päivälle
    - ei sallita tiedon vaikeasti kehitysvammainen tai muu kuin vaikeasti kehitysvammainen siirtämistä, elleivät ne osu jollekin siirretylle pidennetyn oppivelvollisuuden jaksolle
    - ei sallita päällekkäisiä vaikeasti kehitysvammainen- tai muu kuin vaikeasti kehitysvammainen-jaksoja
    - jos pidennetyn oppivelvollisuuden jaksolle on merkitty päättymispäivä, vaaditaan päättymispäivä myös kronologisesti myöhäisimmältä vammaisuusjaksolta
  - Lisäksi erityisen tuen päätöksien osalta validoidaan, että jokin erityisen tuen päätös on voimassa jokaisena pidennetyn oppivelvollisuuden jakson päivänä
  - Samat validaatiot tehdään nyt myös tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeuksille, jos niissä on perusopetuksen järjestämislupa
  - Sallittu väliaikaisesti oppilaitoksen vaihto tilanteessa, jossa vaihdetaan oppilaitoksesta 1.2.246.562.10.63813695861 oppilaitokseen 1.2.246.562.10.42923230215

## 17.8.2022

- International school -opiskeluoikeuden MYP-suorituksissa sallitaan nyt pass/fail -arviointi

## 15.8.2022

- Muutettu ammatillisen koulutuksen validaatiota väliaikaisesti siten, että perusteen voimassaolopäiväystä ei tarkasteta lisättäessä ammatillisen koulutuksen opiskeluoikeuksia järjestelmään.

## 8.8.2022

- Tietoja koulutuksen maksuttomuudesta ei enää vaadita pakollisena millekään opiskeluoikeudelle.

## 5.8.2022

- Korjattu ammatillisen koulutuksen validaatio, jossa tarkistetaan onko opiskeluoikeuden alkamisaika ja päättymisaika perusteen voimassaoloajan mukainen.

## 2.8.2022

- VALMA- ja perusopetuksen lisäopetuksen opiskeluoikeuksille ei sallita uusia tiloja, joiden alkupäivämäärä on 2.8.2022 tai sen jälkeen.

## 12.7.2022

- Lisätty ammatillisen koulutuksen järjestämisluvan piirissä oleville TUVA-opiskeluoikeuksille sallituksi tilaksi 'loma'

## 7.7.2022

- Korjattu vapaan sivistystyön kotoutumiskoulutuksen (ops 2022) ohjauksen osasuorituksen validoinnit vastaamaan perustetta

## 5.7.2022

- Ammatillisen yhteisen tutkinnon osan osa-alueen koodi VVAI22 sallitaan vain suoritukselle, jonka perusteen voimaantulon päivä on 1.8.2022 tai sen jälkeen.

## 4.7.2022

- Sallitaan muuttuneet ammatillisen koulutuksen yhteisten tutkinnon osien koodit:
  - 106727: Viestintä- ja vuorovaikutusosaaminen
  - 106728: Matemaattis-luonnontieteellinen osaaminen
  - 106729: Yhteiskunta- ja työelämäosaaminen

## 16.6.2022

- Aikuisten lukiokoulutuksen uudessa opintosuunnitelmassa (2019) Historian kurssien HI2 ja HI3 nimet on korjattu vastaamaan Opetushallituksen laatimaa opetussuunnitelmaa.

## 14.6.2022

- Sallitaan peruskoulut esiopetuksen opiskeluoikeuden oppilaitoksina, kun järjestämismuotona on ostopalvelu tai palveluseteli.

## 8.6.2022

- Mitätöitäessä opiskeluoikeuksia ei ole n. 1.4.2022 alkaen täydennetty puuttuvia tietoja, kuten oppilaitosta,
  yhteenlaskettuja laajuuksia, perusteen nimeä jne. automaattisesti opiskeluoikeuteen ennen sen tallentamista.
  Tämä on nyt korjattu ja tiedot täydennetään myös mitätöinnin yhteydessä.

## 1.6.2022

- Tutkintokoulutukseen valmentavan koulutuksen ja vapaan sivistystyön oppivelvollisille suunnatun koulutuksen
  opiskeluoikeuksissa ei sallita arvioimattomia osasuorituksia, jos opiskeluoikeus on päättynyt katsotaan
  eronneeksi -tilaan.

## 31.5.2022

- Vapaan sivistystyön opiskeluoikeuksissa, joissa päätason suoritus on vapaatavoitteisen koulutuksen suoritus, on nyt pakollisena tietona opetussuunnitelma. Opintohallintojärjestelmistä siirrettäessä pakotus astuu voimaan 1.8.2022 alkaen.

## 9.5.2022

- Perusopetuksen aineilta ei vaadita enää laajuutta toiminta-alueittain opiskelevilla, vaikka suoritus olisi
  vahvistettu 1.8.2020 jälkeen.

## 12.4.2022

- Opiskeluoikeuden pidennetyn oppivelvollisuuden aikajakson täytyy sisältyä vammaisuusjaksojen aikajaksoihin.
  - Vammaisuuden ja vaikeasti vammaisuuden toisiinsa lomittuvat aikajaksot käsitellään yhtenäisenä aikajaksona validointia varten.
  - Jos opiskeluoikeuden pidennetyn oppivelvollisuuden aikajakso alkaa ennen opiskeluoikeuden alkamispäivää,
    käytetään opiskeluoikeuden alkamispäivää pidennetyn oppivelvollisuusjakson alkamispäivänä vammaisuusjaksojen validointia varten.

## 7.4.2022

- Nuorten perusopetuksen oppimäärän suorituksessa luokka-astetta voi käyttää vain erityisen tutkinnon kanssa.

## 4.4.2022

- Perusopetuksen aineilta vaaditaan laajuus myös toiminta-alueittain opiskelevien tapauksessa, kun suoritus on vahvistettu 1.8.2020 jälkeen.

## 1.4.2022

- Mitätöity opiskeluoikeus jättää väliin opiskeluoikeuden validoinnin.

# 22.3.2022

- Muuta 16.3.2022 tehty validaatio estämään perusopetuksen oppimäärän vahvistaminen opiskeluoikeuden valmistuneeksi
  merkitsemisen sijasta

# 16.3.2022

- Estä perusopetuksessa opiskeluoikeuden valmistuneeksi merkitseminen, jos mukana ei ole 9. luokan
  suoritusta, paitsi jos lisätiedoissa on merkitty vuosiluokkiin sitomaton opetus tai jos lisätiedon kotiopetus aikajakso
  on voimassa päättötodistuksen vahvistuspäivänä tai päättötodistuksessa on suoritustapana erityinen tutkinto

## 14.3.2022

- TUVA-opiskeluoikeuden järjestämislupa ei saa muuttua.

# 8.3.2022

- Perusopetuksen opiskeluoikeuden päättymispäivä ei voi olla vuosiluokan suorituksen alkamispäivää ennen

## 22.3.2022

- VST:n vapaatavoitteiselle opiskeluoikeudelle sallitaan tila mitatoity, jotta opiskeluoikeus voidaan mitätöidä.

## 14.2.2022

- Valmistuneen TUVA opiskeluoikeuden pääsuorituksen laajuus oltava välillä 4-38 viikkoa.
- Valmistuneen TUVA opiskeluoikeuden osasuoritusten laajuudet on oltava sallitulla välillä.
- Valmistuneen TUVA opiskeluoikeuden osasuoritusten rakenne oltava sallittu.
  - Opiskeluoikeudella on olemassa pakollinen osasuoritus Opiskelu- ja urasuunnittelutaidot sekä suoritukset kahdesta muusta koulutuksen osasta.

## 28.1.2022

- Sallitaan 1.8.2021 tai myöhemmin alkaneet lukio-opinnot aiemmalla kuin 2019 opetussuunnitelmalla, jos opiskelee
  aikuisten opetussuunnitelman mukaan.

## 27.1.2022

- Pre-IB 2015 opintojen maksuttomuusvalidointi laajennettu sallimaan kaikki vuonna 2021 aloitetut suoritukset.
- Pre-IB 2015 suoritukset lasketaan oppivelvollisuuden suorittamiseen kelpaavaksi.

## 25.1.2022

- Lukion vanhan opsin mukaisen aikuisten oppimäärällisen lukio-opiskeluoikeuden luonti sallitaan
- Ammatillinen näyttötutkintoon valmistava koulutus voi olla merkittynä vahvistetuksi, vaikka opiskeluoikeus päättyisi tilaan "katsotaan eronneeksi"

## 18.1.2022

- Varhaiskasvatuksen opiskeluoikeuksilla oppilaitosta ei voi vaihtaa, paitsi jos oppilaitos on epäaktiivinen tai se löytyy jo organisaatiohistoriasta.
- Ammatillisen suorituksen suoritustyyppiä ei voi vaihtaa.s

## 18.1.2022

- Ammatillisen opiskeluoikeuden tutkintokoodin ja suoritustavan muuttamiseen liittyvä, 13.12.2021 luotu validaatio ollut virheellinen; Validaatio korjattu. Ammatillisen opiskeluoikeuden tutkintokoodia (suorituksen koulutusmoduulin koodiarvo) ja suoritustapaa ei voi vaihtaa ammatillisen opiskeluoikeuden luonnin jälkeen.
- Validaatio liittyen oppilaitoksen vaihtoon ollut virheellinen. Oppilaitosta, joka on aktiivinen, ei voi vaihtaa jos koulutustoimija pysyy samana.

## 20.12.2021

- Perusopetuksen (aikuiset, nuoret ja lukio) ja perusopetuksen lisäopetuksen kenttää "tehostetunTuenPäätökset" ei enää oteta siirroissa vastaan; tieto tiputetaan pois ilman virhettä/varoitusta
- Perusopetuksen (nuoret) ja perusopetuksen lisäopetuksen kenttää "perusopetuksenAloittamistaLykätty" ei enää oteta siirroissa vastaan; tieto tiputetaan pois ilman virhettä/varoitusta

## 1.1.2022

- Ammatilliselle opiskeluoikeudelle ei voi siirtää suoritusta, jonka rakenteen peruste on vanhentunut.

## 13.12.2021

- Ammatilliselle opiskeluoikeudelle ei voi siirtää kahta päätason suoritusta, ellei toinen ole tyyppiä `AmmatillisenTutkinnonOsittainenTaiKokoSuoritus`, suoritustapana `nayttö`, ja toinen suoritus `NäyttöTutkintoonValmistavaKoulutus`
- Ammatillisen opiskeluoikeuden päätason suoritusten tutkintokoodia (koulutusmoduuli.tunniste.koodiarvo) ja suoritustapaa ei voi vaihtaa suorituksen luonnin jälkeen.

## 3.12.2021

- Pääsuoritustasoinen `lukionOppimääräSuoritettu`-kenttä deprekoitu eikä sitä saa enää siirtää. Kentän korvannut opiskeluoikeustason kenttä `oppimääräSuoritettu`

## 16.12.2021

- Lukion 2019 opetussuunnitelman mukaisessa opiskeluoikeudessa, jossa aineopintoja ja oppimäärä on merkitty
  suoritetuksi: Validoi, että opintopisteitä on tarpeeksi. Aikuisten oppimäärässä pitää olla vähintään 88 op,
  nuorilla vähintään 150 op, joista vähintään 20 op valinnaisia.

## 30.11.2021

- Lukion opiskeluoikeuden uusi oppimääräSuoritettu-kenttä ei voi olla true:
  - jos ei ole vahvistettuja päätason suorituksia
  - jos 2019-opetussuunnitelman mukaisissa oppimärän opinnoissa ei ole tarpeeksi opintopisteitä
    (nuorilla 150 op, joista 20 op valinnaisia, aikuisilla 88 op)
- Lukion 2019-opetussuunnitelman mukaisessa opiskeluoikeudessa oppiaineen oppimäärän suorituksen
  lukionOppimääräSuoritettu-kenttä on deprekoitu, eikä sitä saa enää siirtää

## 22.11.2021

- Salli maksuttomuustieto 2015 opetussuunnitelman mukaisissa Pre IB-opinnoissa, jos ne on aloitettu 1.8.2021 tai myöhemmin

## 15.11.2021

- Salli maksuttomuustieto 2019-opetussuunnitelman mukaisissa Pre-IB-opinnoissa

## 14.10.2021

- Ammatillisen koulutuksen Yhteisille tutkinnon osille (YhteinenTutkinnonOsa-rakenne) sallitaan tunnisteen koodiarvoiksi myös '600001' ja '600002'

## 12.10.2021

- Vapaatavoitteisella vapaan sivistystyön opiskeluoikeudella ei voi olla päättävänä tilana "hyväksytysti suoritettu"
  kun suoritus on vahvistamaton; eikä "keskeytynyt" kun suoritus on vahvistettu.

## 12.10.2021

- Lukion 2019 opetussuunnitelman mukaisessa opiskeluoikeudessa salli paikallisen oppiaineen arviointina 'S' myös
  silloin, kun sen laajuus on yli 2 op.

## 11.10.2021

- Maksuttomuusvalidointi velvoitti maksuttomuustiedon siirtämiseen aina, jos perusopetus oli
  päättynyt ennen vuotta 2021 johonkin muuhun syyhyn kuin valmistumiseen, kuten eroamiseen. Tämä on korjattu.

## 5.10.2021

- Vapaatavoitteiselle vapaan sivistystyön koulutuksen opiskeluoikeudella sallitaan vain päättävät tilat.

## 4.10.2021

- Opiskeluoikeuden yhteydessä on siirrettävä maksuttomuustiedot, kun kaikki seuraavat ehdot täyttyvät:
  - Oppija on syntynyt vuonna 2004 tai myöhemmin
  - Oppija on KOSKI-datan mukaan suorittanut perusopetusta vuoden 2021 puolella
  - Oppijalla on ONR:n tiedon mukaan kotikunta Suomessa
  - Opiskeluoikeus on aktiivinen
  - Opiskeluoikeus/päätason suoritus-kombinaatio kelpaa oppivelvollisuuden suorittamiseen
  - Koski-tietojen mukaan näyttää, että oppijan oppivelvollisuus ei ole päättynyt ennen kuin opiskeluoikeus on alkanut

## 24.9.2021

- International schoolin luokka-asteiden 10-12 suorituksille vaaditaan alkamispäivä, jotta voidaan päätellä, milloin
  toisen asteen opintoja vastaavat opinnot ovat alkaneet.

## 23.9.2021

- Lukion aiemman kuin 2019 opetussuunnitelman käytön esto koskee nyt vain 2005 tai myöhemmin syntyneitä oppijoita

## 22.9.2021

- 8.9.2021 tehtyyn validaation kiristykseen tehty poikkeus
- Lukion aineopiskelijalle sallitaan vanha diaarinumero 1.8.2021 jälkeen, kun lisätiedoissa on ulkomaalainen vaihto-opiskelija

## 14.9.2021

- VST:n terminaalitilat: Muilla kuin vapaatavoitteisella ei ole hyväksytty "Hyväksytysti suoritettu"/"Keskeytynyt". Vapaatavoitteisella ei ole hyväksytty "Valmistunut"/"Katsotaan eronneeksi"

## 8.9.2021

- Lukion aiemman kuin 2019 opetussuunnitelman perusteiden mukaisen opiskeluoikeuden siirto on estetty alkamispäivästä
  1.8.2021 alkaen oppijoilta, jos oppija ei ole käynyt lukiota jo jossain muussa oppilaitoksessa aiemmin. Siirrossa
  kuuluu käyttää lukin 2019 opetussuunnitelman diaarinumeroa ja rakenteita.
- Lukion aiemman kuin 2019 opetussuunitelman perusteiden mukaisen oppimäärän opiskeluikeuteen saa siirtää
  maksuttomuustiedon, jos se on alkanut 1.1.2021 tai myöhemmin. Siirto on estetty vanhemmista opiskeluoikeuksista.

## 1.9.2021

- Vanha opsin mukaisessa Luva-koulutuksessa sallitaan laajuudet vain kursseina. Uuden opsin mukaisessa koulutuksessa vain opintopisteinä.

## 1.5.2021 - 8.9.2021

- Maksuttommuustietojen siirron validaatioita on kehitetty useammassa vaiheessa. Maksuttomuustiedon
  siirto estetään sellaisille oppijoille, joista voi Kosken tietojen perusteella päätellä, että he eivät ole
  uuden oppivelvollisuuslain piirissä. Tämä perustuu oppijan ikään ja siihen, onko oppijalla Koskessa merkintä
  peruskoulusta valmistumisesta siten, että hänen oppivelvollisuutensa on tullut valmiiksi vanhan lain mukaan.

## 19.7.2021

- Estä 'eronnut'/'katsotaan eronneeksi' tilan käyttö, jos suoritus on vahvistettu

## 24.6.2021

- Estetään oppilaitoksen muutos opiskeluoikeudessa, poislukien jotkut poikkeustapaukset.
  - Jos koulutustoimija muuttuu, voi samalla vaihtaa myös aktiivista oppilaitosta
  - Muuto sallitaan myös, jos vanha oppilaitos on passiivinen
  - Muutos sallitaan myös, jos koulutustoimija pysyy samana, vanha oppilaitos on aktiivinen ja
    opiskeluoikeus on joskus ollut osana oppilaitosta, johon opiskeluoikeutta ollaan nyt siirtämässä.

## 22.6.2021

- Vahvistetulla vapaan sivistystyön vapaatavoitteisella koulutuksella tulee olla arvioituja osasuoritukssia

## 16.6.2021

- Perusopetuksessa ja lukion 2015-opetussuunnitelman mukaisessa opetuksessa äidinkielen
  omaisen oppiaineen kieli tulee olla suomi tai ruotsi. Lukion 2019 opetussuunnitelman mukaisissa
  opinnoissa tämä validointi oli jo.

## 19.5.2021

- Nuorten perusopetuksen opiskeluoikeutta ei voida päättää tilaan 'valmistunut', jos opiskeluoikeudelta
  puuttuu vahvistettu päättötodistus

## 18.5.2021

- Lukion valmistavan koulutuksen suorituksella ei voi 1.8.2021 jälkeen olla
  luva2015-opetussuunnitelman mukaisia suorituksia
- Lukioon valmistavassa koulutuksessa 1.8.2021 tai myöhemmin alkaneiden kurssien tulee käyttää vuoden 2021
  opetussuunnitelmaa

## 28.4.2021

- Perusopetuksen laajuus vaaditaan pakollisille oppiaineille 1.8.2020 tai sen jälkeen vahvistetuilta vuosiluokan
  suorituksilta ja päättötodistuksilta. Laajuutta ei kuitenkaan vaadita jos suoritustapana on erityinen tutkinto tai
  kotiopetusjakso on voimassa suorituksen vahvistuspäivänä.
- Yksittäiseltä oppiaineelta ei vaadita laajuutta mikäli sen suoritustapa on erityinen tutkinto.

## 20.4.2021

- Lukion 2019 -opetussuunnitelman mukaisissa opinnoissa äidinkielenomaisen kielen opinnoissa sallitaan vain suomi
  tai ruotsi.

## 13.4.2021

- Lukioon valmistavan koulutuksen suorituksella ei voi olla sekä lukion 2015 että lukion 2019
  opetussuunnitelmien mukaisia osasuorituksia

## 13.4.2021

- 1.8.2020 tai myöhemmin vahvistetuissa perusopetuksen vuosiluokkien tai koko oppimäärän suorituksissa
  on laajuus pakollinen

## 24.3.2021

- Lukion 2019-opetussuunnitelman liikunnan oppiaineessa sallitaan arvosana S riippumatta
  sen moduulien laajuudesta.
- Lukion 2019-opetussuunnitelman muissa oppiaineissa sallitaan arvosana S, jos oppiaineen laajuus on 2 op
  tai vähemmän

## 10.2.2021

- Vahvistetulla kansanopistojen oppivelvollisille suunnatulla vapaan sivistystyön suorituksella tulee olla
  osasuorituksia 53 laajuusyksikön edestä.
- Vahvistetulla kansanopistojen oppivelvollisille suunnatun vapaan sivistystyön suorituksella tulee olla
  osaamiskokonaisuuksia, joiden yhteenlaskettu laajuus on vähintään 4.

## 26.1.2021

- Perusopetuksen kahden eri vuosiluokan samat alkamispäivät estävässä validaatiossa otetaan huomioon oikein myös
  KOSKI-palveluun jo tallennetut suoritukset, joita ei ole mukana tiedonsiirrossa.

## 8.1.2021

- Lops2021 laajuuksien laskenta korjattu: Aiemmin laajuudet asetettiin osana validaatioita ainoastaan oppiaineelle sen
  sisältämien moduulien ja paikallisten oppiaineiden laajuuksien perusteella. Lisäksi oppiaineelle mahdollisesti
  siirrettyä laajuutta ei poistettu, jos oppiaineella ei ollut yhtään osasuorituksia. Nyt laajuudet lasketaan myös
  lukion muissa opinnoissa, ja tyhjennetään ylemmältä tasolta, jos osasuorituksia ei ole.

## 22.12.2020

- Vapaan sivistystyön oppivelvollisuuslinjan opinnoissa täydennetään osaamiskokonaisuuksien ja valinnaisten
  suuntautumisopintojen laajuus automaattisesti opintokokonaisuuksien laajuuksista

## 29.10.2020

- Pre-IB-opinnoissa lukion opetussuunnitelman 2019 mukaan validoidaan samat asiat kuin lukiossa.

## 14.10.2020

- DIA-opiskeluoikeuden tiloilla `lasna` ja `valmistunut` tulee siirtää `opintojenRahoitus`
- Muille DIA-opiskeluoikeuden tiloille ei voi siirtää `opintojenRahoitus`-kenttää

## 6.10.2020

- lops2021: Vieraan kielen moduuleille täytetään tiedonsiirrossa kieli-kenttä oppiaineen tai moduulin
  nimen perusteella silloin kun se on mahdollista. VK-moduuleille on pakollista siirtää kieli-kenttä muissa
  suorituksissa, ja muille kuin vieraan kielen moduuleille sitä ei saa siirtää.
- lops2021: Vieraissa kielissä ei saa käyttää kielivalikomakoodia 97 (Ei suoritusta)
- lops2021: Äidinkielessä ei saa käyttää kielikoodia AIAI: Oman äidinkielen opinnot kuuluu siirtää vieraana
  kielenä eikä äidinkielenä.
- lops2021: Jos vahvistetuissa opinnoissa on suullisen kielitaidon kokeen sisältävän valtakunnallisen moduulin suoritus,
  validoidaan, että päätason suorituksesta löytyy myös suullisen kielitaidon kokeen suoritusmerkintä.
- lops2021: Oman äidinkielen OÄI, RÄI ja SÄI -moduuleita ei voi siirtää: oman äidinkielen opinnot siirretään ainoastaan
  päätason suoritustasolle, ei moduuleina.

## 30.9.2020

- Validoi riittävä opintojaksojen/moduulien laajuus lops2021:ssä. Jotta lukion lops2021-mukaisen oppimäärän suorituksen
  voi merkitä valmiiksi, pitää nuorilla olla vähintään 150 op, joista vähintään 20 op valinnaisia. Aikuisilla pitää olla
  vähintään 88 op.

## 21.9.2020

- Validoi lops2021 oppiaineiden arvosanat. Valtakunnallisten oppiaineiden arvosanojen pitää olla pääsääntöisesti numero.
  Opinto-ohjauksessa ei saa olla numeroarviointia. Liikunnassa H tai S sallitaan numeron sijasta, jos oppiaineen laajuus
  on korkeintaan 2 op. Vieraissa kielissä H tai S sallitaan numeron sijasta, jos oppiaine on valinnainen ja sen laajuus
  on korkeintaan 4 op.
- Validoi lops2021 moduulien ja paikallisten opintojaksojen arvosanat. Arvosanaa O ei sallita missään. Paikallisissa
  opintojaksoissa sallitaan arvoasanat 4-10, S ja H. Valtakunnallisissa moduuleissa sallitaan opinto-ohjauksen moduuleita
  OP1 ja OP2 lukuunottamatta 4-10. Opinto-ohjauksessa taas sallitaan ainoastaan S ja H.

## 20.9.2020

- Validoi lops2021 suoritettu erityisenä tutkintona. Jos oppiaine tai suoritus on suoritettu erityisenä tutkintona,
  ei erillisiä moduulien tai paikallisten opintojaksojen merkintöjä sallita oppiaineisiin.
- Validoi lops2021 temaattisten opintojen osasuoritukset. Niihin voi siirtää vain paikallisia opintojaksoja, ei
  valtakunnallisia moduuleita.
- Validoi lukiodiplomien suoritukset lops2021:ssä. Lukiodiplomeita voi siirtää vain lukiodiplomioppiaineeseen tai
  lukiodiplomia vastaavaan erilliseen oppiaineeseen, jos sellainen on olemassa. Lukiodiplomioppiaineeseen voi siirtää
  vain lukiodiplomeita. Lukiodiplomien laajuus on oltava aina 2 opintopistettä.
- Validoi suorituskielet lops2021:ssä; Oppiaineen, moduulin tai paikallisen opintojakson suorituskieli ei saa olla sama kuin
  ylemmän tason suorituskieli: silloin suorituskieli kuuluu jättää asettamatta.

## 11.9.2020

- Vahvistetulta perusopetuksen vuosiluokan suoritukselta ei vaadita osasuorituksia, jos oppilas on merkitty jäävän luokalle
- Lukion opetussuunnitelman 2019 oppiaineiden oppimäärien suorituksen sisältävän opiskeluoikeuden voi merkitä
  valmistuneeksi, jos ja vain jos suoritus sisältää vähintään yhden arvioidun oppiaineen.

## 30.7.2020

- Perusopetukseen valmistavissa suorituksissa ei edellytetä koulutusmoduulissa laajuutta, jos suoritustapa on 'erityinentutkinto'

## 22.5.2020

- Kaikkien aikajaksojen alkupäivämäärän pitää olla ennen tai sama kuin loppupäivämäärä

## 28.4.2020

- Jos suorituksen tyyppi on "ammatillinen perustutkinto" jonka suoritustapa ops tai reformi, rakenteessa tulee olla "tutkinnonosanryhmä" määriteltynä (Tor-982).
- Ammatillisen tutkinnon yhteisiin osa-alueisiin (koodit '101053', '101054', '101055', '101056', '400012', '400013', '400014') liittyviä validaatioita Tor-982
  - Arvioidun tutkinnon osan laajuus tulee olla sama kuin sen alle siirrettyjen osa-alueiden yhteislaajuus
  - Arvioidun tutkinnon osan alta tulee aina löytyä myös osa-alueita
  - Valmiiksi merkityn ammatillisen perustutkinnon, jonka suoritustapa on reformi, yhteisten tutkinnon osien yhteislaajuus on oltava 35 tai enemmän.
  - Valmiiksi merkityssä ammatillisessa perustutkinnossa ei voi esiintyä saman yhteisen tutkinnon osan koodi kuin kerran
  - Jos suoritustapa on reformi, ei sallita tutkinnon osia '101053', '101054', '101055', '101056' - jos suoritustapa ops ei sallita tutkinnon osia '400012', '400013', '400014' .

## 16.4.2020

- Päätason suorituksen alkamispäivä ei voi olla aiempi kuin sen sisältävän opiskeluoikeuden alkamispäivä

## 18.3.2020

- Ammatillisen osaamisen hankkimistapa ilman lisätietoja ei voi olla "oppisopimus" (Tor-868)

## 17.2.2020

- Nuorten perusopetuksen valinnaiseen valtakunnalliseen aineeseen sallitaan vain arvosanat S ja O, jos laajuus on alle kaksi vuosiviikkotuntia

## 3.2.2020

- Muun ammatillisen koulutuksen osasuorituksen arvioinnille uusi arvosanakoodisto, "arviointiasteikkomuuammatillinenkoulutus"

- Nuorten perusopetuksen oppimäärän paikallisille oppiaineille sallitaan aina arvosana S
- Nuorten perusopetuksen oppimäärän paikallisille oppiaineille sallitaan arvosana O jos oppiaineen laajuus on alle kaksi vuosiviikkotuntia

- Ammatillisen opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut** tai **loma**
- IB-tutkinnon opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna** tai **valmistunut**
- Lukion opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut**
- Lukioon valmistavan koulutuksen opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut**
- International school opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut**
- Aikusten perusopetuksen opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut**

## 30.1.2020

- Estetään käytöstä poistettujen lukiokoulutuksen erityisen koulutustehtävän koodiarvojen tallennus
  - Käytöstä poistetut koodiarvot: ib, kielijakansainvalisyys, matematiikka-luonnontiede-ymparisto-tekniikka, steiner, taide, urheilu, muu

## 28.1.2020

- Osittainen ammattitutkinto voidaan merkitä valmiiksi silloinkin kun suorituksissa on pelkkiä yleisiä aineita.

## 15.1.2020

- Ammatillisissa ops-muotoisissa tutkinnoissa voidaan antaa painotettu keskiarvosana.

## 14.1.2020

- Kun esiopetuksen opiskeluoikeuden järjrestämismuoto syötetään
  - pitää käyttäjän olla luotu organisaatioon joka on tyyppiä KOULUTUSTOIMIJA ja VARHAISKASVATUKSEN_JARJESTAJA
  - pitää päätason suorituksen tyypin olla päiväkodin esiopetus (001102)
- Jos käyttäjä on luotu kahteen tai useampaan organisaatioon jotka ovat tyyppiä KOULUTUSTOIMIJA ja VARHAISKASVATUKSEN_JARJESTAJA pitää opiskeluoikeuden koulutustoimija syöttää

## 13.11.2019

- Nuorten perusopetuksen oppimäärän valinnaisille kieli oppiaineille sallitaan arvosana S jos laajuus on 2 vuosiviikkotuntia tai yli

## 14.10.2019

- Nuorten perusopetuksen oppimäärän oppiaineelle Opinto-ohjaus (OP) sallitaan aina arvosana O

## 9.10.2019

- Nuorten perusopetuksen oppimäärän oppiaineelle Opinto-ohjaus (OP) sallitaan aina arvosana S
- validointi "Nuorten perusopetuksen oppimäärän oppiaineiden arvioinnit saavat olla 'S' ja 'O' vain valinnaisilla oppiaineilla joiden laajus on alle kaksi tai oppiaineilla jotka suoritetaan yksilöllistettynä" suoritetaan vain jos oppimäärä on vahvistettu

## 7.10.2019

- Näyttötutkintoon valmistavan suorituksen voi merkitä valmiiksi vaikka sillä ei ole osasuorituksia tai opiskeluoikeus ei ole linkitetty

## 3.10.2019

- Sallitaan lukion oppimäärän alla vain yksi matematiikan oppiainesuoritus

## 2.10.2019

- Nuorten perusopetuksen oppimäärän oppiaineiden arvioinnit saavat olla 'S' ja 'O' vain valinnaisilla oppiaineilla joiden laajus on alle kaksi tai oppiaineilla jotka suoritetaan yksilöllistettynä

## 24.9.2019

- Ammatillinen opiskeluoikeus, jonka suorituksella ei ole osasuorituksia, voidaan merkitä valmiiksi jos opiskeluoikeus on ostettu ja valmistunut ennen vuotta 2019

## 3.9.2019

- Oppiaineen `NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa` suorituksella tulee olla laajuus

## 2.9.2019

- `PerusopetukseenValmistavaOpetuksen` diaarinumeron validointi lisätty
- Diaarinumeron validoinnit:
  - `AikuistenPerusopetuksenAlkuvaihe`
  - `PerusopetuksenLisäopetus`
  - `LukioonValmistavaKoulutus`
  - `ValmaKoulutus`
  - `TelmaKoulutus`
  - `PerusopetukseenValmistavaOpetus`
- Perusopetuksen valmistavassa opetuksessa luokka-aste vaaditaan jos viimeisin arviointi on muuta kuin 0

## 26.8.2019

- Perusopetuksen vahvistetulla suorituksella tulee olla osasuorituksena vähintään yksi oppiaine
- Valmiiksi merkityllä ei-perusopetuksen päätason suorituksella tulee olla osasuorituksia tai opiskeluoikeuden pitää olla linkitetty

## 27.6.2019

- Suoritukselle tulee olla merkitty alkamispäivä
- Perusopetuksen vuosiluokan suorituksella tulee olla alkamispäivä

## 25.6.2019

- Kahdella saman IB-oppiaineen suorituksella ei molemmilla voi olla numeerista arviointia

## 20.6.2019

- Sisältävän opiskeluoikeuden tulee löytyä `oid`-arvolla

## 14.6.2019

- Valmiiksi merkityllä suorituksella ei voi olla keskeneräisiä osasuorituksia
- Sallitaan keskeneräinen yhteinen tutkinnon osa valmiissa osatutkintotavoitteisessa ammatillisen koulutuksen suorituksessa

## 31.5.2019

- Sallitaan tilan asettaminen valmiiksi mikäli opiskelijalla on yksikin suoritettu aineopinto
- Opiskeluoikeutta aikuistenperusopetus ei voi merkitä valmiiksi jos siltä puuttuu suoritus `aikuistenperusopetuksenoppimaara` tai `perusopetuksenoppiaineenoppimaara`

## 29.5.2019

- Sallitaan korkeakouluopinnon lisääminen ilman arviointia

## 28.5.2019

- Aikuisten perusopetuksen opiskeluoikeuden voi asettaa `valmistunut`-tilaan vaikka alkuvaiheen suorituksella ei ole vahvistusta

## 17.5.2019

- Näyttötutkintoon valmistavan tutkinnon diaarinumeron validointi lisätty

## 29.3.2019

- `Ei tiedossa`-oppiainetta (koulutusmoduulin tunnisteen koodiarvo = `XX`) ei voi merkitä valmiiksi

## 26.3.2019

- LukionOppiaineenOppimääränSuoritus: `Ei tiedossa`-oppiainetta (koulutusmoduulin tunnisteen koodiarvo = `XX`) ei voi merkitä valmiiksi

## 7.3.2019

- Opiskeluoikeuden päättymispäiväksi katsotaan myös päättävän jakson päättymispäivä

## 7.2.2019

- Esiopetuksen diaarinumeron validointi lisätty

## 15.1.2019

- Opiskeluoikeuden tilojen `valmistunut` sekä `eronnut` jälkeen ei voi esiintyä muita tiloja
- Opiskeluoikeudella voi olla vain yksi opiskeluoikeuden päättävä tila
- Opiskeluoikeusjaksojen on oltava päivämääräjärjestyksessä
- Opiskeluoikeuden tila ei saa muuttua lopullisen tilan jälkeen
- Opiskeluoikeudella voi olla vain yksi opiskeluoikeuden päättävä tila
- Opiskeluoikeusjaksojen on oltava päivämääräjärjestyksessä

## 14.1.2019

- Tutkinnon osalle ei saa merkitä tutkintoa samalla diaarinumerolla kuin tutkinnon suoritukselle
- Jos tutkinnon osa kuuluu päätason suorituksen tutkinnon eri perusteeseen saa siinä välittää tutkintotiedon

## 13.12.2018

- Valmiilla DIA:n päätason suorituksella tulee olla laajuus

## 27.8.2018

- Diaarinumero on pakollinen päätason suorituksilla
- Suorituksella lista hyväksyttyjä perusteiden koulutustyyppejä

## 23.8.2018

- Sallitaan arvioimattomat oppiaineet aikuisten perusopetuksen alkuvaiheessa

## 15.8.2018

- `KorkeakouluopinnotTutkinnonOsan` sekä `JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsan` rakenteet hyväksytään validoimatta

## 1.6.2018

- Sallitaan VALMA-koulutuksen opiskeluoikeuden päättymispäivä suorituksen vahvistuspäivän jälkeen

## 13.3.2018

- `NuortenPerusopetuksenOppiaineen` diaarinumeron validointi lisätty

## 21.2.2018

- Jos tutkintosuoritukselta puuttuu alkamispäivä, päättele siirtymäaika ensimmäisestä läsnä-opiskelujaksosta
- Ammatillinen päätason suoritus voi käyttää vain yhtä numeerista arviointiasteikkoa

## 20.2.2018

- `AikuistenPerusopetuksenOppiaineen` diaarinumeron validointi lisätty

## 12.2.2018

- Kun suoritetaan ammatillista tutkintoa näyttönä, voi tutkinnon vahvistus tulla opiskeluoikeuden päättymisen jälkeen
- Opiskeluoikeuden päättymispäivän ei voi olla ennen suorituksen vahvistuksen päivämäärää (pl. ammatillisen näyttötutkinto)

## 6.2.2018

- Reformin mukaisen suoritustavan siirtymäajan tarkistuksessa käytetään ensimmäistä läsnä-jaksoa tutkintosuorituksen alkamispäivän sijaan

## 18.1.2018

- Suorituksen vahvistuksen päivämäärä ei voi olla aiempi kuin sen arviointipäivä
- Suorituksen vahvistuksen päivämäärä ei voi olla aiempi kuin sen alkamispäivä

## 26.10.2017

- Päätason suoritus ei saa sisältää duplikaatteja osasuorituksia

## 5.10.2017

- Opiskeluoikeuden alkamispäivän tulee olla sama kuin ensimmäisen opiskeluoikeusjakson alkupäivä

## 4.10.2017

- Suorituksen vahvistuksen päivämäärä ei voi olla aiempi kuin suorituksen osan vahvistuksen päivämäärä

## 29.9.2017

- Suorituksella tulee olla vahvistus mikäli se on tilassa `valmistunut`
- Vahvistetulla suorituksella tulee olla arviointi
- Vahvistetulle 9. vuosiluokan suoritukselle ei voi syöttää oppiaineita mikäli oppilas ei ole jäänyt luokalle

## 24.8.2017

- Tutkinnon osan ryhmä on pakollinen ammatillisen perustutkinnon tutkinnon osille (pl. näyttötutkinto)

## 3.7.2017

- Tutkinnon perusteen tulee löytyä diaarinumerolla
- Tutkinnon osan ryhmä voidaan määritellä vain ammatillisen perustutkinnon tutkinnon osille

## 26.6.2017

- Sisältävän opiskeluoikeuden oppilaitoksen `oid`:n tulee olla oikea
- Sisältävän opiskeluoikeuden henkilö-`oid`:n tulee vastata syötettyjä henkilötietoja tai henkilö tulee löytyä annetulla henkilötunnuksella

## 29.5.2017

- Opiskeluoikeuden päättymispäivän tulee olla alkamispäivän jälkeen
- Opiskeluoikeuden arvioidun päättymispäivän tulee olla alkamispäivän jälkeen
- Suorituksen arviointipäivä ei voi olla aiempi kuin sen alkamispäivä

## 31.5.2016

- Osasuorituksilla tulee olla sama laajuusyksillö kuin ylemmän tason suorituksella
- Osasuoritusten laajuuksien summan tulee olla sama kuin suorituksen laajuuden

## 25.5.2016

- Osaamisalan koodiarvon tulee löytyä tutkintorakenteesta annetulle perusteelle

## 12.4.2016

- Tutkinnon osan tulee löytyä tutkintorakenteesta annetulle perusteelle
