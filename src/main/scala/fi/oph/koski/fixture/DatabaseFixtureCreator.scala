package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.{MockOpintopolkuHenkilöFacade, OppijaHenkilö, OppijaHenkilöWithMasterInfo, OppijanumerorekisteriKotikuntahistoriaRow}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.json.JsonSerializer.serializeWithRoot
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.oppija.OppijaServletOppijaAdder
import fi.oph.koski.perustiedot.{OpiskeluoikeudenOsittaisetTiedot, OpiskeluoikeudenPerustiedot}
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import slick.dbio.DBIO

import java.io
import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag

abstract class DatabaseFixtureCreator(application: KoskiApplication, opiskeluoikeusFixtureCacheTableName: String, opiskeluoikeusHistoriaFixtureCacheTableName: String) extends QueryMethods with Timing {
  implicit val user: KoskiSpecificSession = KoskiSpecificSession.systemUser
  protected val validator = application.validator
  val db = application.masterDatabase.db
  implicit val accessType: AccessType.Value = AccessType.write
  val raportointiDatabase = application.raportointiDatabase
  val validationConfig = application.validationContext

  protected def updateFieldsAndValidateOpiskeluoikeus[T: TypeTag](oo: T, session: KoskiSpecificSession = user): T =
    validator.extractUpdateFieldsAndValidateOpiskeluoikeus(JsonSerializer.serialize(oo))(session, AccessType.write) match {
      case Right(opiskeluoikeus) => opiskeluoikeus.asInstanceOf[T]
      case Left(status) => throw new RuntimeException("Fixture insert failed for " + JsonSerializer.write(oo) + ": " + status)
    }

  private var fixtureCacheCreated = false
  private var cachedPerustiedot: Option[Seq[OpiskeluoikeudenOsittaisetTiedot]] = None

  def resetFixtures: Unit = {
    if (!application.masterDatabase.isLocal) throw new IllegalStateException("Trying to reset fixtures in remote database")

    val henkilöOids = application.fixtureCreator.allOppijaOids.sorted

    runDbSync(DBIO.sequence(Seq(
      KoskiOpiskeluOikeudet.filter(_.oppijaOid inSetBind (henkilöOids)).delete,
      YtrOpiskeluoikeusHistoria.delete,
      YtrOpiskeluOikeudet.delete,
      KoskiTables.Henkilöt.filter(_.oid inSetBind henkilöOids).delete,
      Preferences.delete,
      KoskiTables.PerustiedotSync.delete,
      KoskiTables.SuoritusJako.delete,
      KoskiTables.SuoritusJakoV2.delete,
      KoskiTables.OAuth2JakoKaikki.delete
    ) ++ oppijat.map(application.henkilöCache.addHenkilöAction)))

    application.perustiedotIndexer.sync(refresh = false) // Make sure the sync queue is empty
    application.perustiedotIndexer.deleteByOppijaOids(henkilöOids, refresh = false)

    if (!fixtureCacheCreated) {
      cachedPerustiedot = Some(
        luoOpiskeluoikeudetJaPerustiedot("default opiskeluoikeudet", defaultOpiskeluOikeudet) ++
        validationConfig.runWithoutValidations { luoOpiskeluoikeudetJaPerustiedot("invalid opiskeluoikeudet", invalidOpiskeluoikeudet) } ++
        luoOpiskeluoikeudetJaPerustiedot("second batch opiskeluoikeudet", secondBatchOpiskeluOikeudet) ++
        luoOpiskeluoikeudetJaPerustiedot("third batch päivitettävät opiskeluoikeudet", thirdBatchPäivitettävätOpiskeluOikeudet, allowUpdate = true)
      )

      application.perustiedotIndexer.sync(refresh = true)
      val henkilöOidsIn = henkilöOids.map("'" + _ + "'").mkString(",")
      runDbSync(DBIO.seq(
        sqlu"drop table if exists #$opiskeluoikeusFixtureCacheTableName",
        sqlu"create table #$opiskeluoikeusFixtureCacheTableName as select * from opiskeluoikeus where oppija_oid in (#$henkilöOidsIn)",
        sqlu"drop table if exists #$opiskeluoikeusHistoriaFixtureCacheTableName",
        sqlu"create table #$opiskeluoikeusHistoriaFixtureCacheTableName as select * from opiskeluoikeushistoria where opiskeluoikeus_id in (select id from #$opiskeluoikeusFixtureCacheTableName)"
      ))
      fixtureCacheCreated = true
    } else {
      runDbSync(DBIO.seq(
        sqlu"alter table opiskeluoikeus disable trigger update_opiskeluoikeus_aikaleima",
        sqlu"insert into opiskeluoikeus select * from #$opiskeluoikeusFixtureCacheTableName",
        sqlu"insert into opiskeluoikeushistoria select * from #$opiskeluoikeusHistoriaFixtureCacheTableName",
        sqlu"alter table opiskeluoikeus enable trigger update_opiskeluoikeus_aikaleima"
      ))
      application.perustiedotIndexer.updatePerustiedot(cachedPerustiedot.get, upsert = true, refresh = true)
    }
  }

  private def luoOpiskeluoikeudetJaPerustiedot(fixtureSetName: String, opiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)], allowUpdate: Boolean = false): Seq[OpiskeluoikeudenOsittaisetTiedot] = {
    val adder = new OppijaServletOppijaAdder(application)

    val errors: mutable.ListBuffer[String] = mutable.ListBuffer.empty

    val result = opiskeluoikeudet.zipWithIndex.flatMap {
      case ((henkilö, inputOo), index) => try {
        val perustiedot = luoOpiskeluoikeudetJaPerustiedotOppijalle(fixtureSetName, allowUpdate, adder, henkilö, inputOo)
        perustiedot.fold(
          error => throw new Exception(s"Fikstuurin opiskeluoikeuden ${index + 1}/${opiskeluoikeudet.length} ($fixtureSetName: ${henkilö.sukunimi} ${henkilö.etunimet}, ${inputOo.tyyppi.koodiarvo}) luonti ei onnistu: $error"),
          o => Seq(o)
        )
      } catch {
        case e: Exception =>
          logger.error(e.getMessage)
          errors += e.getMessage
          Seq.empty
      }
    }

    if (errors.nonEmpty) {
      throw new Exception(errors.mkString(",\n"))
    }

    result
  }

  private def luoOpiskeluoikeudetJaPerustiedotOppijalle(fixtureSetName: String, allowUpdate: Boolean, adder: OppijaServletOppijaAdder, henkilö: OppijaHenkilö, inputOo: KoskeenTallennettavaOpiskeluoikeus): Either[io.Serializable, OpiskeluoikeudenPerustiedot] = {
    val oppijaJson = serializeWithRoot(Oppija(
      henkilö = henkilö.toHenkilötiedotJaOid,
      opiskeluoikeudet = List(inputOo),
    ))

    for {
      versiot <- adder.add(user, oppijaJson, allowUpdate, requestDescription = "")
      oid <- versiot.opiskeluoikeudet.headOption.map(_.oid).toRight(new Exception("Opiskeluoikeutta ei löydy, vaikka se äsken tallennettiin"))
      ooRow <- application.possu.findByOidIlmanKäyttöoikeustarkistusta(oid)
      oo <- ooRow.toOpiskeluoikeus
      perustiedot <- Right(OpiskeluoikeudenPerustiedot.makePerustiedot(ooRow.id, oo, application.henkilöRepository.opintopolku.withMasterInfo(henkilö)))
    } yield perustiedot
  }

  protected def oppijat: List[OppijaHenkilöWithMasterInfo]
  protected def kuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]]
  protected def turvakieltoKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]]

  protected def invalidOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)]

  protected def defaultOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)]

  protected def secondBatchOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)]
  protected def thirdBatchPäivitettävätOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)]
}
