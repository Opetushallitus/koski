package fi.oph.koski.documentation

import fi.oph.koski.documentation.ExampleData.{englanti, helsinki, laajuusVuosiviikkotunneissa}
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
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle
  )

  def primarySuoritus(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = PrimaryVuosiluokanSuoritus(
    koulutusmoduuli = PrimaryLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle,
    osasuoritukset = Some(List(
      primaryLapsiOppimisalueenOsasuoritus(
        oppiainekoodi = "TCAAL",
        arviointi = primaryArviointi(
          päivä = alkamispäivä.plusDays(30),
          kuvaus = Some(LocalizedString.finnish("hyvää työtä!"))
        )
      ),
      primaryOppimisalueenOsasuoritusKieli(
        oppiainekoodi = "L1",
        kieli = ExampleData.sloveeni,
        suorituskieli = ExampleData.englanti,
        arviointi = primaryArviointi(
          arvosana = "fail",
          kuvaus = Some(LocalizedString.finnish("Parempi onni ensi kerralla")),
          päivä = alkamispäivä.plusDays(60)
        )
      ),
      primaryOppimisalueenOsasuoritus(oppiainekoodi = "MA",
        arviointi = primaryArviointi(
          arvosana = "pass",
          päivä = alkamispäivä.plusDays(65)
        )
      )
      // TODO: TOR-1685 Lisää esimerkkejä, kun oikeat oppiainekoodit selvillä, tee myös erilaiset eri luokka-asteille
    ))
  )

  def primaryLapsiOppimisalueenOsasuoritus(oppiainekoodi: String, arviointi: Option[List[PrimaryArviointi]] = None): PrimaryLapsiOppimisalueenOsasuoritus = {
    PrimaryLapsiOppimisalueenOsasuoritus(
      koulutusmoduuli = PrimaryLapsiOppimisalue(Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkilapsioppimisalue")),
      arviointi = arviointi
    )
  }

  def primaryOppimisalueenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[PrimaryArviointi]] = None
  ): PrimaryOppimisalueenOsasuoritus = {
    PrimaryOppimisalueenOsasuoritus(
      koulutusmoduuli = PrimaryMuuOppimisalue(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def primaryOppimisalueenOsasuoritusKieli(
    oppiainekoodi: String,
    laajuus: Int = 2,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[PrimaryArviointi]] = None
  ): PrimaryOppimisalueenOsasuoritus = {
    PrimaryOppimisalueenOsasuoritus(
      koulutusmoduuli = PrimaryKieliOppimisalue(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def primaryArviointi(
    arvosana: String = "pass",
    kuvaus: Option[LocalizedString] = None,
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[PrimaryArviointi]] = {
    Some(List(PrimaryArviointi(
      arvosana = Koodistokoodiviite(arvosana, "arviointiasteikkoeuropeanschoolofhelsinkiprimarymark"),
      kuvaus = kuvaus,
      päivä = päivä,
      arvioitsijat = arvioitsijat
    )))
  }

  def secondaryLowerSuoritus(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = SecondaryLowerVuosiluokanSuoritus(
    koulutusmoduuli = SecondaryLowerLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle,
    osasuoritukset = Some(List(
      secondaryLowerMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MA",
        arviointi = secondaryArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerKieliOppiaineenOsasuoritus(
        oppiainekoodi = "L2",
        arviointi = secondaryArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      )
    ))
    // TODO: TOR-1685 Lisää esimerkkejä, kun oikeat oppiainekoodit selvillä, tee myös erilaiset eri luokka-asteille
  )


  def secondaryLowerMuunOppiaineenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryArviointi]] = None
  ): SecondaryLowerOppiaineenSuoritus = {
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = SecondaryLowerMuuOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryLowerKieliOppiaineenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryArviointi]] = None
  ): SecondaryLowerOppiaineenSuoritus = {
    SecondaryLowerOppiaineenSuoritus(
      koulutusmoduuli = SecondaryLowerKieliOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
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
      päivä = päivä,
      arvioitsijat = arvioitsijat
    )))
  }

  def secondaryUpperSuoritus(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = SecondaryUpperVuosiluokanSuoritus(
    koulutusmoduuli = SecondaryUpperLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle,
    osasuoritukset = Some(List(
      secondaryUpperMuunOppiaineenOsasuoritus(
        oppiainekoodi = "PE",
        arviointi = secondaryArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      ),
      secondaryUpperKieliOppiaineenOsasuoritus(
        oppiainekoodi = "L1",
        arviointi = secondaryArviointi(luokkaaste, alkamispäivä.plusMonths(4))
      ),
      secondaryUpperMuunOppiaineenOsasuoritus(
        oppiainekoodi = "MA",
        arviointi = secondaryArviointi(luokkaaste, alkamispäivä.plusMonths(5))
      )
    ))
    // TODO: TOR-1685 Lisää esimerkkejä, kun oikeat oppiainekoodit selvillä, tee myös erilaiset eri luokka-asteille
  )

  def secondaryUpperSuoritusFinal(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) =
    secondaryUpperSuoritus(luokkaaste, alkamispäivä, jääLuokalle).copy(
      osasuoritukset = Some(List(
        secondaryUpperMuunOppiaineenOsasuoritus(
          oppiainekoodi = "PE",
          arviointi = secondaryS7FinalMarkArviointi(päivä = alkamispäivä.plusMonths(3))
        ),
        secondaryUpperKieliOppiaineenOsasuoritus(
          oppiainekoodi = "L1",
          arviointi = secondaryS7FinalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritus(
          oppiainekoodi = "MA",
          arviointi = secondaryS7FinalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        )
      ))
    )
  // TODO: TOR-1685 Lisää esimerkkejä, kun oikeat oppiainekoodit selvillä, tee myös erilaiset eri luokka-asteille

  def secondaryArviointi(luokkaaste: String, päivä: LocalDate): Option[List[SecondaryArviointi]] = {
    luokkaaste match {
      case "S7" => secondaryS7PreliminaryMarkArviointi(
        päivä = päivä
      )
      case "S4" | "S5" | "S6" => secondaryNumericalMarkArviointi(
        päivä = päivä
      )
      case _ => secondaryGradeArviointi(
        päivä = päivä
      )
    }
  }

  def secondaryUpperMuunOppiaineenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritus(
      koulutusmoduuli = SecondaryUpperMuuOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryUpperKieliOppiaineenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritus(
      koulutusmoduuli = SecondaryUpperKieliOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryNumericalMarkArviointi(
    arvosana: String = "7.5",
    kuvaus: Option[LocalizedString] = None,
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[SecondaryArviointi]] = {
    Some(List(SecondaryNumericalMarkArviointi(
      arvosana = SynteettinenKoodiviite(koodiarvo = arvosana, koodistoUri = "esh/numericalmark"),
      kuvaus = kuvaus,
      päivä = päivä,
      arvioitsijat = arvioitsijat
    )))
  }

  def secondaryS7PreliminaryMarkArviointi(
    arvosana: String = "8.9",
    kuvaus: Option[LocalizedString] = None,
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[SecondaryArviointi]] = {
    Some(List(SecondaryS7PreliminaryMarkArviointi(
      arvosana = SynteettinenKoodiviite(koodiarvo = arvosana, koodistoUri = "esh/s7preliminarymark"),
      kuvaus = kuvaus,
      päivä = päivä,
      arvioitsijat = arvioitsijat
    )))
  }

  def secondaryS7FinalMarkArviointi(
    arvosana: String = "6.48",
    kuvaus: Option[LocalizedString] = None,
    arvioitsijat: Option[List[Arvioitsija]] = Some(List(Arvioitsija("Pekka Paunanen"))),
    päivä: LocalDate
  ): Option[List[SecondaryArviointi]] = {
    Some(List(SecondaryS7FinalMarkArviointi(
      arvosana = SynteettinenKoodiviite(koodiarvo = arvosana, koodistoUri = "esh/s7finalmark"),
      kuvaus = kuvaus,
      päivä = päivä,
      arvioitsijat = arvioitsijat
    )))
  }

  def suoritusVahvistus(päivä: LocalDate) = ExampleData.vahvistusPaikkakunnalla(päivä, europeanSchoolOfHelsinki, helsinki)
}
