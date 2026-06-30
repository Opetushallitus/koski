package fi.oph.koski.perustiedot

import fi.oph.koski.api.misc.{OpiskeluoikeudenMitätöintiJaPoistoTestMethods, PutOpiskeluoikeusTestMethods, SearchTestMethods}
import fi.oph.koski.db.KoskiTables.{KoskiOpiskeluOikeudet, PerustiedotManualSync, PerustiedotSync}
import fi.oph.koski.db.PerustiedotManualSyncRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.VapaaSivistystyöExample
import fi.oph.koski.documentation.VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PerustiedotManualSyncSpec
  extends AnyFreeSpec
    with Matchers
    with KoskiHttpSpec
    with DirtiesFixtures
    with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus]
    with OpiskeluoikeudenMitätöintiJaPoistoTestMethods
    with SearchTestMethods {

  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusVapaatavoitteinen

  private val oppija = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus

  "Perustietojen manuaalisynkronointi" - {
    "ei jää jumiin poistettuun (tyhjä data) opiskeluoikeuteen vaan poistaa sen indeksistä" in {
      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = oppija
      ).oid.get
      val ooId = runDbSync(KoskiOpiskeluOikeudet.filter(_.oid === ooOid).map(_.id).result).head

      // Poiston jälkeen opiskeluoikeuden data-sarake on tyhjä eikä sitä voi deserialisoida
      poistaOpiskeluoikeus(oppija.oid, ooOid)
      runDbSync(KoskiOpiskeluOikeudet.filter(_.oid === ooOid).map(_.poistettu).result).head should be(true)

      // Lisätään poistettu opiskeluoikeus manuaalisynkronointijonoon (kuten ylläpito tekisi käsin)
      runDbSync(PerustiedotManualSync += PerustiedotManualSyncRow(opiskeluoikeusOid = ooOid, upsert = true))

      // Aiemmin tämä heitti MappingExceptionin ja jätti jonon jumiin
      KoskiApplicationForTests.perustiedotIndexer.manualSync(refresh = true)

      // Jonon pitää tyhjentyä
      runDbSync(PerustiedotManualSync.filter(_.opiskeluoikeusOid === ooOid).result) should be(empty)

      // Poistettu opiskeluoikeus on lisätty tavalliseen synkronointijonoon indeksistä poistona
      runDbSync(PerustiedotSync.filter(_.opiskeluoikeusId === ooId).map(_.upsert).result) should contain(false)
    }
  }
}
