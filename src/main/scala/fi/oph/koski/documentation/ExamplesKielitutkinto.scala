package fi.oph.koski.documentation

import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

import java.time.LocalDate
import scala.collection.immutable

object ExamplesKielitutkinto {
  lazy val exampleMockOppija: LaajatOppijaHenkilöTiedot = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
  lazy val exampleHenkilö: UusiHenkilö = asUusiOppija(exampleMockOppija)

  lazy val examples: immutable.Seq[Example] = List(
    Example("kielitutkinto - yleinen kielitutkinto", "Yleisen kielitutkinnon suoritus", Oppija(exampleHenkilö.copy(hetu = "160586-873P"), Seq(ykiOpiskeluoikeus)))
  )

  def ykiOpiskeluoikeus: KielitutkinnonOpiskeluoikeus = KielitutkinnonOpiskeluoikeus(
    tila = KielitutkinnonOpiskeluoikeudenTila(
      opiskeluoikeusjaksot = List(
        Opiskeluoikeusjakso.tutkintopäivä(LocalDate.of(2025, 1, 1)),
        Opiskeluoikeusjakso.valmis(LocalDate.of(2025, 1, 3)),
      )
    ),
    suoritukset = List(
      ExamplesKielitutkinto.PäätasonSuoritus.yleinenKielitutkinto("pt", "EN", LocalDate.of(2025, 1, 3))
    )
  )

  object PäätasonSuoritus {
    def yleinenKielitutkinto(tutkintotaso: String, kieli: String, arviointipäivä: LocalDate): YleisenKielitutkinnonSuoritus =
      YleisenKielitutkinnonSuoritus(
        koulutusmoduuli = YleinenKielitutkinto(
          tunniste = Koodistokoodiviite(tutkintotaso, "ykitutkintotaso"),
          kieli = Koodistokoodiviite(kieli, "kieli"),
        ),
        toimipiste = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
        vahvistus = Some(Päivämäärävahvistus(
          päivä = arviointipäivä,
          myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki),
        )),
        osasuoritukset = Some(List(
          ExamplesKielitutkinto.Osasuoritus.yleisenKielitutkinnonOsa("tekstinymmartaminen", "2", arviointipäivä),
          ExamplesKielitutkinto.Osasuoritus.yleisenKielitutkinnonOsa("kirjoittaminen", "3", arviointipäivä),
          ExamplesKielitutkinto.Osasuoritus.yleisenKielitutkinnonOsa("puheenymmartaminen", "4", arviointipäivä),
          ExamplesKielitutkinto.Osasuoritus.yleisenKielitutkinnonOsa("puhuminen", "4", arviointipäivä),
        )),
        yleisarvosana = Some(Koodistokoodiviite("3", "ykiarvosana")),
      )
  }

  object Osasuoritus {
    def yleisenKielitutkinnonOsa(tyyppi: String, arvosana: String, arviointiPäivä: LocalDate): YleisenKielitutkinnonOsanSuoritus =
      YleisenKielitutkinnonOsanSuoritus(
        koulutusmoduuli = YleisenKielitutkinnonOsa(
          tunniste = Koodistokoodiviite(tyyppi, "ykisuorituksenosa"),
        ),
        arviointi = Some(List(
          YleisenKielitutkinnonOsanArviointi(
            arvosana = Koodistokoodiviite(arvosana, "ykiarvosana"),
            päivä = arviointiPäivä,
          )
        ))
      )
  }

  object Opiskeluoikeusjakso {
    def tutkintopäivä(alku: LocalDate): KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso =
      KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
        alku = alku,
        tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila"),
      )

    def valmis(alku: LocalDate): KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso =
      KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
        alku = alku,
        tila = Koodistokoodiviite("hyvaksytystisuoritettu", "koskiopiskeluoikeudentila"),
      )
  }

}
