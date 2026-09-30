package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsLukio
import fi.oph.koski.documentation.ExamplesLukio2019
import fi.oph.koski.documentation.ExamplesLukio2019.{oppiaineenOppimääräOpiskeluoikeus, oppiaineidenOppimäärienLukioDiplominSuoritus, oppiaineidenOppimäärienSuoritus}
import fi.oph.koski.documentation.Lukio2019ExampleData.numeerinenArviointi
import fi.oph.koski.documentation.LukioExampleData.aikuistenOpetussuunnitelma
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.uusiLukio
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationLukionOppiaineidenOppimaarat2019Spec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio {
  "Diaarinumerot" - {
    val suorituksenKoulutusmoduuliVanhallaPerusteella = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("60/011/2015"))
    val suorituksenKoulutusmoduuliAikuistenPerusteella = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("OPH-2267-2019"))
    val suorituksenKoulutusmoduuliNuortenPerusteella = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("OPH-2263-2019"))

    "Vanha diaarinumero aiheuttaa virheen" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = suorituksenKoulutusmoduuliVanhallaPerusteella)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "60/011/2015" suorituksella lukionaineopinnot, sallitut arvot: OPH-2263-2019"""))
      }
    }

    "Väärä nuorten diaarinumero aiheuttaa virheen" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = suorituksenKoulutusmoduuliAikuistenPerusteella)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "OPH-2267-2019" suorituksella lukionaineopinnot, sallitut arvot: OPH-2263-2019"""))
      }
    }

    "Väärä aikuisten diaarinumero aiheuttaa virheen" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(oppimäärä = aikuistenOpetussuunnitelma, koulutusmoduuli = suorituksenKoulutusmoduuliNuortenPerusteella)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "OPH-2263-2019" suorituksella lukionaineopinnot, sallitut arvot: OPH-2267-2019"""))
      }
    }

    "Nuorten diaarinumero sallitaan" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = suorituksenKoulutusmoduuliNuortenPerusteella)))) {
        verifyResponseStatusOk()
      }
    }

    "Aikuisten diaarinumero sallitaan" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(oppimäärä = aikuistenOpetussuunnitelma, koulutusmoduuli = suorituksenKoulutusmoduuliAikuistenPerusteella)))) {
        verifyResponseStatusOk()
      }
    }
  }

  "Suoritukset" - {
    "Useampi ryhmittelevä lukionaineopinnot-suoritus aiheuttaa virheen" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus, oppiaineidenOppimäärienSuoritus))) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia(
          """Opiskeluoikeudella on enemmän kuin yksi oppiaineiden oppimäärät ryhmittelevä lukionaineopinnot-tyyppinen suoritus"""
          )
        )
      }
    }

    "Muiden lukio-opintojen suoritusten tallentaminen onnistuu" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienLukioDiplominSuoritus))) {
        verifyResponseStatusOk()
      }
    }
  }

  "Opiskeluoikeuksien duplikaatit" - {
    "opiskeluoikeutta voi siirtää kahteen kertaan" - {
      "kun opiskeluoikeus on valmistunut ja päivämäärät ovat päällekkäiset" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus, defaultHenkilö) {
          verifyResponseStatusOk()
        }
        postOppija(makeOppija(defaultHenkilö, List(defaultOpiskeluoikeus))) {
          verifyResponseStatusOk()
        }
      }
      "kun opiskeluoikeus on aktiivinen ja päivämäärät ovat päällekkäiset" in {
        setupOppijaWithOpiskeluoikeus(ExamplesLukio2019.aktiivinenOppiaineenOppimääräOpiskeluoikeus, defaultHenkilö) {
          verifyResponseStatusOk()
        }
        postOppija(makeOppija(defaultHenkilö, List(ExamplesLukio2019.aktiivinenOppiaineenOppimääräOpiskeluoikeus))) {
          verifyResponseStatusOk()
        }
      }
    }
  }

  "Suorituksen tyypin muuttaminen" - {
    "Hylkää lukion oppimäärä aineopinnot muutos" in {
      val oo = lastOpiskeluoikeus(KoskiSpecificMockOppijat.uusiLukio.oid).asInstanceOf[LukionOpiskeluoikeus]
      val aineopSuoritus = ExamplesLukio2019.oppiaineenOppimääräOpiskeluoikeus.suoritukset.head.asInstanceOf[LukionOppiaineidenOppimäärienSuoritus2019].copy(toimipiste = oo.suoritukset.head.toimipiste)
      val mutated = oo.copy(suoritukset = List(aineopSuoritus))
      putOpiskeluoikeus(mutated,uusiLukio, headers = authHeaders() ++ jsonContent) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.suorituksenTyyppiMuuttunut("Lukion oppimäärän opiskeluoikeutta ei voi muuttaa aineopiskeluksi."))
      }
    }

    "Hylkää lukion aineopiskelija oppimäärä muutos" in {
      val oo = lastOpiskeluoikeus(KoskiSpecificMockOppijat.uusiLukionAineopiskelija.oid).asInstanceOf[LukionOpiskeluoikeus]
      val oppimaaraSuoritus = ExamplesLukio2019.opiskeluoikeus.suoritukset.head.asInstanceOf[LukionOppimääränSuoritus2019].copy(toimipiste = oo.suoritukset.head.toimipiste)
      val mutated = oo.copy(suoritukset = List(oppimaaraSuoritus))
      putOpiskeluoikeus(mutated, KoskiSpecificMockOppijat.uusiLukionAineopiskelija, headers = authHeaders() ++ jsonContent) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.suorituksenTyyppiMuuttunut("Lukion aineopiskelijan opiskeluoikeutta ei voi muuttaa oppimääräksi."))
      }
    }
  }

  "Moduulin arviointipäivä" - {
    "Arviointipäivä voi olla aikaisintaan opiskeluoikeuden alkamispäivä" in {
      setupOppijaWithOpiskeluoikeus(FY1ModuulinArviointipäivällä(date(2019, 7, 31))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.arviointiEnnenOpiskeluoikeudenAlkamispäivää(
          "Osasuorituksen moduulikoodistolops2021/FY1 arviointipäivä 31.7.2019 on ennen opiskeluoikeuden alkamispäivää 1.8.2019."
        ))
      }
      setupOppijaWithOpiskeluoikeus(FY1ModuulinArviointipäivällä(date(2019, 8, 1))) {
        verifyResponseStatusOk()
      }
    }
  }

  private def FY1ModuulinArviointipäivällä(arviointipäivä: LocalDate): LukionOpiskeluoikeus =
    defaultOpiskeluoikeus.copy(suoritukset = defaultOpiskeluoikeus.suoritukset.map {
      case s: LukionOppiaineidenOppimäärienSuoritus2019 => s.copy(osasuoritukset = s.osasuoritukset.map(_.map {
        case o: LukionOppiaineenSuoritus2019 => o.copy(osasuoritukset = o.osasuoritukset.map(_.map {
          case m: LukionModuulinSuoritusOppiaineissa2019 if m.koulutusmoduuli.tunniste.koodiarvo == "FY1" =>
            m.copy(arviointi = numeerinenArviointi(8, arviointipäivä))
          case m => m
        }))
        case o => o
      }))
      case s => s
    }).ensuring(_ != defaultOpiskeluoikeus, "FY1-moduulia ei löytynyt oletusopiskeluoikeudesta")

  override def defaultOpiskeluoikeus: LukionOpiskeluoikeus = oppiaineenOppimääräOpiskeluoikeus
  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): LukionOpiskeluoikeus =
    defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  // Lukio 2019 rajoittaa sallitut diaarinumerot arvoihin OPH-2263-2019 ja OPH-2267-2019 -> pakko käyttää tässä eperusteista löytyvää
  override def eperusteistaLöytymätönValidiDiaarinumero: String = "OPH-2263-2019"
}
