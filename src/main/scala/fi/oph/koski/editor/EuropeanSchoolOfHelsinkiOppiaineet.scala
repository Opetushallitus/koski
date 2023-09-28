package fi.oph.koski.editor

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._

case class EuropeanSchoolOfHelsinkiOppiaineet(koodistoViitePalvelu: KoodistoViitePalvelu) {
  def eshOsaSuoritukset(koulutusmoduulinTunniste: String): List[EuropeanSchoolOfHelsinkiOsasuoritus] = {
    koulutusmoduulinTunniste match {
      case "P1" | "P2" => osasuorituksetPrimary12
      case "P3" | "P4" | "P5" => osasuorituksetPrimary345
      case "S1" => osasuorituksetSecondary1
      case "S2" => osasuorituksetSecondary2
      case "S3" => osasuorituksetSecondary3
      case "S4" | "S5" => osasuorituksetSecondary45
      case "S6" => osasuorituksetSecondary6
      case "S7" => osasuorituksetSecondary7
      case _ => List()
    }
  }

  def makePrimaryOsasuoritusLista(osasuoritukset: List[PrimaryOsasuoritus], oppiainekoodi: String) = osasuoritukset.find(_.koulutusmoduuli.tunniste.koodiarvo == oppiainekoodi) match {
    case Some(primaryOsasuoritus: PrimaryOsasuoritus) => primaryOsasuoritus.osasuoritukset.getOrElse(List())
    case _ => List()
  }

  def makeSecondaryLowerOsasuoritusLista(osasuoritukset: List[SecondaryLowerOppiaineenSuoritus], oppiainekoodi: String) = osasuoritukset.find(_.koulutusmoduuli.tunniste.koodiarvo == oppiainekoodi) match {
    case Some(osasuoritus) => osasuoritus.osasuoritukset.getOrElse(List())
    case _ => List()
  }

  def eshAlaOsasuoritukset(koulutusmoduulinTunniste: String, oppiainekoodi: String): List[Suoritus] = {
    koulutusmoduulinTunniste match {
      case "P1" | "P2" => makePrimaryOsasuoritusLista(osasuorituksetPrimary12, oppiainekoodi)
      case "P3" | "P4" | "P5" => makePrimaryOsasuoritusLista(osasuorituksetPrimary345, oppiainekoodi)
      case "S7" => osasurituksetAB().getOrElse(List())
      case _ => List()
    }
  }

  private lazy val osasuorituksetPrimary12: List[PrimaryOsasuoritus] = List(
    primaryLapsiOppimisalueenOsasuoritus(
      oppiainekoodi = "TCAAL",
      alaosasuorituskoodit = Some(List(
        "Engages in learning", "Listens attentively", "Develops working habits", "Works independently", "Perseveres with difficult tasks", "Uses ICT", "Presents work carefully", "Produces quality homework"
      ))
    ),
    primaryLapsiOppimisalueenOsasuoritus(
      oppiainekoodi = "TCAAP",
      alaosasuorituskoodit = Some(List(
        "Seems happy at school", "Is self-confident", "Manages and expresses own feelings", "Evaluates own progress"
      ))

    ),
    primaryLapsiOppimisalueenOsasuoritus(
      oppiainekoodi = "TCAO",
      alaosasuorituskoodit = Some(List(
        "Respect class rules", "Respect school rules", "Cooperates with others", "Respects others", "Shows empathy"
      ))
    ),
    primaryOppimisalueenOsasuoritusKieli(
      oppiainekoodi = "L1",
      alaosasuorituskoodit = Some(List(
        "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
      ))
    ),
    primaryOppimisalueenOsasuoritusKieli(
      oppiainekoodi = "L2",
      alaosasuorituskoodit = Some(List(
        "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
      ))
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "MA",
      alaosasuorituskoodit = Some(List(
        "Numbers & Number system", "Calculation", "Measurement", "Shape and Space", "Data handling", "Problem solving"
      ))
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "DOW",
      alaosasuorituskoodit = Some(List(
        "Biological", "Technological", "Geographical", "Historical"
      ))
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "ART",
      alaosasuorituskoodit = Some(List(
        "Plastic and static visual arts", "The arts and entertainment"
      ))
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "MU",
      alaosasuorituskoodit = Some(List(
        "Music making & performing", "Listening and responding", "Composing"
      ))
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "PE",
      alaosasuorituskoodit = Some(List(
        "Individual activities", "Team activities", "Swimming"
      ))
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "RE",
      alaosasuorituskoodit = None,
    )
  )
  private lazy val osasuorituksetPrimary345: List[PrimaryOsasuoritus] = List(
    primaryLapsiOppimisalueenOsasuoritus(
      oppiainekoodi = "TCAAL",
      alaosasuorituskoodit = Some(List(
        "Engages in learning", "Listens attentively", "Develops working habits", "Works independently", "Perseveres with difficult tasks", "Uses ICT", "Presents work carefully", "Produces quality homework"
      )),
    ),
    primaryLapsiOppimisalueenOsasuoritus(
      oppiainekoodi = "TCAAP",
      alaosasuorituskoodit = Some(List(
        "Seems happy at school", "Is self-confident", "Manages and expresses own feelings", "Evaluates own progress"
      )),
    ),
    primaryLapsiOppimisalueenOsasuoritus(
      oppiainekoodi = "TCAO",
      alaosasuorituskoodit = Some(List(
        "Respect class rules", "Respect school rules", "Cooperates with others", "Respects others", "Shows empathy"
      )),
    ),
    primaryOppimisalueenOsasuoritusKieli(
      oppiainekoodi = "L1",
      alaosasuorituskoodit = Some(List(
        "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
      )),
    ),
    primaryOppimisalueenOsasuoritusKieli(
      oppiainekoodi = "L2",
      alaosasuorituskoodit = Some(List(
        "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
      )),
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "MA",
      alaosasuorituskoodit = Some(List(
        "Numbers & Number system", "Calculation", "Measurement", "Shape and Space", "Data handling", "Problem solving"
      )),
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "DOW",
      alaosasuorituskoodit = Some(List(
        "Biological", "Technological", "Geographical", "Historical"
      )),
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "ART",
      alaosasuorituskoodit = Some(List(
        "Plastic and static visual arts", "The arts and entertainment"
      )),
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "MU",
      alaosasuorituskoodit = Some(List(
        "Music making & performing", "Listening and responding", "Composing"
      )),
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "PE",
      alaosasuorituskoodit = Some(List(
        "Individual activities", "Team activities", "Swimming"
      )),
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "RE",
      alaosasuorituskoodit = None,
    ),
    primaryOppimisalueenOsasuoritus(oppiainekoodi = "EuH",
      alaosasuorituskoodit = Some(List(
        "Communicating and working with others"
      )),
    ),
    primaryOppimisalueenOsasuoritusKieli(
      oppiainekoodi = "ONL",
      alaosasuorituskoodit = Some(List(
        "Listening and understanding", "Speaking", "Reading and understanding", "Writing", "Linguistic development"
      )),
    )
  )
  private lazy val osasuorituksetSecondary1: List[SecondaryLowerOppiaineenSuoritus] = List(
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L1",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L2",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L3",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "ONL",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "HCLFI",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "MA",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "PE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "RE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "HUS",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "SC",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ART",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "MU",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ICT",
    ),
  )
  private lazy val osasuorituksetSecondary2: List[SecondaryLowerOppiaineenSuoritus] = List(
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L1",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L2",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L3",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "ONL",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "HCLFI",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "MA",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "PE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "RE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "HUS",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "SC",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ART",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "MU",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ICT",
    ),
  )
  private lazy val osasuorituksetSecondary3: List[SecondaryLowerOppiaineenSuoritus] = List(
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L1",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L2",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L3",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "ONL",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "HCLFI",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "MA",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "PE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "RE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "HUS",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "SC",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ART",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "MU",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ICT",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "HE",
    ),
  )
  private lazy val osasuorituksetSecondary45: List[SecondaryLowerOppiaineenSuoritus] = List(
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L1",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L2",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L3",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "L4",
    ),
    secondaryLowerKielioppiaineenOsasuoritus(
      oppiainekoodi = "ONL",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "MA",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "PE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "RE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ART",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "MU",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ICT",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "HE",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "BI",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "CH",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "PHY",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "GEO",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "HI",
    ),
    secondaryLowerMuunOppiaineenOsasuoritus(
      oppiainekoodi = "ECO",
    ),
    secondaryLowerKielioppiaineenOsasuoritusLatin(
    ),
    secondaryLowerKielioppiaineenOsasuoritusAncientGreek(
    )
  )
  private lazy val osasuorituksetSecondary6: List[SecondaryUpperOppiaineenSuoritus] = List(
    secondaryUpperKielioppiaineenOsasuoritusS6(
      oppiainekoodi = "L1",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS6(
      oppiainekoodi = "L2",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "MA",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "RE",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "PE",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "BI",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "HI",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "PH",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS6Latin(
    ),
    secondaryUpperKielioppiaineenOsasuoritusS6AncientGreek(
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "GEO",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS6(
      oppiainekoodi = "L3",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS6(
      oppiainekoodi = "L4",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "PHY",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "CH",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "ART",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "MU",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS6(
      oppiainekoodi = "L1adv",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS6(
      oppiainekoodi = "L2adv",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "MAadv",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "PHYpra",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "CHpra",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "BIpra",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "ICT",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "SOC",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "ECO",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS6(
      oppiainekoodi = "POLSCI",
    ),
  )
  private lazy val osasuorituksetSecondary7: List[SecondaryUpperOppiaineenSuoritus] = List(
    secondaryUpperKielioppiaineenOsasuoritusS7(
      oppiainekoodi = "L1",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS7(
      oppiainekoodi = "L2",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "MA",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "RE",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "PE",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "BI",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "HI",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "PH",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS7Latin(
    ),
    secondaryUpperKielioppiaineenOsasuoritusS7AncientGreek(
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "GEO",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS7(
      oppiainekoodi = "L3",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS7(
      oppiainekoodi = "L4",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "PHY",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "CH",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "ART",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "MU",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS7(
      oppiainekoodi = "L1adv",
    ),
    secondaryUpperKielioppiaineenOsasuoritusS7(
      oppiainekoodi = "L2adv",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "MAadv",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "PHYpra",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "CHpra",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "BIpra",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "ICT",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "SOC",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "ECO",
    ),
    secondaryUpperMuunOppiaineenOsasuoritusS7(
      oppiainekoodi = "POLSCI",
    ),
  )

  private def primaryLapsiOppimisalueenOsasuoritus(
                                                    oppiainekoodi: String,
                                                    alaosasuorituskoodit: Option[List[String]] = None,
                                                  ): PrimaryLapsiOppimisalueenSuoritus = {
    PrimaryLapsiOppimisalueenSuoritus(
      arviointi = None,
      koulutusmoduuli = PrimaryLapsiOppimisalue(lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkilapsioppimisalue")),
      osasuoritukset = alaosasuorituskoodit.map(_.map(koodi => PrimaryLapsiOppimisalueenAlaosasuoritus(
        koulutusmoduuli = PrimaryLapsiAlaoppimisalue(
          tunniste = lokalisoituKoodi(koodi, "europeanschoolofhelsinkiprimarylapsialaoppimisalue")
        ),
        arviointi = None
      )))
    )
  }

  private def primaryOppimisalueenOsasuoritus(
                                               oppiainekoodi: String,
                                               alaosasuorituskoodit: Option[List[String]] = None,
                                             ): PrimaryOppimisalueenSuoritus = {
    PrimaryOppimisalueenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = laajuus
      ),
      yksilöllistettyOppimäärä = false,
      suorituskieli = suorituskieli,
      arviointi = None,
      osasuoritukset = alaosasuorituskoodit.map(_.map(koodi => PrimaryOppimisalueenAlaosasuoritus(
        koulutusmoduuli = PrimaryAlaoppimisalue(
          tunniste = lokalisoituKoodi(koodi, "europeanschoolofhelsinkiprimaryalaoppimisalue"),
        ),
        arviointi = None
      )))
    )
  }

  private def primaryOppimisalueenOsasuoritusKieli(
                                                    oppiainekoodi: String,
                                                    alaosasuorituskoodit: Option[List[String]] = None
                                                  ): PrimaryOppimisalueenSuoritus = {
    PrimaryOppimisalueenSuoritus(
      arviointi = None,
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = kieli,
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = alaosasuorituskoodit.map(_.map(koodi => PrimaryOppimisalueenAlaosasuoritus(
        koulutusmoduuli = PrimaryAlaoppimisalue(
          tunniste = lokalisoituKoodi(koodi, "europeanschoolofhelsinkiprimaryalaoppimisalue")
        ),
        arviointi = None
      )))
    )
  }

  private def kieli: Koodistokoodiviite =
    lokalisoituKoodi("99", "kieli")

  private def secondaryLowerMuunOppiaineenOsasuoritus(
                                                       oppiainekoodi: String,
                                                     ): SecondaryLowerOppiaineenSuoritus = {
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = laajuus
      ),
      suorituskieli = suorituskieli,
      arviointi = None
    )
  }

  private def secondaryLowerKielioppiaineenOsasuoritus(
                                                        oppiainekoodi: String,
                                                      ): SecondaryLowerOppiaineenSuoritus = {
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = kieli,
      ),
      suorituskieli = suorituskieli,
      arviointi = None
    )
  }

  private def secondaryLowerKielioppiaineenOsasuoritusLatin(
                                                           ): SecondaryLowerOppiaineenSuoritus = {
    val oppiainekoodi = "LA"
    val kieli = "LA"
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineLatin(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = lokalisoituKoodi(kieli, "kieli")
      ),
      suorituskieli = suorituskieli,
      arviointi = None
    )
  }

  private def secondaryLowerKielioppiaineenOsasuoritusAncientGreek(
                                                                  ): SecondaryLowerOppiaineenSuoritus = {
    val oppiainekoodi = "GRC"
    val kieli = "EL"
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = lokalisoituKoodi(kieli, "kieli")
      ),
      suorituskieli = suorituskieli,
      arviointi = None
    )
  }

  private def secondaryUpperMuunOppiaineenOsasuoritusS6(
                                                         oppiainekoodi: String,
                                                       ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = laajuus
      ),
      suorituskieli = suorituskieli,
      arviointi = None
    )
  }

  private def secondaryUpperKielioppiaineenOsasuoritusS6(
                                                          oppiainekoodi: String,
                                                        ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = kieli,
      ),
      suorituskieli = suorituskieli,
      arviointi = None
    )
  }

  private def secondaryUpperKielioppiaineenOsasuoritusS6Latin(
                                                             ): SecondaryUpperOppiaineenSuoritus = {
    val oppiainekoodi = "LA"
    val kieli = lokalisoituKoodi("LA", "kieli")
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineLatin(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = kieli,
      ),
      suorituskieli = suorituskieli,
      arviointi = None
    )
  }

  private def secondaryUpperKielioppiaineenOsasuoritusS6AncientGreek(
                                                                    ): SecondaryUpperOppiaineenSuoritus = {
    val oppiainekoodi = "GRC"
    val kieli = lokalisoituKoodi("EL", "kieli")
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      arviointi = None
    )
  }

  private def secondaryUpperMuunOppiaineenOsasuoritusS7(
                                                         oppiainekoodi: String,
                                                       ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = laajuus
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = osasurituksetAB
    )
  }

  private def secondaryUpperKielioppiaineenOsasuoritusS7(
                                                          oppiainekoodi: String,
                                                        ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = osasurituksetAB
    )
  }

  private def secondaryUpperKielioppiaineenOsasuoritusS7Latin(
                                                             ): SecondaryUpperOppiaineenSuoritus = {
    val oppiainekoodi = "LA"
    val kieli = lokalisoituKoodi("LA", "kieli")
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineLatin(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = osasurituksetAB
    )
  }

  private def osasurituksetAB(): Option[List[S7OppiaineenAlaosasuoritus]] = Some(List(
    S7OppiaineenAlaosasuoritus(
      koulutusmoduuli = S7OppiaineKomponentti(
        lokalisoituKoodi("A", "europeanschoolofhelsinkis7oppiaineenkomponentti")
      ),
      arviointi = None
    ),
    S7OppiaineenAlaosasuoritus(
      koulutusmoduuli = S7OppiaineKomponentti(
        lokalisoituKoodi("B", "europeanschoolofhelsinkis7oppiaineenkomponentti")
      ),
      arviointi = None
    )
  ))

  private def lokalisoituKoodi(arvo: String, koodisto: String) = koodistoViitePalvelu.validateRequired(koodisto, arvo)

  private def laajuus: LaajuusVuosiviikkotunneissa = LaajuusVuosiviikkotunneissa(1)

  private def suorituskieli: Koodistokoodiviite =
    lokalisoituKoodi("99", "kieli")

  private def secondaryUpperKielioppiaineenOsasuoritusS7AncientGreek(
                                                                    ): SecondaryUpperOppiaineenSuoritus = {
    val oppiainekoodi = "GRC"
    val kieli = lokalisoituKoodi("EL", "kieli")
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(
        lokalisoituKoodi(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = laajuus,
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = osasurituksetAB
    )
  }
}
