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
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle
  )

  def primarySuoritus(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false, todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None) = PrimaryVuosiluokanSuoritus(
    koulutusmoduuli = PrimaryLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle,
    todistuksellaNäkyvätLisätiedot = todistuksellaNäkyvätLisätiedot,
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
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(2))
      ),
      secondaryLowerKieliOppiaineenOsasuoritus(
        oppiainekoodi = "L2",
        arviointi = secondaryLowerArviointi(luokkaaste, alkamispäivä.plusMonths(3))
      )
    ))
    // TODO: TOR-1685 Lisää esimerkkejä, kun oikeat oppiainekoodit selvillä, tee myös erilaiset eri luokka-asteille
  )


  def secondaryLowerMuunOppiaineenOsasuoritus(
    oppiainekoodi: String,
    laajuus: Int = 2,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryLowerArviointi]] = None
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
    arviointi: Option[List[SecondaryLowerArviointi]] = None
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

  def secondaryUpperSuoritusS6(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = {
    SecondaryUpperVuosiluokanSuoritus(
      koulutusmoduuli = SecondaryUpperLuokkaAste(luokkaaste),
      luokka = Some(s"${luokkaaste}A"),
      alkamispäivä = Some(alkamispäivä),
      toimipiste = europeanSchoolOfHelsinki,
      vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
      suorituskieli = ExampleData.englanti,
      jääLuokalle = jääLuokalle,
      osasuoritukset = Some(List(
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "PE",
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(3))
        ),
        secondaryUpperKieliOppiaineenOsasuoritusS6(
          oppiainekoodi = "L1",
          kieli = sloveeni,
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS6(
          oppiainekoodi = "MA",
          arviointi = secondaryNumericalMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        )
      ))
      // TODO: TOR-1685 Lisää esimerkkejä, kun oikeat oppiainekoodit selvillä, tee myös erilaiset eri luokka-asteille
    )
  }

  def secondaryUpperSuoritusS7(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = {
    SecondaryUpperVuosiluokanSuoritus(
      koulutusmoduuli = SecondaryUpperLuokkaAste(luokkaaste),
      luokka = Some(s"${luokkaaste}A"),
      alkamispäivä = Some(alkamispäivä),
      toimipiste = europeanSchoolOfHelsinki,
      vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
      suorituskieli = ExampleData.englanti,
      jääLuokalle = jääLuokalle,
      osasuoritukset = Some(List(
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "PE",
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(3))
        ),
        secondaryUpperKieliOppiaineenOsasuoritusS7(
          oppiainekoodi = "L1",
          kieli = sloveeni,
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(4))
        ),
        secondaryUpperMuunOppiaineenOsasuoritusS7(
          oppiainekoodi = "MA",
          arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(5))
        )
      ))
      // TODO: TOR-1685 Lisää esimerkkejä, kun oikeat oppiainekoodit selvillä, tee myös erilaiset eri luokka-asteille
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
      koulutusmoduuli = SecondaryUpperMuuOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      suorituskieli = suorituskieli,
      arviointi = arviointi
    )
  }

  def secondaryUpperKieliOppiaineenOsasuoritusS6(
    oppiainekoodi: String,
    laajuus: Int = 2,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryNumericalMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS6(
      koulutusmoduuli = SecondaryUpperKieliOppiaine(
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
      koulutusmoduuli = SecondaryUpperMuuOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkimuuoppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus)
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = Some(List(
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponenttiA(
            Koodistokoodiviite("A", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        ),
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponenttiB(
            Koodistokoodiviite("B", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        ),
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponenttiYearMark(
            Koodistokoodiviite("yearmark", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        )
      ))
    )
  }

  def secondaryUpperKieliOppiaineenOsasuoritusS7(
    oppiainekoodi: String,
    laajuus: Int = 2,
    kieli: Koodistokoodiviite = ExampleData.englanti,
    suorituskieli: Koodistokoodiviite = ExampleData.englanti,
    arviointi: Option[List[SecondaryS7PreliminaryMarkArviointi]] = None
  ): SecondaryUpperOppiaineenSuoritus = {
    SecondaryUpperOppiaineenSuoritusS7(
      koulutusmoduuli = SecondaryUpperKieliOppiaine(
        Koodistokoodiviite(oppiainekoodi, "europeanschoolofhelsinkikielioppiaine"),
        laajuus = LaajuusVuosiviikkotunneissa(laajuus),
        kieli = kieli
      ),
      suorituskieli = suorituskieli,
      osasuoritukset = Some(List(
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponenttiA(
            Koodistokoodiviite("A", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        ),
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponenttiB(
            Koodistokoodiviite("B", "europeanschoolofhelsinkis7oppiaineenkomponentti")
          ),
          arviointi = arviointi
        ),
        S7OppiaineenAlaosasuoritus(
          koulutusmoduuli = S7OppiaineKomponenttiYearMark(
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
  ): Option[List[SecondaryS7PreliminaryMarkArviointi]] = {
    Some(List(SecondaryS7PreliminaryMarkArviointi(
      arvosana = SynteettinenKoodiviite(koodiarvo = arvosana, koodistoUri = "esh/s7preliminarymark"),
      kuvaus = kuvaus,
      päivä = päivä,
      arvioitsijat = arvioitsijat
    )))
  }

  def suoritusVahvistus(päivä: LocalDate) = ExampleData.vahvistusPaikkakunnalla(päivä, europeanSchoolOfHelsinki, helsinki)
}
