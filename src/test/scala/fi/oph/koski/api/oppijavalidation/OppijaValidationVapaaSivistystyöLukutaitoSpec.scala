package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

class OppijaValidationVapaaSivistystyöLukutaitoSpec extends TutkinnonPerusteetTest[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "Lukutaitokoulutus" - {
    "Päätason suorituksen laajuus lasketaan automaattisesti osasuoritusten laajuuksista" in {
      val opiskeluoikeus = opiskeluoikeusLukutaito.withSuoritukset(List(
        suoritusLukutaito.withOsasuoritukset(Some(List(
          vapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(laajuus = LaajuusOpintopisteissä(60)),
          vapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(laajuus = LaajuusOpintopisteissä(9))
        )))
      ))
      val result = putAndGetOpiskeluoikeus(opiskeluoikeus)
      result.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe(69)
    }

    "Opiskeluoikeus ei voi käyttää tilaa 'hyvaksytystisuoritettu'" in {
      val opiskeluoikeus = opiskeluoikeusLukutaito.withSuoritukset(List(
        suoritusLukutaito.withOsasuoritukset(Some(List(
          vapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(laajuus = LaajuusOpintopisteissä(60)),
          vapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(laajuus = LaajuusOpintopisteissä(9))
        )))
      )).withTila(
        VapaanSivistystyönOpiskeluoikeudenTila(
          List(
            OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusHyväksytystiSuoritettu)
          )
        )
      )

      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
      }
    }

    "Opiskeluoikeus ei voi käyttää tilaa 'keskeytynyt'" in {
      val opiskeluoikeus = opiskeluoikeusLukutaito.withSuoritukset(List(
        suoritusLukutaito.withOsasuoritukset(Some(List(
          vapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(laajuus = LaajuusOpintopisteissä(60)),
          vapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(laajuus = LaajuusOpintopisteissä(9))
        )))
      )).withTila(
        VapaanSivistystyönOpiskeluoikeudenTila(
          List(
            OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusKeskeytynyt)
          )
        )
      )

      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
      }
    }

    "Ei voi tallentaa väärällä perusteen diaarinumerolla" in {
      val oo = opiskeluoikeusLukutaito.withSuoritukset(
        List(
          suoritusLukutaito.withKoulutusmoduuli(
            VapaanSivistystyönLukutaitokoulutus(perusteenDiaarinumero = Some("OPH-5410-2021")) // ajoneuvoalan perustutkinto
          )
      ))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Suoritukselle VapaanSivistystyönLukutaitokoulutus ei voi käyttää opiskeluoikeuden voimassaoloaikana voimassaollutta perustetta OPH-5410-2021 (7614470), jonka koulutustyyppi on 1(Ammatillinen perustutkinto). Tälle suoritukselle hyväksytyt perusteen koulutustyypit ovat 35(Vapaan sivistystyön lukutaitokoulutus)."))
      }
    }
  }

  private def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): Opiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusLukutaito

  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): VapaanSivistystyönOpiskeluoikeus = {
    opiskeluoikeusLukutaito.withSuoritukset(
      List(
        suoritusLukutaito.withKoulutusmoduuli(VapaanSivistystyönLukutaitokoulutus(perusteenDiaarinumero = diaari))
      )
    ) match {
      case v: VapaanSivistystyönOpiskeluoikeus => v
    }
  }

  override def eperusteistaLöytymätönValidiDiaarinumero: String = "6/011/2015"
}
