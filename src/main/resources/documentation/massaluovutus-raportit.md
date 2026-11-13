# Raportit massaluovutusrajapinnan kautta

Tällä sivulla kuvataan raportit, jotka ovat saatavilla massaluovutusrajapinnan kautta. Raportit vastaavat
raporttinäkymästä ladattavia raportteja ja palauttavat Excel-muotoisia (xlsx) tiedostoja; päällekkäisten
opiskeluoikeuksien raportti on saatavana myös csv-muodossa. Raporttien hakeminen edellyttää samoja
käyttöoikeuksia kuin raporttinäkymän käyttö.

Raportteja haetaan samalla tavalla kuin muitakin massaluovutuskyselyitä. Kyselyn luonti, tilan seuranta ja
tulosten lataus on kuvattu [yleisissä ohjeissa](/koski/dokumentaatio/rajapinnat/massaluovutus). Kyselyt
lähetetään osoitteeseen `POST {{var:baseUrl}}/api/massaluovutus`.
[Katso myös kaikki kyselyiden skeemat graafisessa muodossa](/koski/json-schema-viewer/?schema=massaluovutus-query.json).

{{toc}}

## Yhteiset parametrit

Kyselyn tyyppi (`type`) ja alla luetellut parametrit ovat kaikilla raporteilla samat. Raporttikohtaisissa
kuvauksissa näytetään vain raportin omat parametrit.

{{docs:fi.oph.koski.massaluovutus.RaporttikyselynYhteisetParametrit|title=Yhteiset parametrit|open}}

{{query:PaallekkaisetOpiskeluoikeudetCsv,PaallekkaisetOpiskeluoikeudetXlsx}}

{{query:KotikuntalaskelmaXlsx|exclude=format,language,password}}

{{query:KunnanOppijatXlsx|exclude=format,language,password}}

## Lukiokoulutus

{{query:LukionSuoritustiedotXlsx|h3|raportti}}

{{query:Lukio2019SuoritustiedotXlsx|h3|raportti}}

{{query:LukioKurssikertymatXlsx|h3|raportti}}

{{query:Lukio2019OpintopistekertymatXlsx|h3|raportti}}

{{query:LukioDiaIbInternationalESHOpiskelijamaaratXlsx|h3|raportti}}

{{query:LuvaOpiskelijamaaratXlsx|h3|raportti}}

## Ammatillinen koulutus

{{query:AmmatillinenOpiskelijavuositiedotXlsx|h3|raportti}}

{{query:AmmatillinenTutkintoSuoritustiedotXlsx|h3|raportti}}

{{query:AmmatillinenOsittainenSuoritustiedotXlsx|h3|raportti}}

{{query:MuuAmmatillinenXlsx|h3|raportti}}

{{query:TOPKSAmmatillinenXlsx|h3|raportti}}

## Perusopetus

{{query:PerusopetuksenVuosiluokkaXlsx|h3|raportti}}

{{query:PerusopetuksenOppijamaaratRaporttiXlsx|h3|raportti}}

{{query:PerusopetuksenLisaopetuksenOppijamaaratRaporttiXlsx|h3|raportti}}

{{query:PerusopetukseenValmistavaXlsx|h3|raportti}}

## Aikuisten perusopetus

{{query:AikuistenPerusopetusSuoritustiedotXlsx|h3|raportti}}

{{query:AikuistenPerusopetuksenOppijamaaratRaporttiXlsx|h3|raportti}}

{{query:AikuistenPerusopetuksenKurssikertymaXlsx|h3|raportti}}

## Esiopetus

{{query:EsiopetusXlsx|h3|raportti}}

{{query:EsiopetuksenOppijamaaratRaporttiXlsx|h3|raportti}}

## Tutkintokoulutukseen valmentava koulutus (TUVA)

{{query:TuvaPerusopetuksenOppijamaaratRaporttiXlsx|h3|raportti}}

{{query:TuvaSuoritustiedotXlsx|h3|raportti}}

## IB-tutkinto

{{query:IBSuoritustiedotXlsx|h3|raportti}}

## Vapaa sivistystyö

{{query:VSTJOTPAXlsx|h3|raportti}}

{{query:VSTVapaatavoitteinenXlsx|h3|raportti}}

{{query:VSTKOPSXlsx|h3|raportti}}

{{query:VSTOsaamismerkitXlsx|h3|raportti}}

## Muu kuin säännelty koulutus

{{query:MuuKuinSaanneltyKoulutusXlsx|h3|raportti}}
