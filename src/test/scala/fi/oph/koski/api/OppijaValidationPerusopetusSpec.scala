package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import mojave._
import shapeless.Lens

// Perusopetuksen validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"

  "Suoritusten tila" - {
    "Vahvistettu päättötodistus -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatus(200)
      }
    }

    "Vahvistettu päättötodistus ilman yhtään oppiainetta -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(Nil))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.oppiaineetPuuttuvat("Suorituksella ei ole osasuorituksena yhtään oppiainetta, vaikka sen tila on VALMIS"))
      }
    }

    "Vahvistamaton päättötodistus ilman yhtään oppiainetta -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None, osasuoritukset = Some(Nil))))) {
        verifyResponseStatus(200)
      }
    }

    "Vahvistettu päättötodistus keskeneräisellä oppiaineella -> HTTP 400" in {
      val oppiaineidenArvioinnit = traversal[PerusopetuksenOpiskeluoikeus]
        .field[List[PerusopetuksenPäätasonSuoritus]]("suoritukset")
        .items
        .field[Option[List[Suoritus]]]("osasuoritukset")
        .items.items
        .field[Option[List[Arviointi]]]("arviointi")

      putOpiskeluoikeus(oppiaineidenArvioinnit.set(defaultOpiskeluoikeus)(None)) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella koulutus/201101 on keskeneräinen osasuoritus koskioppiaineetyleissivistava/AI"))
      }
    }

  }
}