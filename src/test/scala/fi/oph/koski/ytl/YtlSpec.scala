package fi.oph.koski.ytl

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.{OpiskeluoikeudenMitätöintiJaPoistoTestMethods, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExamplesLukio2019}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.format.DateTimeFormatter.ISO_INSTANT
import java.time.ZonedDateTime

class YtlSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsAmmatillinen
    with OpiskeluoikeudenMitätöintiJaPoistoTestMethods
    with Matchers
    with BeforeAndAfterAll {

  "YTL rajapinta" - {
    "Yhden oppijan hakeminen hetulla onnistuu ja tuottaa auditlog viestin" in {
      AuditLogTester.clearMessages
      val hetut = List(
        KoskiSpecificMockOppijat.amis
      ).map(_.hetu.get)


      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)

        response.length should equal(1)
        response(0).opiskeluoikeudet.length should equal(1)

        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
      }
    }

    "Koulutustoimijalla ja yrityksessä vahvistetut suoritukset toimivat" in {
      resetFixtures()

      val oppija1 = KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen
      val oppija2 = KoskiSpecificMockOppijat.oppivelvollisuustietoMaster

      val oidit = List(
        oppija1.oid,
        oppija2.oid
      )

      val uusiOo1 = AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmisVahvistettuKoulutustoimijalla()
      putOpiskeluoikeus(uusiOo1, oppija1, authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      val uusiOo2 = AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmisVahvistettuYrityksessä()
      putOpiskeluoikeus(uusiOo1, oppija2, authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      postOidit(oidit) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)

        response.length should equal(2)
        response(0).opiskeluoikeudet.length should equal(1)
        response(1).opiskeluoikeudet.length should equal(1)
      }
    }

    "Master-slave haut" - {
      val masterOppija = KoskiSpecificMockOppijat.oppivelvollisuustietoMaster
      val slaveOppija1 = KoskiSpecificMockOppijat.oppivelvollisuustietoSlave1
      val slaveOppija2 = KoskiSpecificMockOppijat.oppivelvollisuustietoSlave2

      "Slave-oppijalle tallennettu opiskeluoikeus löytyy slave-oppijan oidilla" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val oidit = List(slaveOppija1.henkilö.oid)

        postOidit(oidit) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)

          response.length should equal(1)
          response(0).henkilö.pääoppijaOid should equal(Some(masterOppija.oid))
          response(0).opiskeluoikeudet.length should equal(1)
          response(0).henkilö.oid should equal(slaveOppija1.henkilö.oid)
        }
      }

      "Master-oppijalle tallennettu opiskeluoikeus löytyy slave-oppijan oidilla" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, masterOppija, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val oidit = List(slaveOppija1.henkilö.oid)

        postOidit(oidit) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)

          response.length should equal(1)
          response(0).henkilö.pääoppijaOid should equal(Some(masterOppija.oid))
          response(0).opiskeluoikeudet.length should equal(1)
          response(0).henkilö.oid should equal(slaveOppija1.henkilö.oid)
        }
      }


      "Slave-oppijalle tallennettu opiskeluoikeus löytyy master-oppijan oidilla" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val oidit = List(masterOppija.oid)

        postOidit(oidit) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)

          response.length should equal(1)
          response(0).henkilö.pääoppijaOid should equal(Some(masterOppija.oid))
          response(0).opiskeluoikeudet.length should equal(1)
          response(0).henkilö.oid should equal(masterOppija.oid)
        }
      }

      "Slave-oppijalle tallennettu opiskeluoikeus löytyy toisen slave-oppijan oidilla" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val oidit = List(slaveOppija2.henkilö.oid)

        postOidit(oidit) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)

          response.length should equal(1)
          response(0).henkilö.pääoppijaOid should equal(Some(masterOppija.oid))
          response(0).henkilö.oid should equal(slaveOppija2.henkilö.oid)
          response(0).opiskeluoikeudet.length should equal(1)
        }
      }

      "Saman oppijan hakeminen master-oppijan oidilla ja hetulla palauttaa oppijan vain yhden kerran" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val oidit = List(
          masterOppija.oid
        )
        val hetut = List(
          masterOppija.hetu.get
        )

        postOppijat(oidit, hetut) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)
          response.length should equal(1)
          response(0).opiskeluoikeudet.length should equal(1)
        }
      }

      "Saman oppijan hakeminen slave-oppijan oidilla ja hetulla palauttaa oppijan molemmilla" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val oidit = List(
          slaveOppija2.henkilö.oid
        )
        val hetut = List(
          masterOppija.hetu.get
        )

        postOppijat(oidit, hetut) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)
          response.length should equal(2)
          response(0).opiskeluoikeudet.length should equal(1)
          response(1).opiskeluoikeudet.length should equal(1)
        }
      }

      "Slave-oppijalle tallennettu opiskeluoikeus palautetaan kaikilla haetuilla samaan oppijaan viittaavilla oideilla" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val oidit = List(
          masterOppija.oid,
          slaveOppija1.henkilö.oid,
          slaveOppija2.henkilö.oid
        ).sorted

        postOidit(oidit) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)

          response.length should equal(3)
          val järjestettyResponse = response.sortBy(_.henkilö.oid)

          järjestettyResponse.zipWithIndex.foreach {case (responseOppija, idx) => {
            responseOppija.henkilö.pääoppijaOid should equal(Some(masterOppija.oid))
            responseOppija.henkilö.oid should equal(oidit(idx))
            responseOppija.opiskeluoikeudet.length should equal(1)
          }}
        }
      }

      "Slave-oppijalle tallennettu opiskeluoikeus palautetaan hetulla + slave-oideilla haettaessa niillä kaikilla" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val hetut = List(masterOppija.hetu.get)

        val oidit = List(
          slaveOppija1.henkilö.oid,
          slaveOppija2.henkilö.oid
        )

        postOppijat(oidit = oidit, hetut = hetut) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)

          response.length should equal(3)
          response.foreach(responseOppija => {
            responseOppija.henkilö.pääoppijaOid should equal(Some(masterOppija.oid))
            responseOppija.opiskeluoikeudet.length should equal(1)
          })
        }
      }

      "Slave-oppijalle tallennettu opiskeluoikeus palautetaan vain kerran haettaessa master-hetulla ja master-oidilla" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val hetut = List(masterOppija.hetu.get)

        val oidit = List(masterOppija.oid)

        postOppijat(oidit = oidit, hetut = hetut) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)

          response.length should equal(1)
          response(0).henkilö.pääoppijaOid should equal(Some(masterOppija.oid))
          response(0).opiskeluoikeudet.length should equal(1)
        }
      }

      "Aikaleimalla kysyttäessä linkitettyjä oideja sisältävät oppijat palautetaan aina" in {
        resetFixtures()

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()
        putOpiskeluoikeus(uusiOo, slaveOppija1.henkilö, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        val oidit = List(
          masterOppija.oid,
          slaveOppija1.henkilö.oid,
          slaveOppija2.henkilö.oid
        )

        postOidit(oidit, Some(ZonedDateTime.now.plusDays(1))) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)

          response.length should equal(3)
          response.foreach(responseOppija => {
            responseOppija.henkilö.pääoppijaOid should equal(Some(masterOppija.oid))
            responseOppija.opiskeluoikeudet.length should equal(1)
          })
        }
      }
    }


    "Ei voi kutsua ilman YTL-käyttöoikeutta" in {
      val hetut = List(
        KoskiSpecificMockOppijat.amis
      ).map(_.hetu.get)

      postHetut(hetut, None, MockUsers.luovutuspalveluKäyttäjäArkaluontoinen) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu näillä käyttöoikeuksilla"))
      }
    }

    "Yhden oppijan, jolla useampi opiskeluoikeus, hakeminen oidilla onnistuu, tuottaa auditlog viestin ja palauttaa oikeat tiedot" in {
      AuditLogTester.clearMessages
      val oidit = List(
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2
      ).map(_.oid)

      postOidit(oidit) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)
        response.length should equal(1)
        response(0).opiskeluoikeudet.length should equal(2)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2.oid)))

        // Järjestä opiskeluoikeudet samaan järjestykseen kuin vertailudatassa
        val järjestettyResponse = response.updated(0, response(0).copy(opiskeluoikeudet = response(0).opiskeluoikeudet.sortBy(_.tyyppi.koodiarvo)))

        val expectedResponse = JsonSerializer.parse[List[YtlOppija]](
          expectedMaksuttomuuttaPidennetty2(
            järjestettyResponse(0).opiskeluoikeudet.map(_.oid).map(_.getOrElse("")),
            järjestettyResponse(0).opiskeluoikeudet.map(_.aikaleima).map(_.map(_.toString).getOrElse(""))
          )
        )
        järjestettyResponse should equal(expectedResponse)
      }
    }

    "Usean oppijan hakeminen hetulla onnistuu" in {
      val hetut = List(
        KoskiSpecificMockOppijat.amis,
        KoskiSpecificMockOppijat.lukioKesken,
        KoskiSpecificMockOppijat.lukionAineopiskelija,
        KoskiSpecificMockOppijat.uusiLukio,
        KoskiSpecificMockOppijat.ylioppilasLukiolainen,
        KoskiSpecificMockOppijat.ibFinal,
        KoskiSpecificMockOppijat.ibPreIB2019,
        KoskiSpecificMockOppijat.internationalschool,
        KoskiSpecificMockOppijat.dia,
        KoskiSpecificMockOppijat.eskari,
        KoskiSpecificMockOppijat.eiKoskessa
      ).map(_.hetu.get)

      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)
        response.length should equal(7)
      }
    }

    "Erityisoppilaitosten käsittelyssä" - {
      "poistetaan palautettavista tiedoista kaikki koulutustoimija-, oppilaitos- ja toimipistetiedot, jos opiskeluoikeuden oppilaitos on erityisoppilaitos" in {
        val hetut = List(
          KoskiSpecificMockOppijat.opiskeleeAmmatillisessaErityisoppilaitoksessa,
          KoskiSpecificMockOppijat.opiskeleeAmmatillisessaErityisoppilaitoksessaOrganisaatioHistoriallinen
        ).map(_.hetu.get)

        postHetut(hetut) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)
          response.length should equal(2)

          val järjestettyResponse = response.sortBy(_.opiskeluoikeudet.length)

          järjestettyResponse(0).opiskeluoikeudet.length should equal(1)
          järjestettyResponse(1).opiskeluoikeudet.length should equal(3)

          (järjestettyResponse(0).opiskeluoikeudet ++ järjestettyResponse(1).opiskeluoikeudet).foreach(yoo => {
            val oo = yoo.asInstanceOf[YtlAmmatillinenOpiskeluoikeus]

            oo.organisaatiohistoria should equal(None)
            oo.koulutustoimija should equal(None)
            oo.oppilaitos should equal(None)

            val suoritus = oo.suoritukset(0)

            suoritus.toimipiste should equal(None)
            suoritus.vahvistus.get.myöntäjäOrganisaatio should equal(None)
          })
        }
      }
    }

    "Haku opiskeluoikeuksiaMuuttunutJälkeen aikaleimalla" - {
      val hetut = List(
        KoskiSpecificMockOppijat.amis,
        KoskiSpecificMockOppijat.lukioKesken,
        KoskiSpecificMockOppijat.lukionAineopiskelija,
        KoskiSpecificMockOppijat.uusiLukio,
        KoskiSpecificMockOppijat.ylioppilasLukiolainen,
        KoskiSpecificMockOppijat.ibFinal,
        KoskiSpecificMockOppijat.ibPreIB2019,
        KoskiSpecificMockOppijat.internationalschool,
        KoskiSpecificMockOppijat.dia,
        KoskiSpecificMockOppijat.eskari,
        KoskiSpecificMockOppijat.eiKoskessa,
        KoskiSpecificMockOppijat.luva
      ).map(_.hetu.get)

      "Palauttaa oppijat, kun aikaleima on tarpeeksi menneisyydessä" in {
        val now = ZonedDateTime.now

        postHetut(hetut, Some(now.minusDays(1))) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)
          response.length should equal(7)
        }
      }

      "Ei palauta oppijoita, kun aikaleima on uudempi kuin oppijat" in {
        val now = ZonedDateTime.now

        postHetut(hetut, Some(now)) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)
          response.length should equal(0)
        }
      }

      "Palauttaa oppijalta, jolla on jokin opiskeluoikeus muuttunut, kaikki opiskeluoikeudet" in {
        resetFixtures()

        // Asetetaan aika 3 sekuntia menneisyyteen, koska PostgreSQL:n kello saattaa olla hieman eri ajassa.
        val ennenTallennusta = ZonedDateTime.now.minusSeconds(3)

        val uusiOo = ExamplesLukio2019.opiskeluoikeus.copy()

        putOpiskeluoikeus(uusiOo, KoskiSpecificMockOppijat.internationalschool, authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        postHetut(hetut, Some(ennenTallennusta)) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)
          response.length should equal(1)

          response(0).opiskeluoikeudet.length should equal(2)

          val järjestettyResponse = response.updated(0, response(0).copy(opiskeluoikeudet = response(0).opiskeluoikeudet.sortBy(_.tyyppi.koodiarvo)))

          järjestettyResponse(0).opiskeluoikeudet(0).tyyppi.koodiarvo should equal("internationalschool")
          järjestettyResponse(0).opiskeluoikeudet(1).tyyppi.koodiarvo should equal("lukiokoulutus")
        }
      }

      "Palauttaa oppijalta, jolla jokin opiskeluoikeus on mitätöity, olemassaolevat opiskeluoikeudet" in {
        resetFixtures()

        // Asetetaan aika 3 sekuntia menneisyyteen, koska PostgreSQL:n kello saattaa olla hieman eri ajassa.
        val ennenTallennusta = ZonedDateTime.now.minusSeconds(3)

        val uusiOo = createOpiskeluoikeus(
          oppija = KoskiSpecificMockOppijat.internationalschool,
          opiskeluoikeus = ExamplesLukio2019.opiskeluoikeus.copy(),
          user = MockUsers.jyväskyläTallentaja
        )

        mitätöiOpiskeluoikeus(uusiOo.oid.get, MockUsers.jyväskyläTallentaja)

        postHetut(hetut, Some(ennenTallennusta)) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)
          response.length should equal(1)

          response(0).opiskeluoikeudet.length should equal(1)

          response(0).opiskeluoikeudet(0).tyyppi.koodiarvo should equal("internationalschool")
        }
      }

      "Palauttaa oppijalta, jolla ainoa opiskeluoikeus on mitätöity, tyhjän listan opiskeluoikeuksia" in {
        resetFixtures()

        // Asetetaan aika 3 sekuntia menneisyyteen, koska PostgreSQL:n kello saattaa olla hieman eri ajassa.
        val ennenTallennusta = ZonedDateTime.now.minusSeconds(3)

        val uusiOo = createOpiskeluoikeus(
          oppija = KoskiSpecificMockOppijat.luva,
          opiskeluoikeus = ExamplesLukio2019.opiskeluoikeus.copy(),
          user = MockUsers.jyväskyläTallentaja
        )

        mitätöiOpiskeluoikeus(uusiOo.oid.get, MockUsers.jyväskyläTallentaja)

        postHetut(hetut, Some(ennenTallennusta)) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[List[YtlOppija]](body)
          response.length should equal(1)

          response(0).opiskeluoikeudet.length should equal(0)
        }
      }

    }

    "Kosken testioppijoiden hakeminen oidilla onnistuu" in {
      val oidit = List(
        KoskiSpecificMockOppijat.hetuton,
        KoskiSpecificMockOppijat.syntymäajallinen,
        KoskiSpecificMockOppijat.eero,
        KoskiSpecificMockOppijat.eerola,
        KoskiSpecificMockOppijat.markkanen,
        KoskiSpecificMockOppijat.teija,
        KoskiSpecificMockOppijat.tero,
        KoskiSpecificMockOppijat.presidentti,
        KoskiSpecificMockOppijat.koululainen,
        KoskiSpecificMockOppijat.suoritusTuplana,
        KoskiSpecificMockOppijat.luokallejäänyt,
        KoskiSpecificMockOppijat.ysiluokkalainen,
        KoskiSpecificMockOppijat.vuosiluokkalainen,
        KoskiSpecificMockOppijat.monessaKoulussaOllut,
        KoskiSpecificMockOppijat.lukiolainen,
        KoskiSpecificMockOppijat.lukioKesken,
        KoskiSpecificMockOppijat.uusiLukio,
        KoskiSpecificMockOppijat.uusiLukionAineopiskelija,
        KoskiSpecificMockOppijat.lukionAineopiskelija,
        KoskiSpecificMockOppijat.lukionAineopiskelijaAktiivinen,
        KoskiSpecificMockOppijat.lukionEiTiedossaAineopiskelija,
        KoskiSpecificMockOppijat.ammattilainen,
        KoskiSpecificMockOppijat.tutkinnonOsaaPienempiKokonaisuus,
        KoskiSpecificMockOppijat.muuAmmatillinen,
        KoskiSpecificMockOppijat.muuAmmatillinenKokonaisuuksilla,
        KoskiSpecificMockOppijat.ammatilliseenTetäväänValmistavaMuuAmmatillinen,
        KoskiSpecificMockOppijat.erkkiEiperusteissa,
        KoskiSpecificMockOppijat.amis,
        KoskiSpecificMockOppijat.dippainssi,
        KoskiSpecificMockOppijat.korkeakoululainen,
        KoskiSpecificMockOppijat.amkValmistunut,
        KoskiSpecificMockOppijat.opintojaksotSekaisin,
        KoskiSpecificMockOppijat.amkKesken,
        KoskiSpecificMockOppijat.amkKeskeytynyt,
        KoskiSpecificMockOppijat.monimutkainenKorkeakoululainen,
        KoskiSpecificMockOppijat.virtaEiVastaa,
        KoskiSpecificMockOppijat.oppiaineenKorottaja,
        KoskiSpecificMockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa,
        KoskiSpecificMockOppijat.virtaKaksiPäätösonSuoritusta,
        KoskiSpecificMockOppijat.aikuisOpiskelija,
        KoskiSpecificMockOppijat.aikuisOpiskelijaMuuKuinVos,
        KoskiSpecificMockOppijat.aikuisAineOpiskelijaMuuKuinVos,
        KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinen,
        KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinenMuuKuinVos,
        KoskiSpecificMockOppijat.aikuisOpiskelijaMuuRahoitus,
        KoskiSpecificMockOppijat.kymppiluokkalainen,
        KoskiSpecificMockOppijat.luva,
        KoskiSpecificMockOppijat.luva2019,
        KoskiSpecificMockOppijat.valma,
        KoskiSpecificMockOppijat.ylioppilas,
        KoskiSpecificMockOppijat.ylioppilasLukiolainen,
        KoskiSpecificMockOppijat.ylioppilasEiOppilaitosta,
        KoskiSpecificMockOppijat.toimintaAlueittainOpiskelija,
        KoskiSpecificMockOppijat.telma,
        KoskiSpecificMockOppijat.erikoisammattitutkinto,
        KoskiSpecificMockOppijat.reformitutkinto,
        KoskiSpecificMockOppijat.osittainenammattitutkinto,
        KoskiSpecificMockOppijat.ammatillisenOsittainenRapsa,
        KoskiSpecificMockOppijat.paikallinenTunnustettu,
        KoskiSpecificMockOppijat.tiedonsiirto,
        KoskiSpecificMockOppijat.perusopetuksenTiedonsiirto,
        KoskiSpecificMockOppijat.omattiedot,
        KoskiSpecificMockOppijat.ibFinal,
        KoskiSpecificMockOppijat.ibPredicted,
        KoskiSpecificMockOppijat.ibPreIB2019,
        KoskiSpecificMockOppijat.dia,
        KoskiSpecificMockOppijat.internationalschool,
        KoskiSpecificMockOppijat.eskari,
        KoskiSpecificMockOppijat.eskariAikaisillaLisätiedoilla,
        KoskiSpecificMockOppijat.master,
        KoskiSpecificMockOppijat.slave,
        KoskiSpecificMockOppijat.masterEiKoskessa,
        KoskiSpecificMockOppijat.slaveMasterEiKoskessa,
        KoskiSpecificMockOppijat.omattiedotSlave,
        KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti,
        KoskiSpecificMockOppijat.eiKoskessa,
        KoskiSpecificMockOppijat.eiKoskessaHetuton,
        KoskiSpecificMockOppijat.turvakielto,
        KoskiSpecificMockOppijat.montaJaksoaKorkeakoululainen,
        KoskiSpecificMockOppijat.organisaatioHistoria,
        KoskiSpecificMockOppijat.valtuutusOppija,
        KoskiSpecificMockOppijat.siirtoOpiskelijaVirta,
        KoskiSpecificMockOppijat.faija,
        KoskiSpecificMockOppijat.faijaFeilaa,
        KoskiSpecificMockOppijat.koulusivistyskieliYlioppilas,
        KoskiSpecificMockOppijat.montaKoulusivityskieltäYlioppilas,
        KoskiSpecificMockOppijat.labammattikoulu,
        KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto,
        KoskiSpecificMockOppijat.valviraaKiinnostavaTutkintoKesken,
        KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_nuortenOppimaara,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_aikuistenOppimaara,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_aineopiskelija,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_dia,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_ib,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_international,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_tavallinen,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_erikois,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_virheellisestiSiirretty,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_virheellisestiSiirrettyVieraskielinen,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_kotiopetus,
        KoskiSpecificMockOppijat.organisaatioHistoriallinen,
        KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_oppimaara,
        KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_aineopiskelija_eronnut,
        KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_aineopiskelija_valmistunut,
        KoskiSpecificMockOppijat.luvaOpiskelijamaaratRaportti_nuortenOppimaara,
        KoskiSpecificMockOppijat.luvaOpiskelijamaaratRaportti_aikuistenOppimaara,
        KoskiSpecificMockOppijat.paallekkaisiOpiskeluoikeuksia,
        KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöMaahanmuuttajienKotoutus,
        KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKotoutus,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus,
        KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen,
        KoskiSpecificMockOppijat.eiOikeuttaMaksuttomuuteen,
        KoskiSpecificMockOppijat.etk18vSyntynytKesäkuunEnsimmäisenäPäivänä,
        KoskiSpecificMockOppijat.etk18vSyntynytToukokuunViimeisenäPäivänä,
        KoskiSpecificMockOppijat.oppivelvollisuustietoLiianVanha,
        KoskiSpecificMockOppijat.oppivelvollisuustietoMaster,
        KoskiSpecificMockOppijat.oppivelvollisuustietoSlave1,
        KoskiSpecificMockOppijat.oppivelvollisuustietoSlave2,
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty1,
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2,
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021,
        KoskiSpecificMockOppijat.vuonna2004SyntynytMuttaPeruskouluValmisEnnen2021,
        KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
        KoskiSpecificMockOppijat.nuoriHetuton,
        KoskiSpecificMockOppijat.vuonna2005SyntynytUlkomainenVaihtoopiskelija,
        KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021,
        KoskiSpecificMockOppijat.rikkinäinenOpiskeluoikeus,
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021EiKotikuntaaSuomessa,
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021KotikuntaAhvenanmaalla,
        KoskiSpecificMockOppijat.vuonna2004SyntynytMuttaEronnutPeruskoulustaEnnen2021
      ).map {
        case o: OppijaHenkilöWithMasterInfo => {
          o.henkilö.oid
        }
        case o:  LaajatOppijaHenkilöTiedot => {
          o.oid
        }
      }

      postOidit(oidit) {
        verifyResponseStatusOk()
      }
    }

    "Haku onnistuu myös rikkinäisellä datalla (koodistosta puuttuva koulutuskoodi)" in {
      // Scala-schemaa käytetään tavalla, jossa kaikki json-taulukoiden alkioissa olevat validointivirheet
      // jätetään huomioimatta ja jätetään vain alkio pois listalta. Tämän vuoksi esim. koodistosta
      // puuttuvia arvoja ei huomata. Tämän ei kuitenkaan pitäisi olla ongelma, koska tietokannassa ei voi olla
      // tältä osin rikkinäistä dataa.
      val oidit = List(
        KoskiSpecificMockOppijat.tunnisteenKoodiarvoPoistettu
      ).map(_.oid)

      postOidit(oidit) {
        verifyResponseStatusOk()
      }
    }

    "Sallitaan yhteensä 1000 hetua ja oidia" in {
      val oidit = List.fill(460)(KoskiSpecificMockOppijat.amis.oid)
      val hetut = List.fill(540)(KoskiSpecificMockOppijat.amis.hetu.get)
      postOppijat(oidit, hetut) {
        verifyResponseStatusOk()
      }
    }

    "Ei sallita yli 1000 hetua ja oidia" in {
      val oidit = List.fill(461)(KoskiSpecificMockOppijat.amis.oid)
      val hetut = List.fill(540)(KoskiSpecificMockOppijat.amis.hetu.get)
      postOppijat(oidit, hetut) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest("Liian monta oppijaa, enintään 1000 sallittu"))
      }
    }

    "Saman oppijan hakeminen oidilla ja hetulla palauttaa oppijan vain yhden kerran" in {
      val oidit = List(
        KoskiSpecificMockOppijat.amis
      ).map(_.oid)
      val hetut = List(
        KoskiSpecificMockOppijat.amis
      ).map(_.hetu.get)

      postOppijat(oidit, hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)
        response.length should equal(1)
        response(0).opiskeluoikeudet.length should equal(1)
      }
    }

    "Peruskoulun oppijan hakeminen tuottaa tyhjän vastauksen, koska peruskoulun tietoja ei YTL:lle luovuteta" in {
      AuditLogTester.clearMessages
      val hetut = List(
        KoskiSpecificMockOppijat.ysiluokkalainen
      ).map(_.hetu.get)

      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)
        response.length should equal(0)
      }
    }
  }

  private def postHetut[A](hetut: List[String], opiskeluoikeuksiaMuuttunutJälkeen: Option[ZonedDateTime] = None, user: MockUser = MockUsers.ytlKäyttäjä)(f: => A): A =
    postOppijat(None, Some(hetut), opiskeluoikeuksiaMuuttunutJälkeen, user)(f)

  private def postOidit[A](oidit: List[String], opiskeluoikeuksiaMuuttunutJälkeen: Option[ZonedDateTime] = None, user: MockUser = MockUsers.ytlKäyttäjä)(f: => A): A =
    postOppijat(Some(oidit), None, opiskeluoikeuksiaMuuttunutJälkeen, user)(f)

  private def postOppijat[A](oidit: List[String], hetut: List[String], user: MockUser = MockUsers.ytlKäyttäjä)(f: => A): A =
    postOppijat(Some(oidit), Some(hetut), None, user)(f)

  private def postOppijat[A](oidit: Option[List[String]], hetut: Option[List[String]], opiskeluoikeuksiaMuuttunutJälkeen: Option[ZonedDateTime], user: MockUser)(f: => A): A = {
    post(
      "api/luovutuspalvelu/ytl/oppijat",
      JsonSerializer.writeWithRoot(YtlBulkRequest(oidit = oidit, hetut = hetut, opiskeluoikeuksiaMuuttunutJälkeen = opiskeluoikeuksiaMuuttunutJälkeen.map(_.format(ISO_INSTANT)))),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }
  private def expectedMaksuttomuuttaPidennetty2(opiskeluoikeusOidit: Seq[String], aikaleimat: Seq[String]) =
    s"""
       |[
       |  {
       |    "henkilö": {
       |      "pääoppijaOid": "${KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2.oid}",
       |      "oid": "${KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2.oid}",
       |      "hetu": "220835-2325",
       |      "syntymäaika": "2004-01-01",
       |      "etunimet": "Maksuttomuutta2",
       |      "sukunimi": "Pidennetty2",
       |      "kutsumanimi": "Maksuttomuutta2",
       |      "äidinkieli": {
       |        "koodiarvo": "FI",
       |        "nimi": {
       |          "fi": "suomi",
       |          "sv": "finska",
       |          "en": "Finnish"
       |        },
       |        "lyhytNimi": {
       |          "fi": "suomi",
       |          "sv": "finska",
       |          "en": "Finnish"
       |        },
       |        "koodistoUri": "kieli",
       |        "koodistoVersio": 1
       |      },
       |      "turvakielto": false
       |    },
       |    "opiskeluoikeudet": [
       |      {
       |        "oid": "${opiskeluoikeusOidit(0)}",
       |        "aikaleima": "${aikaleimat(0)}",
       |        "oppilaitos": {
       |          "oid": "1.2.246.562.10.52251087186",
       |          "oppilaitosnumero": {
       |            "koodiarvo": "10105",
       |            "nimi": {
       |              "fi": "Stadin ammatti- ja aikuisopisto",
       |              "sv": "Stadin ammatti- ja aikuisopisto",
       |              "en": "Stadin ammatti- ja aikuisopisto"
       |            },
       |            "lyhytNimi": {
       |              "fi": "Stadin ammatti- ja aikuisopisto",
       |              "sv": "Stadin ammatti- ja aikuisopisto",
       |              "en": "Stadin ammatti- ja aikuisopisto"
       |            },
       |            "koodistoUri": "oppilaitosnumero",
       |            "koodistoVersio": 1
       |          },
       |          "nimi": {
       |            "fi": "Stadin ammatti- ja aikuisopisto",
       |            "sv": "Stadin ammatti- ja aikuisopisto",
       |            "en": "Stadin ammatti- ja aikuisopisto"
       |          },
       |          "kotipaikka": {
       |            "koodiarvo": "091",
       |            "nimi": {
       |              "fi": "Helsinki",
       |              "sv": "Helsingfors"
       |            },
       |            "koodistoUri": "kunta",
       |            "koodistoVersio": 2
       |          }
       |        },
       |        "koulutustoimija": {
       |          "oid": "1.2.246.562.10.346830761110",
       |          "nimi": {
       |            "fi": "Helsingin kaupunki",
       |            "sv": "Helsingfors stad"
       |          },
       |          "yTunnus": "0201256-6",
       |          "kotipaikka": {
       |            "koodiarvo": "091",
       |            "nimi": {
       |              "fi": "Helsinki",
       |              "sv": "Helsingfors"
       |            },
       |            "koodistoUri": "kunta",
       |            "koodistoVersio": 2
       |          }
       |        },
       |        "tila": {
       |          "opiskeluoikeusjaksot": [
       |            {
       |              "alku": "2021-08-01",
       |              "tila": {
       |                "koodiarvo": "lasna",
       |                "nimi": {
       |                  "fi": "Läsnä",
       |                  "sv": "Närvarande",
       |                  "en": "Present"
       |                },
       |                "koodistoUri": "koskiopiskeluoikeudentila",
       |                "koodistoVersio": 1
       |              }
       |            }
       |          ]
       |        },
       |        "suoritukset": [
       |          {
       |            "tyyppi": {
       |              "koodiarvo": "ammatillinentutkinto",
       |              "nimi": {
       |                "fi": "Ammatillinen tutkinto",
       |                "sv": "Yrkesinriktad examen",
       |                "en": "Vocational education  qualification"
       |              },
       |              "koodistoUri": "suorituksentyyppi",
       |              "koodistoVersio": 1
       |            },
       |            "koulutusmoduuli": {
       |              "tunniste": {
       |                "koodiarvo": "351301",
       |                "nimi": {
       |                  "fi": "Autoalan perustutkinto",
       |                  "sv": "Grundexamen inom bilbranschen",
       |                  "en": "Vocational qualification in the Vehicle Sector"
       |                },
       |                "lyhytNimi": {
       |                  "fi": "Autoalan perustutkinto",
       |                  "sv": "Grundexamen inom bilbranschen",
       |                  "en": "Vocational qualification in the Vehicle Sector"
       |                },
       |                "koodistoUri": "koulutus",
       |                "koodistoVersio": 12
       |              },
       |              "perusteenDiaarinumero": "39/011/2014",
       |              "perusteenNimi": {
       |                "fi": "Autoalan perustutkinto",
       |                "sv": "Grundexamen inom bilbranschen"
       |              },
       |              "koulutustyyppi": {
       |                "koodiarvo": "1",
       |                "nimi": {
       |                  "fi": "Ammatillinen perustutkinto",
       |                  "sv": "Yrkesinriktad grundexamen",
       |                  "en": "Vocational upper secondary qualification"
       |                },
       |                "lyhytNimi": {
       |                  "fi": "Ammatillinen perustutkinto",
       |                  "sv": "Yrkesinriktad grundexamen"
       |                },
       |                "koodistoUri": "koulutustyyppi",
       |                "koodistoVersio": 2
       |              }
       |            },
       |            "toimipiste": {
       |              "oid": "1.2.246.562.10.42456023292",
       |              "nimi": {
       |                "fi": "Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka"
       |              },
       |              "kotipaikka": {
       |                "koodiarvo": "091",
       |                "nimi": {
       |                  "fi": "Helsinki",
       |                  "sv": "Helsingfors"
       |                },
       |                "koodistoUri": "kunta",
       |                "koodistoVersio": 2
       |              }
       |            },
       |            "suorituskieli": {
       |              "koodiarvo": "FI",
       |              "nimi": {
       |                "fi": "suomi",
       |                "sv": "finska",
       |                "en": "Finnish"
       |              },
       |              "lyhytNimi": {
       |                "fi": "suomi",
       |                "sv": "finska",
       |                "en": "Finnish"
       |              },
       |              "koodistoUri": "kieli",
       |              "koodistoVersio": 1
       |            }
       |          }
       |        ],
       |        "lisätiedot": {
       |          "maksuttomuus": [
       |            {
       |              "alku": "2021-10-10",
       |              "maksuton": true
       |            }
       |          ],
       |          "oikeuttaMaksuttomuuteenPidennetty": [
       |            {
       |              "alku": "2021-10-10",
       |              "loppu": "2021-10-15"
       |            },
       |            {
       |              "alku": "2021-10-20",
       |              "loppu": "2021-10-25"
       |            }
       |          ]
       |        },
       |        "tyyppi": {
       |          "koodiarvo": "ammatillinenkoulutus",
       |          "nimi": {
       |            "fi": "Ammatillinen koulutus",
       |            "sv": "Yrkesutbildning"
       |          },
       |          "lyhytNimi": {
       |            "fi": "Ammatillinen koulutus"
       |          },
       |          "koodistoUri": "opiskeluoikeudentyyppi",
       |          "koodistoVersio": 1
       |        }
       |      },
       |      {
       |        "oid": "${opiskeluoikeusOidit(1)}",
       |        "aikaleima": "${aikaleimat(1)}",
       |        "oppilaitos": {
       |          "oid": "1.2.246.562.10.14613773812",
       |          "oppilaitosnumero": {
       |            "koodiarvo": "00204",
       |            "nimi": {
       |              "fi": "Jyväskylän normaalikoulu",
       |              "sv": "Jyväskylän normaalikoulu",
       |              "en": "Jyväskylän normaalikoulu"
       |            },
       |            "lyhytNimi": {
       |              "fi": "Jyväskylän normaalikoulu",
       |              "sv": "Jyväskylän normaalikoulu",
       |              "en": "Jyväskylän normaalikoulu"
       |            },
       |            "koodistoUri": "oppilaitosnumero",
       |            "koodistoVersio": 1
       |          },
       |          "nimi": {
       |            "fi": "Jyväskylän normaalikoulu",
       |            "sv": "Jyväskylän normaalikoulu",
       |            "en": "Jyväskylän normaalikoulu"
       |          },
       |          "kotipaikka": {
       |            "koodiarvo": "179",
       |            "nimi": {
       |              "fi": "Jyväskylä",
       |              "sv": "Jyväskylä"
       |            },
       |            "koodistoUri": "kunta",
       |            "koodistoVersio": 2
       |          }
       |        },
       |        "koulutustoimija": {
       |          "oid": "1.2.246.562.10.77055527103",
       |          "nimi": {
       |            "fi": "Jyväskylän yliopisto"
       |          },
       |          "yTunnus": "0245894-7",
       |          "kotipaikka": {
       |            "koodiarvo": "179",
       |            "nimi": {
       |              "fi": "Jyväskylä",
       |              "sv": "Jyväskylä"
       |            },
       |            "koodistoUri": "kunta",
       |            "koodistoVersio": 2
       |          }
       |        },
       |        "tila": {
       |          "opiskeluoikeusjaksot": [
       |            {
       |              "alku": "2021-08-01",
       |              "tila": {
       |                "koodiarvo": "lasna",
       |                "nimi": {
       |                  "fi": "Läsnä",
       |                  "sv": "Närvarande",
       |                  "en": "Present"
       |                },
       |                "koodistoUri": "koskiopiskeluoikeudentila",
       |                "koodistoVersio": 1
       |              }
       |            }
       |          ]
       |        },
       |        "suoritukset": [
       |          {
       |            "tyyppi": {
       |              "koodiarvo": "lukionoppimaara",
       |              "nimi": {
       |                "fi": "Lukion oppimäärä",
       |                "sv": "Gymnasiets lärokurs",
       |                "en": "General upper secondary education  syllabus"
       |              },
       |              "koodistoUri": "suorituksentyyppi",
       |              "koodistoVersio": 1
       |            }
       |          }
       |        ],
       |        "lisätiedot": {
       |          "maksuttomuus": [
       |            {
       |              "alku": "2021-10-10",
       |              "maksuton": true
       |            }
       |          ],
       |          "oikeuttaMaksuttomuuteenPidennetty": [
       |            {
       |              "alku": "2021-10-20",
       |              "loppu": "2021-10-25"
       |            },
       |            {
       |              "alku": "2021-12-30",
       |              "loppu": "2022-01-10"
       |            }
       |          ]
       |        },
       |        "tyyppi": {
       |          "koodiarvo": "lukiokoulutus",
       |          "nimi": {
       |            "fi": "Lukiokoulutus",
       |            "sv": "Gymnasieutbildning"
       |          },
       |          "lyhytNimi": {
       |            "fi": "Lukiokoulutus"
       |          },
       |          "koodistoUri": "opiskeluoikeudentyyppi",
       |          "koodistoVersio": 1
       |        }
       |      }
       |    ]
       |  }
       |]
       |""".stripMargin

}
