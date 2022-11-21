package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.kaikkiAineet
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

// Perusopetuksen validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationPerusopetuksenVuosiluokkaSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    vuosiluokkasuoritus.copy(koulutusmoduuli = vuosiluokkasuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"

  "9. vuosiluokka" - {
    "Oppiaineita syötetty kun oppija ei jää luokalle -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(vuosiluokkasuoritus.copy(jääLuokalle = false, osasuoritukset = kaikkiAineet)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.oppiaineitaEiSallita())
      }
    }

    "Oppiaineita syötetty kun oppija jää luokalle -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(vuosiluokkasuoritus.copy(jääLuokalle = true, osasuoritukset = kaikkiAineet)))) {
        verifyResponseStatusOk()
      }
    }
  }

  "Alkamispäivä, skeemassa alkamispäivä on määritelty optionaaliseksi" - {
    "Suorituksella ei ole alkamispäivää -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(vuosiluokkasuoritus.copy(alkamispäivä = None)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.alkamispäiväPuuttuu("Suoritukselle perusopetuksenluokkaaste/9 ei ole merkitty alkamispäivää"))
      }
    }
  }

  "Jos oppilas jää luokalle, vahvistetulta vuosiluokan suoritukselta ei vaadita osasuorituksia" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(PerusopetusExampleData.seitsemännenLuokanLuokallejääntiSuoritus.copy(osasuoritukset = None)))) {
      verifyResponseStatusOk()
    }
  }

  "Jos oppilaalle on merkitty vuosiluokkiin sitomaton opetus, vahvistetulta vuosiluokan suoritukselta ei vaadita osasuorituksia" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
      suoritukset = List(PerusopetusExampleData.seitsemännenLuokanSuoritus.copy(osasuoritukset = None)),
      lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
        vuosiluokkiinSitoutumatonOpetus = true
      ))
    )) {
      verifyResponseStatusOk()
    }
  }

  "Jos oppilaalle ei ole merkitty vuosiluokkiin sitomatonta opetusta, vahvistetulta vuosiluokan suoritukselta vaaditaan osasuorituksia" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
      suoritukset = List(PerusopetusExampleData.seitsemännenLuokanSuoritus.copy(osasuoritukset = None)),
      lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
        vuosiluokkiinSitoutumatonOpetus = false
      ))
    )) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.oppiaineetPuuttuvat("Suorituksella ei ole osasuorituksena yhtään oppiainetta, vaikka sillä on vahvistus, eikä oppija ole vuosiluokkiin sitomattomassa opetuksessa."))
    }
  }
}
