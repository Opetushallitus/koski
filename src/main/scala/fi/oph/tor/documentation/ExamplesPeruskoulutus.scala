package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._
import ExampleData._
import PeruskoulutusExampleData._

object PeruskoulutusExampleData {
  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(KoodistoKoodiViite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
  lazy val tilaValmis: KoodistoKoodiViite = KoodistoKoodiViite(koodistoUri = "suorituksentila", koodiarvo = "VALMIS")

  def suoritus(aine: String) = PeruskoulunOppiaineSuoritus(
    koulutusmoduuli = oppiaine(aine),
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    alkamispäivä = None,
    toimipiste = jyväskylänNormaalikoulu,
    arviointi = None,
    vahvistus = None
  )

  def oppiaine(koodiarvo: String) = Oppiaine(tunniste = KoodistoKoodiViite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = koodiarvo))
}

object ExamplesPeruskoulutus {
  val uusi = TorOppija(
    exampleHenkilö,
    List(PeruskouluOpiskeluOikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu,
      suoritukset = Nil,
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
          Opiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
        )
      )),
      läsnäolotiedot = None
    ))
  )

  val päättötodistus = TorOppija(
    exampleHenkilö,
    List(PeruskouluOpiskeluOikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2007, 8, 15)),
      arvioituPäättymispäivä = Some(date(2016, 6, 4)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu,
      suoritukset = List(
        PeruskoulunPäättötodistus(
          koulutusmoduuli = Peruskoulutus(),
          paikallinenId = None,
          suorituskieli = None,
          tila = tilaValmis,
          alkamispäivä = None,
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = None,
          vahvistus = Some(Vahvistus(Some(date(2016, 6, 4)))),
          osasuoritukset = Some(
            List(
              suoritus("HI").copy(vahvistus = Some(Vahvistus(Some(date(2016, 6, 4)))))
            ))
        )),
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2007, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusAktiivinen, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
          Opiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusPäättynyt, None)
        )
      )),
      läsnäolotiedot = None
    ))
  )

  val examples = List(
    Example("peruskoulutus - uusi", "Uusi oppija lisätään suorittamaan peruskoulua", uusi),
    Example("peruskoulutus - päättötodistus", "Oppija on saanut peruskoulun päättötodistuksen", päättötodistus)
  )
}
