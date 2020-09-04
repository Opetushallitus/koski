package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import org.scalatest.{FreeSpec, Matchers}

class KorkeakouluSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethodsKorkeakoulu with SearchTestMethods with LocalJettyHttpSpecification {
  "Korkeakoulun opiskeluoikeudet" - {
    "Lisättäessä/päivitettäessä" - {
      "palautetaan HTTP 501" in {
        resetFixtures
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(501, KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä"))
        }
      }
    }

    "Haettaessa henkilötunnuksella" - {
      "Jos henkilöä ei löydy henkilöpalvelusta" - {
        "Haetaan Virrasta ja luodaan henkilö" in {
          searchForHenkilötiedot("250668-293Y").map(_.kokonimi) should equal(List("Harri Koskinen"))
        }
        "Seuraavalla haulla käytetään aiemmin luotua henkilöä" in {
          searchForHenkilötiedot("250668-293Y").map(_.oid) should equal(searchForHenkilötiedot("250668-293Y").map(_.oid))
        }
      }
    }

    "Suoritusten tilat" - {
      "Keskeneräinen tutkinto" - {
        "Näytetään keskeneräisenä" in {
          getOpiskeluoikeudet(MockOppijat.korkeakoululainen.oid).flatMap(_.suoritukset).filter(_.koulutusmoduuli.isTutkinto).map(_.valmis) should equal(List(false))
        }
        "Valitaan uusimman opiskeluoikeusjakson nimi" in {
          getOpiskeluoikeudet(MockOppijat.amkKesken.oid).flatMap(_.suoritukset).filter(_.koulutusmoduuli.isTutkinto).map(_.koulutusmoduuli.nimi.get("fi")) should equal(List("Medianomi (AMK)"))
        }
      }
      "Valmis tutkinto" - {
        "Näytetään valmiina" in {
          getOpiskeluoikeudet(MockOppijat.dippainssi.oid).flatMap(_.suoritukset).filter(_.koulutusmoduuli.isTutkinto).map(_.valmis) should equal(List(true))
        }
        "Kun useampi kuin yksi opiskeluoikeusjakso" in {
          val opiskeluoikeus = getOpiskeluoikeudet(MockOppijat.montaJaksoaKorkeakoululainen.oid).find(_.oppilaitos.exists(_.oid == MockOrganisaatiot.aaltoYliopisto)).get
          opiskeluoikeus.suoritukset.head.koulutusmoduuli.nimi.get("fi") should equal("Fil. maist., fysiikka")
        }
      }
    }

    "Haettaessa" - {
      "Konvertoidaan Virta-järjestelmän opiskeluoikeus" in {
        val oikeudet = getOpiskeluoikeudet(MockOppijat.dippainssi.oid)
        oikeudet.length should equal(2)

        oikeudet(0).tyyppi.koodiarvo should equal("korkeakoulutus")
        oikeudet(0).suoritukset.length should equal(1)
        oikeudet(0).asInstanceOf[KorkeakoulunOpiskeluoikeus].suoritukset.map(_.valmis) should equal(List(true))

        oikeudet(1).tyyppi.koodiarvo should equal("korkeakoulutus")
        oikeudet(1).suoritukset.length should equal(8)
        oikeudet(1).asInstanceOf[KorkeakoulunOpiskeluoikeus].suoritukset.map(_.valmis) foreach {
          _ should equal(true)
        }
      }

      "Hetuttoman opiskeluoikeudet löytyy oidilla" in {
        val oikeudet = getOpiskeluoikeudet(MockOppijat.virtaOppijaHetuton.henkilö.oid)
        oikeudet.length should equal(1)
      }

      "Linkitetyn oppijan slave tiedoilla löytyy" in {
        val oikeudet = getOpiskeluoikeudet(MockOppijat.virtaOppija.oid)
        oikeudet.length should equal(2)
      }

      "Ilmoittautumisjaksot" - {
        "Kohdennettu opiskeluoikeusavaimen perusteella" in {
          val opiskeluoikeus = opiskeluoikeudet("250668-293Y", "10076").head

          val ilmoittautumisjaksot = opiskeluoikeus.lisätiedot.get.lukukausiIlmoittautuminen.get.ilmoittautumisjaksot.map(_.alku.toString)
          ilmoittautumisjaksot should equal(List("2008-08-01", "2009-01-01", "2009-08-01", "2010-01-01", "2010-08-01", "2011-01-01"))
        }

        "Kohdennettu myöntäjän ja päivämäärän perusteella" in {
          val opiskeluoikeus = opiskeluoikeudet("250668-293Y", "10088")
            .find(_.tila.opiskeluoikeusjaksot.lastOption.exists(_.tila.koodiarvo == "1"))
            .get

          val ilmoittautumisjaksot = opiskeluoikeus.lisätiedot.get.lukukausiIlmoittautuminen.get.ilmoittautumisjaksot.map(_.alku.toString)
          ilmoittautumisjaksot should equal(List("2012-08-01", "2013-01-01", "2013-08-01", "2014-01-01", "2014-08-01", "2015-01-01", "2015-08-01", "2016-01-01"))
        }

        "Inaktiiviseen opiskeluoikeuteen ei kohdenneta ilmoittautumisjaksoja" in {
          val opiskeluoikeus = opiskeluoikeudet("250668-293Y", "10088")
            .find(_.tila.opiskeluoikeusjaksot.lastOption.exists(_.tila.koodiarvo == "2"))
            .get

          opiskeluoikeus.lisätiedot.get.lukukausiIlmoittautuminen should equal(None)
        }
      }

      "Laajuudet" - {
        "Osasuoritusten laajuudet lasketaan yhteen jos laajuutta ei tule datassa" in {
          val oo = getOpiskeluoikeudet(MockOppijat.montaJaksoaKorkeakoululainen.oid).find(_.suoritukset.forall(_.tyyppi.koodiarvo == "korkeakoulunopintojakso")).get
          oo.suoritukset.collect { case s: KorkeakoulunOpintojaksonSuoritus => s.koulutusmoduuli.laajuus }.flatten.map(_.arvo).sum should be(414.0f)
        }
      }
    }
  }

  private def opiskeluoikeudet(hetu: String, myöntäjä: String) = {
    oppijaByHetu(hetu).opiskeluoikeudet
      .collect { case o: KorkeakoulunOpiskeluoikeus => o }
      .filter(_.oppilaitos.exists(_.oppilaitosnumero.exists(_.koodiarvo == myöntäjä)))
  }
}
