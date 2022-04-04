# Koskeen tallennettavien tietojen validaatiosäännöt

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

## 22.2.2022
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

    Tässä on tehty monta eri validaatiomuutosta.

    commit 19cfcfa232a2de44a608e1bbdeb23daf8ab7b1ed (from ae9a8ef2dfd4ec568fb253e08b912bffc25011f4)
    Merge: ae9a8ef2d 4a851ea1f
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Tue Jan 18 12:29:34 2022 +0200

        Merge pull request #1684 from Opetushallitus/1622-validaatio-organisaation-vaihto-varhaiskasvatuksen-toimipaikka

        TOR-1622 - validaatio varhaiskasvatuksen toimipisteen vaihdosta

    src/main/resources/reference.conf
    src/main/scala/fi/oph/koski/config/KoskiApplication.scala
    src/main/scala/fi/oph/koski/documentation/AmmatillinenExampleData.scala
    src/main/scala/fi/oph/koski/documentation/ExamplesAmmatillinen.scala
    src/main/scala/fi/oph/koski/eperusteet/RemoteEPerusteetRepository.scala
    src/main/scala/fi/oph/koski/fixture/KoskiSpecificDatabaseFixtureCreator.scala
    src/main/scala/fi/oph/koski/opiskeluoikeus/OpiskeluoikeusChangeValidator.scala
    src/main/scala/fi/oph/koski/opiskeluoikeus/PostgresOpiskeluoikeusRepository.scala
    src/main/scala/fi/oph/koski/opiskeluoikeus/PostgresOpiskeluoikeusRepositoryV2.scala
    src/main/scala/fi/oph/koski/schema/Opiskeluoikeus.scala
    src/main/scala/fi/oph/koski/tiedonsiirto/ExamplesTiedonsiirto.scala
    src/main/scala/fi/oph/koski/validation/AmmatillinenValidation.scala
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    src/test/resources/backwardcompatibility/ammatillinen-full_2022-01-17.json
    src/test/resources/backwardcompatibility/ammatillinen-oppisopimus_2022-01-17.json
    src/test/resources/backwardcompatibility/ammatillinen-osatoisestatutkinnosta_2022-01-17.json
    src/test/resources/backwardcompatibility/ammatillinen-paikallinen_2022-01-17.json
    src/test/resources/backwardcompatibility/ammatillinen-reforminmukainenperustutkinto_2022-01-17.json
    src/test/resources/backwardcompatibility/ammatillinen-tunnustettu_2022-01-17.json
    src/test/resources/backwardcompatibility/tiedonsiirto-epaonnistunut2_2022-01-17.json
    src/test/resources/backwardcompatibility/tiedonsiirto-epaonnistunut3_2020-02-03.json
    src/test/resources/backwardcompatibility/tiedonsiirto-epaonnistunut3_2020-03-17.json
    src/test/resources/backwardcompatibility/tiedonsiirto-epaonnistunut3_2020-07-06.json
    src/test/resources/backwardcompatibility/tiedonsiirto-epaonnistunut3_2020-07-13.json
    src/test/resources/backwardcompatibility/tiedonsiirto-epaonnistunut3_2022-01-17.json
    src/test/resources/backwardcompatibility/tiedonsiirto-epaonnistunut_2022-01-17.json
    src/test/resources/backwardcompatibility/tiedonsiirto-onnistunut_2022-01-17.json
    src/test/resources/backwardcompatibility/tiedonsiirto-vainsyntymaaika_2022-01-17.json
    "src/test/scala/fi/oph/koski/api/K\303\244ytt\303\266oikeusryhm\303\244tSpec.scala"
    src/test/scala/fi/oph/koski/api/OppijaUpdateSpec.scala
    src/test/scala/fi/oph/koski/api/OppijaValidationAmmatillinenSpec.scala
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266VapaatavoitteinenSpec.scala"
    src/test/scala/fi/oph/koski/api/SuoritusjakoSpec.scala
    src/test/scala/fi/oph/koski/api/SuostumuksenPeruutusSpec.scala
    src/test/scala/fi/oph/koski/api/TiedonsiirtoSpec.scala
    src/test/scala/fi/oph/koski/perftest/TiedonsiirtoFixtureDataInserter.scala
    web/test/spec/ammatillinenArviointiasteikkoSpec.js
    web/test/spec/ammatillinenSpec.js

## 18.1.2022
- Ammatillisen opiskeluoikeuden tutkintokoodin ja suoritustavan muuttamiseen liittyvä, 13.12.2021 luotu validaatio ollut virheellinen; Validaatio korjattu. Ammatillisen opiskeluoikeuden tutkintokoodia (suorituksen koulutusmoduulin koodiarvo) ja suoritustapaa ei voi vaihtaa ammatillisen opiskeluoikeuden luonnin jälkeen.
- Validaatio liittyen oppilaitoksen vaihtoon ollut virheellinen. Oppilaitosta, joka on aktiivinen, ei voi vaihtaa jos koulutustoimija pysyy samana.

## 20.12.2021
- Perusopetuksen (aikuiset, nuoret ja lukio) ja perusopetuksen lisäopetuksen kenttää "tehostetunTuenPäätökset" ei enää oteta siirroissa vastaan; tieto tiputetaan pois ilman virhettä/varoitusta
- Perusopetuksen (nuoret) ja perusopetuksen lisäopetuksen kenttää "perusopetuksenAloittamistaLykätty"  ei enää oteta siirroissa vastaan; tieto tiputetaan pois ilman virhettä/varoitusta

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

## 16.11.2021

    Onko tässä joku oikea validaatiomuutos?

    commit a4057c2d3d9dd20714a6823aabddd55397b8309e (from 422448cbba8875f62919411eb513ec083dac97dd)
    Merge: 422448cbb dcc45d096
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Tue Nov 16 14:21:39 2021 +0200

        Merge pull request #1621 from Opetushallitus/tor-1370-vst-suostumuksen-käsittely

        tor-1370 VST suostumuksen käsittely

    sandbox/read_only_koski.sql
    src/main/resources/db/migration/V74__create_poistettu_opiskeluoikeus.sql
    src/main/resources/db/migration/V75__add_suoritusjako_tehty_to_opiskeluoikeus.sql
    src/main/resources/localization/koski-default-texts.json
    src/main/scala/ScalatraBootstrap.scala
    src/main/scala/fi/oph/koski/config/KoskiApplication.scala
    src/main/scala/fi/oph/koski/db/KoskiTables.scala
    src/main/scala/fi/oph/koski/fixture/DatabaseFixtureCreator.scala
    src/main/scala/fi/oph/koski/fixture/Fixtures.scala
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    src/main/scala/fi/oph/koski/kela/KelaOppijaConverter.scala
    src/main/scala/fi/oph/koski/log/AuditLog.scala
    src/main/scala/fi/oph/koski/opiskeluoikeus/CompositeOpiskeluoikeusRepository.scala
    src/main/scala/fi/oph/koski/opiskeluoikeus/KoskiOpiskeluoikeusRepository.scala
    src/main/scala/fi/oph/koski/opiskeluoikeus/PostgresOpiskeluoikeusRepository.scala
    src/main/scala/fi/oph/koski/oppija/KoskiOppijaFacade.scala
    src/main/scala/fi/oph/koski/perustiedot/OpiskeluoikeudenPerustiedotIndexer.scala
    src/main/scala/fi/oph/koski/schema/Suoritus.scala
    "src/main/scala/fi/oph/koski/schema/VapaanSivistysty\303\266VapaatavoitteinenKoulutus.scala"
    src/main/scala/fi/oph/koski/suoritusjako/SuoritusjakoService.scala
    src/main/scala/fi/oph/koski/suostumus/SuostumuksenPeruutusService.scala
    src/main/scala/fi/oph/koski/suostumus/SuostumuksenPeruutusServlet.scala
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    src/main/scala/fi/oph/koski/validation/SuostumuksenPeruutusValidaatiot.scala
    src/test/scala/fi/oph/koski/api/OpiskeluoikeusValidationSpec.scala
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266VapaatavoitteinenSpec.scala"
    src/test/scala/fi/oph/koski/api/SuostumuksenPeruutusSpec.scala
    web/app/omattiedot/OmatTiedotOpiskeluoikeus.jsx
    web/app/omattiedot/SuostumuksenPeruutusPopup.jsx
    web/app/opiskeluoikeus/OpiskeluoikeusEditor.jsx
    web/app/style/opiskeluoikeus.less
    web/test/runner.html
    web/test/spec/suostumuksenPeruutusSpec.js

## 14.10.2021

- Ammatillisen koulutuksen Yhteisille tutkinnon osille (YhteinenTutkinnonOsa-rakenne) sallitaan tunnisteen koodiarvoiksi myös '600001' ja '600002'

## 12.10.2021

    commit d4a13dbb55f946f4d0d7016eb480043b01f9fe94 (from 3db05d966ca7bf99aea02ee5fe8c919206098a77)
    Merge: 3db05d966 3b6d3e32c
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Tue Oct 12 13:17:47 2021 +0300

        Merge pull request #1595 from Opetushallitus/tor-1556-vst-vapaatavoitteisen-tiedonsiirtovalidaatiot-ja-vahvistus-puljaus

        tor-1556 - Vapaatavoitteisen VST:n suorituksen vahvistuksen puljaus ja validaatiot

    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    src/main/scala/fi/oph/koski/koskiuser/MockUsers.scala
    "src/main/scala/fi/oph/koski/validation/VapaaSivistysty\303\266Validation.scala"
    src/test/resources/backwardcompatibility/vapaasivistystyo-vapaatavoitteinenkoulutus_2021-10-08.json
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266MuutSpec.scala"
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266VapaatavoitteinenSpec.scala"
    web/test/page/addOppijaPage.js
    web/test/spec/vapaaSivistystyoSpec.js

## 12.10.2021
- Lukion 2019 opetussuunnitelman mukaisessa opiskeluoikeudessa salli paikallisen oppiaineen arviointina 'S' myös
  silloin, kun sen laajuus on yli 2 op.

## 11.10.2021
- Maksuttomuusvalidointi velvoitti maksuttomuustiedon siirtämiseen aina, jos perusopetus oli
  päättynyt ennen vuotta 2021 johonkin muuhun syyhyn kuin valmistumiseen, kuten eroamiseen. Tämä on korjattu.

## 5.10.2021

    commit 797d0898931b3f78cd5260f3a8f98849ee8c366b (from 34367b064897dc64b3f62361cf8c9525ffa666e1)
    Merge: 34367b064 e2fc60478
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Tue Oct 5 13:09:51 2021 +0300

        Merge pull request #1576 from Opetushallitus/tor-1509-vst-vapaatavoitteista-alkupäivämäärä-pois

        TOR-1509 - VST:n vapaatavoitteiselle ei siirrettä aloituspäivämäärää; vain lopettavat tilat sallittu

    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    "src/main/scala/fi/oph/koski/validation/VapaaSivistysty\303\266Validation.scala"
    src/test/resources/backwardcompatibility/vapaasivistystyo-vapaatavoitteinenkoulutus_2021-10-04.json
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266Spec.scala"
    web/app/opiskeluoikeus/OpiskeluoikeudenUusiTilaPopup.jsx
    web/app/opiskeluoikeus/opiskeluoikeus.js
    web/app/uusioppija/UusiOpiskeluoikeus.jsx
    web/test/page/addOppijaPage.js
    web/test/spec/vapaaSivistystyoSpec.js


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

## 30.7.2021

    commit e1ba803a60dc3c3a6790048a1103ed454c8e9aa6 (from 39e174371d28b7db65392fda4c05f58941933f21)
    Merge: 39e174371 840948e48
    Author: Aleksi Saarela <30666971+suarela@users.noreply.github.com>
    Date:   Fri Jul 30 11:56:04 2021 +0300

        Merge pull request #1490 from Opetushallitus/tor-1468-vst

        Tor 1468 VST päivityksiä

    src/main/resources/localization/koski-default-texts.json
    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/documentation/KoskiTiedonSiirtoHtml.scala
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    src/main/scala/fi/oph/koski/schema/Suoritus.scala
    "src/main/scala/fi/oph/koski/schema/VapaaSivistysty\303\266.scala"
    "src/main/scala/fi/oph/koski/schema/VapaanSivistysty\303\266nLukutaitokoulutus.scala"
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    "src/main/scala/fi/oph/koski/validation/VapaaSivistysty\303\266Validation.scala"
    src/test/resources/backwardcompatibility/vapaasivistystyo-lukutaitokoulutus_2021-07-27.json
    src/test/resources/backwardcompatibility/vapaasivistystyo-oppivelvollisillesuunnattukoulutus_2021-07-27.json
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266Spec.scala"
    web/app/suoritus/SuoritustaulukkoCommon.jsx
    web/app/vapaasivistystyo/VapaanSivistystyonSuoritustaulukko.jsx
    web/test/spec/vapaaSivistystyoSpec.js


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

    commit a8352b7cc8a7cef058d65d23a13029f60988e2a3 (from 2b0d520666349b90811734b1ce15d58257ced42c)
    Merge: 2b0d52066 5ffe90ed9
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Tue Jun 22 13:14:13 2021 +0300

        Merge pull request #1451 from Opetushallitus/tor-1171-vst-muut-koulutukset-tietomalli

        TOR-1171 VST muut (vapaatavoitteiset) koulutukset tietomalli

    src/main/resources/localization/koski-default-texts.json
    src/main/resources/mockdata/koodisto/koodistot/arviointiasteikkovstvapaatavoitteinen.json
    src/main/resources/mockdata/koodisto/koodit/arviointiasteikkovstvapaatavoitteinen.json
    src/main/resources/mockdata/koodisto/koodit/koulutus.json
    src/main/resources/mockdata/koodisto/koodit/suorituksentyyppi.json
    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/fixture/KoskiSpecificDatabaseFixtureCreator.scala
    src/main/scala/fi/oph/koski/henkilo/KoskiSpecificMockOppijat.scala
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    src/main/scala/fi/oph/koski/koodisto/Koodistot.scala
    "src/main/scala/fi/oph/koski/schema/VapaanSivistysty\303\266VapaatavoitteinenKoulutus.scala"
    "src/main/scala/fi/oph/koski/validation/VapaaSivistysty\303\266Validation.scala"
    src/test/resources/backwardcompatibility/vapaasivistystyo-vapaatavoitteinenkoulutus_2021-06-21.json
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266Spec.scala"

## 21.6.2021

    commit 627870099e9ced52ad2f62465f2d1cd28c5f9445 (from 69a5c3915c35d6995ec42399146ea6156742800b)
    Merge: 69a5c3915 b2c575451
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Mon Jun 21 16:08:07 2021 +0300

        Merge pull request #1447 from Opetushallitus/vst-koto-korjauksia

        VST-KOTO -korjauksia - lisätty eperusteet, nimiä vaihdeltu, korjattu koodistoja, yms

    src/main/resources/mockdata/eperusteet/rakenne-vst-maahanmuuttajien-kotoutumiskoulutus.json
    src/main/resources/mockdata/koodisto/koodit/arviointiasteikkokehittyvankielitaidontasot.json
    src/main/resources/mockdata/koodisto/koodit/koulutustyyppi.json
    src/main/resources/mockdata/koodisto/koodit/suorituksentyyppi.json
    src/main/resources/mockdata/koodisto/koodit/vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus.json
    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/eperusteet/MockEPerusteetRepository.scala
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    src/main/scala/fi/oph/koski/schema/Lukio2019.scala
    src/main/scala/fi/oph/koski/schema/Suoritus.scala
    "src/main/scala/fi/oph/koski/schema/VapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/tutkinto/Koulutustyyppi.scala
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    "src/main/scala/fi/oph/koski/validation/VapaaSivistysty\303\266Validation.scala"
    src/test/resources/backwardcompatibility/vapaasivistystyo-maahanmuuttajienkotoutuskoulutus_2021-06-17.json
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266Spec.scala"
    web/app/uusioppija/UusiVapaanSivistystyonSuoritus.jsx
    web/app/vapaasivistystyo/UusiVapaanSivistystyonOsasuoritus.jsx
    web/app/vapaasivistystyo/VapaanSivistystyonSuoritustaulukko.jsx
    web/test/spec/vapaaSivistystyoSpec.js

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

## 26.4.2021

    commit ce4087ccff1b2a85f39e432ecd97f9e1b028a67a (from e982652e0b2e6d191d8ca84a870efe001206ee22)
    Merge: e982652e0 d73dc3ce8
    Author: Aleksi Saarela <30666971+suarela@users.noreply.github.com>
    Date:   Mon Apr 26 13:47:41 2021 +0300

        Merge pull request #1305 from Opetushallitus/tor-1342-kali

        VST korjauksia ja mahdollisuus luoda käyttöliittymässä uusi VST:n opiskeluoikeus KOPS-suorituksella

    src/main/scala/fi/oph/koski/oppilaitos/OppilaitosServlet.scala
    src/main/scala/fi/oph/koski/organisaatio/Oppilaitostyyppi.scala
    "src/main/scala/fi/oph/koski/schema/VapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    web/app/uusioppija/UusiOpiskeluoikeus.jsx
    web/app/uusioppija/UusiVapaanSivistystyonSuoritus.jsx
    web/test/page/addOppijaPage.js
    web/test/runner.html
    web/test/spec/vapaaSivistystyoSpec.js

## 23.4.2021

    commit 9d386ba7627be920075d7b57dab65b22d39bc76a (from ba5f02d1847b9d76ab7c2f18bf754c62b8b2f4df)
    Merge: ba5f02d18 c036941dd
    Author: Aleksi Saarela <30666971+suarela@users.noreply.github.com>
    Date:   Fri Apr 23 11:38:08 2021 +0300

        Merge pull request #1302 from Opetushallitus/tor-1348

        VST:n koulutusten laajuuksien muutos, validaatiosääntöjen hiomista

    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    "src/main/scala/fi/oph/koski/schema/VapaaSivistysty\303\266.scala"
    "src/main/scala/fi/oph/koski/schema/VapaanSivistysty\303\266nLukutaitokoulutus.scala"
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    src/test/resources/backwardcompatibility/vapaasivistystyo-lukutaitokoulutus_2021-04-22.json
    src/test/resources/backwardcompatibility/vapaasivistystyo-maahanmuuttajienkotoutuskoulutus_2021-04-22.json
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266Spec.scala"

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

## 23.3.2021

    commit 296d41fdd56b13b21fb4b98caf59dd25c1e4e113 (from 0d4176abe8a72b1f0d9bf3ca319fc0f13ae52549)
    Merge: 0d4176abe 4367d98d0
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Tue Mar 23 17:05:15 2021 +0200

        Merge pull request #1226 from Opetushallitus/tor-1170-vst-koto

        Tor-1170 VST KOTO - Oppijan esimerkkia laajennettu. Validaatioita muillekin kokonaisuuksille.

    src/main/resources/localization/koski-default-texts.json
    src/main/resources/mockdata/koodisto/koodistot/arviointiasteikkovstkoto.json
    src/main/resources/mockdata/koodisto/koodit/suorituksentyyppi.json
    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    src/main/scala/fi/oph/koski/koodisto/Koodistot.scala
    "src/main/scala/fi/oph/koski/schema/VapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266Spec.scala"

## 18.3.2021

    commit 2ab6741a0f3b919fbc4d7e15611fa618367b12f0 (from 92602a4d99c595d7bbd5234184109c20854f35ff)
    Merge: 92602a4d9 ab335b99c
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Thu Mar 18 13:23:52 2021 +0200

        Merge pull request #1207 from Opetushallitus/tor-1170-vst-koto

        Tor 1170 vst koto

    src/main/resources/localization/koski-default-texts.json
    src/main/resources/mockdata/koodisto/koodistot/arviointiasteikkovstkoto.json
    src/main/resources/mockdata/koodisto/koodistot/vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus.json
    src/main/resources/mockdata/koodisto/koodistot/vstmaahanmuuttajienkotoutumiskoulutus.json
    src/main/resources/mockdata/koodisto/koodit/arviointiasteikkovstkoto.json
    src/main/resources/mockdata/koodisto/koodit/koulutus.json
    src/main/resources/mockdata/koodisto/koodit/suorituksentyyppi.json
    src/main/resources/mockdata/koodisto/koodit/vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus.json
    src/main/scala/fi/oph/koski/documentation/ExampleData.scala
    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/fixture/KoskiSpecificDatabaseFixtureCreator.scala
    src/main/scala/fi/oph/koski/henkilo/KoskiSpecificMockOppijat.scala
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    src/main/scala/fi/oph/koski/koodisto/Koodistot.scala
    src/main/scala/fi/oph/koski/schema/Laajuus.scala
    "src/main/scala/fi/oph/koski/schema/VapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266Spec.scala"

## 10.2.2021

    commit 39072cded873e560e8c3e0041afe8b1db91f87e0 (from 00fde473ced057e49414c856fac6b4bbba4f8a58)
    Merge: 00fde473c 5cef3e88c
    Author: Jalmari Ikävalko <tzaeru@gmail.com>
    Date:   Wed Feb 10 12:55:57 2021 +0200

        Merge pull request #1128 from Opetushallitus/tor-1770-suoritusten-määrän-validointi

        Tor-1170 VST päätason suoritusten osasuoritusten yhteenlasketun laajuuden validointi

    src/main/resources/mockdata/koodisto/koodit/koulutus.json
    "src/main/scala/fi/oph/koski/documentation/ExamplesVapaaSivistysty\303\266.scala"
    src/main/scala/fi/oph/koski/http/KoskiErrorCategory.scala
    src/main/scala/fi/oph/koski/validation/KoskiValidator.scala
    src/test/resources/backwardcompatibility/vapaasivistystyo-oppivelvollisillesuunnattukoulutus_2021-02-02.json
    "src/test/scala/fi/oph/koski/api/OppijaValidationVapaaSivistysty\303\266Spec.scala"

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
