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
