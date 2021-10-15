package fi.oph.koski.valpas

import java.time.LocalDate.{of => date}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.kela.{ValpasKelaBulkRequest, ValpasKelaOppija, ValpasKelaOppivelvollisuudenKeskeytys, ValpasKelaRequest}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.valpasrepository.ValpasExampleData
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.scalatest.BeforeAndAfterEach

class ValpasKelaServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    AuditLogTester.clearMessages
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
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 11, 22))
          response.henkilö.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(Some(date(2025, 12, 31)))
          response.oppivelvollisuudenKeskeytykset should be(Seq.empty)
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
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 10, 18))
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
          response.henkilö.oppivelvollisuusVoimassaAsti should equal(date(2023, 9, 15))
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
    }
  }

  private def postHetu[A](hetu: String, user: ValpasMockUser = ValpasMockUsers.valpasKela)(f: => A): A = {
    post(
      "valpas/api/luovutuspalvelu/kela/hetu",
      JsonSerializer.writeWithRoot(ValpasKelaRequest(hetu)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def postHetut[A](hetut: List[String], user: ValpasMockUser = ValpasMockUsers.valpasKela)(f: => A): A = {
    post(
      "valpas/api/luovutuspalvelu/kela/hetut",
      JsonSerializer.writeWithRoot(ValpasKelaBulkRequest(hetut)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }
}

