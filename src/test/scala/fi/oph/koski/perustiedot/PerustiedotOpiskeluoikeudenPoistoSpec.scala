package fi.oph.koski.perustiedot

import fi.oph.koski.db.KoskiTables.{KoskiOpiskeluOikeudet, PerustiedotSync}
import fi.oph.koski.db.PerustiedotSyncRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.{JInt, JObject}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PerustiedotOpiskeluoikeudenPoistoSpec
  extends AnyFreeSpec
    with Matchers
    with KoskiHttpSpec
    with DatabaseTestMethods
    with DirtiesFixtures {

  private val indexer = KoskiApplicationForTests.perustiedotIndexer
  private val skeema = KoskiApplicationForTests.config.getString("dbs.koski.schemaName")

  "Opiskeluoikeusrivin poisto suoraan tietokannasta poistaa opiskeluoikeuden perustiedot-indeksistä" - {
    "kun jonossa ei ole muuta" in {
      val id = opiskeluoikeusId(KoskiSpecificMockOppijat.ysiluokkalainen)
      dokumenttejaIndeksissä(id) should equal(1)

      runDbSync(sqlu"DELETE FROM opiskeluoikeus WHERE id = $id")
      indexer.sync(refresh = true)

      dokumenttejaIndeksissä(id) should equal(0)
    }

    "vaikka samassa transaktiossa olisi jonotettu päivitys" in {
      val id = opiskeluoikeusId(KoskiSpecificMockOppijat.eskari)
      dokumenttejaIndeksissä(id) should equal(1)

      runDbSync(DBIO.seq(
        PerustiedotSync += PerustiedotSyncRow(opiskeluoikeusId = id, data = JObject("id" -> JInt(id)), upsert = true),
        sqlu"DELETE FROM opiskeluoikeus WHERE id = $id"
      ).transactionally)
      runDbSync(PerustiedotSync.filter(_.opiskeluoikeusId === id).map(_.upsert).result) should not contain true
      indexer.sync(refresh = true)

      dokumenttejaIndeksissä(id) should equal(0)
    }

    "vaikka poistajan istunnon search_path ei sisältäisi Kosken skeemaa" in {
      val id = opiskeluoikeusId(KoskiSpecificMockOppijat.lukiolainen)
      dokumenttejaIndeksissä(id) should equal(1)

      runDbSync(DBIO.seq(
        sqlu"SET LOCAL search_path TO public",
        sqlu"DELETE FROM #$skeema.opiskeluoikeus WHERE id = $id"
      ).transactionally)
      indexer.sync(refresh = true)

      dokumenttejaIndeksissä(id) should equal(0)
    }

    "laukaisimen search_path ei riipu migraation ajajasta" in {
      // Edellinen testi menee läpi myös oletuspolulla "$user", public, jos käyttäjä on nimetty skeeman mukaan
      runDbSync(
        sql"SELECT array_to_string(proconfig, ',') FROM pg_proc WHERE proname = 'add_perustiedot_sync_delete_row'".as[String]
      ) should equal(Vector(s"search_path=$skeema"))
    }
  }

  private def opiskeluoikeusId(oppija: OppijaHenkilö): Int =
    runDbSync(KoskiOpiskeluOikeudet.filter(_.oppijaOid === oppija.oid).map(_.id).result).head

  private def dokumenttejaIndeksissä(id: Int): Int = {
    val vastaus = indexer.index.runSearch(JObject("query" -> JObject("term" -> JObject("id" -> JInt(id))))).get
    extract[Int](vastaus \ "hits" \ "total" \ "value")
  }
}
