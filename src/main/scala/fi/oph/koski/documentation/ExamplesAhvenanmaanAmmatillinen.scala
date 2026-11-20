package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.oppilaitos
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object AhvenanmaanAmmatillinenExampleData {
  lazy val ålandsYrkesgymnasium: Oppilaitos = oppilaitos(MockOrganisaatiot.ålandsYrkesgymnasium)
  lazy val ålandsYrkesgymnasiumToimipiste: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.ålandsYrkesgymnasium)
  val maarianhamina = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "478", nimi = Some(Finnish("Maarianhamina")))

  // Ahvenanmaan sote-todistukset viittaavat manner-Suomen perusteeseen OPH-5085-2021.
  val sosiaaliJaTerveysalanPerustutkinto = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("371101", "koulutus"), perusteenDiaarinumero = Some("OPH-5085-2021"))

  def arviointi(arvosana: String, päivä: LocalDate) =
    Some(List(AmmatillinenArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkoammatillinen15"), päivä)))

  private val ammatillisetTutkinnonOsat = Some(Koodistokoodiviite("1", "ammatillisentutkinnonosanryhma"))
  private val yhteisetTutkinnonOsat = Some(Koodistokoodiviite("2", "ammatillisentutkinnonosanryhma"))

  val kasvunTukeminenJaOhjaus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100832", "tutkinnonosat"), pakollinen = true, Some(LaajuusOsaamispisteissä(25))),
    tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat,
    toimipiste = Some(ålandsYrkesgymnasiumToimipiste),
    arviointi = arviointi("4", date(2025, 5, 20)),
    näyttö = Some(Näyttö(
      kuvaus = Some("Barnets utveckling och handledning i daghem"),
      suorituspaikka = Some(NäytönSuorituspaikka(Koodistokoodiviite("1", "ammatillisennaytonsuorituspaikka"), "Daghemmet Solrosen, Mariehamn")),
      suoritusaika = Some(NäytönSuoritusaika(date(2025, 5, 5), date(2025, 5, 9))),
      arviointi = None
    ))
  )

  val matemaattisLuonnontieteellinenOsaaminen = YhteisenAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = YhteinenTutkinnonOsa(Koodistokoodiviite("106728", "tutkinnonosat"), pakollinen = true, Some(LaajuusOsaamispisteissä(6))),
    tutkinnonOsanRyhmä = yhteisetTutkinnonOsat,
    toimipiste = Some(ålandsYrkesgymnasiumToimipiste),
    arviointi = arviointi("3", date(2025, 12, 12)),
    osasuoritukset = Some(List(
      YhteisenTutkinnonOsanOsaAlueenSuoritus(
        koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("MLMA", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(4))),
        arviointi = arviointi("3", date(2025, 12, 12))
      ),
      YhteisenTutkinnonOsanOsaAlueenSuoritus(
        koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("MLFK", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(2))),
        arviointi = arviointi("3", date(2025, 12, 12))
      )
    ))
  )

  val perustutkinnonSuoritus = AhvenanmaanAmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
    toimipiste = ålandsYrkesgymnasiumToimipiste,
    alkamispäivä = Some(date(2023, 8, 15)),
    vahvistus = vahvistus(date(2026, 5, 29), ålandsYrkesgymnasium, Some(maarianhamina)),
    suorituskieli = ruotsinKieli,
    osasuoritukset = Some(List(kasvunTukeminenJaOhjaus, matemaattisLuonnontieteellinenOsaaminen))
  )

  val opiskeluoikeus = AhvenanmaanAmmatillinenOpiskeluoikeus(
    oppilaitos = Some(ålandsYrkesgymnasium),
    tila = AhvenanmaanAmmatillinenOpiskeluoikeudenTila(List(
      AhvenanmaanAmmatillinenOpiskeluoikeusjakso(date(2023, 8, 15), opiskeluoikeusLäsnä),
      AhvenanmaanAmmatillinenOpiskeluoikeusjakso(date(2026, 5, 29), opiskeluoikeusValmistunut)
    )),
    suoritukset = List(perustutkinnonSuoritus)
  )
}

object ExamplesAhvenanmaanAmmatillinen {
  import AhvenanmaanAmmatillinenExampleData._

  val ahvenanmaanAmmattiopiskelija = Oppija(
    asUusiOppija(KoskiSpecificMockOppijat.ahvenanmaanAmmattiopiskelija),
    List(opiskeluoikeus)
  )

  val examples = List(
    Example("ahvenanmaan ammatillinen - perustutkinto", "Ahvenanmaalainen opiskelija on suorittanut ammatillisen perustutkinnon (grundexamen)", ahvenanmaanAmmattiopiskelija),
  )
}
