package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExamplesLukio2019.{oppiaineenOppimääräOpiskeluoikeus, oppiaineidenOppimäärienLukioDiplominSuoritus, oppiaineidenOppimäärienSuoritus}
import fi.oph.koski.documentation.LukioExampleData.aikuistenOpetussuunnitelma
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

class OppijaValidationLukionOppiaineidenOppimaarat2019Spec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio {
  "Diaarinumerot" - {
    val suorituksenKoulutusmoduuliVanhallaPerusteella = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("60/011/2015"))
    val suorituksenKoulutusmoduuliAikuistenPerusteella = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("OPH-2267-2019"))
    val suorituksenKoulutusmoduuliNuortenPerusteella = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("OPH-2263-2019"))

    "Vanha diaarinumero aiheuttaa virheen" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = suorituksenKoulutusmoduuliVanhallaPerusteella)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "60/011/2015" suorituksella lukionaineopinnot, sallitut arvot: OPH-2263-2019"""))
      }
    }

    "Väärä nuorten diaarinumero aiheuttaa virheen" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = suorituksenKoulutusmoduuliAikuistenPerusteella)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "OPH-2267-2019" suorituksella lukionaineopinnot, sallitut arvot: OPH-2263-2019"""))
      }
    }

    "Väärä aikuisten diaarinumero aiheuttaa virheen" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(oppimäärä = aikuistenOpetussuunnitelma, koulutusmoduuli = suorituksenKoulutusmoduuliNuortenPerusteella)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "OPH-2263-2019" suorituksella lukionaineopinnot, sallitut arvot: OPH-2267-2019"""))
      }
    }

    "Nuorten diaarinumero sallitaan" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = suorituksenKoulutusmoduuliNuortenPerusteella)))) {
        verifyResponseStatusOk()
      }
    }

    "Aikuisten diaarinumero sallitaan" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(oppimäärä = aikuistenOpetussuunnitelma, koulutusmoduuli = suorituksenKoulutusmoduuliAikuistenPerusteella)))) {
        verifyResponseStatusOk()
      }
    }
  }

  "Suoritukset" - {
    "Useampi ryhmittelevä lukionaineopinnot-suoritus aiheuttaa virheen" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus, oppiaineidenOppimäärienSuoritus))) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia(
          """Opiskeluoikeudella on enemmän kuin yksi oppiaineiden oppimäärät ryhmittelevä lukionaineopinnot-tyyppinen suoritus"""
          )
        )
      }
    }

    "Muiden lukio-opintojen suoritusten tallentaminen onnistuu" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienLukioDiplominSuoritus))) {
        verifyResponseStatusOk()
      }
    }
  }

  override def defaultOpiskeluoikeus: LukionOpiskeluoikeus = oppiaineenOppimääräOpiskeluoikeus
  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): LukionOpiskeluoikeus =
    defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(koulutusmoduuli = oppiaineidenOppimäärienSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  // Lukio 2019 rajoittaa sallitut diaarinumerot arvoihin OPH-2263-2019 ja OPH-2267-2019 -> pakko käyttää tässä eperusteista löytyvää
  override def eperusteistaLöytymätönValidiDiaarinumero: String = "OPH-2263-2019"
}
