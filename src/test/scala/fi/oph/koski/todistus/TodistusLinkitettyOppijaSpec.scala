package fi.oph.koski.todistus

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper

class TodistusLinkitettyOppijaSpec extends TodistusSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  private val master = KoskiSpecificMockOppijat.master
  private val slave = KoskiSpecificMockOppijat.slave
  private val hetu = master.hetu.get

  private def luoOpiskeluoikeusSlavelle(): String = {
    val slaveOo = setupOppijaWithAndGetOpiskeluoikeus(
      vahvistettuKielitutkinnonOpiskeluoikeus,
      slave.henkilö,
      MockUsers.paakayttaja
    )
    slaveOo.oid.get
  }

  private def verifyPdfSisältääMasterinTiedot(pdfBytes: Array[Byte]): Unit = {
    master.etunimet should not be(slave.henkilö.etunimet)

    val document = Loader.loadPDF(pdfBytes)
    try {
      val pdfText = new PDFTextStripper().getText(document)

      pdfText should include(master.etunimet)

      pdfText should not include slave.henkilö.etunimet
    } finally {
      document.close()
    }
  }

  "Todistus linkitetyn oppijan opiskeluoikeudesta" - {
    "käyttää master-oppijan henkilötietoja kun kansalainen lataa todistuksen" in {
      withoutRunningTiedoteScheduler {
        val opiskeluoikeusOid = luoOpiskeluoikeusSlavelle()
        val req = TodistusGenerateRequest(opiskeluoikeusOid, "fi")

        val job = addGenerateJobSuccessfully(req, hetu) { j =>
          j.oppijaOid should equal(slave.henkilö.oid)
          j
        }
        val completedJob = waitForCompletion(job.id, hetu)
        completedJob.state should equal(TodistusState.COMPLETED)

        verifyDownloadResultAndContent(s"/todistus/download/${completedJob.id}", hetu) {
          verifyPdfSisältääMasterinTiedot(response.getContentBytes())
        }
      }
    }

    "käyttää master-oppijan henkilötietoja kun virkailija lataa todistuksen" in {
      withoutRunningTiedoteScheduler {
        val opiskeluoikeusOid = luoOpiskeluoikeusSlavelle()
        val req = TodistusGenerateRequest(opiskeluoikeusOid, "fi")

        val job = addGenerateJobSuccessfullyAsVirkailijaPääkäyttäjä(req) { j =>
          j.oppijaOid should equal(slave.henkilö.oid)
          j
        }
        val completedJob = waitForCompletionAsVirkailijaPääkäyttäjä(job.id)
        completedJob.state should equal(TodistusState.COMPLETED)

        verifyPresignedResultAndContent(s"/todistus/download/presigned/${completedJob.id}") {
          verifyPdfSisältääMasterinTiedot(response.getContentBytes())
        }
      }
    }
  }
}
