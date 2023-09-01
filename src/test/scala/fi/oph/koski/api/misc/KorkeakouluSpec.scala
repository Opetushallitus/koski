package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class KorkeakouluSpec extends AnyFreeSpec with Matchers with OpiskeluoikeusTestMethodsKorkeakoulu with SearchTestMethods with KoskiHttpSpec {
  "Korkeakoulun opiskeluoikeudet" - {
    "Lisättäessä/päivitettäessä" - {
      "palautetaan HTTP 501" in {
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
          getOpiskeluoikeudet(KoskiSpecificMockOppijat.korkeakoululainen.oid).flatMap(_.suoritukset).filter(_.koulutusmoduuli.isTutkinto).map(_.valmis) should equal(List(false))
        }
        "Valitaan uusimman opiskeluoikeusjakson nimi" in {
          getOpiskeluoikeudet(KoskiSpecificMockOppijat.amkKesken.oid).flatMap(_.suoritukset).filter(_.koulutusmoduuli.isTutkinto).map(_.koulutusmoduuli.nimi.get("fi")) should equal(List("Medianomi (AMK)"))
        }
      }
      "Valmis tutkinto" - {
        "Näytetään valmiina" in {
          getOpiskeluoikeudet(KoskiSpecificMockOppijat.dippainssi.oid).flatMap(_.suoritukset).filter(_.koulutusmoduuli.isTutkinto).map(_.valmis) should equal(List(true))
        }
        "Kun useampi kuin yksi opiskeluoikeusjakso" in {
          val opiskeluoikeus = getOpiskeluoikeudet(KoskiSpecificMockOppijat.montaJaksoaKorkeakoululainen.oid).find(_.oppilaitos.exists(_.oid == MockOrganisaatiot.aaltoYliopisto)).get
          opiskeluoikeus.suoritukset.head.koulutusmoduuli.nimi.get("fi") should equal("Fil. maist., fysiikka")
        }
      }

      "Opiskeluoikeusjaksoilta voi löytyä tarkentava nimi" in {
        val opiskeluoikeus = opiskeluoikeudet("250668-293Y", "02470").head

        opiskeluoikeus.tila.opiskeluoikeusjaksot.head.nimi.get.values.values.toList should equal (List("Tieto- ja viestintätekniikan tutkinto-ohjelma", "Examensprogram inom informations- och kommunikationsteknik", "Information Technology Master's Programme"))
      }

      "Jos Virran Jakso-tietorakenteessa on määritelty nimi, sitä käytetään myös tutkinnon nimenä" in {
        val opiskeluoikeus = opiskeluoikeudet("250668-293Y", "02470").head

        opiskeluoikeus.suoritukset.head.koulutusmoduuli.nimi should equal (Finnish("Tieto- ja viestintätekniikan tutkinto-ohjelma",Some("Examensprogram inom informations- och kommunikationsteknik"),Some("Information Technology Master's Programme")))
      }
    }

    "Maksettavat lukuvuosimaksutiedot" - {
      "Koski näyttää maksutiedot" in {
        val opiskeluoikeus = opiskeluoikeudet("250668-293Y", "02470").head

        val maksettavatLukuvuosimaksut = opiskeluoikeus.lisätiedot.get.maksettavatLukuvuosimaksut.get.head
        maksettavatLukuvuosimaksut.alku.toString should equal("2015-10-20")
        maksettavatLukuvuosimaksut.loppu.get.toString should equal("2016-04-12")
        maksettavatLukuvuosimaksut.summa.get should equal (4000)
      }
    }

    "Haettaessa" - {
      "Konvertoidaan Virta-järjestelmän opiskeluoikeus" in {
        val oikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.dippainssi.oid)
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
        val oikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.virtaOppijaHetuton.henkilö.oid)
        oikeudet.length should equal(1)
      }

      "Linkitetyn oppijan slave tiedoilla löytyy" in {
        val oikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.virtaOppija.oid)
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

        "Lukuvuosimaksutiedot löytyvät" in {
          val opiskeluoikeus = opiskeluoikeudet("250668-293Y", "10076").head

          val maksutiedot = opiskeluoikeus.lisätiedot.get.lukukausiIlmoittautuminen.get.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.getOrElse("").toString)
          maksutiedot should equal(List("Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(Some(true),Some(2000),Some(2000))", "", "", "", "", ""))
        }
      }

      "Laajuudet" - {
        "Osasuoritusten laajuudet lasketaan yhteen jos laajuutta ei tule datassa" in {
          val oo = getOpiskeluoikeudet(KoskiSpecificMockOppijat.montaJaksoaKorkeakoululainen.oid).find(_.suoritukset.forall(_.tyyppi.koodiarvo == "korkeakoulunopintojakso")).get
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
