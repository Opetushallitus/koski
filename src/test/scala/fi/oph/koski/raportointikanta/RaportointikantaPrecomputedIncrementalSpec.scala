package fi.oph.koski.raportointikanta

import fi.oph.koski.api.misc.{OpiskeluoikeudenMitätöintiJaPoistoTestMethods, PutOpiskeluoikeusTestMethods, TestMethodsLukio}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.documentation.LukioExampleData.opiskeluoikeusAktiivinen
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.raportit.Lukio2019RaaportitTestData
import fi.oph.koski.schema.{LukionOpiskeluoikeudenTila, LukionOpiskeluoikeus, LukionOpiskeluoikeusjakso, Oppija}
import fi.oph.koski.util.Wait
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiApplicationForTests}
import org.json4s.jackson.JsonMethods
import org.postgresql.util.PSQLException
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

class RaportointikantaPrecomputedIncrementalSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with DatabaseTestMethods
    with DirtiesFixtures
    with OpiskeluoikeudenMitätöintiJaPoistoTestMethods
    with PutOpiskeluoikeusTestMethods[LukionOpiskeluoikeus] {

  def tag = implicitly[reflect.runtime.universe.TypeTag[LukionOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = TestMethodsLukio.lukionOpiskeluoikeus
  override def defaultUser = MockUsers.paakayttaja

  private val taulut = Seq(
    "osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella",
    "lukion_oppiaineen_oppimaaran_kurssien_rahoitusmuodot",
    "lukion_aineopintojen_moduulien_rahoitusmuodot",
    "lukion_oppiaineen_oppimaaran_eri_vuonna_korotetut",
    "lukion_aineopintojen_eri_vuonna_korotetut",
  )

  private val vertailustaOhitettavatSarakkeet = Set("osasuoritus_id", "paatason_suoritus_id")

  override protected def alterFixture(): Unit = {
    // Lisää LOPS2019-aineopinnot (lukionaineopinnot), jotta lukion_aineopintojen_* -taulut eivät jää tyhjiksi.
    val aineopinnotOo = defaultOpiskeluoikeus.copy(
      tila = LukionOpiskeluoikeudenTila(List(
        LukionOpiskeluoikeusjakso(alku = date(2000, 1, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
      )),
      suoritukset = List(Lukio2019RaaportitTestData.oppiaineidenOppimäärienSuoritus)
    )
    putOppija(Oppija(KoskiSpecificMockOppijat.teija, List(aineopinnotOo))) {
      verifyResponseStatusOk()
    }
    reloadRaportointikanta()
  }

  "Precomputed-taulut inkrementaalisessa päivityksessä" - {
    "Testin lähtötilanne on mielekäs: jokaisessa taulussa on dataa" in {
      taulut.foreach { taulu =>
        withClue(s"Taulu $taulu on tyhjä, testi ei ole mielekäs: ") {
          snapshot(taulu) should not be empty
        }
      }
    }

    "Taulut säilyvät samana kun kaikki niihin vaikuttavat opiskeluoikeudet päivitetään inkrementaalisesti" in {
      val ennen = snapshotAll()

      val vaikuttavatOidit = ennen.values.flatten.flatMap(opiskeluoikeusOidRivistä).toSeq.distinct
      vaikuttavatOidit should not be empty
      vaikuttavatOidit.foreach(KoskiApplicationForTests.päivitetytOpiskeluoikeudetJono.lisää)

      päivitäRaportointikantaInkrementaalisesti()

      snapshotAll() should equal(ennen)
    }

    "Taulut vastaavat täyslatausta kun opiskeluoikeuksia mitätöidään ja päivitetään inkrementaalisesti" in {
      val mitätöitävätOidit = taulut.flatMap(esimerkkiOid).distinct
      mitätöitävätOidit should not be empty

      mitätöitävätOidit.foreach { oid =>
        mitätöiOpiskeluoikeus(oid, MockUsers.paakayttaja)
        KoskiApplicationForTests.päivitetytOpiskeluoikeudetJono.lisää(oid)
      }
      päivitäRaportointikantaInkrementaalisesti()

      val inkrementaalinen = snapshotAll()

      withClue("Mitätöityjen opiskeluoikeuksien rivien pitää kadota kaikista tauluista: ") {
        inkrementaalinen.values.flatten.exists(rivi => mitätöitävätOidit.exists(rivi.contains)) should be(false)
      }

      reloadRaportointikanta()
      val täyslataus = snapshotAll()

      inkrementaalinen should equal(täyslataus)
    }

    "updateOpiskeluoikeusPrecomputedTables välittää poikkeuksen eteenpäin eikä niele sitä, jos taulun päivitys epäonnistuu" in {
      // Luo väliaikaiskantaan vain perustaulut ilman precomputed-tauluja
      tempRaportointiDb.dropAndCreateObjects()
      val thrown = the[PSQLException] thrownBy {
        tempRaportointiDb.updateOpiskeluoikeusPrecomputedTables(Seq("1.2.246.562.15.00000000001"))
      }
      thrown.getMessage should include("does not exist")
    }
  }

  private def snapshotAll(): Map[String, Seq[String]] =
    taulut.map(taulu => taulu -> snapshot(taulu)).toMap

  private def esimerkkiOid(taulu: String): Option[String] =
    mainRaportointiDb.runDbSync(
      sql"select opiskeluoikeus_oid from #${mainRaportointiDb.schema.name}.#$taulu limit 1".as[String]
    ).headOption

  // Kuvaa taulun sisällön deterministisesti: jokainen rivi tekstiksi (pysyvät sarakkeet), järjestettynä.
  // Riippumaton rivien fyysisestä järjestyksestä ja synteettisistä id-arvoista.
  private def snapshot(taulu: String): Seq[String] = {
    val sarakkeet = pysyvätSarakkeet(taulu)
    val sarakelista = sarakkeet.mkString(", ")
    mainRaportointiDb.runDbSync(
      sql"select row(#$sarakelista)::text from #${mainRaportointiDb.schema.name}.#$taulu order by 1".as[String]
    )
  }

  private def pysyvätSarakkeet(taulu: String): Seq[String] =
    mainRaportointiDb.runDbSync(
      sql"""
        select column_name
        from information_schema.columns
        where table_schema = ${mainRaportointiDb.schema.name} and table_name = $taulu
        order by column_name
      """.as[String]
    ).filterNot(vertailustaOhitettavatSarakkeet.contains)

  private def opiskeluoikeusOidRivistä(rivi: String): Option[String] =
    "1\\.2\\.246\\.562\\.15\\.\\d+".r.findFirstIn(rivi)

  private def päivitäRaportointikantaInkrementaalisesti(): Unit = {
    val loadResult = KoskiApplicationForTests.raportointikantaService.loadRaportointikanta(
      force = false,
      skipUnchangedData = true
    )
    loadResult should be(true)
    Wait.until(isLoading)
    Wait.until(loadComplete)
    withClue("Päivitysjono on inkrementaalisen päivityksen jälkeen tyhjä") {
      KoskiApplicationForTests.päivitetytOpiskeluoikeudetJono.kaikki.isEmpty should equal(true)
    }
  }

  private def isLoading: Boolean = authGet("api/raportointikanta/status") {
    (JsonMethods.parse(body) \ "etl" \ "isLoading").extract[Boolean]
  }
}
