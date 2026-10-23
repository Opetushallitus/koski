package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä}
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
      val result = setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus)
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

      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
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

      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
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

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Suoritukselle VapaanSivistystyönLukutaitokoulutus ei voi käyttää opiskeluoikeuden voimassaoloaikana voimassaollutta perustetta OPH-5410-2021 (7614470), jonka koulutustyyppi on 1(Ammatillinen perustutkinto). Tälle suoritukselle hyväksytyt perusteen koulutustyypit ovat 35(Vapaan sivistystyön lukutaitokoulutus)."))
      }
    }

    "Viimeinen sallittu alkamispäivä 31.12.2026" - {
      def jakso(päivä: java.time.LocalDate, tila: Koodistokoodiviite) =
        OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(päivä, tila)

      def alkanutJaEronnut(alku: java.time.LocalDate, loppu: java.time.LocalDate) = opiskeluoikeusLukutaito.copy(
        arvioituPäättymispäivä = None,
        tila = VapaanSivistystyönOpiskeluoikeudenTila(List(jakso(alku, opiskeluoikeusLäsnä), jakso(loppu, opiskeluoikeusKatsotaanEronneeksi))),
        suoritukset = List(suoritusLukutaito.copy(osasuoritukset = None, vahvistus = None)),
      )

      "Opiskeluoikeus voi alkaa 31.12.2026 ja jatkua sen jälkeen" in {
        setupOppijaWithOpiskeluoikeus(alkanutJaEronnut(date(2026, 12, 31), date(2027, 6, 1))) {
          verifyResponseStatusOk()
        }
      }

      "Opiskeluoikeus ei voi alkaa 1.1.2027" in {
        setupOppijaWithOpiskeluoikeus(alkanutJaEronnut(date(2027, 1, 1), date(2027, 6, 1))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoJaLukutaitoAlkamispäivä(
            "Vapaan sivistystyön kotoutumiskoulutuksen tai lukutaitokoulutuksen opiskeluoikeus ei voi alkaa 1.1.2027 tai sen jälkeen"
          ))
        }
      }
    }
  }

  private def setupOppijaWithAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): Opiskeluoikeus = setupOppijaWithOpiskeluoikeus(oo) {
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
