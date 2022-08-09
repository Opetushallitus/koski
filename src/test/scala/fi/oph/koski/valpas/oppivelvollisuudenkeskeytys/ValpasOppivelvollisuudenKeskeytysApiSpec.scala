package fi.oph.koski.valpas.oppivelvollisuudenkeskeytys

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.valpas.ValpasTestBase
import fi.oph.koski.valpas.db.ValpasSchema.OppivelvollisuudenKeskeytyshistoriaRow
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.valpasrepository.{OppivelvollisuudenKeskeytyksenMuutos, UusiOppivelvollisuudenKeskeytys, ValpasOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.json4s.jackson.JsonMethods
import org.scalatest.BeforeAndAfterEach

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

class ValpasOppivelvollisuudenKeskeytysApiSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach(): Unit = {
    AuditLogTester.clearMessages()
  }

  val oppivelvollisuudenKeskeytysService = KoskiApplicationForTests.valpasOppivelvollisuudenKeskeytysService
  val user: ValpasMockUser = ValpasMockUsers.valpasUseitaKuntia
  val oppijaOid: String = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid
  val organisaatioOid: String = MockOrganisaatiot.helsinginKaupunki

  val okMääräaikainenKeskeytys: UusiOppivelvollisuudenKeskeytys = okMääräaikainenKeskeytys(oppijaOid)

  def okMääräaikainenKeskeytys(oppijaOid: String): UusiOppivelvollisuudenKeskeytys = UusiOppivelvollisuudenKeskeytys(
    oppijaOid = oppijaOid,
    alku = LocalDate.of(2021, 8, 1),
    loppu = Some(LocalDate.of(2021, 9, 1)),
    tekijäOrganisaatioOid = organisaatioOid,
  )

  "Audit-logitus" - {
    "Keskeytyksen lisääminen" in {
      lisääKeskeytys(okMääräaikainenKeskeytys) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> ValpasOperation.VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYS.toString,
          "target" -> Map(
            ValpasAuditLogMessageField.oppijaHenkilöOid.toString -> oppijaOid,
            ValpasAuditLogMessageField.ilmoittajaOrganisaatioOid.toString -> organisaatioOid,
          )
        ))
      }
    }

    "Keskeytyksen muokkaaminen" in {
      muokkaaKeskeytystä(OppivelvollisuudenKeskeytyksenMuutos(
        id = uusiKeskeytys.id,
        alku = LocalDate.of(2022, 1, 1),
        loppu = Some(LocalDate.of(2022, 2, 1))
      )) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> ValpasOperation.VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYKSEN_MUOKKAUS.toString,
          "target" -> Map(
            ValpasAuditLogMessageField.oppijaHenkilöOid.toString -> oppijaOid,
            ValpasAuditLogMessageField.ilmoittajaOrganisaatioOid.toString -> organisaatioOid,
          )
        ))
      }
    }

    "Keskeytyksen poistaminen" in {
      poistaKeskeytys(uusiKeskeytys.id) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> ValpasOperation.VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYKSEN_POISTO.toString,
          "target" -> Map(
            ValpasAuditLogMessageField.oppijaHenkilöOid.toString -> oppijaOid,
            ValpasAuditLogMessageField.ilmoittajaOrganisaatioOid.toString -> organisaatioOid,
          )
        ))
      }
    }

    "Muutoshistoria" - {
      "Keskeytyksen lisääminen, muutos ja poisto" in {
        val alkuMuokattu = LocalDate.of(2022, 1, 1)
        val loppuMuokattu = LocalDate.of(2022, 2, 1)

        lisääKeskeytys(okMääräaikainenKeskeytys) {
          verifyResponseStatusOk()
          val ovKeskeytysUuid = getResult.id
          muokkaaKeskeytystä(OppivelvollisuudenKeskeytyksenMuutos(
            id = ovKeskeytysUuid,
            alku = alkuMuokattu,
            loppu = Some(loppuMuokattu),
          )) {
            verifyResponseStatusOk()
            poistaKeskeytys(ovKeskeytysUuid) {
              verifyResponseStatusOk()

              val entry1 = OppivelvollisuudenKeskeytyshistoriaRow(
                ovKeskeytysUuid = UUID.fromString(ovKeskeytysUuid),
                muutosTehty = LocalDateTime.now(),
                muutoksenTekijä = user.oid,
                oppijaOid = oppijaOid,
                alku = okMääräaikainenKeskeytys.alku,
                loppu = okMääräaikainenKeskeytys.loppu,
                luotu = LocalDateTime.now(),
                tekijäOid = user.oid,
                tekijäOrganisaatioOid = organisaatioOid,
                peruttu = false,
              )
              val entry2 = entry1.copy(
                alku = alkuMuokattu,
                loppu = Some(loppuMuokattu),
              )
              val entry3 = entry2.copy(
                peruttu = true,
              )
              val expectedHistoria = List(entry1, entry2, entry3)

              val muutosHistoria = getResultedMuutoshistoria
              muutosHistoria.size should equal(expectedHistoria.size)
              muutosHistoria
                .zip(expectedHistoria)
                .foreach(Function.tupled(verifyHistoriaEntry))
            }
          }
        }
      }

      "Keskeytyksen lisääminen, muutos ja poisto Koskesta puuttuvalle oppijalle" in {
        val alkuMuokattu = LocalDate.of(2022, 1, 1)
        val loppuMuokattu = LocalDate.of(2022, 2, 1)

        val oppijaOid = ValpasMockOppijat.eiKoskessaOppivelvollinen.oid

        lisääKeskeytys(okMääräaikainenKeskeytys(oppijaOid)) {
          verifyResponseStatusOk()
          val ovKeskeytysUuid = getResult.id
          muokkaaKeskeytystä(OppivelvollisuudenKeskeytyksenMuutos(
            id = ovKeskeytysUuid,
            alku = alkuMuokattu,
            loppu = Some(loppuMuokattu),
          )) {
            verifyResponseStatusOk()
            poistaKeskeytys(ovKeskeytysUuid) {
              verifyResponseStatusOk()

              val entry1 = OppivelvollisuudenKeskeytyshistoriaRow(
                ovKeskeytysUuid = UUID.fromString(ovKeskeytysUuid),
                muutosTehty = LocalDateTime.now(),
                muutoksenTekijä = user.oid,
                oppijaOid = oppijaOid,
                alku = okMääräaikainenKeskeytys.alku,
                loppu = okMääräaikainenKeskeytys.loppu,
                luotu = LocalDateTime.now(),
                tekijäOid = user.oid,
                tekijäOrganisaatioOid = organisaatioOid,
                peruttu = false,
              )
              val entry2 = entry1.copy(
                alku = alkuMuokattu,
                loppu = Some(loppuMuokattu),
              )
              val entry3 = entry2.copy(
                peruttu = true,
              )
              val expectedHistoria = List(entry1, entry2, entry3)

              val muutosHistoria = getResultedMuutoshistoria
              muutosHistoria.size should equal(expectedHistoria.size)
              muutosHistoria
                .zip(expectedHistoria)
                .foreach(Function.tupled(verifyHistoriaEntry))
            }
          }
        }
      }
    }
  }

  private def uusiKeskeytys: ValpasOppivelvollisuudenKeskeytys =
    oppivelvollisuudenKeskeytysService
      .addOppivelvollisuudenKeskeytys(okMääräaikainenKeskeytys)(session(user))
      .right.get

  private def lisääKeskeytys[A](keskeytys: UusiOppivelvollisuudenKeskeytys)(f: => A): A = post(
    uri = "/valpas/api/oppija/ovkeskeytys",
    headers = authHeaders(user) ++ jsonContent,
    body = JsonSerializer.writeWithRoot(keskeytys, pretty = true)
  )(f)

  private def muokkaaKeskeytystä[A](keskeytys: OppivelvollisuudenKeskeytyksenMuutos)(f: => A): A = put(
    uri = "/valpas/api/oppija/ovkeskeytys",
    headers = authHeaders(user) ++ jsonContent,
    body = JsonSerializer.writeWithRoot(keskeytys, pretty = true)
  )(f)

  private def poistaKeskeytys[A](keskeytysId: String)(f: => A): A = delete(
    uri = s"/valpas/api/oppija/ovkeskeytys/${keskeytysId}",
    headers = authHeaders(user) ++ jsonContent,
  )(f)

  private def getResultedMuutoshistoria: Seq[OppivelvollisuudenKeskeytyshistoriaRow] =
    getMuutoshistoria(UUID.fromString(getResult.id))

  private def getResult: ValpasOppivelvollisuudenKeskeytys =
    JsonSerializer.extract[ValpasOppivelvollisuudenKeskeytys](JsonMethods.parse(body))

  private def getMuutoshistoria(id: UUID): Seq[OppivelvollisuudenKeskeytyshistoriaRow] =
    oppivelvollisuudenKeskeytysService
      .getOppivelvollisuudenKeskeytyksenMuutoshistoria(id)(session(user))
      .right.get

  private def verifyHistoriaEntry(a: OppivelvollisuudenKeskeytyshistoriaRow, b: OppivelvollisuudenKeskeytyshistoriaRow): Unit = {
    withClue(s"Actual: ${a}\nExpected: ${b}") {
      a.ovKeskeytysUuid should equal(b.ovKeskeytysUuid)
      a.oppijaOid should equal(b.oppijaOid)
      a.alku should equal(b.alku)
      a.loppu should equal(b.loppu)
      a.peruttu should equal(b.peruttu)
      a.muutoksenTekijä should equal(b.muutoksenTekijä)
      a.tekijäOid should equal(b.tekijäOid)
      a.tekijäOrganisaatioOid should equal(b.tekijäOrganisaatioOid)
    }
  }
}
