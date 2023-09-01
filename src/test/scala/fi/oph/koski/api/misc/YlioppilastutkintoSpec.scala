package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import fi.oph.koski.schema.{YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonSuoritus}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class YlioppilastutkintoSpec extends AnyFreeSpec with Matchers with OpiskeluoikeusTestMethodsYlioppilastutkinto with KoskiHttpSpec with SearchTestMethods {
  "Ylioppilastutkinnot" - {
    "Lisättäessä/päivitettäessä" - {
      "palautetaan HTTP 501" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(501, KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä"))
        }
      }
    }

    "Haettaessa henkilötunnuksella" - {
      "Jos henkilöä ei löydy henkilöpalvelusta" - {
        "Haetaan YTR:stä ja luodaan henkilö" in {
          searchForHenkilötiedot("250493-602S").map(_.kokonimi) should equal(List("Christian Aalto"))
        }
        "Seuraavalla haulla käytetään aiemmin luotua henkilöä" in {
          searchForHenkilötiedot("250493-602S").map(_.oid) should equal(searchForHenkilötiedot("250493-602S").map(_.oid))
        }
      }
    }

    "Haettaessa opintotietoja" - {
      "Konvertoidaan YTR-järjestelmän tiedot Koski-järjestelmän opiskeluoikeudeksi" in {
        val oikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.ylioppilas.oid)
        oikeudet.length should equal(1)

        oikeudet(0).tyyppi.koodiarvo should equal("ylioppilastutkinto")
        oikeudet(0).suoritukset.length should equal(1)
        val tutkintoSuoritus: YlioppilastutkinnonSuoritus = oikeudet(0).asInstanceOf[YlioppilastutkinnonOpiskeluoikeus].suoritukset(0)
        tutkintoSuoritus.valmis should equal(true)
        tutkintoSuoritus.osasuoritusLista.length should equal(5)
        tutkintoSuoritus.osasuoritusLista.foreach { koeSuoritus =>
          koeSuoritus.valmis should equal(true)
        }
      }

      "Suoritus on valmis myös silloin kun oppilaitostieto puuttuu" in {
        // huom, pääkäyttäjä koska tätä oppijaa ei voi yhdistää mihinkään oppilaitokseen/organisaatioon
        val oikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.ylioppilasEiOppilaitosta.oid, user = paakayttaja)
        oikeudet.length should equal(1)

        oikeudet(0).oppilaitos should equal(None)
        val tutkintoSuoritus: YlioppilastutkinnonSuoritus = oikeudet(0).asInstanceOf[YlioppilastutkinnonOpiskeluoikeus].suoritukset(0)
        tutkintoSuoritus.valmis should equal(true)
      }
    }

  }
}
