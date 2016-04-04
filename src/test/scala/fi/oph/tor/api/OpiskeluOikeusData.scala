package fi.oph.tor.api

import java.time.LocalDate
import fi.oph.tor.json.Json._
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._
import org.json4s.JValue

trait OpiskeluOikeusData {
  val defaultHenkilö = NewHenkilö("010101-123N", "Testi", "Testi", "Toivola")

  def makeOppija(henkilö: Henkilö = defaultHenkilö, opiskeluOikeudet: List[AnyRef] = List(opiskeluoikeus())): JValue = toJValue(Map(
    "henkilö" -> henkilö,
    "opiskeluoikeudet" -> opiskeluOikeudet
  ))

  val autoalanPerustutkinto: TutkintoKoulutus = TutkintoKoulutus(KoodistoKoodiViite("351301", "koulutus"), Some("39/011/2014"))

  lazy val tutkintoSuoritus: AmmatillinenTutkintoSuoritus = AmmatillinenTutkintoSuoritus(
    koulutusmoduuli = autoalanPerustutkinto,
    tutkintonimike = None,
    osaamisala = None,
    suoritustapa = None,
    järjestämismuoto = None,
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaKesken,
    alkamispäivä = None,
    toimipiste = OidOrganisaatio(MockOrganisaatiot.lehtikuusentienToimipiste),
    arviointi = None,
    vahvistus = None,
    osasuoritukset = None
  )

  def opiskeluoikeus(suoritus: AmmatillinenTutkintoSuoritus = tutkintoSuoritus) = OpiskeluOikeus(None, None, None, None, None, None,
    oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
    suoritus = suoritus,
    None, None, None, None
  )

  val laajuus = Laajuus(11, KoodistoKoodiViite("6", "opintojenlaajuusyksikko"))

  val tutkinnonOsa: OpsTutkinnonosa = OpsTutkinnonosa(KoodistoKoodiViite("100023", "tutkinnonosat"), true, Some(laajuus), None, None)

  val paikallinenTutkinnonOsa = PaikallinenTutkinnonosa(
    Paikallinenkoodi("1", "paikallinen osa", "paikallinenkoodisto"), "Paikallinen tutkinnon osa", false, Some(laajuus)
  )

  def arviointiHyvä(päivä: Option[LocalDate] = None): Some[List[Arviointi]] = Some(List(Arviointi(KoodistoKoodiViite("2", "arviointiasteikkoammatillinent1k3"), päivä)))

  val tilaValmis: KoodistoKoodiViite = KoodistoKoodiViite("VALMIS", "suorituksentila")
  val tilaKesken: KoodistoKoodiViite = KoodistoKoodiViite("KESKEN", "suorituksentila")
  val tilaKeskeytynyt: KoodistoKoodiViite = KoodistoKoodiViite("KESKEYTYNYT", "suorituksentila")
  val vahvistus: Some[Vahvistus] = Some(Vahvistus(Some(LocalDate.parse("2016-08-08"))))

  val tutkinnonSuoritustapaNäyttönä = Some(Suoritustapa(KoodistoKoodiViite("naytto", "suoritustapa")))

  val tutkinnonOsaSuoritus = AmmatillinenOpsTutkinnonosaSuoritus(
    tutkinnonOsa, None, None, None, None, None, None, tilaKesken, None,
    OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka")),
    arviointiHyvä(), None)

  val paikallinenTutkinnonOsaSuoritus = AmmatillinenPaikallinenTutkinnonosaSuoritus(
    paikallinenTutkinnonOsa, None, None, None, None, None, tilaKesken, None,
    OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka")),
    arviointiHyvä(), None)
}
