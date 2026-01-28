package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.ExamplesPerusopetukseenValmistavaOpetus.{perusopetukseenValmistavaOpiskeluoikeus, perusopetukseenValmistavanOpetuksenSuoritus}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.{arviointi, oppiaine, vuosiviikkotuntia}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._

import java.time.LocalDate
import scala.reflect.runtime.universe.TypeTag

class OppijaValidationPerusopetukseenValmistavaSpec extends TutkinnonPerusteetTest[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus] with KoskiHttpSpec {
  "Nuorten perusopetuksen oppiaineen suoritus valmistavassa opetuksessa" - {
    "Luokka-astetta ei vaadita jos arvionti on 'O'" in {
      val suoritus = perusopetukseenValmistavanOpetuksenSuoritus.copy(osasuoritukset = Option(List(NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
        koulutusmoduuli = oppiaine("FY").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1)),
        arviointi = PerusopetusExampleData.arviointi("O", kuvaus = None)
      ))))

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
        verifyResponseStatusOk()
      }
    }

    "Laajuus on pakollinen" in {
      val suoritus = perusopetukseenValmistavanOpetuksenSuoritus.copy(osasuoritukset = Option(List(NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
        koulutusmoduuli = oppiaine("FY").copy(laajuus = None),
        luokkaAste = Some(Koodistokoodiviite("7", "perusopetuksenluokkaaste")),
        arviointi = arviointi(9)
      ))))

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu("Oppiaineen koskioppiaineetyleissivistava/FY laajuus puuttuu"))
      }
    }

    "Laajuus ei pakollinen kun suoritustapa 'erityinentutkinto'" in {
      val suoritus = perusopetukseenValmistavanOpetuksenSuoritus.copy(osasuoritukset = Option(List(NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
        koulutusmoduuli = oppiaine("FY").copy(laajuus = None),
        luokkaAste = Some(Koodistokoodiviite("7", "perusopetuksenluokkaaste")),
        arviointi = arviointi(9),
        suoritustapa = Some(PerusopetusExampleData.suoritustapaErityinenTutkinto)
      ))))

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
        verifyResponseStatusOk()
      }
    }

    "Kokonaislaajuuden yksikkö oltava tunneissa" in {
      val suoritus = perusopetukseenValmistavanOpetuksenSuoritus.copy(kokonaislaajuus = Some(LaajuusVuosiviikkotunneissa(5)))
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.perusopetukseenValmistavaOpetusKokonaislaajuusYksikköEiTunneissa())
      }
    }

    "Suorituskieli vain suomi tai ruotsi" in {
      lazy val suomenKieli = Koodistokoodiviite("FI", Some("suomi"), "kieli", None)
      lazy val ruotsinKieli = Koodistokoodiviite("SV", Some("ruotsi"), "kieli", None)
      lazy val englanti = Koodistokoodiviite("EN", Some("englanti"), "kieli", None)

      val suoritusFi = perusopetukseenValmistavanOpetuksenSuoritus.copy(suorituskieli = suomenKieli)
      val suoritusSv = perusopetukseenValmistavanOpetuksenSuoritus.copy(suorituskieli = ruotsinKieli)
      val suoritusEn = perusopetukseenValmistavanOpetuksenSuoritus.copy(suorituskieli = englanti)

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritusFi))) {
        verifyResponseStatusOk()
      }

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritusSv))) {
        verifyResponseStatusOk()
      }

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritusEn))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.virheellinenSuorituskieli("Suorituskielen tulee olla suomi tai ruotsi"))
      }
    }



    "Samaa opiskeluoikeutta ei voi siirtää kahteen kertaan" in {
      duplikaattiaEiSallittu(defaultOpiskeluoikeus, defaultOpiskeluoikeus)
    }

    "Samaa opiskeluoikeutta ei voi siirtää kahteen kertaan, vaikka päivämäärät ovat erilaiset (mutta päällekkäiset)" - {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(
        tila = PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(List(
          PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(LocalDate.of(2017, 9, 1), opiskeluoikeusLäsnä)
        ))
      )

      duplikaattiaEiSallittu(defaultOpiskeluoikeus, opiskeluoikeus)
    }

    def duplikaattiaEiSallittu(oo1: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus, oo2: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus): Unit = {
      setupOppijaWithOpiskeluoikeus(oo1, defaultHenkilö) {
        verifyResponseStatusOk()
      }
      postOppija(makeOppija(defaultHenkilö, List(oo2))) {
        verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
      }
    }
  }

  "Lisäopetus-lisätiedot" - {
    "Lisäopetusjakso sallittu kun jakso alkaa 1.8.2026 tai sen jälkeen" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List(Aikajakso(LocalDate.of(2026, 8, 1), Some(LocalDate.of(2026, 10, 1)))))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "Lisäopetusjakso ei sallittu kun jakso alkaa ennen 1.8.2026" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List(Aikajakso(LocalDate.of(2026, 7, 31), Some(LocalDate.of(2026, 10, 1)))))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.perusopetukseenValmistavaOpetus.lisäopetusEiSallittuEnnen())
      }
    }

    "Lisäopetuksen kokonaiskesto voi olla enintään 365 päivää" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List(Aikajakso(LocalDate.of(2027, 1, 1), Some(LocalDate.of(2027, 12, 31)))))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "Lisäopetuksen kokonaiskesto ei saa ylittää 365 päivää" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List(Aikajakso(LocalDate.of(2027, 1, 1), Some(LocalDate.of(2028, 1, 1)))))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.perusopetukseenValmistavaOpetus.lisäopetuksenKestoYlittääVuoden())
      }
    }

    "Useiden lisäopetusjaksojen yhteiskesto ei saa ylittää 365 päivää" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List(
            Aikajakso(LocalDate.of(2027, 1, 1), Some(LocalDate.of(2027, 6, 30))),
            Aikajakso(LocalDate.of(2027, 8, 1), Some(LocalDate.of(2028, 1, 31)))
          ))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.perusopetukseenValmistavaOpetus.lisäopetuksenKestoYlittääVuoden())
      }
    }

    "Useiden lisäopetusjaksojen yhteiskesto sallittu kun enintään 365 päivää" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List(
            Aikajakso(LocalDate.of(2027, 1, 1), Some(LocalDate.of(2027, 6, 1))),
            Aikajakso(LocalDate.of(2027, 8, 1), Some(LocalDate.of(2027, 12, 31)))
          ))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "Lisäopetusjaksot eivät saa olla päällekkäin" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List(
            Aikajakso(LocalDate.of(2027, 1, 1), Some(LocalDate.of(2027, 6, 30))),
            Aikajakso(LocalDate.of(2027, 6, 1), Some(LocalDate.of(2027, 8, 31)))
          ))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.perusopetukseenValmistavaOpetus.lisäopetusJaksotPäällekkäin())
      }
    }

    "Peräkkäiset lisäopetusjaksot sallittu" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List(
            Aikajakso(LocalDate.of(2027, 1, 1), Some(LocalDate.of(2027, 5, 31))),
            Aikajakso(LocalDate.of(2027, 6, 1), Some(LocalDate.of(2027, 8, 31)))
          ))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "Tyhjä lisäopetus-lista sallittu" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = Some(List())
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "Lisätiedot ilman lisäopetusta sallittu" in {
      val opiskeluoikeus = keskeneräinenOpiskeluoikeus.copy(
        lisätiedot = Some(PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot(
          lisäopetus = None
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    lazy val keskeneräinenOpiskeluoikeus: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = {
      defaultOpiskeluoikeus.copy(
        tila = PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(List(
          PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(LocalDate.of(2017, 8, 15), opiskeluoikeusLäsnä)
        )),
        suoritukset = List(perusopetukseenValmistavanOpetuksenSuoritus.copy(vahvistus = None))
      )
    }
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    perusopetukseenValmistavanOpetuksenSuoritus.copy(koulutusmoduuli = perusopetukseenValmistavanOpetuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"

  override def tag: TypeTag[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus] = implicitly[TypeTag[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = perusopetukseenValmistavaOpiskeluoikeus
}
