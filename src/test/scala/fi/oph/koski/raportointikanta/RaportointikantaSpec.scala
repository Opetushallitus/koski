package fi.oph.koski.raportointikanta

import java.time.LocalDate
import java.sql.Date

import fi.oph.koski.{KoskiApplicationForTests, schema}
import fi.oph.koski.api.LocalJettyHttpSpecification
import org.scalatest.{FreeSpec, Matchers}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.JsonFiles
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor

class RaportointikantaSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers {

  private val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  "Raportointikannan rakennus-APIt" - {
    "Skeeman luonti (ja kannan tyhjennys)" in {
      authGet("api/raportointikanta/clear") {
        verifyResponseStatusOk()
        opiskeluoikeusCount should equal(0)
        henkiloCount should equal(0)
        organisaatioCount should equal(0)
        koodistoKoodiCount should equal(0)
      }
    }
    "Opiskeluoikeuksien lataus" in {
      authGet("api/raportointikanta/opiskeluoikeudet") {
        verifyResponseStatusOk()
        opiskeluoikeusCount should be > 30
      }
    }
    "Henkilöiden lataus" in {
      authGet("api/raportointikanta/henkilot") {
        val mockOppija = MockOppijat.eero
        verifyResponseStatusOk()
        henkiloCount should be > 30
        val henkilo = raportointiDatabase.runDbSync(raportointiDatabase.RHenkilöt.filter(_.hetu === mockOppija.hetu.get).result)
        henkilo should equal(Seq(RHenkilöRow(
          mockOppija.oid,
          mockOppija.hetu,
          Some(Date.valueOf("1901-01-01")),
          mockOppija.sukunimi,
          mockOppija.etunimet,
          Some("FI"),
          None,
          false
        )))
      }
    }
    "Organisaatioiden lataus" in {
      authGet("api/raportointikanta/organisaatiot") {
        verifyResponseStatusOk()
        organisaatioCount should be > 10
        val organisaatio = raportointiDatabase.runDbSync(raportointiDatabase.ROrganisaatiot.filter(_.organisaatioOid === MockOrganisaatiot.aapajoenKoulu).result)
        organisaatio should equal(Seq(ROrganisaatioRow(MockOrganisaatiot.aapajoenKoulu, "Aapajoen koulu", "OPPILAITOS", Some("11"), Some("04044"), Some("851"))))
      }
    }
    "Koodistojen lataus" in {
      authGet("api/raportointikanta/koodistot") {
        verifyResponseStatusOk()
        koodistoKoodiCount should be > 500
        val koodi = raportointiDatabase.runDbSync(raportointiDatabase.RKoodistoKoodit.filter(_.koodistoUri === "opiskeluoikeudentyyppi").filter(_.koodiarvo === "korkeakoulutus").result)
        koodi should equal(Seq(RKoodistoKoodiRow("opiskeluoikeudentyyppi", "korkeakoulutus", "Korkeakoulutus")))
      }
    }
  }

  "Opiskeluoikeuksien lataus" - {
    import KoskiSchema.deserializationContext
    val ammatillinenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/ammatillinen-perustutkinto_2018-02-14.json")
    val oid = "1.2.246.562.15.123456"
    val ammatillinenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](ammatillinenJson).right.get.opiskeluoikeudet.head.asInstanceOf[AmmatillinenOpiskeluoikeus].copy(oid = Some(oid))
    val perusopetuksenJson = JsonFiles.readFile("src/test/resources/backwardcompatibility/perusopetuksenoppimaara-paattotodistus_2018-02-14.json")
    val perusopetuksenOpiskeluoikeus = SchemaValidatingExtractor.extract[Oppija](perusopetuksenJson).right.get.opiskeluoikeudet.head.asInstanceOf[PerusopetuksenOpiskeluoikeus].copy(oid = Some(oid))

    val Läsnä = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
    val Loma =  Koodistokoodiviite("loma", "koskiopiskeluoikeudentila")
    val Valmistunut = Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila")

    "Aikajaksorivien rakennus" - {
      "Opiskeluoikeusjaksot, kesken" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          ))
        )
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna", false, None, 0, 0, 0, 0, 100, 0, 0)
        ))
      }
      "Opiskeluoikeusjaksot, päättynyt" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä),
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 6, 1), tila = Loma),
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 9, 1), tila = Läsnä, opintojenRahoitus = Some(Koodistokoodiviite("2", "opintojenrahoitus"))),
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 12, 16), tila = Valmistunut)
          ))
        )
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-05-31"), "lasna"),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-06-01"), Date.valueOf("2016-08-31"), "loma"),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-09-01"), Date.valueOf("2016-12-15"), "lasna", opintojenRahoitus = Some("2")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-12-16"), Date.valueOf("2016-12-16"), "valmistunut", opiskeluoikeusPäättynyt = true)
        ))
      }
      "Ammatillisen opiskeluoikeuden lisätiedot, yksinkertainen tapaus" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            erityinenTuki = Some(List(Aikajakso(LocalDate.of(2016, 2, 1), Some(LocalDate.of(2016, 2, 28)))))
          ))
        )
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna"),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-28"), "lasna", erityinenTuki = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-29"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna")
        ))
      }
      "Ammatillisen opiskeluoikeuden lisätiedot, monimutkainen 1" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            erityinenTuki = Some(List(
              Aikajakso(LocalDate.of(2016, 2, 1), Some(LocalDate.of(2016, 2, 10))),
              Aikajakso(LocalDate.of(2016, 4, 1), Some(LocalDate.of(2016, 4, 20)))
            )),
            osaAikaisuusjaksot = Some(List(
              OsaAikaisuusJakso(LocalDate.of(2016, 3, 1), Some(LocalDate.of(2016, 3, 31)), 80)
            )),
            vankilaopetuksessa = Some(List(
              Aikajakso(LocalDate.of(2016, 2, 5), Some(LocalDate.of(2016, 4, 10)))
            ))
          ))
        )
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna"),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-04"), "lasna", erityinenTuki = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-05"), Date.valueOf("2016-02-10"), "lasna", erityinenTuki = 1, vankilaopetuksessa = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-11"), Date.valueOf("2016-02-29"), "lasna", vankilaopetuksessa = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-03-31"), "lasna", vankilaopetuksessa = 1, osaAikaisuus = 80),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-01"), Date.valueOf("2016-04-10"), "lasna", erityinenTuki = 1, vankilaopetuksessa = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-11"), Date.valueOf("2016-04-20"), "lasna", erityinenTuki = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-21"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna")
        ))
      }
      "Ammatillien opiskeluoikeuden lisätiedot, monimutkainen 2" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            opiskeluvalmiuksiaTukevatOpinnot = Some(List(
              OpiskeluvalmiuksiaTukevienOpintojenJakso(LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 29), LocalizedString.finnish("Kuvaus"))
            )),
            vaikeastiVammainen = Some(List(
              Aikajakso(LocalDate.of(2016, 2, 29), Some(LocalDate.of(2016, 3, 31)))
            ))
          ))
        )
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna"),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-28"), "lasna", opiskeluvalmiuksiaTukevatOpinnot = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-29"), Date.valueOf("2016-02-29"), "lasna", opiskeluvalmiuksiaTukevatOpinnot = 1, vaikeastiVammainen = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-03-31"), "lasna", vaikeastiVammainen = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-04-01"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna")
        ))
      }
      "Perusopetuksen opiskeluoikeuden lisätiedot" in {
        val opiskeluoikeus = perusopetuksenOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(alku = LocalDate.of(2017, 1, 1), tila = Läsnä)
          )),
          lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
            vaikeastiVammainen = Some(List(
              Aikajakso(LocalDate.of(2017, 1, 1), Some(LocalDate.of(2017, 3, 31)))
            ))
          ))
        )
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-01-01"), Date.valueOf("2017-03-31"), "lasna", vaikeastiVammainen = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2017-04-01"), Date.valueOf(OpiskeluoikeusLoader.IndefiniteFuture), "lasna")
        ))
      }
      "Aikajaksot rajataan opiskeluoikeuden alku/loppupäivän väliin" in {
        val opiskeluoikeus = ammatillinenOpiskeluoikeus.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 1, 15), tila = Läsnä),
            AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.of(2016, 12, 16), tila = Valmistunut)
          )),
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
            hojks = None,
            erityinenTuki = Some(List(
              Aikajakso(LocalDate.of(2016, 1, 1), Some(LocalDate.of(2016, 12, 15)))
            )),
            osaAikaisuusjaksot = Some(List(
              OsaAikaisuusJakso(LocalDate.of(2016, 3, 1), Some(LocalDate.of(2016, 12, 31)), 80)
            ))
          ))
        )
        val aikajaksoRows = OpiskeluoikeusLoader.buildROpiskeluoikeusAikajaksoRows(oid, opiskeluoikeus)
        aikajaksoRows should equal(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-02-29"), "lasna", erityinenTuki = 1),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-03-01"), Date.valueOf("2016-12-15"), "lasna", erityinenTuki = 1, osaAikaisuus = 80),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-12-16"), Date.valueOf("2016-12-16"), "valmistunut", osaAikaisuus = 80, opiskeluoikeusPäättynyt = true)
        ))
      }
    }
  }

  private def opiskeluoikeusCount: Int = raportointiDatabase.runDbSync(raportointiDatabase.ROpiskeluoikeudet.length.result)
  private def henkiloCount: Int = raportointiDatabase.runDbSync(raportointiDatabase.RHenkilöt.length.result)
  private def organisaatioCount: Int = raportointiDatabase.runDbSync(raportointiDatabase.ROrganisaatiot.length.result)
  private def koodistoKoodiCount: Int = raportointiDatabase.runDbSync(raportointiDatabase.RKoodistoKoodit.length.result)
}

