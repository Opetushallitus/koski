package fi.oph.koski.schedule

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.henkilo.MockOppijat.eskari
import fi.oph.koski.henkilo.{MockOppijat, VerifiedHenkilöOid}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusByOid, PostgresOpiskeluoikeusRepository}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.util.{Futures, Wait}
import org.scalatest.{FreeSpec, Matchers}

class PerustiedotSyncSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification with KoskiDatabaseMethods {
  // TODO: rewrite
  private implicit val session = KoskiSession.systemUser
  private lazy val application = KoskiApplicationForTests
  private lazy val opiskeluoikeusRepository = new PostgresOpiskeluoikeusRepository(application.masterDatabase.db, application.historyRepository, application.henkilöCache, application.oidGenerator, application.henkilöRepository.opintopolku, application.perustiedotSyncRepository)
  override protected def db: DB = KoskiApplicationForTests.masterDatabase.db

  /*

  "Synkkaa perustiedot kannasta" - {
    Futures.await(application.perustiedotIndexer.init)
    "Päivittää synkkausta tarvitsevat oppijat tietokannasta elasticsearchiin" in {
      val opiskeluoikeus = modifyOpiskeluoikeus(oppija(MockOppijat.eskari.oid).tallennettavatOpiskeluoikeudet.head.withPäättymispäivä(now))
      runSync(opiskeluoikeus)
      val päivitetytPerustiedot = application.perustiedotRepository.findHenkiloPerustiedotByOids(List(eskari.oid)).head
      päivitetytPerustiedot.päättymispäivä should equal(opiskeluoikeus.päättymispäivä)
    }
  }

  private def modifyOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    opiskeluoikeusRepository.createOrUpdate(VerifiedHenkilöOid(MockOppijat.eskari.henkilö), opiskeluoikeus, allowUpdate = true)
    opiskeluoikeus
  }

  private def runSync(opiskeluoikeus: Opiskeluoikeus) {
    markForSyncing(opiskeluoikeus)
    PerustiedotSyncScheduler.syncPerustiedot(application)(None)
    application.elasticSearch.refreshIndex
    Wait.until(opiskeluoikeus.päättymispäivä == application.perustiedotRepository.findHenkiloPerustiedotByOids(List(eskari.oid)).head.päättymispäivä, timeoutMs = 2000)
  }

  private def markForSyncing(opiskeluoikeus: Opiskeluoikeus) =
    KoskiApplicationForTests.perustiedotSyncRepository.syncLater(List(opiskeluoikeusRow(opiskeluoikeus.oid.get).id))

  private def opiskeluoikeusRow(oid: String) =
    runDbSync(opiskeluoikeusRepository.findByIdentifierAction(OpiskeluoikeusByOid(oid))).right.get.head

  */
}
