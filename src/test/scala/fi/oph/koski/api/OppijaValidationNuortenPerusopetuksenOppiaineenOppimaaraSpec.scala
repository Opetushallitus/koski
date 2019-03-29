package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData.{suomenKieli, _}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.perusopetuksenDiaarinumero
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._


class OppijaValidationNuortenPerusopetuksenOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = MuuNuortenPerusopetuksenOppiaine(
          tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "HI"),
          perusteenDiaarinumero = diaari
        ),
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = PerusopetusExampleData.arviointi(9),
        suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
        vahvistus = vahvistus,
        suorituskieli = suomenKieli
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(NuortenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä)))
  )

  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"

  override def defaultOpiskeluoikeus: PerusopetuksenOpiskeluoikeus = opiskeluoikeusWithPerusteenDiaarinumero(Some(perusopetuksenDiaarinumero))

  "Ei tiedossa oppiainetta" - {
    "ei voi vahvistaa" in {
      val eitiedossa = defaultOpiskeluoikeus.copy(suoritukset = List(NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = EiTiedossaOppiaine(),
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = PerusopetusExampleData.arviointi(9),
        suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
        vahvistus = vahvistus,
        suorituskieli = suomenKieli
      )))

      putOpiskeluoikeus(eitiedossa) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tyhjänOppiaineenVahvistus(""""Ei tiedossa"-oppiainetta ei voi merkitä valmiiksi"""))
      }
    }
  }
}
