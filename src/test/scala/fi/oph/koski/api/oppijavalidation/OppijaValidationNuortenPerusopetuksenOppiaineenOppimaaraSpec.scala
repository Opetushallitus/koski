package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.perusopetuksenDiaarinumero
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http.{ErrorMatcher, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._


class OppijaValidationNuortenPerusopetuksenOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsPerusopetus {
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

      setupOppijaWithOpiskeluoikeus(eitiedossa) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tyhjänOppiaineenVahvistus(""""Ei tiedossa"-oppiainetta ei voi merkitä valmiiksi"""))
      }
    }
  }

  "Sama luokka-aste moneen kertaan samalle oppiaineelle" - {
    "ei voi lisätä" in {
      val eitiedossa = defaultOpiskeluoikeus.copy(suoritukset = List(NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = EiTiedossaOppiaine(),
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = PerusopetusExampleData.arviointi(9),
        luokkaAste = perusopetuksenLuokkaAste("8"),
        suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
        vahvistus = None,
        suorituskieli = suomenKieli
      ),
        NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
          koulutusmoduuli = EiTiedossaOppiaine(),
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = PerusopetusExampleData.arviointi(9),
          luokkaAste = perusopetuksenLuokkaAste("8"),
          suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
          vahvistus = None,
          suorituskieli = suomenKieli
        )))

      setupOppijaWithOpiskeluoikeus(eitiedossa) {
        verifyResponseStatus(400, HttpStatus(400, KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenLuokkaAsteSamaUseammassaSuorituksessa("""Samaa luokka-astetta ei voi olla useammalla nuorten perusopetuksen erityisen tutkinnon suorituksella.""").errors ++ KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenLuokkaAsteSamaUseammassaSuorituksessa("""Samaa luokka-astetta ei voi olla useammalla nuorten perusopetuksen erityisen tutkinnon suorituksella.""").errors))
      }


    }
  }

  "Luokka-aste muulle kuin erityiselle tutkinnolle" - {
    "ei voi lisätä" in {
      val eitiedossa = defaultOpiskeluoikeus.copy(suoritukset = List(NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = EiTiedossaOppiaine(),
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = PerusopetusExampleData.arviointi(9),
        luokkaAste = Some(Koodistokoodiviite("8", "perusopetuksenluokkaaste")),
        suoritustapa = PerusopetusExampleData.suoritustapaKoulutus,
        vahvistus = vahvistus,
        suorituskieli = suomenKieli
      )))

      setupOppijaWithOpiskeluoikeus(eitiedossa) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*opiskeluoikeudet.0.suoritukset.0.luokkaAste.*".r))
      }
    }
  }
}
