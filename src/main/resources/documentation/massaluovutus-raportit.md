# Raportit massaluovutusrajapinnan kautta

Tämä dokumentaatio kuvaa raportit, jotka ovat saatavilla massaluovutusrajapinnan kautta.
Raportit palauttavat Excel-muotoisia (xlsx) tiedostoja.

Raporttien käyttö tapahtuu samalla tavalla kuin muutkin massaluovutuskyselyt.
Katso [massaluovutusrajapinnan yleinen dokumentaatio](/koski/dokumentaatio/massaluovutus-koulutuksenjarjestajille)
kyselyn luonnista ja tulosten hakemisesta.

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:LukionSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:Lukio2019SuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:LukioKurssikertymatXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:Lukio2019OpintopistekertymatXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:LukioDiaIbInternationalESHOpiskelijamaaratXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLuvaOpiskelijamaarat}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLuvaOpiskelijamaarat}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:LuvaOpiskelijamaaratXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AmmatillinenOpiskelijavuositiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AmmatillinenTutkintoSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AmmatillinenOsittainenSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:MuuAmmatillinenXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:TOPKSAmmatillinenXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PerusopetuksenVuosiluokkaXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PerusopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PerusopetuksenLisaopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PerusopetukseenValmistavaXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AikuistenPerusopetusSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AikuistenPerusopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AikuistenPerusopetuksenKurssikertymaXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:EsiopetusXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:EsiopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:TuvaPerusopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:TuvaSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:IBSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:VSTJOTPAXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:MuuKuinSaanneltyKoulutusXlsx}}
