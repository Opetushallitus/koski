package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesLukio2019.{oppiaineenOppimääräOpiskeluoikeus, oppiaineidenOppimäärienSuoritus}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.{Diaarinumero, Perusteet}

class OppijaValidationLukionOppiaineidenOppimaarat2019Spec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsLukio {
  "Diaarinumerot" - {
    "Vanha diaarinumero aiheuttaa virheen" in {
      val suorituksenKoulutusmoduuliVanhallaPerusteella = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("60/011/2015"))
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = suorituksenKoulutusmoduuliVanhallaPerusteella)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "60/011/2015" suorituksella lukionoppiaineidenoppimaarat2019, sallitut arvot: OPH-2263-2019, OPH-2267-2019"""))
      }
    }

    "Diaarinumerot OPH-2263-2019 ja OPH-2267-2019 sallitaan" in {
      Perusteet.lops2019.diaarit.collect { case Diaarinumero(diaari) =>
        val suorituksenKoulutusmoduuli = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some(diaari))
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = suorituksenKoulutusmoduuli)))) {
          verifyResponseStatusOk()
        }
      }
    }
  }

  "Suoritukset" - {
    "Useampi ryhmittelevä lukionoppiaineidenoppimaarat2019-suoritus aiheuttaa virheen" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus, oppiaineidenOppimäärienSuoritus))) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia(
          """Opiskeluoikeudella on enemmän kuin yksi oppiaineiden oppimäärät ryhmittelevä lukionoppiaineidenoppimaarat2019-tyyppinen suoritus"""
          )
        )
      }
    }
  }

  override def defaultOpiskeluoikeus: LukionOpiskeluoikeus = oppiaineenOppimääräOpiskeluoikeus
  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): LukionOpiskeluoikeus =
    defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  // Lukio 2019 rajoittaa sallitut diaarinumerot arvoihin OPH-2263-2019 ja OPH-2267-2019 -> pakko käyttää tässä eperusteista löytyvää
  override def eperusteistaLöytymätönValidiDiaarinumero: String = "OPH-2263-2019"
}
