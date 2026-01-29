package fi.oph.koski.todistus

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{AuditLogTester, KoskiAuditLogMessageField, KoskiOperation}

class TodistusAuditLogSpec extends TodistusSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  "Audit-lokitukset" - {
    "TODISTUKSEN_LUONTI lokitetaan kun kansalainen luo todistuksen" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        AuditLogTester.clearMessages()

        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          AuditLogTester.verifyLastAuditLogMessage(Map(
            "operation" -> KoskiOperation.TODISTUKSEN_LUONTI.toString,
            "target" -> Map(
              KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid,
              KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> opiskeluoikeusOid,
              KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
            )
          ))
        }
      }
    }

    "TODISTUKSEN_LUONTI lokitetaan kun huoltaja luo todistuksen huollettavalle" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOid = KoskiSpecificMockOppijat.eskari.oid
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        AuditLogTester.clearMessages()

        addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          AuditLogTester.verifyLastAuditLogMessage(Map(
            "operation" -> KoskiOperation.TODISTUKSEN_LUONTI.toString,
            "target" -> Map(
              KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> huollettavanOid,
              KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> huollettavanOpiskeluoikeusOid,
              KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
            )
          ))
        }
      }
    }

    "TODISTUKSEN_LATAAMINEN lokitetaan kun kansalainen lataa valmiin todistuksen" in {
      val lang = "fi"
      val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
      val hetu = oppija.hetu.get
      val oppijaOid = oppija.oid
      val opiskeluoikeus = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid)
      val opiskeluoikeusOid = opiskeluoikeus.flatMap(_.oid).get
      val opiskeluoikeusVersionumero = opiskeluoikeus.flatMap(_.versionumero).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      AuditLogTester.clearMessages()

      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", hetu)

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> KoskiOperation.TODISTUKSEN_LATAAMINEN.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> opiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> opiskeluoikeusVersionumero.toString,
          KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
        )
      ))
    }

    "TODISTUKSEN_LATAAMINEN lokitetaan kun huoltaja lataa huollettavan todistuksen" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOid = KoskiSpecificMockOppijat.eskari.oid
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get
      val huollettavanOpiskeluoikeusVersionumero = huollettavanOo.versionumero.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

      val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, huoltajanHetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      AuditLogTester.clearMessages()

      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", huoltajanHetu)

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> KoskiOperation.TODISTUKSEN_LATAAMINEN.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> huollettavanOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> huollettavanOpiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> huollettavanOpiskeluoikeusVersionumero.toString,
          KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
        )
      ))
    }

    "TODISTUKSEN_LATAAMINEN lokitetaan kun oppija lataa huoltajan luoman todistuksen" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val huollettavanOid = KoskiSpecificMockOppijat.eskari.oid
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get
      val huollettavanOpiskeluoikeusVersionumero = huollettavanOo.versionumero.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

      // Huoltaja luo todistuspyynnön
      val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, huoltajanHetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      AuditLogTester.clearMessages()

      // Huollettava itse lataa todistuksen
      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", huollettavanHetu)

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> KoskiOperation.TODISTUKSEN_LATAAMINEN.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> huollettavanOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> huollettavanOpiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> huollettavanOpiskeluoikeusVersionumero.toString,
          KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
        )
      ))
    }

    "TODISTUKSEN_LATAAMINEN ei lokiteta kun todistus ei ole valmis" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob
        }

        AuditLogTester.clearMessages()

        getResult(s"/todistus/download/${todistusJob.id}", hetu) {
          verifyResponseStatus(503)
        }

        // Vain KANSALAINEN_LOGIN-loki pitäisi löytyä, ei TODISTUKSEN_LATAAMINEN-lokia
        val logMessages = AuditLogTester.getLogMessages
        logMessages.length should equal(1)
        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> KoskiOperation.KANSALAINEN_LOGIN.toString
        ))
      }
    }

    "TODISTUKSEN_ESIKATSELU lokitetaan kun OPH-pääkäyttäjä katsoo todistuksen esikatselua" in {
      val lang = "fi"
      val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
      val oppijaOid = oppija.oid
      val opiskeluoikeus = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid)
      val opiskeluoikeusOid = opiskeluoikeus.flatMap(_.oid).get
      val opiskeluoikeusVersionumero = opiskeluoikeus.flatMap(_.versionumero).get

      AuditLogTester.clearMessages()

      get(s"todistus/preview/$lang/$opiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatusOk()
      }

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> KoskiOperation.TODISTUKSEN_ESIKATSELU.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> opiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> opiskeluoikeusVersionumero.toString
        )
      ))
    }
  }
}
