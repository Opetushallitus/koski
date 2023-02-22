package fi.oph.koski.koodisto

case class KoodistoAsetus(
  koodisto: String,
  vaadiSuomenkielinenNimi: Boolean = true,
  vaadiRuotsinkielinenNimi: Boolean = true,
  koodistoVersio: Option[Int] = None
) {
  override def toString: String = koodisto + koodistoVersio.map(v => s"_$v").getOrElse("")
}

object Koodistot {
  // Koski-spesifiset koodistot.
  val koskiKoodistoAsetukset = List (
    KoodistoAsetus("aikuistenperusopetuksenkurssit2015"),
    KoodistoAsetus("aikuistenperusopetuksenalkuvaiheenkurssit2017"),
    KoodistoAsetus("aikuistenperusopetuksenalkuvaiheenoppiaineet"),
    KoodistoAsetus("aikuistenperusopetuksenpaattovaiheenkurssit2017"),
    KoodistoAsetus("aineryhmaib", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("ammatillisennaytonarvioinnistapaattaneet"),
    KoodistoAsetus("ammatillisennaytonarviointikeskusteluunosallistuneet"),
    KoodistoAsetus("ammatillisennaytonarviointikohde"),
    KoodistoAsetus("ammatillisennaytonsuorituspaikka"),
    KoodistoAsetus("ammatillisentutkinnonosanlisatieto"),
    KoodistoAsetus("ammatillisentutkinnonosanryhma"),
    KoodistoAsetus("ammatillisentutkinnonsuoritustapa"),
    KoodistoAsetus("arviointiasteikkoammatillinenhyvaksyttyhylatty"),
    KoodistoAsetus("arviointiasteikkoammatillinent1k3"),
    KoodistoAsetus("arviointiasteikkoammatillinen15", vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("arviointiasteikkocorerequirementsib", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("arviointiasteikkodiatutkinto", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("arviointiasteikkodiavalmistava", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("arviointiasteikkoib", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("arviointiasteikkointernationalschool", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("arviointiasteikkolisapisteetib", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("arviointiasteikkomuuammatillinenkoulutus"),
    KoodistoAsetus("arviointiasteikkovst"),
    KoodistoAsetus("arviointiasteikkovstvapaatavoitteinen"),
    KoodistoAsetus("arviointiasteikkoyleissivistava"),
    KoodistoAsetus("arviointiasteikkokehittyvankielitaidontasot"),
    KoodistoAsetus("arviointiasteikkotuva"),
    KoodistoAsetus("dialukukausi", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("diaosaalue", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("diapaattokoe", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("effortasteikkoib", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("erityinenkoulutustehtava"),
    KoodistoAsetus("erityisopetuksentoteutuspaikka"),
    KoodistoAsetus("internationalschooldiplomatype", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("internationalschoolluokkaaste", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("koskikoulutustendiaarinumerot"),
    KoodistoAsetus("koskiopiskeluoikeudentila"),
    KoodistoAsetus("koskioppiaineetyleissivistava"),
    KoodistoAsetus("koskiyoarvosanat", vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("koskiyokokeet"),
    KoodistoAsetus("lahdejarjestelma", vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("lasnaolotila"),
    KoodistoAsetus("lukioonvalmistavankoulutuksenkurssit2015"),
    KoodistoAsetus("lukioonvalmistavankoulutuksenmoduulit2019"),
    KoodistoAsetus("lukionkurssintyyppi"),
    KoodistoAsetus("lukionkurssitops2003nuoret"),
    KoodistoAsetus("lukionkurssitops2004aikuiset"),
    KoodistoAsetus("lukionoppimaara"),
    KoodistoAsetus("lukionmuutopinnot"),
    KoodistoAsetus("opetusryhma"),
    KoodistoAsetus("opintojenrahoitus"),
    KoodistoAsetus("opiskeluoikeudentyyppi"),
    KoodistoAsetus("oppiaineaidinkielijakirjallisuus"),
    KoodistoAsetus("oppiainediaaidinkieli"),
    KoodistoAsetus("oppiainematematiikka"),
    KoodistoAsetus("oppiaineetdia", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("oppiaineentasoib", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("oppiaineetib", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("oppiaineetinternationalschool", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("oppiaineetluva"),
    KoodistoAsetus("osaaikainenerityisopetuslukuvuodenaikana"),
    KoodistoAsetus("osaamisenhankkimistapa"),
    KoodistoAsetus("perusopetuksenluokkaaste"),
    KoodistoAsetus("perusopetuksentodistuksenliitetieto"),
    KoodistoAsetus("perusopetuksensuoritustapa"),
    KoodistoAsetus("perusopetuksentoimintaalue"),
    KoodistoAsetus("perusopetuksentukimuoto"),
    KoodistoAsetus("suorituksentyyppi"),
    KoodistoAsetus("tutkinnonosatvalinnanmahdollisuus"),
    KoodistoAsetus("uskonnonoppimaara"),
    KoodistoAsetus("vstmuuallasuoritetutopinnot"),
    KoodistoAsetus("vstmuutopinnot"),
    KoodistoAsetus("vstosaamiskokonaisuus"),
    KoodistoAsetus("vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
    KoodistoAsetus("vstlukutaitokoulutuksenkokonaisuus"),
    KoodistoAsetus("vstkoto2022kokonaisuus"),
    KoodistoAsetus("vstkoto2022kielijaviestintakoulutus"),
    KoodistoAsetus("vstkoto2022yhteiskuntajatyoosaamiskoulutus"),
    KoodistoAsetus("valpasopiskeluoikeudentila"),
    KoodistoAsetus("valpashaunvalintatila"),
    KoodistoAsetus("valpasvastaanottotieto"),
    KoodistoAsetus("tuvajarjestamislupa"),
    KoodistoAsetus("europeanschoolofhelsinkiluokkaaste"),
    KoodistoAsetus("europeanschoolofhelsinkicurriculum"),
    KoodistoAsetus("europeanschoolofhelsinkilapsioppimisalue"),
    KoodistoAsetus("europeanschoolofhelsinkikielioppiaine"),
    KoodistoAsetus("europeanschoolofhelsinkimuuoppiaine"),
    KoodistoAsetus("europeanschoolofhelsinkis7oppiaineenkomponentti"),
    KoodistoAsetus("europeanschoolofhelsinkiprimarylapsialaoppimisalue"),
    KoodistoAsetus("europeanschoolofhelsinkiprimaryalaoppimisalue"),
    KoodistoAsetus("arviointiasteikkoeuropeanschoolofhelsinkiosasuoritus"),
    KoodistoAsetus("arviointiasteikkoeuropeanschoolofhelsinkiprimarymark"),
    KoodistoAsetus("arviointiasteikkoeuropeanschoolofhelsinkisecondarygrade"),
    KoodistoAsetus("arviointiasteikkomuks"),
    KoodistoAsetus("arviointiasteikkoeuropeanschoolofhelsinkinumericalmark"),
    KoodistoAsetus("arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark"),
    KoodistoAsetus("arviointiasteikkoeuropeanschoolofhelsinkifinalmark"),
    KoodistoAsetus("ebtutkinnonoppiaineenkomponentti"),
    KoodistoAsetus("taiteenperusopetustaiteenala"),
    KoodistoAsetus("taiteenperusopetusoppimaara"),
    KoodistoAsetus("taiteenperusopetuskoulutuksentoteutustapa"),
    KoodistoAsetus("arviointiasteikkotaiteenperusopetus"),
    KoodistoAsetus("arviointiasteikkovstjotpa"),
    KoodistoAsetus("ytrkoulutustausta"),
    KoodistoAsetus("ytrtutkintokokonaisuudentila"),
    KoodistoAsetus("ytrtutkintokokonaisuudentyyppi"),
    KoodistoAsetus("ammatillisensuorituksenkorotus")
  )
  val koskiKoodistot = koskiKoodistoAsetukset.map(_.toString)

  // Muut koodistot, joita Koski käyttää
  val muutKoodistoAsetukset = List (
    KoodistoAsetus("ammatillisenoppiaineet"),
    KoodistoAsetus("ammatilliseentehtavaanvalmistavakoulutus"),
    KoodistoAsetus("hakutapa"),
    KoodistoAsetus("hakutyyppi"),
    KoodistoAsetus("jarjestamismuoto"),
    KoodistoAsetus("kieli"),
    KoodistoAsetus("kielivalikoima"),
    KoodistoAsetus("koulutus"),
    KoodistoAsetus("koulutus", koodistoVersio = Some(11)),
    KoodistoAsetus("koulutustyyppi"),
    KoodistoAsetus("kunta"),
    KoodistoAsetus("lukionkurssit"),
    KoodistoAsetus("maatjavaltiot1"),
    KoodistoAsetus("maatjavaltiot2"),
    KoodistoAsetus("opintojenlaajuusyksikko"),
    KoodistoAsetus("oppilaitosnumero", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false),
    KoodistoAsetus("oppilaitostyyppi"),
    KoodistoAsetus("oppilaitoksenopetuskieli"),
    KoodistoAsetus("osaamisala", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false), // ks. EPerusteetLocalizationTest
    KoodistoAsetus("suorituksentila"),
    KoodistoAsetus("tutkinnonosat", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false), // ks. EPerusteetLocalizationTest
    KoodistoAsetus("tutkintonimikkeet", vaadiSuomenkielinenNimi = false, vaadiRuotsinkielinenNimi = false), // ks. EPerusteetLocalizationTest
    KoodistoAsetus("vardajarjestamismuoto"),
    KoodistoAsetus("virtaarvosana"),
    KoodistoAsetus("virtalukukausiilmtila"),
    KoodistoAsetus("virtaopiskeluoikeudentila"),
    KoodistoAsetus("virtaopiskeluoikeudenluokittelu"),
    KoodistoAsetus("virtaopiskeluoikeudentyyppi"),
    KoodistoAsetus("virtaopsuorluokittelu"),
    KoodistoAsetus("moduulikoodistolops2021"),
    KoodistoAsetus("yhteystietojenalkupera"),
    KoodistoAsetus("yhteystietotyypit"),
    KoodistoAsetus("opintokokonaisuudet"),
    KoodistoAsetus("koulutuksenosattuva"),
  )
  val muutKoodistot = muutKoodistoAsetukset.map(_.toString)

  val koodistoAsetukset = (koskiKoodistoAsetukset ++ muutKoodistoAsetukset).sortBy(_.koodisto)
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
    1d) Koodiston versiointi: Jos haluat lisätä jostain koodistosta kokonaan eri version, niin lisää koodisto ja koodit json-tiedostojen loppuun koodiston versio alaviivalla erotettuna.
        Esim: koulutus_11.json. Koodisto ilman määriteltyä versiota tulkitaan aina uusimmaksi versioksi.

    2) Kommitoi uudet json-fileet. Muutoksia olemassa oleviin fileisiin ei kannattane tässä yhteydessä kommitoida.
    3) Aja koski-applikaatio -Dconfig.resource=koskidev.conf -Dkoodisto.create=true, jolloin uusi koodisto kopioituu myös koskidev-ympäristöön.
   */
}
