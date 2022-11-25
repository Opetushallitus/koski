package fi.oph.koski.documentation

import fi.oph.koski.documentation.ExampleData.{englanti, helsinki, laajuusVuosiviikkotunneissa, sloveeni}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{PrimaryOsasuoritus, _}

import java.time.LocalDate

object EuropeanSchoolOfHelsinkiExampleData {
  lazy val europeanSchoolOfHelsinki: Oppilaitos = Oppilaitos(MockOrganisaatiot.europeanSchoolOfHelsinki, Some(Koodistokoodiviite("03782", None, "oppilaitosnumero", None)), Some("European School of Helsinki"))
  lazy val europeanSchoolOfHelsinkiToimipiste: Toimipiste = Toimipiste("1.2.246.562.10.12798841685")

  def vahvistus(päivä: LocalDate) = ExampleData.vahvistusPaikkakunnalla(päivä, europeanSchoolOfHelsinki, ExampleData.helsinki)

  def nurserySuoritus(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = NurseryVuosiluokanSuoritus(
    koulutusmoduuli = NurseryLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    jääLuokalle = jääLuokalle
  )

  def primarySuoritus12(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false, todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None) = PrimaryVuosiluokanSuoritus(
    koulutusmoduuli = PrimaryLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    jääLuokalle = jääLuokalle,
    todistuksellaNäkyvätLisätiedot = todistuksellaNäkyvätLisätiedot,
    osasuoritukset = Some(List(
      primaryLapsiOppimisalueenOsasuoritus(
        oppiainekoodi = "TCAAL",
        alaosasuorituskoodit = Some(List(
          "Engages in learning", "Listens attentively", "Develops working habits", "Works independently", "Perseveres with difficult tasks", "Uses ICT", "Presents work carefully", "Produces quality homework"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30),
          kuvaus = Some(LocalizedString.finnish("hyvää työtä!"))
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          päivä = alkamispäivä.plusDays(30)
        )
      ),
      primaryLapsiOppimisalueenOsasuoritus(
        oppiainekoodi = "TCAAP",
        alaosasuorituskoodit = Some(List(
          "Seems happy at school", "Is self-confident", "Manages and expresses own feelings", "Evaluates own progress"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "4",
          päivä = alkamispäivä.plusDays(30)
        )
      ),
      primaryLapsiOppimisalueenOsasuoritus(
        oppiainekoodi = "TCAO",
        alaosasuorituskoodit = Some(List(
          "Respect class rules", "Respect school rules", "Cooperates with others", "Respects others", "Shows empathy"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "2",
          päivä = alkamispäivä.plusDays(30)
        )
      ),
      primaryOppimisalueenOsasuoritusKieli(
        oppiainekoodi = "L1",
        laajuus = 16,
        kieli = ExampleData.englanti,
        suorituskieli = ExampleData.englanti,
        alaosasuorituskoodit = Some(List(
          "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30),
          kuvaus = Some(LocalizedString.finnish("Focus on writing next year")),
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "1",
          päivä = alkamispäivä.plusDays(60)
        ),
      ),
      primaryOppimisalueenOsasuoritusKieli(
        oppiainekoodi = "L2",
        laajuus = 5,
        kieli = ExampleData.ranska,
        suorituskieli = ExampleData.englanti,
        alaosasuorituskoodit = Some(List(
          "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30),
          kuvaus = Some(LocalizedString.finnish("Doing good")),
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "3",
          päivä = alkamispäivä.plusDays(60)
        ),
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "MA",
        laajuus = 8,
        yksilöllistettyOppimäärä = true,
        alaosasuorituskoodit = Some(List(
          "Numbers & Number system", "Calculation", "Measurement", "Shape and Space", "Data handling", "Problem solving"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "2",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "DOW",
        laajuus = 3,
        alaosasuorituskoodit = Some(List(
          "Biological", "Technological", "Geographical", "Historical"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "3",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "ART",
        laajuus = 3,
        alaosasuorituskoodit = Some(List(
          "Plastic and static visual arts", "The arts and entertainment"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "3",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "MU",
        laajuus = 3,
        alaosasuorituskoodit = Some(List(
          "Music making & performing", "Listening and responding", "Composing"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "4",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "PE",
        laajuus = 4,
        alaosasuorituskoodit = Some(List(
          "Individual activities", "Team activities", "Swimming"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "4",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "RE",
        laajuus = 2,
        alaosasuorituskoodit = None,
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        )
      )
    ))
  )

  def primarySuoritus345(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false, todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None) = PrimaryVuosiluokanSuoritus(
    koulutusmoduuli = PrimaryLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    jääLuokalle = jääLuokalle,
    todistuksellaNäkyvätLisätiedot = todistuksellaNäkyvätLisätiedot,
    osasuoritukset = Some(List(
      primaryLapsiOppimisalueenOsasuoritus(
        oppiainekoodi = "TCAAL",
        alaosasuorituskoodit = Some(List(
          "Engages in learning", "Listens attentively", "Develops working habits", "Works independently", "Perseveres with difficult tasks", "Uses ICT", "Presents work carefully", "Produces quality homework"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30),
          kuvaus = Some(LocalizedString.finnish("hyvää työtä!"))
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          päivä = alkamispäivä.plusDays(30)
        )
      ),
      primaryLapsiOppimisalueenOsasuoritus(
        oppiainekoodi = "TCAAP",
        alaosasuorituskoodit = Some(List(
          "Seems happy at school", "Is self-confident", "Manages and expresses own feelings", "Evaluates own progress"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "4",
          päivä = alkamispäivä.plusDays(30)
        )
      ),
      primaryLapsiOppimisalueenOsasuoritus(
        oppiainekoodi = "TCAO",
        alaosasuorituskoodit = Some(List(
          "Respect class rules", "Respect school rules", "Cooperates with others", "Respects others", "Shows empathy"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "2",
          päivä = alkamispäivä.plusDays(30)
        )
      ),
      primaryOppimisalueenOsasuoritusKieli(
        oppiainekoodi = "L1",
        laajuus = 9,
        kieli = ExampleData.englanti,
        suorituskieli = ExampleData.englanti,
        alaosasuorituskoodit = Some(List(
          "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30),
          kuvaus = Some(LocalizedString.finnish("Focus on writing next year")),
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "1",
          päivä = alkamispäivä.plusDays(60)
        ),
      ),
      primaryOppimisalueenOsasuoritusKieli(
        oppiainekoodi = "L2",
        laajuus = 5,
        kieli = ExampleData.ranska,
        suorituskieli = ExampleData.englanti,
        alaosasuorituskoodit = Some(List(
          "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30),
          kuvaus = Some(LocalizedString.finnish("Doing good")),
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "3",
          päivä = alkamispäivä.plusDays(60)
        ),
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "MA",
        laajuus = 7,
        yksilöllistettyOppimäärä = true,
        alaosasuorituskoodit = Some(List(
          "Numbers & Number system", "Calculation", "Measurement", "Shape and Space", "Data handling", "Problem solving"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "2",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "DOW",
        laajuus = 4,
        alaosasuorituskoodit = Some(List(
          "Biological", "Technological", "Geographical", "Historical"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "3",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "ART",
        laajuus = 2,
        alaosasuorituskoodit = Some(List(
          "Plastic and static visual arts", "The arts and entertainment"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "3",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "MU",
        laajuus = 1,
        alaosasuorituskoodit = Some(List(
          "Music making & performing", "Listening and responding", "Composing"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "4",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "PE",
        laajuus = 1,
        alaosasuorituskoodit = Some(List(
          "Individual activities", "Team activities", "Swimming"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "4",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "RE",
        laajuus = 2,
        alaosasuorituskoodit = None,
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "EuH",
        laajuus = 2,
        alaosasuorituskoodit = Some(List(
          "Communicating and working with others"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(30)
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "1",
          päivä = alkamispäivä.plusDays(65)
        )
      ),
      primaryOppimisalueenOsasuoritusKieli(
        laajuus = 3,
        oppiainekoodi = "ONL",
        kieli = ExampleData.ruotsinKieli,
        yksilöllistettyOppimäärä = true,
        suorituskieli = ExampleData.suomenKieli,
        alaosasuorituskoodit = Some(List(
          "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
        )),
        arviointi = osasuoritusArviointi(
          päivä = alkamispäivä.plusDays(60),
          kuvaus = Some(LocalizedString.finnish("Du talar svenska bra")),
        ),
        alaosasuoritusArviointi = primaryAlaoppimisalueArviointi(
          arvosana = "1",
          päivä = alkamispäivä.plusDays(60)
        ),
      ),
    ))
  )

  def primaryLapsiOppimisalueenOsasuoritus(
    oppiainekoodi: String,
    arviointi: Option[List[EuropeanSchoolOfHelsinkiOsasuoritusArviointi]] = None,
    alaosasuorituskoodit: Option[List[String]] = None,
    alaosasuoritusArviointi: Option[List[PrimaryAlaoppimisalueArviointi]] = None
  ): PrimaryLapsiOppimisalueenSuoritus = {
    PrimaryLapsiOppimisalueenSuoritus(
      arviointi = arviointi,
      koulutusmoduuli = PrimaryLapsiOppimisalue(Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkilapsioppimisalue")),
      osasuoritukset = alaosasuorituskoodit.map(_.map(koodi => PrimaryLapsiOppimisalueenAlaosasuoritus(
        koulutusmoduuli = PrimaryLapsiAlaoppimisalue(
          tunniste = Koodistokoodiviite(koodi, "europeanschoolofhelsinkiprimarylapsialaoppimisalue")
        ),
        arviointi = alaosasuoritusArviointi
      )))
    )
  }

  def primaryOppimisalueenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    yksilöllistettyOppimäärä: Boolean = false,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[EuropeanSchoolOfHelsinkiOsasuoritusArviointi]] = None,
    alaosasuorituskoodit: Option[List[String]] = None,
    alaosasuoritusArviointi: Option[List[PrimaryAlaoppimisalueArviointi]] = None
  ): PrimaryOppimisalueenSuoritus = {
    PrimaryOppimisalueenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      yksilöllistettyOppimäärä = yksilöllistettyOppimäärä,
      suorituskieli = suorituskieli,
      arviointi = arviointi,
      osasuoritukset = alaosasuorituskoodit.map(_.map(koodi => PrimaryOppimisalueenAlaosasuoritus(
        koulutusmoduuli = PrimaryAlaoppimisalue(
          tunniste = Koodistokoodiviite(koodi, "europeanschoolofhelsinkiprimaryalaoppimisalue"),
        ),
        arviointi = alaosasuoritusArviointi
      )))
    )
  }

  def primaryOppimisalueenOsasuoritusKieli(
    oppiainekoodi: String,
    laajuus: Int = 2,
    yksilöllistettyOppimäärä: Boolean = false,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[EuropeanSchoolOfHelsinkiOsasuoritusArviointi]] = None,
    alaosasuoritusArviointi: Option[List[PrimaryAlaoppimisalueArviointi]] = None,
    alaosasuorituskoodit: Option[List[String]] = None
  ): PrimaryOppimisalueenSuoritus = {
    PrimaryOppimisalueenSuoritus(
      arviointi = arviointi,
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      yksilöllistettyOppimäärä = yksilöllistettyOppimäärä,
      suorituskieli = suorituskieli,
      osasuoritukset = alaosasuorituskoodit.map(_.map(koodi => PrimaryOppimisalueenAlaosasuoritus(
        koulutusmoduuli = PrimaryAlaoppimisalue(
          tunniste = Koodistokoodiviite(koodi, "europeanschoolofhelsinkiprimaryalaoppimisalue")
        ),
        arviointi = alaosasuoritusArviointi
      )))
    )
  }

  def osasuoritusArviointi(
    arvosana: String = "pass",
    kuvaus: Option[LocalizedString] = None,
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[EuropeanSchoolOfHelsinkiOsasuoritusArviointi]] = {
    Some(List(EuropeanSchoolOfHelsinkiOsasuoritusArviointi(
      arvosana = Koodistokoodiviite(arvosana, "arviointiasteikkoeuropeanschoolofhelsinkiosasuoritus"),
      kuvaus = kuvaus,
      päivä = Some(päivä),
      arvioitsijat = arvioitsijat
    )))
  }

  def primaryAlaoppimisalueArviointi(
    arvosana: String = "3",
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[PrimaryAlaoppimisalueArviointi]] = {
    Some(List(PrimaryAlaoppimisalueArviointi(
      arvosana = Koodistokoodiviite(arvosana, "arviointiasteikkoeuropeanschoolofhelsinkiprimarymark"),
      päivä = Some(päivä),
      arvioitsijat = arvioitsijat
    )))
  }

  def secondaryLowerSuoritus1(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = SecondaryLowerVuosiluokanSuoritus(
    koulutusmoduuli = SecondaryLowerLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    jääLuokalle = jääLuokalle,
    osasuoritukset = Some(List(
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L1",
        laajuus = 6,
        kieli = ExampleData.englanti,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L2",
        laajuus = 5,
        kieli = ExampleData.ranska,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L3",
        laajuus = 2,
        kieli = ExampleData.saksa,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "ONL",
        laajuus = 2,
        kieli = ExampleData.ruotsinKieli,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "HCLFI",
        laajuus = 2,
        kieli = ExampleData.suomenKieli,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MA",
        laajuus = 4,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "PE",
        laajuus = 3,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "RE",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "HUS",
        laajuus = 3,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "SC",
        laajuus = 4,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ART",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MU",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ICT",
        laajuus = 1,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
    ))
  )

  def secondaryLowerSuoritus2(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = SecondaryLowerVuosiluokanSuoritus(
    koulutusmoduuli = SecondaryLowerLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    jääLuokalle = jääLuokalle,
    osasuoritukset = Some(List(
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L1",
        laajuus = 5,
        kieli = ExampleData.englanti,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L2",
        laajuus = 4,
        kieli = ExampleData.ranska,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L3",
        laajuus = 3,
        kieli = ExampleData.saksa,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "ONL",
        laajuus = 2,
        kieli = ExampleData.ruotsinKieli,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "HCLFI",
        laajuus = 2,
        kieli = ExampleData.suomenKieli,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MA",
        laajuus = 4,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "PE",
        laajuus = 3,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "RE",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "HUS",
        laajuus = 3,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "SC",
        laajuus = 4,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ART",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MU",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ICT",
        laajuus = 1,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
    ))
  )

  def secondaryLowerSuoritus3(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = SecondaryLowerVuosiluokanSuoritus(
    koulutusmoduuli = SecondaryLowerLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    jääLuokalle = jääLuokalle,
    osasuoritukset = Some(List(
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L1",
        laajuus = 4,
        kieli = ExampleData.englanti,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L2",
        laajuus = 4,
        kieli = ExampleData.ranska,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L3",
        laajuus = 3,
        kieli = ExampleData.saksa,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "ONL",
        laajuus = 2,
        kieli = ExampleData.ruotsinKieli,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "HCLFI",
        laajuus = 2,
        kieli = ExampleData.suomenKieli,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MA",
        laajuus = 4,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "PE",
        laajuus = 3,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "RE",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "HUS",
        suorituskieli = ExampleData.ranska,
        laajuus = 3,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "SC",
        laajuus = 4,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ART",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MU",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ICT",
        laajuus = 1,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "HE",
        laajuus = 1,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
    ))
  )

  def secondaryLowerSuoritus45(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = SecondaryLowerVuosiluokanSuoritus(
    koulutusmoduuli = SecondaryLowerLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    jääLuokalle = jääLuokalle,
    osasuoritukset = Some(List(
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L1",
        laajuus = 4,
        kieli = ExampleData.englanti,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L2",
        laajuus = 3,
        kieli = ExampleData.ranska,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L3",
        laajuus = 3,
        kieli = ExampleData.saksa,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L4",
        laajuus = 4,
        kieli = ExampleData.viro,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "ONL",
        laajuus = 4,
        kieli = ExampleData.ruotsinKieli,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerKielioppiaineenOsasuoritus(
        oppiainekoodi = "L5",
        laajuus = 2,
        kieli = ExampleData.sloveeni,
        suorituskieli = ExampleData.englanti,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MA",
        laajuus = 6,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "PE",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "RE",
        laajuus = 1,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ART",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MU",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ICT",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "HE",
        laajuus = 1,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "BI",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "CH",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "PHY",
        laajuus = 2,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "GEO",
        laajuus = 2,
        suorituskieli = ExampleData.ranska,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "HI",
        laajuus = 2,
        suorituskieli = ExampleData.ranska,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "ECO",
        laajuus = 4,
        suorituskieli = ExampleData.ranska,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerKielioppiaineenOsasuoritusLatin(
        suorituskieli = ExampleData.englanti,
        laajuus = 4,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(4))
      ),
      secondaryLowerKielioppiaineenOsasuoritusAncientGreek(
        suorituskieli = ExampleData.englanti,
        laajuus = 4,
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(4))
      )
    ))
  )

  def secondaryLowerMuunOppiaineenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryLowerArviointi]] = None
  ): SecondaryLowerOppiaineenSuoritus = {
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryLowerKielioppiaineenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryLowerArviointi]] = None
  ): SecondaryLowerOppiaineenSuoritus = {
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryLowerKielioppiaineenOsasuoritusLatin(
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryLowerArviointi]] = None
  ): SecondaryLowerOppiaineenSuoritus = {
    val oppiainekoodi = "LA"
    val kieli = "LA"
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineLatin(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = Koodistokoodiviite(kieli, "kieli")
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryLowerKielioppiaineenOsasuoritusAncientGreek(
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryLowerArviointi]] = None
  ): SecondaryLowerOppiaineenSuoritus = {
    val oppiainekoodi = "GRC"
    val kieli = "EL"
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = Koodistokoodiviite(kieli, "kieli")
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }


  def secondaryGradeArviointi(
    arvosana: String = "B",
    kuvaus: Option[LocalizedString] = None,
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[SecondaryGradeArviointi]] = {
    Some(List(SecondaryGradeArviointi(
      arvosana = Koodistokoodiviite(arvosana, "arviointiasteikkoeuropeanschoolofhelsinkisecondarygrade"),
      kuvaus = kuvaus,
      päivä = Some(päivä),
      arvioitsijat = arvioitsijat
    )))
  }

  def secondaryUpperSuoritusS6(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = {
    SecondaryUpperVuosiluokanSuoritus(
      koulutusmoduuli = SecondaryUpperLuokkaAste(luokkaaste),
      luokka = Some(s"${luokkaaste}A"),
      alkamispäivä = Some(alkamispäivä),
      toimipiste = europeanSchoolOfHelsinki,
      vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
      jääLuokalle = jääLuokalle,
      osasuoritukset = Some(List(
        secondaryUpperKielioppiaineenOsasuoritusS6(
          oppiainekoodi = "L1",
          laajuus = 4,
          kieli = ExampleData.englanti,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS6(
          oppiainekoodi = "L2",
          laajuus = 3,
          kieli = ExampleData.ranska,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "MA",
          laajuus = 5,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "RE",
          laajuus = 1,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "PE",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "BI",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "HI",
          suorituskieli = ExampleData.ranska,
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "PH",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS6Latin(
          laajuus = 4,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS6AncientGreek(
          laajuus = 4,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "GEO",
          laajuus = 4,
          suorituskieli = ExampleData.ranska,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS6(
          oppiainekoodi = "L3",
          laajuus = 4,
          kieli = ExampleData.saksa,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS6(
          oppiainekoodi = "L4",
          laajuus = 4,
          kieli = ExampleData.viro,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS6(
          oppiainekoodi = "L5",
          laajuus = 4,
          kieli = ExampleData.sloveeni,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "PHY",
          laajuus = 4,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "CH",
          laajuus = 4,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "ART",
          laajuus = 4,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "MU",
          laajuus = 4,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS6(
          oppiainekoodi = "L1adv",
          laajuus = 3,
          kieli = ExampleData.englanti,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS6(
          oppiainekoodi = "L2adv",
          laajuus = 3,
          kieli = ExampleData.ranska,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "MAadv",
          laajuus = 3,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "PHYpra",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "CHpra",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "BIpra",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "ICT",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "SOC",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "ECO",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "POLSCI",
          laajuus = 2,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
      ))
    )
  }

  def secondaryUpperSuoritusS7(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = {
    SecondaryUpperVuosiluokanSuoritus(
      koulutusmoduuli = SecondaryUpperLuokkaAste(luokkaaste),
      luokka = Some(s"${luokkaaste}A"),
      alkamispäivä = Some(alkamispäivä),
      toimipiste = europeanSchoolOfHelsinki,
      vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
      jääLuokalle = jääLuokalle,
      osasuoritukset = Some(List(
        secondaryUpperKielioppiaineenOsasuoritusS7(
          oppiainekoodi = "L1",
          laajuus = 4,
          kieli = ExampleData.englanti,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS7(
          oppiainekoodi = "L2",
          laajuus = 3,
          kieli = ExampleData.ranska,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "MA",
          laajuus = 5,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "RE",
          laajuus = 1,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "PE",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "BI",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "HI",
          suorituskieli = ExampleData.ranska,
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "PH",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS7Latin(
          laajuus = 4,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS7AncientGreek(
          laajuus = 4,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "GEO",
          laajuus = 4,
          suorituskieli = ExampleData.ranska,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS7(
          oppiainekoodi = "L3",
          laajuus = 4,
          kieli = ExampleData.saksa,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS7(
          oppiainekoodi = "L4",
          laajuus = 4,
          kieli = ExampleData.viro,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS7(
          oppiainekoodi = "L5",
          laajuus = 4,
          kieli = ExampleData.sloveeni,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "PHY",
          laajuus = 4,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "CH",
          laajuus = 4,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "ART",
          laajuus = 4,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "MU",
          laajuus = 4,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS7(
          oppiainekoodi = "L1adv",
          laajuus = 3,
          kieli = ExampleData.englanti,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperKielioppiaineenOsasuoritusS7(
          oppiainekoodi = "L2adv",
          laajuus = 3,
          kieli = ExampleData.ranska,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "MAadv",
          laajuus = 3,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "PHYpra",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "CHpra",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "BIpra",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "ICT",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "SOC",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "ECO",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "POLSCI",
          laajuus = 2,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        ),
      ))
    )
  }

  def secondaryLowerArviointi(luokkaaste: String, päivä: LocalDate): Option[List[SecondaryLowerArviointi]] = {
    luokkaaste match {
      case "S4" | "S5" => secondaryNumericalMarkArviointi(
        päivä = päivä
      )
      case "S1" | "S2" | "S3" => secondaryGradeArviointi(
        päivä = päivä
      )
    }
  }

  def secondaryUpperMuunOppiaineenOsasuoritusS6(
    oppiainekoodi: String,
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryUpperKielioppiaineenOsasuoritusS6(
    oppiainekoodi: String,
    laajuus: Int = 2,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryUpperKielioppiaineenOsasuoritusS6Latin(
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    val oppiainekoodi = "LA"
    val kieli = Koodistokoodiviite("LA", "kieli")
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineLatin(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryUpperKielioppiaineenOsasuoritusS6AncientGreek(
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    val oppiainekoodi = "GRC"
    val kieli = Koodistokoodiviite("EL", "kieli")
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryUpperMuunOppiaineenOsasuoritusS7(
    oppiainekoodi: String,
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryS7PreliminaryMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = Some(List(
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponentti(
            Koodistokoodiviite("A", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        ),
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponentti(
            Koodistokoodiviite("B", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        ),
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponentti(
            Koodistokoodiviite("yearmark", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        )
      ))
    )
  }

  def secondaryUpperKielioppiaineenOsasuoritusS7(
    oppiainekoodi: String,
    laajuus: Int = 2,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryS7PreliminaryMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = Some(List(
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponentti(
            Koodistokoodiviite("A", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        ),
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponentti(
            Koodistokoodiviite("B", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        )
      ))
    )
  }

  def secondaryUpperKielioppiaineenOsasuoritusS7Latin(
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryS7PreliminaryMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    val oppiainekoodi = "LA"
    val kieli = Koodistokoodiviite("LA", "kieli")
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineLatin(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = Some(List(
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponentti(
            Koodistokoodiviite("yearmark", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        )
      ))
    )
  }

  def secondaryUpperKielioppiaineenOsasuoritusS7AncientGreek(
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryS7PreliminaryMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    val oppiainekoodi = "GRC"
    val kieli = Koodistokoodiviite("EL", "kieli")
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = Some(List(
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponentti(
            Koodistokoodiviite("yearmark", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        )
      ))
    )
  }

  def secondaryNumericalMarkArviointi(
    arvosana: String = "7.5",
    kuvaus: Option[LocalizedString] = None,
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[SecondaryNumericalMarkArviointi]] = {
    Some(List(SecondaryNumericalMarkArviointi(
      arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoeuropeanschoolofhelsinkinumericalmark"),
      kuvaus = kuvaus,
      päivä = Some(päivä),
      arvioitsijat = arvioitsijat
    )))
  }

  def secondaryS7PreliminaryMarkArviointi(
    arvosana: String = "8.9",
    kuvaus: Option[LocalizedString] = None,
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[SecondaryS7PreliminaryMarkArviointi]] = {
    Some(List(SecondaryS7PreliminaryMarkArviointi(
      arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark"),
      kuvaus = kuvaus,
      päivä = Some(päivä),
      arvioitsijat = arvioitsijat
    )))
  }

  def ebTutkinnonSuoritus(alkamispäivä: LocalDate) = {
    val vahvistusPäivä = alkamispäivä.plusYears(1).withMonth(6).withDayOfMonth(25)
    EBTutkinnonSuoritus(
      koulutusmoduuli = EBTutkinto(),
      luokka = Some(s"S7 EN"),
      toimipiste = europeanSchoolOfHelsinki,
      vahvistus = suoritusVahvistus(vahvistusPäivä),
      todistuksellaNäkyvätLisätiedot = Some(LocalizedString.finnish("The marks of Ethics/Religion are not considered for the calculation of the European Baccalaureate preliminary and final marks.")),
      yleisarvosana = Some(89.68),
      osasuoritukset = Some(List(
        EBTutkinnonOsasuoritus(
          koulutusmoduuli =  EuropeanSchoolOfHelsinkiMuuOppiaine(
            Koodistokoodiviite("MA", "europeanschoolofhelsinkimuuoppiaine"),
            laajuus = LaajuusVuosiviikkotunneissa(4)
          ),
          suorituskieli = ExampleData.englanti,
          osasuoritukset = Some(List(
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Preliminary", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoPreliminaryMarkArviointi(päivä = vahvistusPäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Written", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistusPäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistusPäivä)
            )
          ))
        ),
        EBTutkinnonOsasuoritus(
          koulutusmoduuli =  EuropeanSchoolOfHelsinkiKielioppiaine(
            Koodistokoodiviite("L1", "europeanschoolofhelsinkikielioppiaine"),
            laajuus = LaajuusVuosiviikkotunneissa(4),
            kieli = ExampleData.ranska
          ),
          suorituskieli = ExampleData.englanti,
          osasuoritukset = Some(List(
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Preliminary", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoPreliminaryMarkArviointi(päivä = vahvistusPäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Written", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistusPäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Oral", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistusPäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistusPäivä)
            )
          ))
        ),
        EBTutkinnonOsasuoritus(
          koulutusmoduuli =  EuropeanSchoolOfHelsinkiKielioppiaine(
            Koodistokoodiviite("L2", "europeanschoolofhelsinkikielioppiaine"),
            laajuus = LaajuusVuosiviikkotunneissa(4),
            kieli = ExampleData.saksa
          ),
          suorituskieli = ExampleData.englanti,
          osasuoritukset = Some(List(
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Preliminary", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoPreliminaryMarkArviointi(päivä = vahvistusPäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Written", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistusPäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Oral", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistusPäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistusPäivä)
            )
          ))
        ),
      ))
    )
  }

  def ebTutkintoPreliminaryMarkArviointi(
    arvosana: String = "8.6",
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[EBTutkintoPreliminaryMarkArviointi]] = {
    Some(List(EBTutkintoPreliminaryMarkArviointi(
      arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark"),
      päivä = Some(päivä),
      arvioitsijat = arvioitsijat
    )))
  }

  def ebTutkintoFinalMarkArviointi(
    arvosana: String = "8.67",
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[EBTutkintoFinalMarkArviointi]] = {
    Some(List(EBTutkintoFinalMarkArviointi(
      arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoeuropeanschoolofhelsinkifinalmark"),
      päivä = Some(päivä),
      arvioitsijat = arvioitsijat
    )))
  }



  def suoritusVahvistus(päivä: LocalDate) = ExampleData.vahvistusPaikkakunnalla(päivä, europeanSchoolOfHelsinki, helsinki)
}
