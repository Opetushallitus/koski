package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAikuistenPerusopetus
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

// Lukiosuoritusten validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.
class OppijaValidationAikuistenPerusopetuksenOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[AikuistenPerusopetuksenOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsAikuistenPerusopetus {
  private def defaultSuoritus(diaari: Option[String]) = AikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
    koulutusmoduuli = MuuAikuistenPerusopetuksenOppiaine(
      tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "HI"),
      perusteenDiaarinumero = diaari
    ),
    toimipiste = jyväskylänNormaalikoulu,
    arviointi = PerusopetusExampleData.arviointi(9),
    suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
    vahvistus = vahvistus,
    suorituskieli = suomenKieli
  )

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = AikuistenPerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(defaultSuoritus(diaari)),
    tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(AikuistenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
  )

  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"

  "Ei tiedossa oppiainetta" - {
    "ei voi vahvistaa" in {
      val eitiedossa = defaultOpiskeluoikeus.copy(suoritukset = List(AikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
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

  "Pakollinen oppiaine" - {
    "pakollinen oppiaine on sallittu, kun suoritustapa on erityinen tutkinto" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "pakollinen oppiaine on sallittu, kun suoritustapa on koulutus" in {
      val koulutusOpiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(
        defaultSuoritus(Some("OPH-1280-2017")).copy(
          suoritustapa = PerusopetusExampleData.suoritustapaKoulutus
        )
      ))

      setupOppijaWithOpiskeluoikeus(koulutusOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "valinnainen oppiaine ei ole sallittu, kun suoritustapa on erityinen tutkinto" in {
      val valinnainen = defaultOpiskeluoikeus.copy(suoritukset = List(
        defaultSuoritus(Some("OPH-1280-2017")).copy(
          koulutusmoduuli = MuuAikuistenPerusopetuksenOppiaine(
            tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "HI"),
            perusteenDiaarinumero = Some("OPH-1280-2017"),
            pakollinen = false
          ),
          suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
        )
      ))

      setupOppijaWithOpiskeluoikeus(valinnainen) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.perusopetus.erityinenTutkintoValinnainenOppiaineenOppimäärä("HI")())
      }
    }

    "valinnainen oppiaine on sallittu, kun suoritustapa on koulutus" in {
      val valinnainen = defaultOpiskeluoikeus.copy(suoritukset = List(
        defaultSuoritus(Some("OPH-1280-2017")).copy(
          koulutusmoduuli = MuuAikuistenPerusopetuksenOppiaine(
            tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "HI"),
            perusteenDiaarinumero = Some("OPH-1280-2017"),
            pakollinen = false
          ),
          suoritustapa = PerusopetusExampleData.suoritustapaKoulutus,
        )
      ))

      setupOppijaWithOpiskeluoikeus(valinnainen) {
        verifyResponseStatusOk()
      }
    }

    "pakollinen paikallinen oppiaine ei ole sallittu, kun suoritustapa on erityinen tutkinto" in {
      val valinnainen = defaultOpiskeluoikeus.copy(suoritukset = List(
        defaultSuoritus(Some("OPH-1280-2017")).copy(
          koulutusmoduuli = AikuistenPerusopetuksenPaikallinenOppiaine(
            tunniste = PaikallinenKoodi("latina", finnish("Latina"), None),
            laajuus = None,
            finnish("Kuvaus"),
            perusteenDiaarinumero = Some("OPH-1280-2017"),
            pakollinen = true
          ),
          suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
        )
      ))

      setupOppijaWithOpiskeluoikeus(valinnainen) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.perusopetus.erityinenTutkintoPaikallinenOppiaineenOppimäärä("latina")())
      }
    }

    "valinnainen paikallinen oppiaine ei ole sallittu, kun suoritustapa on erityinen tutkinto" in {
      val valinnainen = defaultOpiskeluoikeus.copy(suoritukset = List(
        defaultSuoritus(Some("OPH-1280-2017")).copy(
          koulutusmoduuli = AikuistenPerusopetuksenPaikallinenOppiaine(
            tunniste = PaikallinenKoodi("latina", finnish("Latina"), None),
            laajuus = None,
            finnish("Kuvaus"),
            perusteenDiaarinumero = Some("OPH-1280-2017"),
            pakollinen = false
          ),
          suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
        )
      ))

      setupOppijaWithOpiskeluoikeus(valinnainen) {
        verifyResponseStatus(
          400,
          KoskiErrorCategory.badRequest.validation.perusopetus.erityinenTutkintoValinnainenOppiaineenOppimäärä("latina")(),
          KoskiErrorCategory.badRequest.validation.perusopetus.erityinenTutkintoPaikallinenOppiaineenOppimäärä("latina")()
        )
      }
    }

    "pakollinen paikallinen oppiaine on sallittu, kun suoritustapa on koulutus" in {
      val valinnainen = defaultOpiskeluoikeus.copy(suoritukset = List(
        defaultSuoritus(Some("OPH-1280-2017")).copy(
          koulutusmoduuli = AikuistenPerusopetuksenPaikallinenOppiaine(
            tunniste = PaikallinenKoodi("latina", finnish("Latina"), None),
            laajuus = None,
            finnish("Kuvaus"),
            perusteenDiaarinumero = Some("OPH-1280-2017"),
            pakollinen = true
          ),
          suoritustapa = PerusopetusExampleData.suoritustapaKoulutus,
        )
      ))

      setupOppijaWithOpiskeluoikeus(valinnainen) {
        verifyResponseStatusOk()
      }
    }

    "valinnainen paikallinen oppiaine on sallittu, kun suoritustapa on koulutus" in {
      val valinnainen = defaultOpiskeluoikeus.copy(suoritukset = List(
        defaultSuoritus(Some("OPH-1280-2017")).copy(
          koulutusmoduuli = AikuistenPerusopetuksenPaikallinenOppiaine(
            tunniste = PaikallinenKoodi("latina", finnish("Latina"), None),
            laajuus = None,
            finnish("Kuvaus"),
            perusteenDiaarinumero = Some("OPH-1280-2017"),
            pakollinen = false
          ),
          suoritustapa = PerusopetusExampleData.suoritustapaKoulutus,
        )
      ))

      setupOppijaWithOpiskeluoikeus(valinnainen) {
        verifyResponseStatusOk()
      }
    }
  }
}
