package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

import java.time.LocalDate

object ExamplesEB {
  val alkamispäivä = ExamplesEuropeanSchoolOfHelsinki.alkamispäivä.plusYears(19).withMonth(6).withDayOfMonth(14)
  val päättymispäivä = alkamispäivä.plusDays(1)

  val eb = ebTutkinnonSuoritus(päättymispäivä)

  val opiskeluoikeus = EBOpiskeluoikeus(
      oppilaitos = Some(EuropeanSchoolOfHelsinkiExampleData.europeanSchoolOfHelsinki),
      tila = EBOpiskeluoikeudenTila(
        List(
          EBOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          EBOpiskeluoikeusjakso(päättymispäivä, LukioExampleData.opiskeluoikeusPäättynyt)
        )
      ),
      suoritukset = List(
        eb
      )
    )

  def ebTutkinnonSuoritus(vahvistuspäivä: LocalDate) = {
    EBTutkinnonSuoritus(
      koulutusmoduuli = EBTutkinto(),
      toimipiste = EuropeanSchoolOfHelsinkiExampleData.europeanSchoolOfHelsinki,
      vahvistus = EuropeanSchoolOfHelsinkiExampleData.suoritusVahvistus(vahvistuspäivä),
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
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistuspäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistuspäivä)
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
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistuspäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Oral", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistuspäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistuspäivä)
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
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistuspäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Oral", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistuspäivä)
            ),
            EBOppiaineenAlaosasuoritus(
              koulutusmoduuli = EBOppiaineKomponentti(
                tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
              ),
              arviointi = ebTutkintoFinalMarkArviointi(päivä = vahvistuspäivä)
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
