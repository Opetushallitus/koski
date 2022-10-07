package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests

import java.time.LocalDate.{of => date}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.db.ValpasDatabaseFixtureLoader
import fi.oph.koski.valpas.kela.{ValpasKelaBulkRequest, ValpasKelaOppija, ValpasKelaOppivelvollisuudenKeskeytys, ValpasKelaRequest}
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.oppija.ValpasErrorCategory
import fi.oph.koski.valpas.valpasrepository.ValpasExampleData
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.scalatest.BeforeAndAfterEach

import java.time.LocalDate

class ValpasKelaServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    super.beforeEach()
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
    AuditLogTester.clearMessages

  }

  override protected def afterEach(): Unit = {
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
    super.afterEach()
  }

  "Kelan Valpas API" - {
    "Yhden oppijan rajapinta" - {
      "Yhden oppijan hakeminen onnistuu ja tuottaa auditlog viestin" in {
        AuditLogTester.clearMessages

        val oppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021

        postHetu(oppija.hetu.get) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPPIVELVOLLISUUSREKISTERI_LUOVUTUS", "target" -> Map("oppijaHenkilöOid" -> oppija.oid)))
        }
      }

      "Yhden oppijan hakeminen palauttaa oppijan, jolla ei ole oppivelvollisuuden keskeytyksiä, tiedot" in {
        val oppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021

        postHetu(oppija.hetu.get) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[ValpasKelaOppija](body)
          response.henkilö.hetu should equal(oppija.hetu)
          response.henkilö.oid should equal(oppija.oid)
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 11, 21))
          response.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(date(2025, 12, 31)))
          response.oppivelvollisuudenKeskeytykset should be(Seq.empty)
        }
      }

      "Palautetaan 404 jos on ohitettu vuosi, jolloin oppija täyttää 20" in {
        KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
          .asetaMockTarkastelupäivä(date(2026, 1, 1))

        AuditLogTester.clearMessages

        postHetu(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.hetu.get) {
          verifyResponseStatus(404, ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
          AuditLogTester.verifyNoAuditLogMessages()
        }
      }

      "Yhden oppijan hakeminen palauttaa oppijan, jolla on oppivelvollisuuden keskeytyksiä, tiedot" in {
        val oppija = ValpasMockOppijat.oppivelvollisuusKeskeytetty

        val expectedKeskeytysData = Seq(ValpasExampleData.oppivelvollisuudenKeskeytykset(0), ValpasExampleData.oppivelvollisuudenKeskeytykset(1))

        val expectedKeskeytykset = expectedKeskeytysData.map(data =>
          ValpasKelaOppivelvollisuudenKeskeytys(
            uuid = "",
            alku = data.alku,
            loppu = data.loppu,
            luotu = data.luotu,
            peruttu = data.peruttu
          )
        )

        postHetu(oppija.hetu.get) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[ValpasKelaOppija](body)
          response.henkilö.hetu should equal(oppija.hetu)
          response.henkilö.oid should equal(oppija.oid)
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 10, 17))
          response.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(date(2025, 12, 31)))

          response.oppivelvollisuudenKeskeytykset.length should be(expectedKeskeytykset.length)
          response.oppivelvollisuudenKeskeytykset.zip(expectedKeskeytykset).zipWithIndex.map {
            case ((actual, expected), index) => {
              withClue(s"index ${index}: ") {
                actual.uuid should not be empty
                actual.alku should equal(expected.alku)
                actual.loppu should equal(expected.loppu)
                actual.luotu should equal(expected.luotu)
                actual.peruttu should equal(expected.peruttu)
              }
            }
          }
        }
      }

      "Yhden oppijan hakeminen palauttaa oppijan, jolla on toistaiseksi voimassaolevia keskeytyksiä, tiedot" in {
        val oppija = ValpasMockOppijat.oppivelvollisuusKeskeytettyToistaiseksi

        val expectedKeskeytysData = Seq(ValpasExampleData.oppivelvollisuudenKeskeytykset(2))

        val expectedKeskeytykset = expectedKeskeytysData.map(data =>
          ValpasKelaOppivelvollisuudenKeskeytys(
            uuid = "",
            alku = data.alku,
            loppu = data.loppu,
            luotu = data.luotu,
            peruttu = data.peruttu
          )
        )

        postHetu(oppija.hetu.get) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[ValpasKelaOppija](body)
          response.henkilö.hetu should equal(oppija.hetu)
          response.henkilö.oid should equal(oppija.oid)
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 9, 14))
          response.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(date(2025, 12, 31)))

          response.oppivelvollisuudenKeskeytykset.length should be(expectedKeskeytykset.length)
          response.oppivelvollisuudenKeskeytykset.zip(expectedKeskeytykset).zipWithIndex.map {
            case ((actual, expected), index) => {
              withClue(s"index ${index}: ") {
                actual.uuid should not be empty
                actual.alku should equal(expected.alku)
                actual.loppu should equal(expected.loppu)
                actual.luotu should equal(expected.luotu)
                actual.peruttu should equal(expected.peruttu)
              }
            }
          }
        }
      }

      "Yhden oppijan hakeminen palauttaa oppijan, joka on vain opppijanumerorekisterissä, jos on ikänsä puolesta ovl-lain piirissä" in {
        val oppija = ValpasMockOppijat.eiKoskessaOppivelvollinen

        postHetu(oppija.hetu.get) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[ValpasKelaOppija](body)
          response.henkilö.hetu should equal(oppija.hetu)
          response.henkilö.oid should equal(oppija.oid)
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 1, 23))
          response.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(date(2025, 12, 31)))

          response.oppivelvollisuudenKeskeytykset.length should be(0)
        }
      }

      "Yhden oppijan hakeminen palauttaa oppijan, joka on vain opppijanumerorekisterissä, jos on ikänsä puolesta ovl-lain piirissä, vaikka olisi yli 18-vuotias" in {
        KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
          .asetaMockTarkastelupäivä(date(2023, 1, 25))

        val oppija = ValpasMockOppijat.eiKoskessaOppivelvollinen

        postHetu(oppija.hetu.get) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[ValpasKelaOppija](body)
          response.henkilö.hetu should equal(oppija.hetu)
          response.henkilö.oid should equal(oppija.oid)
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 1, 23))
          response.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(date(2025, 12, 31)))

          response.oppivelvollisuudenKeskeytykset.length should be(0)
        }
      }

      "Yhden oppijan hakeminen ei palauta vain oppijanumerorekisterissä olevaa oppijaa, jos on ohitettu vuosi, jolloin hän täyttää 20" in {
        KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
          .asetaMockTarkastelupäivä(date(2026, 1, 1))

        val oppija = ValpasMockOppijat.eiKoskessaOppivelvollinen

        postHetu(oppija.hetu.get) {
          verifyResponseStatus(404, ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
          AuditLogTester.verifyNoAuditLogMessages()
        }
      }

      "Yhden oppijan hakeminen ei palauta vain oppijanumerorekisterissä olevaa oppijaa, jos ei olla vielä elokuussa oppijan 7-vuotisvuotena" in {
        KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
          .asetaMockTarkastelupäivä(date(2021, 7, 31))

        val oppija = ValpasMockOppijat.eiKoskessa7VuottaTäyttävä

        postHetu(oppija.hetu.get) {
          verifyResponseStatus(404, ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
          AuditLogTester.verifyNoAuditLogMessages()
        }
      }

      "Yhden oppijan hakeminen palauttaa oppijan, joka ei ole Koskessa, mutta jolla on oppivelvollisuuden keskeytyksiä, tiedot" in {
        val oppija = ValpasMockOppijat.eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia

        val expectedKeskeytysData = Seq(6,7).map(ValpasExampleData.oppivelvollisuudenKeskeytykset)

        val expectedKeskeytykset = expectedKeskeytysData.map(data =>
          ValpasKelaOppivelvollisuudenKeskeytys(
            uuid = "",
            alku = data.alku,
            loppu = data.loppu,
            luotu = data.luotu,
            peruttu = data.peruttu
          )
        )

        postHetu(oppija.hetu.get) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[ValpasKelaOppija](body)
          response.henkilö.hetu should equal(oppija.hetu)
          response.henkilö.oid should equal(oppija.oid)
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 7, 25))
          response.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(date(2025, 12, 31)))

          response.oppivelvollisuudenKeskeytykset.length should be(expectedKeskeytykset.length)
          response.oppivelvollisuudenKeskeytykset.zip(expectedKeskeytykset).zipWithIndex.map {
            case ((actual, expected), index) =>
              withClue(s"index ${index}: ") {
                actual.uuid should not be empty
                actual.alku should equal(expected.alku)
                actual.loppu should equal(expected.loppu)
                actual.luotu should equal(expected.luotu)
                actual.peruttu should equal(expected.peruttu)
              }
          }
        }
      }

      "Palautetaan 404 jos oppija puuttuu" in {
        AuditLogTester.clearMessages

        val puuttuvaHetu = "191105A033F"

        postHetu(puuttuvaHetu) {
          verifyResponseStatus(404, ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
          AuditLogTester.verifyNoAuditLogMessages()
        }
      }

      "Palautetaan 404 jos oppija ei ole oppivelvollisuuslain piirissä" in {
        AuditLogTester.clearMessages

        postHetu(ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.hetu.get) {
          verifyResponseStatus(404, ValpasErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
          AuditLogTester.verifyNoAuditLogMessages()
        }
      }

      "Palautetaan 403-virhe, jos käyttäjällä ei ole oikeutta API:n käyttöön" in {
        AuditLogTester.clearMessages

        postHetu(ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.hetu.get, ValpasMockUsers.valpasHelsinki) {
          verifyResponseStatus(403, ValpasErrorCategory.forbidden())
          AuditLogTester.verifyNoAuditLogMessages()
        }
      }

      "Oppivelvollisuudesta vapautetulle oppijalle palautetaan sen mukaiset päättymispäivät" in {
        val expectedDate = ValpasExampleData.oppivelvollisuudestaVapautetut
          .find(_._1.oid == ValpasMockOppijat.oppivelvollisuudestaVapautettu.oid)
          .get._3
          .minusDays(1)

        KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
          .asetaMockTarkastelupäivä(expectedDate)
        new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()

        postHetu(ValpasMockOppijat.oppivelvollisuudestaVapautettu.hetu.get) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[ValpasKelaOppija](body)


          response.henkilö.oppivelvollisuusVoimassaAsti should equal(expectedDate)
          response.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(expectedDate))
        }
      }
    }

    "Usean oppijan rajapinta" - {

      "Yksittäisen oppijan hakeminen onnistuu ja tuottaa auditlog viestin" in {
        AuditLogTester.clearMessages

        val oppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021
        val hetut = Seq(oppija.hetu.get)

        postHetut(hetut) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPPIVELVOLLISUUSREKISTERI_LUOVUTUS", "target" -> Map("oppijaHenkilöOid" -> oppija.oid)))
        }
      }

      "Usean oppijan hakeminen onnistuu ja tuottaa yhtä monta auditlog-viestiä, kuin oppivelvollisia oppijoita löytyi" in {
        AuditLogTester.clearMessages

        val oppijat = Seq(
          ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
          ValpasMockOppijat.oppivelvollisuusKeskeytetty,
          ValpasMockOppijat.oppivelvollisuusKeskeytettyToistaiseksi
        )
        val puuttuvaHetu = "191105A033F"
        val eiOppivelvollinenHetu = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.hetu.get
        val hetut = oppijat.map(_.hetu.get) ++ Seq(puuttuvaHetu, eiOppivelvollinenHetu)

        postHetut(hetut) {
          verifyResponseStatusOk()
          val auditLogMessages = AuditLogTester.getLogMessages

          auditLogMessages.length should be(3)
        }
      }

      "Usean oppijan hakeminen palauttaa löytyneet oppijat ja heidän oppivelvollisuuden keskeytysten tiedot" in {
        val oppijat = Seq(
          ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
          ValpasMockOppijat.oppivelvollisuusKeskeytetty,
          ValpasMockOppijat.oppivelvollisuusKeskeytettyToistaiseksi,
        )
        val puuttuvaHetu = "191105A033F"
        val eiOppivelvollinenHetu = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.hetu.get
        val hetut = oppijat.map(_.hetu.get) ++ Seq(puuttuvaHetu, eiOppivelvollinenHetu)

        val expectedOppivelvollisuusVoimassaAsti = Seq(
          date(2023, 11, 21),
          date(2023, 10, 17),
          date(2023, 9, 14)
        )

        val expectedOikeusKoulutuksenMaksuttomuuteenVoimassaAsti =
          Seq.fill(3)(date(2025, 12, 31))

        val expectedKeskeytysDatat = Seq(
          Seq.empty,
          Seq(ValpasExampleData.oppivelvollisuudenKeskeytykset(0), ValpasExampleData.oppivelvollisuudenKeskeytykset(1)),
          Seq(ValpasExampleData.oppivelvollisuudenKeskeytykset(2))
        )

        val expectedKeskeytykset = expectedKeskeytysDatat.map(_.map(data =>
          ValpasKelaOppivelvollisuudenKeskeytys(
            uuid = "",
            alku = data.alku,
            loppu = data.loppu,
            luotu = data.luotu,
            peruttu = data.peruttu
          )
        ))

        postHetut(hetut) {
          verifyResponseStatusOk()
          val oppijaResponset = JsonSerializer.parse[Seq[ValpasKelaOppija]](body)
            .map(oppija => oppija.henkilö.oid -> oppija)
            .toMap

          oppijat.zipWithIndex.foreach{ case(oppija, index) => {
            withClue(s"oppija ${index}: ") {
              val oppijaResponse = oppijaResponset(oppija.oid)

              oppijaResponse.henkilö.hetu should equal(oppija.hetu)
              oppijaResponse.henkilö.oid should equal(oppija.oid)
              oppijaResponse.henkilö.oppivelvollisuusVoimassaAsti should equal(expectedOppivelvollisuusVoimassaAsti(index))
              oppijaResponse.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(expectedOikeusKoulutuksenMaksuttomuuteenVoimassaAsti(index)))

              oppijaResponse.oppivelvollisuudenKeskeytykset.length should be(expectedKeskeytykset(index).length)
              oppijaResponse.oppivelvollisuudenKeskeytykset.zip(expectedKeskeytykset(index)).zipWithIndex.map {
                case ((actual, expected), index) => {
                  withClue(s"keskeytys ${index}: ") {
                    actual.uuid should not be empty
                    actual.alku should equal(expected.alku)
                    actual.loppu should equal(expected.loppu)
                    actual.luotu should equal(expected.luotu)
                    actual.peruttu should equal(expected.peruttu)
                  }
                }
              }
            }
          }}
        }
      }

      "Palautetaan 400-virhe, jos yritetään kysyä liian monen oppijan tietoja" in {
        AuditLogTester.clearMessages

        val liikaaHetuja = Seq.fill(1001)("191105A033F")

        postHetut(liikaaHetuja) {
          verifyResponseStatus(400, ValpasErrorCategory.badRequest("Liian monta hetua, enintään 1000 sallittu"))
          AuditLogTester.verifyNoAuditLogMessages()
        }
      }

      "Palautetaan 403-virhe, jos käyttäjällä ei ole oikeutta API:n käyttöön" in {
        AuditLogTester.clearMessages

        postHetut(Seq(ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.hetu.get), ValpasMockUsers.valpasHelsinki) {
          verifyResponseStatus(403, ValpasErrorCategory.forbidden())
          AuditLogTester.verifyNoAuditLogMessages()
        }
      }
    }
  }

  private def postHetu[A](hetu: String, user: ValpasMockUser = ValpasMockUsers.valpasKela)(f: => A): A = {
    post(
      "valpas/api/luovutuspalvelu/kela/hetu",
      JsonSerializer.writeWithRoot(ValpasKelaRequest(hetu)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def postHetut[A](hetut: Seq[String], user: ValpasMockUser = ValpasMockUsers.valpasKela)(f: => A): A = {
    post(
      "valpas/api/luovutuspalvelu/kela/hetut",
      JsonSerializer.writeWithRoot(ValpasKelaBulkRequest(hetut)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }
}

