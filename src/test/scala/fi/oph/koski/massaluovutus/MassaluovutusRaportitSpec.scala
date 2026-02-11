package fi.oph.koski.massaluovutus

import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.massaluovutus.raportit._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.util.Wait
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.nio.charset.StandardCharsets
import java.time.LocalDate

class MassaluovutusRaportitSpec extends AnyFreeSpec with MassaluovutusTestMethods with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  override protected def beforeEach(): Unit = {
    resetFixturesSkipInvalidOpiskeluoikeudet()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Wait.until { !app.massaluovutusService.hasWork }
    app.massaluovutusService.truncate()
  }

  "Lukion suoritustietojen tarkistus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryLukionSuoritustiedot(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2012, 1, 1),
        loppu = LocalDate.of(2016, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryLukionSuoritustiedot].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Lukio 2019 suoritustietojen tarkistus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryLukio2019Suoritustiedot(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2012, 1, 1),
        loppu = LocalDate.of(2016, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryLukio2019Suoritustiedot].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Lukion kurssikertymät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryLukioKurssikertymat(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2012, 1, 1),
        loppu = LocalDate.of(2016, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryLukioKurssikertymat].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Lukio 2019 opintopistekertymät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryLukio2019Opintopistekertymat(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2012, 1, 1),
        loppu = LocalDate.of(2016, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryLukio2019Opintopistekertymat].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Lukio/DIA/IB/International/ESH opiskelijamäärät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2016, 1, 15),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Ammatillinen opiskelijavuositiedot" - {

    "Spreadsheet" - {
      val query = MassaluovutusQueryAmmatillinenOpiskelijavuositiedot(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2016, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.stadinAmmattiopistoPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryAmmatillinenOpiskelijavuositiedot].organisaatioOid should contain(MockOrganisaatiot.stadinAmmattiopisto)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Ammatillinen tutkinto suoritustietojen tarkistus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2016, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.stadinAmmattiopistoPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot].organisaatioOid should contain(MockOrganisaatiot.stadinAmmattiopisto)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Ammatillinen osittainen tutkinto suoritustietojen tarkistus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2016, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.stadinAmmattiopistoPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot].organisaatioOid should contain(MockOrganisaatiot.stadinAmmattiopisto)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Muu ammatillinen koulutus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryMuuAmmatillinen(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2016, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.stadinAmmattiopistoPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryMuuAmmatillinen].organisaatioOid should contain(MockOrganisaatiot.stadinAmmattiopisto)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "TOPKS ammatillinen koulutus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryTOPKSAmmatillinen(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2016, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.stadinAmmattiopistoPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryTOPKSAmmatillinen].organisaatioOid should contain(MockOrganisaatiot.stadinAmmattiopisto)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Perusopetuksen vuosiluokka" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryPerusopetuksenVuosiluokka(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2012, 1, 1),
        vuosiluokka = "9",
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryPerusopetuksenVuosiluokka].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Perusopetuksen oppijamäärät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryPerusopetuksenOppijamaaratRaportti(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2012, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryPerusopetuksenOppijamaaratRaportti].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Aikuisten perusopetuksen suoritustietojen tarkistus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryAikuistenPerusopetusSuoritustiedot(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2008, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
        raportinTyyppi = "päättövaihe",
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryAikuistenPerusopetusSuoritustiedot].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Aikuisten perusopetuksen oppijamäärät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2010, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Aikuisten perusopetuksen kurssikertymät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2008, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Esiopetus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryEsiopetus(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2012, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryEsiopetus].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Esiopetuksen oppijamäärät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryEsiopetuksenOppijamaaratRaportti(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2012, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryEsiopetuksenOppijamaaratRaportti].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Perusopetukseen valmistava opetus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryPerusopetukseenValmistava(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2008, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryPerusopetukseenValmistava].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "TUVA perusopetuksen oppijamäärät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2022, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.stadinAmmattiopistoPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti].organisaatioOid should contain(MockOrganisaatiot.stadinAmmattiopisto)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "TUVA suoritustiedot" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryTuvaSuoritustiedot(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2021, 1, 1),
        loppu = LocalDate.of(2025, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.stadinAmmattiopistoPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryTuvaSuoritustiedot].organisaatioOid should contain(MockOrganisaatiot.stadinAmmattiopisto)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "IB-tutkinnon suoritustiedot" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryIBSuoritustiedot(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2012, 1, 1),
        loppu = LocalDate.of(2025, 1, 1),
        raportinTyyppi = "ibtutkinto",
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.paakayttaja
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.ressunLukio)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryIBSuoritustiedot].organisaatioOid should contain(MockOrganisaatiot.ressunLukio)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Vapaan sivistystyön JOTPA" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryVSTJOTPA(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2022, 1, 1),
        loppu = LocalDate.of(2025, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.varsinaisSuomiPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.varsinaisSuomenKansanopisto)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryVSTJOTPA].organisaatioOid should contain(MockOrganisaatiot.varsinaisSuomenKansanopisto)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Muu kuin säännelty koulutus" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryMuuKuinSaanneltyKoulutus(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2022, 1, 1),
        loppu = LocalDate.of(2025, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.muuKuinSäänneltyKoulutusYritys
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.MuuKuinSäänneltyKoulutusToimija.oppilaitos)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryMuuKuinSaanneltyKoulutus].organisaatioOid should contain(MockOrganisaatiot.MuuKuinSäänneltyKoulutusToimija.oppilaitos)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Lukioon valmistavan koulutuksen opiskelijamäärät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryLuvaOpiskelijamaarat(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2016, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.paakayttaja
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.ressunLukio)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryLuvaOpiskelijamaarat].organisaatioOid should contain(MockOrganisaatiot.ressunLukio)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Perusopetuksen lisäopetuksen oppijamäärät" - {
    "Spreadsheet" - {
      val query = MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti(
        format = QueryFormat.xlsx,
        paiva = LocalDate.of(2012, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
        val queryId = addQuerySuccessfully(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti].organisaatioOid should contain(MockOrganisaatiot.jyväskylänNormaalikoulu)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }
}
