package fi.oph.koski.api

import fi.oph.koski.documentation.PerusopetusExampleData.{suoritus, _}
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
        verifyResponseStatusOk()
      }
    }

    "Vahvistettu päättötodistus ilman yhtään oppiainetta -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(Nil))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.oppiaineetPuuttuvat("Suorituksella ei ole osasuorituksena yhtään oppiainetta, vaikka sillä on vahvistus"))
      }
    }

    "Vahvistamaton päättötodistus ilman yhtään oppiainetta -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None, osasuoritukset = Some(Nil))))) {
        verifyResponseStatusOk()
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

    "Kaksi samaa oppiainetta" - {
      "Identtisillä tiedoilla -> HTTP 400" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9)),
          suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9))
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran ryhmässä pakolliset"))
        }
      }
      "Eri kielivalinnalla -> HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9)),
          suoritus(äidinkieli("AI2")).copy(arviointi = arviointi(9))
        )))))) {
          verifyResponseStatusOk()
        }
      }
      "Valinnaisissa oppiaineissa -> HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          suoritus(äidinkieli("AI1").copy(pakollinen = false)).copy(arviointi = arviointi(9)),
          suoritus(äidinkieli("AI1").copy(pakollinen = false)).copy(arviointi = arviointi(9))
        )))))) {
          verifyResponseStatusOk()
        }
      }
    }
  }
}