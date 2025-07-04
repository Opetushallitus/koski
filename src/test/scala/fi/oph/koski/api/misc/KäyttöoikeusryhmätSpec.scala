package fi.oph.koski.api.misc

import fi.oph.koski.db.KoskiTables
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData.suomenKieli
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExamplesEsiopetus}
import fi.oph.koski.fixture.AmmatillinenOpiskeluoikeusTestData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers.{korkeakouluViranomainen, perusopetusViranomainen, toinenAsteViranomainen, viranomainenGlobaaliKatselija}
import fi.oph.koski.koskiuser._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotResponse
import fi.oph.koski.schema._
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiHttpSpec}
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate

class KäyttöoikeusryhmätSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsAmmatillinen
    with SearchTestMethods
    with QueryTestMethods
    with DatabaseTestMethods
    with DirtiesFixtures {

  // hetu -> oo
  private def resetTestioppijat(): Map[String, AmmatillinenOpiskeluoikeus] = {
    val tyhjennettävät = Seq(
      KoskiSpecificMockOppijat.tyhjä
    )
    tyhjennettävät.foreach(poistaOppijanOpiskeluoikeusDatat)

    val setupattavat = Seq(
      (KoskiSpecificMockOppijat.eero, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, versio = Some(11))),
      (KoskiSpecificMockOppijat.ammattilainen, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis()),
      (KoskiSpecificMockOppijat.markkanen, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia, versio = Some(11)).copy(ostettu = true)),
    )

    setupattavat.map {
      case (oppija, opiskeluoikeus) =>
        oppija.hetu.get -> setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus, oppija, MockUsers.paakayttaja)
    }.toMap
  }

  "koski-oph-pääkäyttäjä" - {
    val user = MockUsers.paakayttaja
    "voi muokata kaikkia opiskeluoikeuksia" in {
      val tietokannassaOlevaOo = resetTestioppijat()(defaultHenkilö.hetu)

      putOpiskeluoikeus(
        defaultOpiskeluoikeus.withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatusOk()
      }
    }

    "voi hakea kaikkia opiskeluoikeuksia" in {
      searchForNames("eero", user) should equal(List("Jouni Çelik-Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }

    "voi hakea ja katsella kaikkia opiskeluoikeuksia" in {
      queryOppijat(user = user).length should equal(koskeenTallennetutOppijatCount)
      authGet("api/oppija/" + KoskiSpecificMockOppijat.ammattilainen.oid, user) {
        verifyResponseStatusOk()
      }
    }
  }

  "koski-viranomainen-katselija" - {
    val user = MockUsers.viranomainen

    "ei voi muokata opiskeluoikeuksia" in {
      val tietokannassaOlevaOo = resetTestioppijat()(defaultHenkilö.hetu)

      putOpiskeluoikeus(
        defaultOpiskeluoikeus.withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.52251087186"))
      }
    }

    "voi hakea ja katsella kaikkia opiskeluoikeuksia" in {
      queryOppijat(user = user).length should equal(koskeenTallennetutOppijatCount)
      authGet("api/oppija/" + KoskiSpecificMockOppijat.ammattilainen.oid, user) {
        verifyResponseStatusOk()
      }
    }

    "voi hakea ja katsella ytr-ylioppilastutkintosuorituksia" in {
      haeOpiskeluoikeudetHetulla("250493-602S", user).count(_.tyyppi.koodiarvo == "ylioppilastutkinto") should equal(1)
    }

    "voi hakea ja katsella virta-ylioppilastutkintosuorituksia" in {
      haeOpiskeluoikeudetHetulla("250668-293Y", user).count(_.tyyppi.koodiarvo == "korkeakoulutus") should be >= 1
    }
  }

  "tallennusoikeudet muttei LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-roolia" - {
    val user = MockUsers.tallentajaEiLuottamuksellinen
    "ei voi muokata opiskeluoikeuksia" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        opiskeluoikeusLähdejärjestelmästäOmnia.withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = OidHenkilö(KoskiSpecificMockOppijat.markkanen.oid),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.51720121923"))
      }
    }
  }

  "koski-oppilaitos-palvelukäyttäjä jolla LUOTTAMUKSELLINEN_KAIKKI_TIEDOT käyttöoikeus" - {
    val user = MockUsers.omniaPalvelukäyttäjä
    "voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        opiskeluoikeusLähdejärjestelmästäOmnia
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = OidHenkilö(KoskiSpecificMockOppijat.markkanen.oid),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatusOk()
      }
    }

    "voi muokata vain lähdejärjestelmällisiä opiskeluoikeuksia" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        opiskeluoikeusOmnia
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = OidHenkilö(KoskiSpecificMockOppijat.markkanen.oid),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.lähdejärjestelmäIdPuuttuu("Käyttäjä on palvelukäyttäjä mutta lähdejärjestelmää ei ole määritelty"))
      }
    }

    "voi hakea ja katsella opiskeluoikeuksia vain omassa organisaatiossa" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia, versio = Some(11)).copy(ostettu = true)
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = KoskiSpecificMockOppijat.markkanen
      ) {
        verifyResponseStatusOk()
      }

      searchForNames("eero", user) should equal(List("Eéro Jorma-Petteri Markkanen-Fagerström"))
      authGet("api/oppija/" + KoskiSpecificMockOppijat.markkanen.oid, user) {
        verifyResponseStatusOk()
      }
    }

    "voi hakea opiskeluoikeuksia kyselyrajapinnasta" in {
      queryOppijat(user = user).map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].sukunimi) should equal(List("Markkanen-Fagerström", "Paallekkaisia"))
    }

    "ei voi muokata opiskeluoikeuksia muussa organisaatiossa" in {
      val tietokannassaOlevaOo = resetTestioppijat()(defaultHenkilö.hetu)

      putOpiskeluoikeus(
        defaultOpiskeluoikeus.withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.52251087186"))
      }
    }

    "ei voi katsella opiskeluoikeuksia muussa organisaatiossa" in {
      authGet("api/oppija/" + KoskiSpecificMockOppijat.eero.oid, user) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.00000000001 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }

    "voi hakea ja katsella ytr-ylioppilastutkintosuorituksia" - {
      "vain omassa organisaatiossaan" in {
        haeOpiskeluoikeudetHetulla("250493-602S", MockUsers.omniaPalvelukäyttäjä).count(_.tyyppi.koodiarvo == "ylioppilastutkinto") should equal(0)
        haeOpiskeluoikeudetHetulla("250493-602S", MockUsers.kalle).count(_.tyyppi.koodiarvo == "ylioppilastutkinto") should equal(1)
      }
    }

    "voi hakea ja katsella virta-ylioppilastutkintosuorituksia" - {
      "vain omassa organisaatiossaan" in {
        haeOpiskeluoikeudetHetulla("250668-293Y", MockUsers.omniaPalvelukäyttäjä).count(_.tyyppi.koodiarvo == "korkeakoulutus") should equal(0)
        haeOpiskeluoikeudetHetulla("250668-293Y", MockUsers.kalle).count(_.tyyppi.koodiarvo == "korkeakoulutus") should be >= 1
      }
    }

    "näkee luottamuksellisen datan" in {
      authGet("api/oppija/" + KoskiSpecificMockOppijat.markkanen.oid, user) {
        verifyResponseStatusOk()
        kaikkiSensitiveDataNäkyy()
      }
    }

    "alusta muutetut fixturet" in resetFixtures()
  }

  "vastuukäyttäjä" - {
    val user = MockUsers.stadinVastuukäyttäjä
    "ei näe luottamuksellista dataa" in {
      authGet("api/oppija/" + KoskiSpecificMockOppijat.eero.oid, user) {
        verifyResponseStatusOk()
        kaikkiSensitiveDataPiilotettu()
      }
    }
  }

  "palvelukäyttäjä, jolla useampi juuriorganisaatio" - {
    "voi tallentaa tietoja" in {
      val tietokannassaOlevaOo = resetTestioppijat()(defaultHenkilö.hetu)

      putOpiskeluoikeus(
        opiskeluoikeusLähdejärjestelmästäOmnia
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        headers = authHeaders(MockUsers.kahdenOrganisaatioPalvelukäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatusOk()
      }
    }
  }

  "koski-oppilaitos-katselija" - {
    val user = MockUsers.omniaKatselija
    "ei voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        opiskeluoikeusOmnia.withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = OidHenkilö(KoskiSpecificMockOppijat.markkanen.oid),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.51720121923"))
      }
    }

    "voi hakea ja katsella opiskeluoikeuksia omassa organisaatiossa" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia, versio = Some(11)).copy(ostettu = true)
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = KoskiSpecificMockOppijat.markkanen
      ) {
        verifyResponseStatusOk()
      }

      searchForNames("eero", user) should equal(List("Eéro Jorma-Petteri Markkanen-Fagerström"))
      authGet("api/oppija/" + KoskiSpecificMockOppijat.markkanen.oid, user) {
        verifyResponseStatusOk()
        kaikkiSensitiveDataNäkyy()
      }
    }
  }

  "koski-oppilaitos-esiopetus-katselija" - {
    val user = MockUsers.jyväskylänKatselijaEsiopetus
    "ei voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      resetTestioppijat()

      putOpiskeluoikeus(
        ExamplesEsiopetus.opiskeluoikeus,
        henkilö = OidHenkilö(KoskiSpecificMockOppijat.markkanen.oid),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio(s"Ei oikeuksia organisatioon ${MockOrganisaatiot.jyväskylänNormaalikoulu}"))
      }
    }

    "voi hakea ja katsella esiopetuksen opiskeluoikeuksia omassa organisaatiossa" in {
      searchForNames(KoskiSpecificMockOppijat.eskari.etunimet, user) should equal(
        List(
          KoskiSpecificMockOppijat.esikoululainen2025,
          KoskiSpecificMockOppijat.eskari,
          KoskiSpecificMockOppijat.eskariAikaisillaLisätiedoilla,
        ).map(o => s"${o.etunimet} ${o.sukunimi}")
      )
      authGet("api/oppija/" + KoskiSpecificMockOppijat.eskari.oid, user) {
        verifyResponseStatusOk()
      }
    }

    "ei voi hakea ja katsella muita kuin esiopetuksen opiskeluoikeuksia omassa organisaatiossa" in {
      searchForNames("kaisa", user) should be(Nil)
      authGet("api/oppija/" + KoskiSpecificMockOppijat.koululainen.oid, user) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${KoskiSpecificMockOppijat.koululainen.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
  }

  "koski-oppilaitos-tallentaja" - {
    val user = MockUsers.omniaTallentaja
    "voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        opiskeluoikeusOmnia
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = OidHenkilö(KoskiSpecificMockOppijat.markkanen.oid),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatusOk()
      }
    }

    "ei voi tallentaa opiskeluoikeuksia käyttäen lähdejärjestelmä-id:tä" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        opiskeluoikeusLähdejärjestelmästäOmnia
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = OidHenkilö(KoskiSpecificMockOppijat.markkanen.oid),
        headers = authHeaders(user) ++ jsonContent
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.lähdejärjestelmäIdEiSallittu("Lähdejärjestelmä määritelty, mutta käyttäjä ei ole palvelukäyttäjä"))
      }
    }

    "ei voi muokata lähdejärjestelmän tallentamia opiskeluoikeuksia" - {
      val oppija = KoskiSpecificMockOppijat.tyhjä
      "ilman opiskeluoikeuden oid:ia luodaan uusi opiskeluoikeus" in {
        resetTestioppijat()

        setupOppijaWithOpiskeluoikeus(
          opiskeluoikeusLähdejärjestelmästäOmnia,
          henkilö = oppija,
          headers = authHeaders(MockUsers.omniaPalvelukäyttäjä) ++ jsonContent
        ) {
          verifyResponseStatusOk()
          haeOpiskeluoikeudetHetulla(oppija.hetu, user).count(_.tyyppi.koodiarvo == "ammatillinenkoulutus") should equal(1)
          // 2024-04-11 jälkeen opiskeluoikeusOmnia tulkitaan aiemman duplikaattiopiskeluoikeudeksi, joten jätetään validaatio tämän testin osalta ajamatta
          defaultKoskiApplication.validationContext.runWithoutValidations {
            putOpiskeluoikeus(
              opiskeluoikeusOmnia,
              henkilö = oppija,
              headers = authHeaders(user) ++ jsonContent
            ) {
              verifyResponseStatusOk()
              haeOpiskeluoikeudetHetulla(oppija.hetu, user).count(_.tyyppi.koodiarvo == "ammatillinenkoulutus") should equal(2)
            }
          }
        }
      }
      "opiskeluoikeus-oid:ia käytettäessä muutos estetään" in {
        val oid = haeOpiskeluoikeudetHetulla(oppija.hetu, user).filter(_.tyyppi.koodiarvo == "ammatillinenkoulutus").filter(_.lähdejärjestelmänId.isDefined)(0).oid.get
        defaultKoskiApplication.validationContext.runWithoutValidations {
          putOpiskeluoikeus(opiskeluoikeusOmnia.copy(oid = Some(oid)), henkilö = oppija, headers = authHeaders(user) ++ jsonContent) {
            verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden lähdejärjestelmäId:tä ei voi poistaa."))
          }
        }
      }
    }
  }

  "viranomainen jolla oikeudet kaikkiin koulutusmuotoihin muttei arkaluontoisiin tietoihin" - {
    "ei näe luottamuksellista dataa" in {
      authGet("api/oppija/" + KoskiSpecificMockOppijat.eero.oid, viranomainenGlobaaliKatselija) {
        verifyResponseStatusOk()
        suppeaSensitiveDataPiilotettu()
        laajaSensitiveDataPiilotettu()
        erittäinSensitiveDataPiilotettu()
      }
    }

    "ei voi muokata opiskeluoikeuksia" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        opiskeluoikeusLähdejärjestelmästäOmnia
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = OidHenkilö(KoskiSpecificMockOppijat.markkanen.oid),
        headers = authHeaders(viranomainenGlobaaliKatselija) ++ jsonContent
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.51720121923"))
      }
    }

    "voi hakea kaikkia opiskeluoikeuksia" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia, versio = Some(11)).copy(ostettu = true)
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = KoskiSpecificMockOppijat.markkanen
      ) {
        verifyResponseStatusOk()
      }

      searchForNames("eero", viranomainenGlobaaliKatselija) should equal(List("Jouni Çelik-Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }

    "voi hakea ja katsella kaikkia opiskeluoikeuksia" in {
      queryOppijat(user = MockUsers.viranomainenGlobaaliKatselija).length should equal(koskeenTallennetutOppijatCount) //  - 2
      authGet("api/oppija/" + KoskiSpecificMockOppijat.ammattilainen.oid, viranomainenGlobaaliKatselija) {
        verifyResponseStatusOk()
      }
    }
  }

  "viranomainen jolla oikeudet vain perusopetukseen" - {
    "voi hakea perusopetuksen opiskeluoikeuksia" in {
      searchForNames("eero", perusopetusViranomainen) should be(empty)
      searchForNames("kaisa", perusopetusViranomainen) should be(List("Kaisa Koululainen", "Kaisa Kymppiluokkalainen"))
    }

    "näkee vain perusopetuksen opiskeluoikeudet" in {
      queryOppijat(user = perusopetusViranomainen).flatMap(_.opiskeluoikeudet).map(_.tyyppi.koodiarvo).toSet should be(Set("aikuistenperusopetus", "esiopetus", "europeanschoolofhelsinki", "internationalschool", "perusopetukseenvalmistavaopetus", "perusopetuksenlisaopetus", "perusopetus"))
    }

    "ei näe muun typpisiä opiskeluoikeuksia" in {
      authGet("api/oppija/" + KoskiSpecificMockOppijat.ammattilainen.oid, perusopetusViranomainen) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${KoskiSpecificMockOppijat.ammattilainen.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
  }

  "viranomainen jolla oikeudet toiseen asteeseen" - {
    "voi hakea toisen asteen opiskeluoikeuksia" in {
      val tietokannassaOlevaOo = resetTestioppijat()(KoskiSpecificMockOppijat.markkanen.hetu.get)

      putOpiskeluoikeus(
        AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia, versio = Some(11)).copy(ostettu = true)
          .withOidAndVersion(tietokannassaOlevaOo.oid, tietokannassaOlevaOo.versionumero),
        henkilö = KoskiSpecificMockOppijat.markkanen
      ) {
        verifyResponseStatusOk()
      }

      searchForNames("ylermi", toinenAsteViranomainen) should be(empty)
      searchForNames(KoskiSpecificMockOppijat.dippainssi.hetu.get, toinenAsteViranomainen) should be(empty)
      searchForNames(KoskiSpecificMockOppijat.ylioppilas.hetu.get, toinenAsteViranomainen) should equal(List("Ynjevi Ylioppilas"))
      searchForNames("eero", toinenAsteViranomainen) should equal(List("Jouni Çelik-Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }

    "näkee vain toisen asteen opiskeluoikeudet" in {
      queryOppijat(user = toinenAsteViranomainen).flatMap(_.opiskeluoikeudet).map(_.tyyppi.koodiarvo).toSet should be(Set("tuva", "ammatillinenkoulutus", "europeanschoolofhelsinki", "ebtutkinto", "ibtutkinto", "internationalschool", "lukiokoulutus", "luva", "diatutkinto", "vapaansivistystyonkoulutus"))
      authGet("api/oppija/" + KoskiSpecificMockOppijat.ylioppilas.oid, toinenAsteViranomainen) {
        verifyResponseStatusOk()
      }
    }

    "ei näe muun typpisiä opiskeluoikeuksia" in {
      authGet("api/oppija/" + KoskiSpecificMockOppijat.ysiluokkalainen.oid, toinenAsteViranomainen) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${KoskiSpecificMockOppijat.ysiluokkalainen.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
  }

  "viranomainen jolla oikeudet korkeakouluun" - {
    "voi hakea korkeakouluopiskeluoikeuksia" in {
      searchForNames(KoskiSpecificMockOppijat.ylioppilas.hetu.get, korkeakouluViranomainen) should be(empty)
      searchForNames(KoskiSpecificMockOppijat.dippainssi.hetu.get, korkeakouluViranomainen) should be(List("Dilbert Dippainssi"))
      searchForNames("eero", korkeakouluViranomainen) should be(empty)
    }

    "näkee vain korkeakouluopiskeluoikeudet" in {
      queryOppijat(user = korkeakouluViranomainen) should be(empty)
      authGet("api/oppija/" + KoskiSpecificMockOppijat.dippainssi.oid, korkeakouluViranomainen) {
        verifyResponseStatusOk()
      }
    }

    "ei näe muun typpisiä opiskeluoikeuksia" in {
      authGet("api/oppija/" + KoskiSpecificMockOppijat.ysiluokkalainen.oid, korkeakouluViranomainen) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${KoskiSpecificMockOppijat.ysiluokkalainen.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
  }

  "Kela ei voi kutsua muita apeja" in {
    verifyMuidenApienKutsuminenEstetty(MockUsers.kelaLaajatOikeudet)
    verifyMuidenApienKutsuminenEstetty(MockUsers.kelaSuppeatOikeudet)
  }

  "YTL ei voi kutsua muita apeja" in {
    verifyMuidenApienKutsuminenEstetty(MockUsers.ytlKäyttäjä)
  }

  "Valvira ei voi kutsua muita apeja" in {
    verifyMuidenApienKutsuminenEstetty(MockUsers.valviraKäyttäjä)
  }

  "Migri ei voi kutsua muita apeja" in {
    verifyMuidenApienKutsuminenEstetty(MockUsers.migriKäyttäjä)
  }

  "Globaalit lukuoikeudet, mutta rajattu tiettyyn päätason suoritukseen" - {
    "Perustiedot-api: Näkee vain sallitun päätason suorituksen mukaiset opiskeluoikeudet" - {
      def testPerustiedot(user: KoskiMockUser, opiskeluoikeustyypit: List[String], päätasonsuoritustyypit: List[String]) = {
        authGet("api/opiskeluoikeus/perustiedot", user) {
          verifyResponseStatusOk()
          val tiedot = readPaginatedResponse[OpiskeluoikeudenPerustiedotResponse].tiedot
          tiedot
            .map(_.tyyppi.koodiarvo)
            .distinct
            .sorted should equal(opiskeluoikeustyypit.sorted)
          tiedot
            .flatMap(_.suoritukset)
            .map(_.tyyppi.koodiarvo)
            .distinct
            .sorted should equal(päätasonsuoritustyypit.sorted)
        }
      }

      "Yleinen kielitutkinto" in {
        testPerustiedot(
          user = MockUsers.yleisenKielitutkinnonKäyttäjä,
          opiskeluoikeustyypit = List("kielitutkinto"),
          päätasonsuoritustyypit = List("yleinenkielitutkinto")
        )
      }

      "Valtionhallinnon kielitutkinto" in {
        testPerustiedot(
          user = MockUsers.valtionhallinnonKielitutkinnonKäyttäjä,
          opiskeluoikeustyypit = List("kielitutkinto"),
          päätasonsuoritustyypit = List("valtionhallinnonkielitutkinto")
        )
      }

      "Yleinen kielitutkinto + valtionhallinnon kielitutkinto" in {
        testPerustiedot(
          user = MockUsers.ykiJaVktKäyttäjä,
          opiskeluoikeustyypit = List("kielitutkinto"),
          päätasonsuoritustyypit = List("yleinenkielitutkinto", "valtionhallinnonkielitutkinto")
        )
      }
    }

    "Vanhan käyttöliittymän oppija editor -api: käyttäjä näkee oppijalta vain sallitut päätason suoritukset" - {
      def testEditorApi(user: MockUser, expectedWords: List[String], unexpectedWords: List[String]): Unit = {
        authGet("api/editor/1.2.246.562.24.00000000177", user) {
          verifyResponseStatusOk()
          expectedWords.foreach { response.body should include (_) }
          unexpectedWords.foreach { response.body should not include _ }
        }
      }

      "Yleinen kielitutkinto" in {
        testEditorApi(
          user = MockUsers.yleisenKielitutkinnonKäyttäjä,
          expectedWords = List("yleinenkielitutkinto"),
          unexpectedWords = List("valtionhallinnonkielitutkinto")
        )
      }

      "Valtionhallinnon kielitutkinto" in {
        testEditorApi(
          user = MockUsers.valtionhallinnonKielitutkinnonKäyttäjä,
          expectedWords = List("valtionhallinnonkielitutkinto"),
          unexpectedWords = List("yleinenkielitutkinto")
        )
      }

      "Yleinen kielitutkinto + valtionhallinnon kielitutkinto" in {
        testEditorApi(
          user = MockUsers.ykiJaVktKäyttäjä,
          expectedWords = List("yleinenkielitutkinto", "valtionhallinnonkielitutkinto"),
          unexpectedWords = List()
        )
      }
    }

    "Uuden käyttöliittymän oppija-api: käyttäjä näkee oppijalta vain sallitut päätason suoritukset" - {
      def testUi2OppijaApi(user: MockUser, opiskeluoikeustyypit: List[String], päätasonsuoritustyypit: List[String]) = {
        authGet("api/oppija/1.2.246.562.24.00000000177/uiv2", user) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.extract[Oppija](parse(body))
          oppija.opiskeluoikeudet
            .map(_.tyyppi.koodiarvo)
            .distinct
            .sorted should equal(opiskeluoikeustyypit.sorted)
          oppija.opiskeluoikeudet
            .flatMap(_.suoritukset)
            .map(_.tyyppi.koodiarvo)
            .distinct
            .sorted should equal(päätasonsuoritustyypit.sorted)
        }
      }

      "Yleinen kielitutkinto" in {
        testUi2OppijaApi(
          user = MockUsers.yleisenKielitutkinnonKäyttäjä,
          opiskeluoikeustyypit = List("kielitutkinto"),
          päätasonsuoritustyypit = List("yleinenkielitutkinto")
        )
      }

      "Valtionhallinnon kielitutkinto" in {
        testUi2OppijaApi(
          user = MockUsers.valtionhallinnonKielitutkinnonKäyttäjä,
          opiskeluoikeustyypit = List("kielitutkinto"),
          päätasonsuoritustyypit = List("valtionhallinnonkielitutkinto")
        )
      }

      "Yleinen kielitutkinto + valtionhallinnon kielitutkinto" in {
        testUi2OppijaApi(
          user = MockUsers.ykiJaVktKäyttäjä,
          opiskeluoikeustyypit = List("kielitutkinto"),
          päätasonsuoritustyypit = List("yleinenkielitutkinto", "valtionhallinnonkielitutkinto")
        )
      }
    }
  }

  "Organisaation käyttöoikeudet, mutta rajoitettu tiettyyn päätason suoritukseen" - {
    "Käyttäjä voi kirjoittaa opiskeluoikeuden oikealla päätason suorituksella" in {
      val oo = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(
        oppilaitosId = MockOrganisaatiot.stadinAmmattiopisto
      ).copy(
        suoritukset = List(
          TelmaKoulutuksenSuoritus(
            koulutusmoduuli = TelmaKoulutus(
              perusteenDiaarinumero = Some("OPH-2659-2017"),
            ),
            toimipiste = Toimipiste(oid = MockOrganisaatiot.stadinAmmattiopisto),
            suorituskieli = suomenKieli,
            osasuoritukset = None,
          )
        )
      )
      val oppija = KoskiSpecificMockOppijat.telma
      setupOppijaWithOpiskeluoikeus(oo, oppija, MockUsers.stadinTelma) {
        verifyResponseStatusOk()
      }
    }

    "Käyttäjä ei voi kirjoittaa opiskeluoikeutta, jossa on vääränlainen päätason suoritus" in {
      val oo = AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus( // <-- ei ole telma
        oppilaitosId = MockOrganisaatiot.stadinAmmattiopisto
      )
      val oppija = KoskiSpecificMockOppijat.telma
      setupOppijaWithOpiskeluoikeus(oo, oppija, MockUsers.stadinTelma) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeudenTyyppi("Ei oikeuksia opiskeluoikeuden tyyppiin ammatillinenkoulutus (ammatillinentutkinto)"))
      }
    }
  }

  private def verifyMuidenApienKutsuminenEstetty(user: MockUser) = {
    authGet("api/henkilo/hetu/010101-123N", user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
    authGet("api/oppija/" + KoskiSpecificMockOppijat.ysiluokkalainen.oid, user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
    authGet("api/tiedonsiirrot/yhteenveto", user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
    authGet("api/opiskeluoikeus/" + lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid).oid.get, user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
    authGet("api/opiskeluoikeus/perustiedot", user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
    authGet("api/opiskeluoikeus/historia/" + lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid).oid.get, user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
    authGet("api/oppilaitos", user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
    authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy&downloadToken=test123", user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
  }

  private val opiskeluoikeusOmnia: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
    oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)),
    suoritukset = List(autoalanPerustutkinnonSuoritus().copy(toimipiste = Oppilaitos(MockOrganisaatiot.omnia)))
  )

  private val opiskeluoikeusLähdejärjestelmästäOmnia = opiskeluoikeusOmnia.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("omnia-309945")))

  private def haeOpiskeluoikeudetHetulla(hetu: String, käyttäjä: UserWithPassword) = searchForHenkilötiedot(hetu).map(_.oid).flatMap { oid =>
    getOpiskeluoikeudet(oid, käyttäjä)
  }

  private lazy val expectedAikajakso = Some(List(Aikajakso(LocalDate.of(2019, 5, 30), None)))

  private def suppeaSensitiveDataNäkyy(oos: Seq[Opiskeluoikeus] = readOppija.opiskeluoikeudet) = readLisätiedot(oos).vankilaopetuksessa should equal(expectedAikajakso)
  private def suppeaSensitiveDataPiilotettu(oos: Seq[Opiskeluoikeus] = readOppija.opiskeluoikeudet) = readLisätiedot(oos).vankilaopetuksessa should equal(None)
  private def laajaSensitiveDataNäkyy(oos: Seq[Opiskeluoikeus] = readOppija.opiskeluoikeudet) = readLisätiedot(oos).erityinenTuki should equal(expectedAikajakso)
  private def laajaSensitiveDataPiilotettu(oos: Seq[Opiskeluoikeus] = readOppija.opiskeluoikeudet) = readLisätiedot(oos).erityinenTuki should equal(None)
  private def erittäinSensitiveDataPiilotettu(oos: Seq[Opiskeluoikeus] = readOppija.opiskeluoikeudet) = readLisätiedot(oos).vaikeastiVammainen should equal(None)
  private def erittäinSensitiveDataNäkyy(oos: Seq[Opiskeluoikeus] = readOppija.opiskeluoikeudet) = readLisätiedot(oos).vaikeastiVammainen should equal(expectedAikajakso)

  private def kaikkiSensitiveDataNäkyy(oos: Seq[Opiskeluoikeus] = readOppija.opiskeluoikeudet) = {
    suppeaSensitiveDataNäkyy(oos)
    laajaSensitiveDataNäkyy(oos)
    erittäinSensitiveDataNäkyy(oos)
  }

  private def kaikkiSensitiveDataPiilotettu(oos: Seq[Opiskeluoikeus] = readOppija.opiskeluoikeudet) = {
    suppeaSensitiveDataPiilotettu(oos)
    laajaSensitiveDataPiilotettu(oos)
    erittäinSensitiveDataPiilotettu(oos)
  }

  private def readLisätiedot(opiskeluoikeudet: Seq[Opiskeluoikeus]) = opiskeluoikeudet.head.lisätiedot.get.asInstanceOf[AmmatillisenOpiskeluoikeudenLisätiedot]

  private def koskeenTallennetutOppijatCount =
    runDbSync(KoskiTables.KoskiOpiskeluOikeudetWithAccessCheck(KoskiSpecificSession.systemUser)
      .join(KoskiTables.Henkilöt).on(_.oppijaOid === _.oid)
      .filter(_._2.masterOid.isEmpty)
      .map(_._1.oppijaOid).result)
      .distinct
      .length
}
