package fi.oph.koski.massaluovutus

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
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

  private def raporttiKyselyOnnistuu(query: MassaluovutusQueryParameters, user: UserWithPassword): Unit = {
    val queryId = addQuerySuccessfully(query, user) { response =>
      response.status should equal(QueryState.pending)
      response.queryId
    }
    val complete = waitForCompletion(queryId, user)

    complete.files should have length 1
    complete.files.foreach(verifyResult(_, user))

    val raportitService = new RaportitService(app)
    complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
  }

  private def raporttiKyselyEiOnnistu(query: MassaluovutusQueryParameters, user: UserWithPassword): Unit = {
    addQuery(query, user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden())
    }
  }

  // Lukio

  "Lukion suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryLukionSuoritustiedot(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Lukio 2019 suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryLukio2019Suoritustiedot(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Lukion kurssikertymät" - {
    val query = MassaluovutusQueryLukioKurssikertymat(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Lukio 2019 opintopistekertymät" - {
    val query = MassaluovutusQueryLukio2019Opintopistekertymat(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Lukio/DIA/IB/International/ESH opiskelijamäärät" - {
    val query = MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat(
      paiva = LocalDate.of(2016, 1, 15),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  // Ammatillinen

  "Ammatillinen opiskelijavuositiedot" - {
    val query = MassaluovutusQueryAmmatillinenOpiskelijavuositiedot(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "Ammatillinen tutkinto suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "Ammatillinen osittainen tutkinto suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "Muu ammatillinen koulutus" - {
    val query = MassaluovutusQueryMuuAmmatillinen(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "TOPKS ammatillinen koulutus" - {
    val query = MassaluovutusQueryTOPKSAmmatillinen(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  // Perusopetus

  "Perusopetuksen vuosiluokka" - {
    val query = MassaluovutusQueryPerusopetuksenVuosiluokka(
      paiva = LocalDate.of(2012, 1, 1),
      vuosiluokka = "9",
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Perusopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryPerusopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2012, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Aikuisten perusopetuksen suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryAikuistenPerusopetusSuoritustiedot(
      alku = LocalDate.of(2008, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      raportinTyyppi = "päättövaihe",
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Aikuisten perusopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2010, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Aikuisten perusopetuksen kurssikertymät" - {
    val query = MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma(
      alku = LocalDate.of(2008, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  // Esiopetus

  "Esiopetus" - {
    val query = MassaluovutusQueryEsiopetus(
      paiva = LocalDate.of(2012, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Esiopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryEsiopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2012, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  // Muut

  "Perusopetukseen valmistava opetus" - {
    val query = MassaluovutusQueryPerusopetukseenValmistava(
      alku = LocalDate.of(2008, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "TUVA perusopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2022, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "TUVA suoritustiedot" - {
    val query = MassaluovutusQueryTuvaSuoritustiedot(
      alku = LocalDate.of(2021, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "IB-tutkinnon suoritustiedot" - {
    val query = MassaluovutusQueryIBSuoritustiedot(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      raportinTyyppi = "ibtutkinto",
      organisaatioOid = Some(MockOrganisaatiot.ressunLukio),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.paakayttaja)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Vapaan sivistystyön JOTPA" - {
    val query = MassaluovutusQueryVSTJOTPA(
      alku = LocalDate.of(2022, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.varsinaisSuomenKansanopisto),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.varsinaisSuomiPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Muu kuin säännelty koulutus" - {
    val query = MassaluovutusQueryMuuKuinSaanneltyKoulutus(
      alku = LocalDate.of(2022, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.MuuKuinSäänneltyKoulutusToimija.oppilaitos),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.muuKuinSäänneltyKoulutusYritys)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Lukioon valmistavan koulutuksen opiskelijamäärät" - {
    val query = MassaluovutusQueryLuvaOpiskelijamaarat(
      paiva = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.ressunLukio),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.paakayttaja)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Perusopetuksen lisäopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2012, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }
}
