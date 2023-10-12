package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

import java.time.LocalDate

object ExamplesEB {
  val alkamispäivä = ExamplesEuropeanSchoolOfHelsinki.alkamispäivä.plusYears(19).withMonth(6).withDayOfMonth(14)
  val päättymispäivä = alkamispäivä.plusDays(1)

  val eb = ebTutkinnonSuoritus(päättymispäivä, EuropeanSchoolOfHelsinkiExampleData.suoritusVahvistus(päättymispäivä))

  val opiskeluoikeus =
    ebOpiskeluoikeus(
      alkamispäivä = alkamispäivä,
      arviointipäivä = päättymispäivä,
      vahvistuspäivä = Some(päättymispäivä),
      päättymispäiväJaTila = Some((päättymispäivä, LukioExampleData.opiskeluoikeusPäättynyt))
    )

  def ebOpiskeluoikeus(
    alkamispäivä: LocalDate,
    arviointipäivä: LocalDate,
    vahvistuspäivä: Option[LocalDate] = None,
    päättymispäiväJaTila: Option[(LocalDate, Koodistokoodiviite)]
  ) = {
    val päättymispäivä = päättymispäiväJaTila.map(_._1)
    val päättymistila = päättymispäiväJaTila.map(_._2)

    val vahvistus = vahvistuspäivä.map(pvm => EuropeanSchoolOfHelsinkiExampleData.suoritusVahvistus(pvm)).flatten

    val tila = EBOpiskeluoikeudenTila(
      List(EBOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen)) ++
      (
        päättymispäivä match {
          case Some(pvm) => List(EBOpiskeluoikeusjakso(pvm, päättymistila.get))
          case _ => List.empty
        }
      )
    )

    EBOpiskeluoikeus(
      oppilaitos = Some(EuropeanSchoolOfHelsinkiExampleData.europeanSchoolOfHelsinki),
      tila = tila,
      suoritukset = List(
        ebTutkinnonSuoritus(arviointipäivä, vahvistus)
      )
    )
  }

  def ebTutkinnonSuoritus(arviointipäivä: LocalDate, vahvistus: Option[HenkilövahvistusPaikkakunnalla]) = {
    EBTutkinnonSuoritus(
      koulutusmoduuli = EBTutkinto(),
      toimipiste = EuropeanSchoolOfHelsinkiExampleData.europeanSchoolOfHelsinki,
      vahvistus = vahvistus,
      todistuksellaNäkyvätLisätiedot = Some(LocalizedString.finnish("The marks of Ethics/Religion are not considered for the calculation of the European Baccalaureate preliminary and final marks.")),
      yleisarvosana = Some(89.68),
      osasuoritukset = Some(List(
        EBTutkinnonOsasuoritus(
          koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
            Koodistokoodiviite("MA", "europeanschoolofhelsinkimuuoppiaine"),
            laajuus = LaajuusVuosiviikkotunneissa(4)
          ),
          suorituskieli = ExampleData.englanti,
          osasuoritukset = Some(List(
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Written", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = arviointipäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = arviointipäivä)
            )
          ))
        ),
        EBTutkinnonOsasuoritus(
          koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
            Koodistokoodiviite("L1", "europeanschoolofhelsinkikielioppiaine"),
            laajuus = LaajuusVuosiviikkotunneissa(4),
            kieli = ExampleData.ranska
          ),
          suorituskieli = ExampleData.englanti,
          osasuoritukset = Some(List(
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Written", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = arviointipäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Oral", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = arviointipäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = arviointipäivä)
            )
          ))
        ),
        EBTutkinnonOsasuoritus(
          koulutusmoduuli = EuropeanSchoolOfHelsinkiKielioppiaine(
            Koodistokoodiviite("L2", "europeanschoolofhelsinkikielioppiaine"),
            laajuus = LaajuusVuosiviikkotunneissa(4),
            kieli = ExampleData.saksa
          ),
          suorituskieli = ExampleData.englanti,
          osasuoritukset = Some(List(
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Written", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = arviointipäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Oral", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = arviointipäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = arviointipäivä)
            )
          ))
        ),
      ))
    )
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


  val examples = List(
    Example("EB-tutkinto", "EB-tutkinto", Oppija(asUusiOppija(KoskiSpecificMockOppijat.europeanSchoolOfHelsinki), List(opiskeluoikeus)))
  )
}
