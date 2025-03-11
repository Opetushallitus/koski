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
    Example(
      "kielitutkinto - yleinen kielitutkinto",
      "Vanhemman rakenteen mukainen yleisen kielitutkinnon suoritus (sisältää yleisarvosanan)",
      Oppija(exampleHenkilö.copy(hetu = "160586-873P"), Seq(ykiOpiskeluoikeus(LocalDate.of(2011, 1, 1), "FI", "kt"))),
    )
  )

  def ykiOpiskeluoikeus(tutkintopäivä: LocalDate, kieli: String, tutkintotaso: String): KielitutkinnonOpiskeluoikeus = {
    val arviointipäivä = tutkintopäivä.plusDays(60)
    KielitutkinnonOpiskeluoikeus(
      tila = KielitutkinnonOpiskeluoikeudenTila(
        opiskeluoikeusjaksot = List(
          Opiskeluoikeusjakso.tutkintopäivä(tutkintopäivä),
          Opiskeluoikeusjakso.valmis(arviointipäivä),
        )
      ),
      suoritukset = List(
        ExamplesKielitutkinto.PäätasonSuoritus.yleinenKielitutkinto(tutkintotaso, kieli, arviointipäivä)
      )
    )
  }

  object PäätasonSuoritus {
    def yleinenKielitutkinto(tutkintotaso: String, kieli: String, arviointipäivä: LocalDate): YleisenKielitutkinnonSuoritus = {
      val arvosana = tutkintotaso match {
        case "pt" => 1
        case "kt" => 3
        case "yt" => 5
      }
      YleisenKielitutkinnonSuoritus(
        koulutusmoduuli = YleinenKielitutkinto(
          tunniste = Koodistokoodiviite(tutkintotaso.toString, "ykitutkintotaso"),
          kieli = Koodistokoodiviite(kieli, "kieli"),
        ),
        toimipiste = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
        vahvistus = Some(Päivämäärävahvistus(
          päivä = arviointipäivä,
          myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki),
        )),
        osasuoritukset = Some(List(
          ExamplesKielitutkinto.Osasuoritus.yleisenKielitutkinnonOsa("tekstinymmartaminen", s"$arvosana", arviointipäivä),
          ExamplesKielitutkinto.Osasuoritus.yleisenKielitutkinnonOsa("kirjoittaminen", s"alle$arvosana", arviointipäivä),
          ExamplesKielitutkinto.Osasuoritus.yleisenKielitutkinnonOsa("puheenymmartaminen", s"${arvosana + 1}", arviointipäivä),
          ExamplesKielitutkinto.Osasuoritus.yleisenKielitutkinnonOsa("puhuminen", s"$arvosana", arviointipäivä),
        )),
        yleisarvosana = if (arviointipäivä.isBefore(LocalDate.of(2012, 1, 1))) Some(Koodistokoodiviite(s"$arvosana", "ykiarvosana")) else None,
      )
    }
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
