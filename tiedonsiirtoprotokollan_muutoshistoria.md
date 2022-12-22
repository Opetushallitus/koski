## xx.xx.xxxx

- Taiteen perusopetuksen opiskeluoikeuteen lisätty kenttä hankintakoulutus.

## 8.2.2023

- Ylioppilastutkinnon opiskeluoikeuteen on lisätty valinnaiset kentät sitä ennakoiden, että YO-tutkinnon opiskeluoikeudet
tallennetaan tulevaisuudessa myös Koskeen. Datan sisällössä ainut muutos on, että YO-tutkinnon päätason suorituksen
toimipiste-kentässä palautetaan aina jatkossa Ylioppilastutkintolautakunnan organisaation tiedot, jos muuta dataa ei ole
saatavilla.

## 26.1.2023

- Taiteen perusopetuksen osasuorituksille lisätty valinnainen tunnustettu-rakenne.

## 19.1.2023

- Jatkuvaan oppimiseen suunnatun vapaan sivistystyön koulutuksen osasuoritusten ja aliosasuoritusten laajuus on muutettu valinnaiseksi kentäksi.

## 2.1.2023

- IB-opintoihin lisätty oppiaineet koodeilla CLA, LIT, INF, DES, SPO, MATAA ja MATAI

## 30.12.2022

- Virta-datasta luetaan opiskeluoikeuden sekä suorituksen luokittelutieto.

## 12.12.2022

- Tietomalliin lisätty taiteen perusopetuksen opiskeluoikeus.

## 30.11.2022

- Lisätty uusi muun kuin säännellyn koulutuksen tietomalli.

## 15.11.2022

- Ammatillisen perustutkinnon osatutkinnosta valmistuneilta vaaditaan keskiarvo, kun valmistumispäivä on 1.1.2022 tai sen jälkeen.

## 9.11.2022

- TUVA-koulutuksen valinnaisten opintojen kokonaislaajuus voidaan syöttää ilman osasuorituksia
- Jatkuvaan oppimiseen suunnatun vapaan sivistystyön koulutuksen opiskeluoikeuden koulutusmoduulista poistettu `kuvaus`-kenttä.
- Opiskeluoikeuden siirto lähdejärjestelmän id:llä päivittää samalla id:llä ja oppilaitos-oid:lla löytyvää opiskeluoikeutta.
  - Jos siirrossa on mukana myös opiskeluoikeuden oid, päivitetään kyseisellä oid:lla löytyvää opiskeluoikeutta.
  - Jatkossa siirrot lähdejärjestelmän id:n perusteella eivät epäonnistu, vaikka oppijalla olisi sama lähdejärjestelmän id useammalla kuin yhdellä opiskeluoikeudella, jos opiskeluoikeudet ovat eri oppilaitoksissa.
  - Jos samalla lähdejärjestelmän id:n ja oppilaitos-oid:n yhdistelmällä ei löydy opiskeluoikeutta, luodaan uusi opiskeluoikeus.

## 4.11.2022

- Suorituksen tilan deprekointi dokumentoitu selkeämmin.

## 26.10.2022

- Tietomalliin lisätty katselmoitava toteutus jatkuvaan oppimiseen suunnatulle vapaan sivistyön koulutuksen opiskeluoikeudelle. Tämän tietomallin mukaisia opiskeluoikeuksia
  ei pysty vielä tallentamaan.

## 17.10.2022

- Tietomalliin lisätty osittainen toteutus Helsingin eurooppalaista koulua varten. Tämän tietomallin mukaisia opiskeluoikeuksia
  ei pysty vielä tallentamaan.

## 4.10.2022

- Lisätty VST-KOTO-koulutuksen kieli- ja viestintäopintojen osasuorituksen alaosasuorituksen hyväksyttyihin arvosanoihin `alle_A1.1` ja `yli_C1.1`.

## 14.9.2022

- Poistettu tutkintokoulutukseen valmentavan koulutuksen perusopetuksen järjestämisluvan lisätiedoista kenttä `pidennettyOppivelvollisuus`

## 22.8.2022

- Korjattu bugi: tutkintokoulutukseen valmentavan koulutuksen tietomallissa opiskeluoikeuden tyyppi-kenttää ei ollut pakotettu arvoon "tuva"

## 17.8.2022

- Lisätty International school -opiskeluoikeuden MYP-suorituksen arviointiasteikkoon `Pass` ja `Fail` koodistosta `arviointiasteikkointernationalschool`

## 12.7.2022

- Lisätty ammatillisen koulutuksen järjestämisluvan piirissä oleville TUVA-opiskeluoikeuksille sallituksi tilaksi 'loma'

## 8.7.2022

- Lisätty TUVA-koulutuksen perusopetuksen lisätietoihin uusi kenttä `erityisenTuenPäätökset`

## 5.7.2022

- Sallitaan ammatillisen yhteisen tutkinnon osan osa-alueen uusi koodi VVAI22 (Viestintä- ja vuorovaikutus äidinkielellä)

## 4.7.2022

- Sallitaan muuttuneet ammatillisen koulutuksen yhteisten tutkinnon osien koodit:
  - 106727: Viestintä- ja vuorovaikutusosaaminen
  - 106728: Matemaattis-luonnontieteellinen osaaminen
  - 106729: Yhteiskunta- ja työelämäosaaminen

## 30.6.2022

- Vapaan sivistystyön kotoutumiskoulutuksen opetussuunnitelma 2022:
  - Poistettu kieli- ja viestintäkoulutusten arvioinnin tietomallista arvioitsijat.
  - Poistettu vaatimus arvioinnista ohjausosasuoritukselle.

## 27.6.2022

- Lisätty validaatio, että lukion 2015 oppimäärän suoritusta ei voi käyttää lukion 2019 diaarinumerojen kanssa.

## 6.6.2022

- Lisätty vapaan sivistystyön kotoutumiskoulutuksen opetussuunnitelman 2022 mukainen tietomalli

## 23.5.2022

- VST-opiskeluoikeuksiin on lisätty linkitys paikallisiin opetussuunnitelmiin ePerusteissa

## 19.5.2022

- Korjattu sallitut koodiarvot international school opiskeluoikeudelle koodistosta `arviointiasteikkoib`.
  - International school IB oppiaineen arviointi ja numeerinen international school oppiaineen arviointi.
  - Lisätty json-skeemaan hyväksytyt arvosanan koodiarvot `S` ja `F`. Poistettu json-skeemasta epävalidit koodiarvot `pass`/`fail` kyseisiltä arvioinneilta.
- TUVA-opiskeluoikeudelle lisätty uusi sallittu rahoitusmuoto työvoimakoulutus.
- TUVA-opiskeluoikeuden osasuorituksista lasketaan päätason suorituksen laajuus.
  - Osasuoritusten osasuorituksien laajuudet lasketaan vastaavasti osasuorituksen laajuudeksi.
  - Koski voi ylikirjoittaa tiedonsiirrossa annetut laajuudet.

## 22.4.2022

- TUVA-tietomallin perusopetuksen järjestämisluvan lisätiedoista poistettu tarpeettomana kenttä erityisen tuen päätöksistä.

## 5.4.2022

- Nuorten perusopetuksen oppiaineen oppimäärän suoritukseen on lisätty valinnainen kenttä "luokkaAste"

## 10.3.2022

- TUVA-tietomallin opiskeluoikeuden tilan opiskeluoikeusjaksoihin lisätty valinnainen kenttä opintojenRahoitus.
- TUVA-tietomallin osasuoritusten arviointi-kentät muutettu käyttämään uutta koodistoa arviointiasteikkotuva.
- TUVA-tietomallin perusopetuksen lisätiedoista korvattu vanhentunut ulkomailla-kenttä kentällä ulkomaanjaksot.

## 8.3.2022

- Korvattu koodiston _suorituksentyyppituva_ käyttö TUVA tietomallin osasuorituksen tyypissä koodistolla _suorituksentyyppi_.
  - Koodistokoodit siirretty uuteen koodistoon ja vanhat koodiarvot osin päivitetty.
- Poistettu nimi-kenttä TUVA-osasuoritusten koulutusmoduuleista tarpeettomana.

## 7.2.2022

- Lisätty tietomalli Tutkintokoulutukseen valmentava koulutus (TUVA)

## 13.1.2022

- Seuraavat kentät on merkätty redundanteiksi. Nämä kentät tiputetaan hiljaisesti pois siirroissa, eikä niitä sisällytetä Koskesta saataviin tietoihin:
  - Kaikkien opiskeluoikeuksien lisätiedoissa:
    - oikeusMaksuttomaanAsuntolapaikkaan
  - Aikuisten perusopetuksen opiskeluoikeuden lisätiedot:
    - tukimuodot
    - tehostetunTuenPäätös
    - tehostetunTuenPäätökset
    - vuosiluokkiinSitoutumatonOpetus
    - vammainen
    - vaikeastiVammainen
  - Esiopetuksen opiskeluoikeuden lisätiedot:
    - tukimuodot
    - Erityisen tuen päätösten alla:
      - toteutuspaikka
      - tukimuodot
  - IB-tutkinnon opiskeluoikeuden lisätiedot:
    - alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy
    - yksityisopiskelija
  - Lukion opiskeluoikeuden lisätiedot
    - alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy
    - yksityisopiskelija
  - Perusopetuksen lisäopetuksen opiskeluoikeuden lisätiedot
    - aloittanutEnnenOppivelvollisuutta
    - tukimuodot
    - tehostetunTuenPäätös
    - vuosiluokkiinSitoutumatonOpetus
    - joustavaPerusopetus
    - Erityisen tuen päätösten alla:
      - toteutuspaikka
      - tukimuodot
  - Perusopetuksen opiskeluoikeuden lisätiedot
    - tukimuodot
    - tehostetunTuenPäätös
    - Erityisen tuen päätösten alla:
      - toteutuspaikka
      - tukimuodot

## x.12.2021

- Sensitiiviseksi merkitty seuraavat:
  "alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy"
  "yksilöllistettyOppimäärä"
  Ammatillisen osasuorituksen lisätieto kun koodiarvona "mukautettu"
  "keskiarvoSisältääMukautettujaArvosanoja"
  "PerusopetuksenToiminta_AlueenSuoritus"

## 30.11.2021

- Lukion opiskeluoikeuteen lisätty kenttä oppimääräSuoritettu. Se täytetään automaattisesti, jos opiskeluikeudessa on
  oppimäärän vahvistettu suoritus.

## 8.9.2021

- Lukion aiemman kuin 2019 opetussuunnitelman perusteiden mukaisiin opintoihin saa siirtää maksuttomuustiedon, jos
  opiskeluoikeus on alkanut 1.1.2021 tai myöhemmin.

## 1.5.2021 - 1.8.2021

- Uuden oppivelvollisuuslain mukaisiin peruskoulun jälkeisten opintojen opiskeluoikeuksiin on lisätty mahdollisuus siirtää
  tieto koulutuksen maksuttomuudesta opiskeluoikeuden lisätiedoissa.

## 22.12.2020

- Lisätty tietomalliluonnos Vapaan sivistystyön oppivelvollisuuslinjoille. Tämä on vielä luonnos kommentointia varten ja
  tunnettuja selvityksessä olevista avoimista kysymyksistä johtuvia puutteita on vielä useita.

## 29.10.2020

- Lisätty tietomalli Pre-IB-opinnoille lukion opetussuunnitelman 2019 mukaan
- Poistettu Lukion opetussuunnitelman 2019 suorituksentyyppikoodistosta 2019 jälkiliite. Eri vuosien opetussuunnitelmat
  erotellaan vain koulutusmoduulin sisällöllä.

## 6.10.2020

- Lukion 2019 opetussuunnitelmassa suullisen kielitaidon kokeen koodisto vaihdettu kieli-koodistosta
  kielivalikoima-koodistoon
- Lukion 2019 opetussuunnitelmassa valtakunnallisia moduuleita ei voi enää lisätä temaattisiin opintoihin
- Lukion 2019 opetussuunnitelmassa valtakunnallisia moduuleita ei voi enää lisätä paikallisiin oppiaineisiin

## 24.8.2020

- poistettu toistaiseksi lukion 2019 opetussuunnitelman integraatio osana Pre IB -opintojen suorituksia.

## 13.8.2020

- Lisätty uusi Kela-rajapinta. Ei ole vielä käytössä tuotantoympäristössä.
- Lisätty ensimmäinen versio lukion 2019 opetussuunnitelman mukaisista suorituksista. Uusien suoritustyyppien tallennus
  ei ole vielä mahdollista tuotantoympäristössä.

## 17.6.2020

- Deprekoitu esiopetuksen, perusopetuksen ja perusopetuksen lisäopetuksen opiskeluoikeuden lisätiedoista kenttä "tukimuodot"
- Lisätty perusopetuksen ja perusopetuksen lisäopetuksen opiskeluoikeuden erityisen tuen ja tehostetun tuen päätöksiin (toistaiseksi deprekoinnilla piilotettu) kenttä tukimuodoista
- Lisätty esiopetuksen suoritustietoihin (toistaiseksi deprekoinnilla piilotettu) osaAikainenErityisopetus, joka käyttää koodistoa osaaikainenerityisopetuslukuvuodenaikana
- Lisätty perusopetuksen vuosiluokan suoritukseen (toistaiseksi deprekoinnilla piilotettu) kenttä osaAikainenErityisopetus
- Lisätty perusopetuksen lisäopetuksen suoritukseen (toistaiseksi deprekoinnilla piilotettu) kenttä osaAikainenErityisopetus

## 15.5.2020

- Lisätty perusopetukseen valmistavan opiskeluoikeuden tila "Loma"

## 7.4.2020

- Lisätty koulusivistyskieli-kenttä ylioppilastutkinnon, nuorten perusopetuksen oppimäärän ja lukion oppimäärän suoritukseen

## 27.3.2020

- Lisätty korkeakoulun opiskeluoikeuden lisätietoihin optionaalinen kenttä järjestäväOrganisaatio

## 26.3.2020

- Lisätty suoritustapa-rakenne nuorten perusopetuksen vuosiluokansuoritukseen

## 5.3.2020

- Lisätty kenttä oman äidinkielen opinnot suorituksiin
  - Aikuisten perusopetuksen oppimäärän suoritus
  - Aikuisten perusopetuksen alkuvaiheen suoritus
  - Lukion oppimäärän suoritus
  - Nuorten perusopetuksen vuosiluokan suoritus
  - Nuorten perusopetuksen oppimäärän suoritus
  - Perusopetukseen valmistavan opetuksen suoritus

## 14.1.2020

- Lisätty esiopetuksen opiskeluoikeuteen kenttä järjestämismuoto

## 4.12.2019

- Lisätty esiopetuksen opiskeluoikeuden lisätietoihin kentät:
  - tukimuodot
  - majoitusetu
  - kuljetusetu
  - koulukoti

## 6.11.2019

- Lisätty mahdollisuus siirtää kokonaisuuksia "Korkeakouluopintoja" ja "Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja" osasuorituksina päätason suoritukselle Ammatillisen tutkinnon osa/osia.

## 19.9.2019

- Lisätty suoritustapa-kenttä nuorten perusopetuksen oppiaineen suoritukseen valmistavassa opetuksessa
- Yhteisen tutkinnon osan osa-alueita voi lisätä osasuorituksiksi suorituksiin:
  - Muun ammatillisen koulutuksen suoritus
  - Tutkinnon osaa pienemmistä kokonaisuuksista koostuva suoritus

## 4.9.2019

- Perusopetukseen valmistavan koulutuksen suoritukseen lisätty kokonaislaajuus
- Perusopetukseen valmistavan koulutuksen suorituksesta poistettu Nuorten perusopetuksen oppiaineen suoritus
- Perusopetukseen valmistavan koulutuksen suoritukseen lisätty Nuorten perusopetuksen oppiaineen suoritus valmistavassa opetuksessa

## 9.8.2019

- Lisätty kentät vammainen, vaikeastiVammainen ja sisäoppilaitosmainenMajoitus esiopetuksen lisätietoihin

## 6.6.2019

- Lisätty kenttä työssäoppimispaikanYTunnus ammatillisen koulutuksen koulutusopimukseen

## 31.5.2019

- Aikuisten perusopetuksen opiskeluoikeuden voi asettaa valmistunut tilaan, vaikka oppimäärän alkuvaiheella ei olisi vahvistusta.

## 28.4.2019

- Lisätty oppisopimuksenPurkaminen-kenttä ammatillisen oppisopimus-tietoon

## 5.4.2019

- Lisätty uskonnonOppimäärä nuorten ja aikuisten perusopetuksen sekä lukion uskonnon oppiaineeseen

## 29.3.2019

- Lisätty ulkomaanjaksot ja kotiopetusjaksot-kentät nuorten perusopetuksen lisätietoihin
- Lisätty ulkomaanjaksot-kenttä aikuiten perusopetuksen lisätietoihin

## 25.3.2019

- Lisätty ostettu-kenttä ammatillisen opiskeluoikeuteen

## 11.3.2019

- Opiskeluoikeuden päättymispäivä päätellään opiskeluoikeuden tilan opiskeluoikeusjaksoista

## 10.1.2019

- Lisätty kenttä keskiarvo ammatilliseen perustutkintoon

## 10.9.2018

- Lisätty kentät erityisenTuenPäätökset ja tehostetunTuenPäätökset nuorten perusopetuksen ja perusopetuksen lisäopetuksen lisätietoihin
- Lisätty kenttä erityisenTuenPäätökset esiopetuksen lisäopetuksen lisätietoihin
- Lisätty kenttä tehostetunTuenPäätökset aikuisten perusopetuksen lisätietoihin

## 29.8.2018

- Lisätty ammatilliseen reformin mukaiseen perustutkintoon valinnan mahdollisuus, eli kaksi uutta tutkinnon osaa:
  - Yhteisten tutkinnon osien osa-alueet, lukio-opinnot tai muita jatko-opintovalmiuksia tukevat opinnot sekä
  - Korkeakouluopinnot

## 13.8.2018

- Sallitaan perusopetuksen valmistavan opetuksen osasuorituksena nuorten perusopetuksen oppiaineen suoritus

## 12.6.2018

- Lisätty mahdollisuus suorittaa muita lukion opintoja (Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot) Pre IB -suorituksen osasuorituksina

## 18.5.2018

- Lisätty tunnustettu-rakenne aikuisten perusopetuksen kursseihin

## 11.4.2018

- Muutettu Valma-koulutusta niin, että yhteisen tutkinnon osan osa-alueen suoritukset voi/pitää siirtää suoraan päätason suorituksen alle

## 19.3.2018

- Sallitaan osa-aikaisuusjakson osa-aikaisuuden arvoksi 0%

## 6.3.2018

- Sallitaan saman oppiaineen suoritus useampaan kertaan perusopetuksen alkuvaiheessa

## 2.3.2018

- Lisätty validointi: ammatillisen päätason suorituksen alla ei voi käyttää useampaa kuin yhtä numeerista arviointiasteikkoa

## 1.3.2018

- Lisätty mahdollisuus syöttää yhteisen tutkinnon osan osa-alueen suorituksia Valma-kouluksen osan suoritukseen

## 21.2.2018

- Aikuisten perusopetuksen oppiaineiden ja kurssien laajuudet voi antaa kursseina

## 12.2.2018

- Näytön arvioinnin kentät arvioinnistaPäättäneet ja arviointikeskusteluunOsallistuneet muutettu valinnaisiksi

## 25.1.2018

- Lisätty aikuisten perusopetuksen oppimäärän ja alkuvaiheen suorituksiin valinnainen luokka-kenttä

## 22.1.2018

- Lisätty Perusopetuksen lisäopetuksen suoritukseen valinnainen luokka-kenttä

## 12.1.2018

- Sallitaan saman valinnaisen tutkinnonosan suoritus useampaan kertaan

## 19.12.2017

- Poistettu laajuus ammatillisen päätason suorituksen koulutussopimukset-kentästä

## 18.12.2017

- Myös valtakunnallinen lukion kurssi voi olla soveltava

## 14.12.2017

- Muutettu opiskeluoikeuksien lisätiedoista kenttä `vammainen` boolean-tyyppisestä listaksi alku-loppu päivämääräpareja

## 11.12.2017

- Muutettu ammatillisen opiskeluoikeuden lisätiedon kenttä `vaikeastiVammainen` boolean-tyyppisestä listaksi alku-loppu päivämääräpareja
- Muutettu ammatillisen opiskeluoikeuden lisätiedon kenttä `vammainenJaAvustaja` boolean-tyyppisestä listaksi alku-loppu päivämääräpareja

## 8.12.2017

- Erotettu uskonto ja elämänkatsomustieto omiksi oppiaineikseen perusopetuksessa ja lukiossa

## 4.12.2017

- Lisätty sisäoppilaitosmainen majoitus Luva-koulutuksen lisätietoihin

## 1.12.2017

- Lisätty oppimäärä (aikuisten/nuorten ops) lukioon valmistavan koulutuksen suoritukseen
- Lisätty ammatillisen opiskeluoikeuden lisätietoihin koulutusvienti

## 23.11.2017

- Poistettu rajoitus, jossa koulutussopimukset ja osaamisen hankkimistavat hyväksytään vain, jos kyseessä on reformi-suoritustapa
- Poistettu rajoitus, jossa työssäoppimisjaksot ja järjestämismuodot hyväksytään vain, jos kyseessä on ops tai näyttö-suoritustapa

## 21.11.2017

- Sallitaan saman valinnaisen oppiaineen esiintyminen useampaan kertaan perusopetuksessa

## 14.11.2017

- Ammatillisen reformin mukaiset muutokset
  - Lisätty koulutussopimukset kaikille ammatillisen päätason suorituksille
  - Lisätty osaamisenhankkimistavat ammatillisen tutkinnon-, ammatillisen tutkinnon osittaiselle- ja näyttötutkintoon valmistavan koulutuksen suorituksille
  - Lisätty erityinen tuki ja vaativan erityisen tuen erityinen tehtävä ammatillisen opiskeluoikeuden lisätiedoille
  - Ammatillisen tutkinnon ja osittaisen ammatillisen tutkinnon suorituksilla hyväksytään koulutussopimukset ja osaamisen hankkimistavat vain, jos suoritustapa on `reformi`
  - Ammatillisen tutkinnon ja osittaisen ammatillisen tutkinnon suorituksilla hyväksytään järjestämismuodot ja työssäoppimisjaksot vain, jos suoritustapa on `ops` tai `naytto`
  - Osittaisen ammatillisen tutkinnon suoritukseen on lisätty pakollinen suoritustapa tieto (`ops`, `naytto` tai `reformi`)
  - suoritustapa-koodistoon on lisätty arvo `reformi`
  - Lisätty uusi koodisto `osaamisenhankkimistapa`
  - Lisätty uusi ammatillisen arviointi-asteikon koodisto `arviointiasteikkoammatillinen15`
  - Lisätty koodistoon `ammatillisennaytonarvioinnistapaattaneet` uusi arvo `5` - `muu koulutuksen järjestäjän edustaja`

## 13.11.2017

- Muutettu vankilaopetuksessa kenttä boolean-tyyppisestä listaksi alku-loppu päivämäääräpareja

## 8.11.2017

- Poistettu ammatillisen opiskeluoikeuden lisätietojen poissaolojaksot
- Lisätty ammatillisen opiskeluoikeuden tila "Loma" yli 4 viikkoa kestävälle lomajaksolle

## 30.10.2017

- Lisätty kenttä koulukoti nuorten perusopetuksen opiskeluoikeuden lisätietohin

## 27.10.2017

- Paikallinen lukion kurssi voi olla tyyppiä "pakollinen"

## 26.10.2017

- Sallitaan kieliaineissa duplikaatit jos eri kielivalinta ja lukion matematiikassa jos eri oppimäärä

## 23.10.2017

- Poistettu arviointi näyttötutkintoon valmistavan koulutuksen osan suorituksesta
- Lisätty opiskeluvalmiuksia tukevat opinnot ammatillisen opiskeluoikeuden lisätietohin

## 18.10.2017

- Lisätty PUT/POST-rajapintojen vastausdokumenttiin opiskeluoikeuksien lähdejärjestelmäId-kenttä

## 17.10.2017

- Lisätty rahoituksenPiirissä-kenttä osaamisen tunnustamiseen
- Lisätty opintojen rahoitus myös lukion ja aikuisten perusopetuksen opiskeluoikeuksien opiskeluoikeusjaksoihin
- Muutettu pakolliseksi, seuraavien kenttien alku-tieto:
  - aikuisten perusopetuksen kentät ulkomailla, majoitusetu, oikeusMaksuttomaanAsuntolapaikkaan
  - esiopetuksen pidennettyOppivelvollisuus
  - nuorten perusopetuksen kentät pidennettyOppivelvollisuus, tehostetunTuenPäätös, joustavaPerusopetus, kotiopetus, ulkomailla, majoitusetu, kuljetusetu, oikeusMaksuttomaanAsuntolapaikkaan

## 16.10.2017

- Muutettu ammatillisen opiskeluoikeuden lisätietojen osaAikaisuus-kenttä listaksi osa-aikaisuusjaksoja (osaAikaisuusJaksot)

## 11.10.2017

- Lisätty lukion opiskeluoikeuden lisätietoihin sisäoppilaitosmainenMajoitus
- Perusopetuksen lisäopetuksen toiminta-alueen suorituksen korotus-kenttä valinnaiseksi

## 9.10.2017

Validointimuutoksia:

- Ammatillisen koulutuksen osien laajuuksien summia ei tarkisteta

## 29.9.2017

- Opiskeluoikeuden päättymispäivän sekä suorituksen vahvistuksen ja arvioinnin voi asettaa tulevaisuuteen
- Suorituksen tilaa ei enää käsitellä tietoja vastaanotettaessa, vaan tila lasketaan Koskessa arvioinnin ja vahvistuksen perusteella
- Ammatillisen tutkinnon osittaiseen suoritukseen lisätty vahvistus
- Muiden lukio-opintojen suoritukseen lisätty arviointi
- Näyttötutkintoon valmistavan koulutuksen osan suoritukseen lisätty arviointi

## 25.9.2017

- Lisätty opiskeluoikeuden tila Mitätöity

## 22.9.2017

- Poistettu yksilöllistettyOppimäärä ja painotettuOpetus perusopetuksen oppiaineen oppimäärän suorituksesta

## 20.9.2017

- Eriytetty aikuisten perusopetus omaksi opiskeluoikeustyypikseen

## 5.9.2017

- Ammatillisen tutkinnon suorituksen suoritustapa pakolliseksi

## 1.9.2017

Luetellaan muutoksia, jotka on toteutettu aiemmin, mutta joita ei ole merkitty muutoshistoriaan.

- Lisätty ammatillisen opiskeluoikeuden lisätietoja: vaikeastiVammainen, vammainenJaAvustaja, majoitus,
  sisäoppilaitosmainenMajoitus, vaativanErityisentuenYhteydessäJärjestettäväMajoitus, vankilaopetuksessa, osaAikaisuus,
  poissaolojaksot, henkilöstökoulutus
- Lisätty perusopetuksen opiskeluoikeuden lisätietoja: vaikeastiKehitysvammainen, majoitusetu, kuljetusetu,
  oikeusMaksuttomaanAsuntolapaikkaan
- Lisätty lukiokoulutuksen opiskeluoikeuden lisätietoja: oikeusMaksuttomaanAsuntolapaikkaan

## 28.8.2017

- Lisätty sisältyyOpiskeluoikeuteen-kenttä kaikkiin opiskeluoikeuksiin
- Muutettu näyttötutkintoon valmistavan koulutuksen suorituksen sekä VALMA- ja TELMA-suoritusten vahvistuksen paikkakunta valinnaiseksi

## 23.8.2017

- Poistettu opiskeluoikeuden tila Erotettu

## 10.8.2017

- Opiskeluoikeuden kenttä "id" (kokonaisluku) muutettu merkkijonotyyppiseksi kentäksi "oid"

## 8.8.2017

- Lisätty puuttuvat rakennevalidoinnit (diaarinumero) ammatillisen tutkinnon osittaiselle suoritukselle

## 7.8.2017

- Jos tutkinnon osa kuuluu päätason suorituksen tutkintoon, ei tutkinnon osan suorituksessa saa olla erillistä tutkintotietoa

## 3.8.2017

- Lisätty vammainen-kenttä perusopetuksen opiskeluoikeuden lisätietoihin

## 2.8.2017

- Lisätty perusopetuksen opiskeluoikeuden lisätietoihin kenttä `sisäoppilaitosmainenMajoitus` samoin kuin ammatillisen opiskeluoikeuden lisätiedoissa.

## 1.8.2017

- Käytetään aikuisten perusopetuksen alkuvaiheen koulutusmoduulissa suorituksentyyppi-koodiston koodia, jotta saadaan koulutusmoduulille eri koodi kuin perusopetuksen suorituksessa

## 6.7.2017

- Lisätty aikuisten perusopetuksen alkuvaiheen opinnot

## 5.7.2017

- Lisätty aikuisten perusopetuksen kurssitaso
- Hyväksytään perusopetuksen oppiaineen oppimäärän suorituksessa vain aikuisten perusopetuksen diaarinumerot (ei nuorten)

## 4.7.2017

- Erotettu aikuisten perusopetuksen oppimäärän suoritus omaksi suoritustyypikseen
- Lisätty tutkintonimikkeet ja toinenTutkintonimike-flägi osittaisen ammatillisen tutkinnon suoritukseen

## 30.6.2017

- Tutkinnonosan ryhmätieto pakolliseksi ammatillisissa perustutkinnoissa

## 29.6.2017

- Muutettu järjestämismuoto listaksi järjestämismuotojaksoja
- Muutettu perusteenDiaarinumero ja suorituskieli pakollisiksi päätason suorituksissa

## 28.6.2017

- Poistettu työssäoppimisjaksot tutkinnonosan tasolta

## 14.6.2017

- Perusopetuksen oppimäärän suoritukselta vaaditaan vähintään yksi oppiaine, jos suoritus on tilassa VALMIS
- 9.vuosiluokan suoritukseen ei voi syöttää oppiaineita, kun suoritus on VALMIS, eikä oppilas jää luokalle

## 29.5.2017

- Muutettu opiskeluoikeuden tila Katsotaan eronneeksi -> Erotettu

## 22.5.2017

- Lisätty toinenOsaamisala -flägi osittaisen ammatillisen tutkinnon suoritukseen

## 18.5.2017 (versio 2017.6.2)

- Lisätty työssäoppimisjaksot myös ammatillisiin päätason suorituksiin

## 17.5.2017

- Lisätty osaamisalat ja todistuksellaNäkyvätLisätiedot osittaisen ammatillisen tutkinnon suoritukseen

## 17.5.2017 (versio 2017.5.4)

- Lisätty tutkinnonOsanRyhmä-kenttä ammatillisen tutkinnon osan suoritukseen

## 11.5.2017

- Lisätty valinnainen kenttä työssäoppimispaikan nimi työssäoppimisjaksoihin

## 4.5.2017

- Lisätty valinnaiset alku- ja loppupäivämäärät ammatillisen opiskeluoikeuden lisätietojen hojks-osioon
- Perusopetuksen lisäopetuksessa samat opiskeluoikeuden lisätiedot kuin perusopetuksessa
- Lisätty valinnainen ryhmätieto lukion ja ammatillisen koulutuksen päätason suorituksiin

## 12.4.2017

- Lisätty perusteenDiaarinumero perusopetuksen oppiaineeseen

## 18.4.2017

- Lisätty kenttä muutSuorituskielet esiopetuksen ja perusopetuksen päätason suorituksiin

## 12.4.2017

- Lisätty perusteenDiaarinumero lukion oppiaineeseen

## 4.4.2017

- Lisätty ulkomaanjaksot LUVA-koulutuksen lisätietoihin

## 29.3.2017

- Pieniä muutoksia ja korjauksia LUVA-koulutuksen rakenteeseen

## 24.3.2017

- Lisätty LUVA-koulutukseen oppiainetaso
- Haetaan diaarinumeroita ePerusteiden lisäksi myös koodistosta koskikoulutustendiaarinumerot

## 17.3.2017

- Lisätty lukion paikalliset oppiaineet
- Lisätty kielivalinta ammatillisen tutkinnon osa osa-alueeseen (oppiaineisiin AI, TK1, VK)
- Yhteisille tutkinnon osille alisuorituksiksi osa-alueita, muille tutkinnon osille tutkinnon osaa pienempiä kokonaisuuksia
- Lisätty sanallinen arviointi lukion kursseihin
- Lisätty oman äidinkielen opinnot lukionmuutopinnot-koodistoon

## 16.3.2017

- Lisätty pakollinen-flägi ammatillisen tutkinnon osan osa-alueisiin

## 13.3.2017

- Lisätty mahdollisuus käyttää valtakunnallista koodistoa \"ammatillisenoppiaineet\" yhteisten tutkinnon osien osa-alueissa

## 10.3.2017

- Lisätty lukion "muut opinnot" eli kurssit, jotka eivät liity yksittäiseen oppiaineeseen

## 21.2.2017

- Ammatillisen tutkinnon suorituksen paikkakunta on valinnainen
- Ammatillisen tutkinnon osan suorituksen näytön kuvaus ja paikkakunta ovat valinnaisia

## 10.2.2017

Validointimuutoksia:

- Oppilaitos ei pakollinen, jos se on pääteltävissä päätason suoritusten toimipisteiden perusteella
- Osasuoritusten laajuuksien summa validoidaan pääsuorituksen laajuutta vasten vain, mikäli pääsuoritus on VALMIS
  (vaikuttaa esimerkiksi ammatillisten opintojen tutkinnon osiin ja lukion kursseihin)

## 27.1.2017

Muutoksia ammatillisen näytön tietoihin.

Lisätty:

```
näyttö.arviointi.arvioitsijat._.ntm : Boolean
näyttö.suoritusaika.alku : Date
näyttö.suoritusaika.loppu : Date
näyttö.haluaaTodistuksen : Boolean
```

Muutettu:

```
tutkintotoimikunnanNumero: String (oli Int)
arvioinnistaPäättäneet: List[Koodistokoodiviite] (oli Koodistokoodiviite)
arviointikeskusteluunOsallistuneet: List[Koodistokoodiviite] (oli Koodistokoodiviite)
```

Koodisto `ammatillisennaytonarvioinnistapaattaneet` ja `ammatillisennaytonarviointikeskusteluunosallistuneet` uudistettu.

## 26.1.2017

- Työssäoppimisjakson työtehtävät-kentän pakollisuus poistettu

## 19.1.2017

- Lisätty validaatio: opiskeluoikeuden tilahistoriassa ei saa esiintyä uusia tiloja lopullisen tilan (valmistunut, eronnut, katsotaaneronneeksi) jälkeen

## 10.1.2017

- Validointimuutos: päätason suoritus ei saa olla "kesken" jos opiskeluoikeus on "valmis"

## 21.12.2016

- Validointimuutos: VALMIS-tilassa olevilla suorituksilla oltava arviointi

## 20.12.2016

- Ammatillisten tutkinnon osien vahvistuksessa paikkakunta ei pakollinen

## 28.11.2016

- Opiskeluoikeuden ja suoritusten alkamispäivä saa olla tulevaisuudessa
- Lisätty sanallinen arviointi (kuvaus-kenttä) ammatillisten tutkinnon osien arviointiin
- Lisätty arvosana, päivä ja arvioitsijat näytön arviointiin

## 10.11.2016

- Koski ei enää hyväksy keinotekoisia henkilötunnuksia tietoja syötettäessä (loppuosan alkunumero 9)
- Suoritusten ja opiskeluoikeuksien päivämäärät eivät saa olla tulevaisuudessa (poislukien arvioity päättymispäivä)

## 9.11.2016

- Poistettu ammatillisen erityisopetuksen peruste -kenttä

## 3.11.2016

- Suoritettavien koulutusmoduulien laajuus oltava > 0

## 27.10.2016

- Poistettu opiskeluoikeudesta läsnäolotiedot (riittävä tietotaso saadaan opiskeluoikeuden tilan opiskeluoikeusjaksoista)

## 6.10.2016

- Lisätty todistuksellaNäkyvätLisätiedot Pre-IB-suoritukseen sekä perusopetuksen ja lukion oppiaineen oppimäärän suoritukseen
- Lisätty lukion kurssin suoritukseen suoritettuSuullisenaKielikokeena
- Lisätty suoritustapa, yksilöllistettyOppimäärä ja painotettuOpetus perusopetuksen oppiaineen oppimäärän suoritukseen

## 5.10.2016

- Muutettu lähdejärjestelmänId-kentän dokumentaatio: identifioidaan järjestelmän tyyppi, ei instanssia

## 3.10.2016

- Lisätty todistuksellaNäkyvätLisätiedot ammatillisen, ib-tutkinnon, lukion ja perusopetuksen todistuksia vastaaviin suorituksiin

## 30.9.2016

- Ammatillisen tutkinnon osan suorittaminen uudelleen mallinnettu: poistettu tavoite-kenttä ja lisätty mahdollisuus suorittaa osittainen tutkinto
- Poistettu tavoite-kenttä perusopetuksesta ja lukiokoulutuksesta
- Lisätty mahdollisuus suorittaa Valtakunnallinen ammatillinen tutkinnonosa Valmassa
- Lisätty mahdollisuus suorittaa Valtakunnallinen ammatillinen tutkinnonosa Telmassa
- Lisätty mahdollisuus suorittaa Valtakunnallinen ammatillinen tutkinnonosa näyttötutkintoon valmistavassa koulutuksessa

## 29.9.2016

- Luvaan lisätty suoritettuLukiodiplomina-kenttä
- Lukioon valmistavassa koulutuksessa ei ole mahdollista suorittaa lukion oppiaineen oppimäärä
- Lukioon valmistavan kurssin suorituksessa suoritettuLukiodiplomina-flägi
- Perusopetukseen valmistavaan koulutukseen lisätty perusteenDiaarinumero
- Perusopetuksesta, lisäopetuksesta ja lukiokoulutuksesta poistettu liitetiedot (lukiossa nimellä lisätiedot)
- Lisätty järjestämismuoto näyttötutkintoon valmistavan koulutuksen suoritukseen
- Lisätty alkamispäivä ja työssäoppimisjaksot näyttötutkintoon valmistavan koulutuksen osan suoritukseen
- Lisätty kuvaus näyttötutkintoon valmistavan koulutuksen osaan

## 28.9.2016

- Lisätty näytön tiedot Valman ja Telman osasuorituksiin
- Lisätty mahdollisuus syöttää muita kuin peruskoulun oppiaineen suorituksia lisäopetukseen
- Lisätty mahdollisuus syöttää lisäopetus toiminta-alueittain
- Lisätty yksilöllistettyOppimäärä perusopetuksen lisäopetuksen oppiaineen suoritukseen
- Lisätty kuvaus ulkomaanjaksoon
- Lisätty kuvaus Telma ja Valma koulutuksen osiin
- Lisätty ammatillisten tutkinnon lisätietoihin Muutos arviointiasteikossa
- Lisätty perusteenDiaarinumero Valma- ja Telma-koulutuksiin
- Lisätty työssäOppimisjaksot Valma- ja Telma-koulutuksiin
- Lisätty alkamispäivä Valma- ja Telma-koulutusten osien suorituksiin

## 27.9.2016

- Lisätty perusopetuksen valinnainen paikallinen oppiaine
- Lisätty uusi yleisivistävä arviointikoodi: valinnainen
- Lisätty perusopetuksen vuosiluokan todistuksen sekä päättötodistuksen liitteet
- Lisätty perusopetuksen oppiaineen suoritukseen tieto painotuksesta opetuksessa
- Lisätty todistuksellaNäkyvätLisätiedot perusopetuksen lisäopetukseen
- Lisätty perusopetuksen lisäopetuksen todistuksen liitetiedot
- Lisätty perusteen diaarinumero perusopetuksen lisäopetuksen suoritukseen
- Lisätty perusopetuksen lisäopetuksen opiskeluoikeuden lisätiedot (pidennetty oppivelvollisuus)
- Valmiita päätason suorituksia ei poisteta, vaikka ne puuttuisivat päivitettäessä
- Lisätty valinnainen kielikylpykieli perusopetuksen oppimäärän suoritukseen

## 26.9.2016

- Lisätty IB oppiaineen suorituksen arviointi
- Lisätty IB core aineille oma arviointiasteikko
- Lisätty IB-tutkinnon lisäpisteet

## 23.9.2016

- Lisätty IB kurssin kuvaus
- Lisätty Pre-IB oppiaineen suorituksen arviointi
- Lisätty suoritettuLukiodiplomina kenttä lukion kurssin suoritukseen
- Lisätty Luva koulutuksen suoritukseen lisätiedot ja optionaalinen diaarinumero
- Lisätty Luvan opiskeluoikeuden lisätiedot

## 21.9.2016

- Lisätty kurssinTyyppi kenttä lukion kurssin suoritukseen (pakollinen, syventävä, soveltava)

## 20.9.2016

- Oppiaineiden suorituksissa ei vaadita vahvistusta, vaikka suoritus olisi valmis
- IB-tutkintojen tiedot lisätty (työversio)
- Lukion opiskeluoikeuden lisätiedoista poistettu majoitukseen liittyvät tiedot
- Lisätty arvioituPäättymispäivä lukion ja lukioon valmistavan koulutuksen opiskeluoikeuteen
- Lisätty kuvaus-kenttä lukioon valmistaville kursseille
- Valmiita päätason suorituksia ei poisteta Koskesta, vaikka ne puuttuisivat tiedonsiirrossa opiskeluoikeuden alta
- Lukioon valmistavassa koulutuksessa on mahdollista suorittaa lukion oppiaineen oppimäärä
- Lisätty lukion oppimäärän suoritukseen lisätiedot (suullisen kielitaidon koe, lukiodiplomi, täydentävä opiskelu, muu)

## 4.7.2016

- Valma ja telma siirretty ammatillisen opiskeluoikeuden alle
- Valman ja telman osasuorituksiin voi sisällyttää lisätietoja samalla tavalla, kuin ammatillisen tutkinnon osan suoritukseen
- Valma ja telma lisätty mahdollisiksi opintojen tavoitteiksi ammatilliseen opiskeluoikeuteen

## 30.6.2016

- Erityinen koulutustehtävä valmiiksi
- Hyväksiluku lukion kurssin suoritukseen valmiiksi

## 28.6.2016

- Näyttötutkintoon valmistavassa koulutuksessa käytössä väliaikainen koodi. Tutkinto, johon koulutus tähtää on siirretty omaan tutkinto-kenttäänsä
- Näytön arvioinnissa nimenmuutos arviointiKohteet -> arviointikohteet
- Poistettu paikallinenKoodi ja kuvaus valtakunnallisilta ammatillisilta tutkinnon osilta
- Nimenmuutos ulkomaanjakso -> ulkomaanjaksot
- Perusopetus, lukio ja ammatillinen käyttävät nyt samaa koskiopiskeluoikeudentila-koodistoa opiskeluoikeuksien tiloihin
- Opiskeluoikeusjaksot pakollisia (vähintään 1). Opiskeluoikeus- ja läsnäolojaksoista poistettu redundantti loppupäivämäärä
- Tarkistetaan, että opiskeluoikeuden alkamispäivä täsmää ensimmäisen opiskeluoikeusjakson alkupäivään
- Tarkistetaan, että opiskeluoikeuden päättymispäivä täsmää opiskeluoikeuden lopettavan opiskeluoikeusjakson alkupäivään

## 27.6.2016

- Nimi muutettu ammatilliseen näyttötutkintoon valmistavan koulutuksen suorituksessa: loppumispäivä -> päättymispäivä
- Poistettu turha tunniste-taso ammatillisten tutkintojen suoritustavasta
- Lisätty erityisopetuksen peruste ammatillisen opiskeluoikeuden lisätietojen hojks-osioon
- Muutettu ammatillisen opiskeluoikeuden lisätietojen hojks:n rakennetta: poistettu hojksTehty-flagi

## 23.6.2016

- Lisätty perusopetukseen valmistava opetus
- Poistettu Suoritus.paikallinenId
- Lisätty ammatilliseen peruskoulutukseen valmentava koulutus
- Lisätty työhön ja itsenäiseen elämään valmentava koulutus

## 20.6.2016

- Lisätty lukion oppiaineen oppimäärän suoritus
- Lisätty ulkomainen vaihtoopiskelija lukion opiskeluoikeuden lisätietoihin
- Lisätty alle 18 vuotiaan aikuisten lukiokoulutuksen aloittamisen syy lukion opiskeluoikeuden lisätietoihin
- Lisätty yksityisopiskelija lukion opiskeluoikeuden lisätietoihin
- Lisätty erityinen koulutustehtävä (kesken) lukion opiskeluoikeuden lisätietoihin
- Lisätty oikeus maksuttomaan asuntolapaikkaan lukion opiskeluoikeuden lisätietoihin
- Lisätty sisäoppilaitosmainen majoitus lukion opiskeluoikeuden lisätietoihin
- Lisätty oppimäärä (aikuisten/nuorten ops) lukion oppimäärän suoritukseen
- Lisätty ulkomaanjakso lukion opiskeluoikeuden lisätietoihin
- Lisätty kuvaus paikalliseen lukiokurssiin
- Lisätty hyväksiluku lukion kurssin suoritukseen (kesken)
- Lisätty näyttötutkintoon valmistava koulutus

## 16.6.2016

- Lisätty oikeusMaksuttomaanAsuntolapaikkaan ammatilliseen koulutukseen
- Lisätty työssäoppimistiedot ammatilliseen koulutukseen

## 13.6.2016

- Lisätty mahdollisuun sanalliseen arviointiin perusopetuksessa ja perusopetuksen lisäopetuksessa
- Yhdenmukaisettu arviointi perusopetus/lisäopetus, siirretty lisäopetuksen oppiaineen suorituksen korotus-kenttä pois arvosanasta
- Perusopetuksen vuosiluokkasuoritus täydennetty

## 9.6.2016

- Lisätty "hyväksytty" kenttä kaikkiin arviointeihin. Sisään tulevassa datassa kentän arvoa ei tarvita.
- Poistettu arvioitsijat perusopetuksen ja lukion arvioinneista
- Lisätty vahvistus ja käyttäytymisenArvio perusopetuksen lukuvuosisuoritukseen
- Lisätty yksilöllistettyOppimäärä perusopetuksen oppiaineen suoritukseen
- Lisätty toiminta-alueittainen arviointi perusopetuksen oppimäärän suoritukseen
- Lisätty lisätiedot (tukimuodot, päätökset, aloittamisen lykkäys, pidennetty oppivelvollisuus, joustava perusopetus, vuosiluokkiin situotumaton opetus, ulkomaanjaksot, kotiopetus, majoitus- ja kuljetustiedot, aamu- ja iltapäivätoiminta)
- Käytetään lukion oppimäärälle uutta koulutuskoodia 309902

## 30.5.2016

- Poistettu arviointi-kenttä ammatillisista tutkinnoista
- Tehty arvioinnin päivä-kenttä pakolliseksi ammatillisen tutkinnon osissa ja lukion kursseissa

## Draft 3

- Mukana peruskoulun ja lukion päättötodistustiedot
- Epäyhteensopivuudet:

1. Opiskeluoikeudessa ja suorituksessa tyyppi-koodisto
2. Koulutusmoduulitoteutus-taso poistettu rakenteesta; suorituksen alla suoraan koulutusmoduuli
3. Vahvistuksessa pakollisina tietoina päivä, paikkakunta, myöntäjäOrganisaatio ja myöntäjäHenkilöt (vahvistus itsessään ei ole muulloin kuin VALMIS-tilaisissa suorituksissa)
4. Kaikki vapaatekstikentät, organisaatioden nimet ja koodistokoodien nimet on kielistettyjä
5. Toimipistetieto vaaditaan vain päätason suoritukselta
6. Opiskeluoikeuden suoritus-kenttä korvattu listatyyppisellä suoritukset-kentällä. Ammatillisissa opinnoissa tämä on kuitenkin aina yhden alkion lista.

## Draft 2

- Ammatillinen, tarkennettu/laajennettu
- Pieniä epäyhteensopivuuksia edelliseen

## Draft 1

- Vain ammatillinen
