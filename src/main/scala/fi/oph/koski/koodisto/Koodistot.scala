package fi.oph.koski.koodisto

object Koodistot {
  // Koski-spesifiset koodistot.
  val koskiKoodistot = List (
    "aikuistenperusopetuksenkurssit2015",
    "aikuistenperusopetuksenalkuvaiheenkurssit2017",
    "aikuistenperusopetuksenalkuvaiheenoppiaineet",
    "aikuistenperusopetuksenpaattovaiheenkurssit2017",
    "aineryhmaib",
    "ammatillisennaytonarvioinnistapaattaneet",
    "ammatillisennaytonarviointikeskusteluunosallistuneet",
    "ammatillisennaytonarviointikohde",
    "ammatillisennaytonsuorituspaikka",
    "ammatillisentutkinnonosanlisatieto",
    "ammatillisentutkinnonosanryhma",
    "ammatillisentutkinnonsuoritustapa",
    "arviointiasteikkoammatillinenhyvaksyttyhylatty",
    "arviointiasteikkoammatillinent1k3",
    "arviointiasteikkoammatillinen15",
    "arviointiasteikkocorerequirementsib",
    "arviointiasteikkoib",
    "arviointiasteikkolisapisteetib",
    "arviointiasteikkoyleissivistava",
    "effortasteikkoib",
    "erityinenkoulutustehtava",
    "koskikoulutustendiaarinumerot",
    "koskiopiskeluoikeudentila",
    "koskioppiaineetyleissivistava",
    "koskiyoarvosanat",
    "koskiyokokeet",
    "lahdejarjestelma",
    "lasnaolotila",
    "lukionkurssintyyppi",
    "lukionkurssitops2003nuoret",
    "lukionkurssitops2004aikuiset",
    "lukionoppimaara",
    "lukionmuutopinnot",
    "opetusryhma",
    "opintojenrahoitus",
    "opiskeluoikeudentyyppi",
    "oppiaineaidinkielijakirjallisuus",
    "oppiainematematiikka",
    "oppiaineentasoib",
    "oppiaineetib",
    "oppiaineetluva",
    "osaamisenhankkimistapa",
    "perusopetuksenluokkaaste",
    "perusopetuksentodistuksenliitetieto",
    "perusopetuksensuoritustapa",
    "perusopetuksentoimintaalue",
    "perusopetuksentukimuoto",
    "suorituksentyyppi"
  )
  
  // Muut koodistot, joita Koski käyttää
  val muutKoodistot = List (
    "ammatillisenoppiaineet",
    "jarjestamismuoto",
    "kieli",
    "kielivalikoima",
    "koulutus",
    "koulutustyyppi",
    "kunta",
    "lukionkurssit",
    "maatjavaltiot2",
    "opintojenlaajuusyksikko",
    "oppilaitosnumero",
    "oppilaitostyyppi",
    "osaamisala",
    "suorituksentila",
    "tutkinnonosat",
    "tutkintonimikkeet",
    "virtaarvosana",
    "virtalukukausiilmtila",
    "virtaopiskeluoikeudentila",
    "virtaopiskeluoikeudentyyppi"
  )

  val koodistot = (koskiKoodistot ++ muutKoodistot).sorted

  /*
    Uuden koodiston lisäys:

    1) Lisää koodisto tähän repositorioon

    1a) Olemassa oleva koodisto QA-ympäristöstä: Aja KoodistoMockDataUpdater -Dconfig.resource=qa.conf, jolloin koodiston sisältö haetaan qa-ympäristöstä paikallisiin json-fileisiin.
        Lisää koodiston nimi yllä olevaan muutKoodistot-listaan
    1b) Olemassa oleva koodisto tuotantoympäristöstä: Lisää koodiston nimi ylläolevaan muutKoodistot listaan, ja aja
        mvn exec:java -Dexec.mainClass=fi.oph.koski.koodisto.KoodistoMockDataUpdater -Dopintopolku.virkailija.url=https://virkailija.opintopolku.fi -DkoskiKoodistot=false -DmuutKoodistot=true
    1c) Uusi Koski-spesifinen koodisto: Tee käsin koodistofileet src/main/resources/koodisto
        Lisää koodiston nimi yllä olevaan koskiKoodistot-listaan

    2) Kommitoi uudet json-fileet. Muutoksia olemassa oleviin fileisiin ei kannattane tässä yhteydessä kommitoida.
    3) Aja koski-applikaatio -Dconfig.resource=koskidev.conf -Dkoodisto.create=true, jolloin uusi koodisto kopioituu myös koskidev-ympäristöön.
   */
}
