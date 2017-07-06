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
    "ammatillisenoppiaineet",
    "ammatillistenopintojenpoissaolonsyy",
    "ammatillisentutkinnonosanlisatieto",
    "ammatillisentutkinnonosanryhma",
    "ammatillisentutkinnonsuoritustapa",
    "arviointiasteikkoammatillinenhyvaksyttyhylatty",
    "arviointiasteikkoammatillinent1k3",
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
    "perusopetuksenluokkaaste",
    "perusopetuksentodistuksenliitetieto",
    "perusopetuksensuoritustapa",
    "perusopetuksentoimintaalue",
    "perusopetuksentukimuoto",
    "suorituksentyyppi"
  )
  
  // Muut koodistot, joita Koski käyttää
  val muutKoodistot = List (
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
    "virtaopiskeluoikeudentila"
  )

  val koodistot = (koskiKoodistot ++ muutKoodistot).sorted

  /*
    Uuden koodiston lisäys:

    1) Lisää koodisto tähän repositorioon

    1a) Olemassa oleva koodisto QA-ympäristöstä: Aja KoodistoMockDataUpdater -Dconfig.resource=qa.conf, jolloin koodiston sisältö haetaan qa-ympäristöstä paikallisiin json-fileisiin.
        Lisää koodiston nimi yllä olevaan muutKoodistot-listaan
    1b) Uusi Koski-spesifinen koodisto: Tee käsin koodistofileet src/main/resources/koodisto
        Lisää koodiston nimi yllä olevaan koskiKoodistot-listaan

    3) Kommitoi uudet json-fileet. Muutoksia olemassa oleviin fileisiin ei kannattane tässä yhteydessä kommitoida.
    4) Aja koski-applikaatio -Dconfig.resource=koskidev.conf -Dkoodisto.create=true, jolloin uusi koodisto kopioituu myös koskidev-ympäristöön.
   */
}
