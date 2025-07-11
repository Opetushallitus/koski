package fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, OoPtsMask, Rooli}
import fi.oph.koski.log.KoskiAuditLogMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_HAKU
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.massaluovutus.MassaluovutusUtils.defaultOrganisaatio
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, QueryResultWriter}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.util.ChainingSyntax.chainingOps
import fi.oph.koski.util.Retry.retryWithInterval
import fi.oph.scalaschema.annotation.{Description, Title}
import slick.jdbc.SQLActionBuilder

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

@Title("Organisaation opiskeluoikeudet")
@Description("Palauttaa hakuehtojen mukaiset organisaation ja sen alaorganisaatioiden opiskeluoikeudet.")
trait MassaluovutusQueryOrganisaationOpiskeluoikeudet extends MassaluovutusQueryParameters with DatabaseConverters with Logging {
  @EnumValues(Set("organisaationOpiskeluoikeudet"))
  def `type`: String
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  def organisaatioOid: Option[String]
  @Description("Palauta vain opiskeluoikeudet, jotka ovat alkaneet annettuna päivänä tai myöhemmin.")
  def alkanutAikaisintaan: LocalDate
  @Description("Palauta vain opiskeluoikeudet, jotka ovat alkaneet annettuna päivänä tai aiemmin.")
  def alkanutViimeistään: Option[LocalDate]

  @Description("Palauta vain opiskeluoikeudet, jotka ovat päättyneet annettuna päivänä tai myöhemmin.")
  def päättynytAikaisintaan: Option[LocalDate]

  @Description("Palauta vain opiskeluoikeudet, jotka ovat päättyneet annettuna päivänä tai aiemmin.")
  def päättynytViimeistään: Option[LocalDate]
  @Description("Jos true, haetaan vain opiskeluoikeudet, jotka eivät ole päättyneet tai merkitty päättymään tulevaisuudessa.")
  @Description("Ei voi olla asetettuna, jos päättynytAikaisintaan tai päättynytViimeistään on asetettu.")
  def eiPäättymispäivää: Option[Boolean]

  @Description("Palauta vain opiskeluoikeudet, joita on päivitetty annetun ajanhetken jälkeen.")
  @Description("Haettaessa muuttuneita opiskeluoikeuksia sitten viimeisen datahaun, kannattaa tätä arvoa aikaistaa tunnilla, jotta varmistaa kaikkien muutoksien osumisen tulosjoukkoon.")
  def muuttunutJälkeen: Option[LocalDateTime]
  @Description("Palauta vain opiskeluoikeudet, joilla on annettu koulutusmuoto.")
  @EnumValues(MassaluovutusQueryOrganisaationOpiskeluoikeudet.allowedKoulutusmuodot)
  def koulutusmuoto: Option[String]
  @Description("Jos true, palautetaan myös mitätöidyt opiskeluoikeudet")
  def mitätöidyt: Option[Boolean]

  def fetchData(application: KoskiApplication, writer: QueryResultWriter, oppilaitosOids: List[Organisaatio.Oid])(implicit user: KoskiSpecificSession): Either[String, Unit]

  def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    val oppilaitosOids = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid.get)
    fetchData(
      application = application,
      writer = writer,
      oppilaitosOids = oppilaitosOids,
    ).tap(_ => auditLog)
  }

  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasGlobalReadAccess || (
      organisaatioOid.exists(user.organisationOids(AccessType.read).contains)
        && koulutusmuoto.forall(k => user.allowedOpiskeluoikeudetJaPäätasonSuoritukset.intersects(OoPtsMask(k)))
        && user.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
      )

  override def fillAndValidate(implicit user: KoskiSpecificSession): Either[HttpStatus, MassaluovutusQueryOrganisaationOpiskeluoikeudet] = {
    for {
      s1 <-
        if (organisaatioOid.isEmpty) {
          defaultOrganisaatio.map(withOrganisaatioOid)
        } else {
          Right(this)
        }
      s2 <-
        if (s1.eiPäättymispäivää.contains(true) && (s1.päättynytViimeistään.isDefined || s1.päättynytAikaisintaan.isDefined)) {
          Left(KoskiErrorCategory.badRequest("eiPäättymispäivää ei voi olla true, jos päättynytAikaisintaan tai päättynysViimeistään on annettu"))
        } else {
          Right(s1)
        }
    } yield s2
  }

  def withOrganisaatioOid(organisaatioOid: Organisaatio.Oid): MassaluovutusQueryOrganisaationOpiskeluoikeudet

  protected def auditLog(implicit user: KoskiSpecificSession): Unit = {
    AuditLog.log(KoskiAuditLogMessage(
      OPISKELUOIKEUS_HAKU,
      user,
      Map(hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(Map(
        "organisaatio" -> List(organisaatioOid.get),
        "alkanutAikaisintaan" -> List(alkanutAikaisintaan.format(DateTimeFormatter.ISO_DATE)),
        "alkanutViimeistään" -> alkanutViimeistään.toList.map(_.format(DateTimeFormatter.ISO_DATE)),
        "päättynytAikaisintaan" -> päättynytAikaisintaan.toList.map(_.format(DateTimeFormatter.ISO_DATE)),
        "päättynytViimeistään" -> päättynytViimeistään.toList.map(_.format(DateTimeFormatter.ISO_DATE)),
        "eiPäättymispäivää" -> eiPäättymispäivää.toList.map(_.toString),
        "muuttunutJälkeen" -> muuttunutJälkeen.toList.map(_.format(DateTimeFormatter.ISO_DATE)),
        "koulutusmuoto" -> koulutusmuoto.toList,
      ).filter(_._2.nonEmpty))),
    ))
  }

  protected def getDb(application: KoskiApplication): DB = application.replicaDatabase.db

  protected def defaultBaseFilter(oppilaitosOids: List[Organisaatio.Oid])(implicit session: KoskiSpecificSession): SQLActionBuilder = SQLHelpers.concatMany(
    Some(sql"WHERE NOT poistettu "),
    if (includeMitätöidyt(session)) None else Some(sql" AND NOT mitatoity "),
    Some(sql" AND oppilaitos_oid = ANY($oppilaitosOids) AND alkamispaiva >= $alkanutAikaisintaan "),
    alkanutViimeistään.map(l => sql" AND alkamispaiva <= $l "),
    päättynytAikaisintaan.map(l => sql" AND paattymispaiva >= $l"),
    päättynytViimeistään.map(l => sql" AND paattymispaiva <= $l"),
    eiPäättymispäivää.flatMap(b => if (b) Some(sql" AND paattymispaiva IS NULL ") else Some(sql" AND paattymispaiva IS NOT NULL ")),
    muuttunutJälkeen.map(Timestamp.valueOf).map(a => sql" AND aikaleima >= $a "),
    if (hasAccessToAllKoulutusmuodot(session)) {
      koulutusmuoto.map(t => sql" AND koulutusmuoto = $t ")
    } else {
      Some(sql" AND koulutusmuoto = any(${allowedKoulutusmuodotForUser(session)}) ")
    },
  )

  protected def getOppijaOids(db: DB, filters: SQLActionBuilder): Seq[String] = QueryMethods.runDbSync(
    db,
    SQLHelpers.concat(sql"SELECT DISTINCT oppija_oid FROM opiskeluoikeus ", filters).as[(String)]
  )

  protected def forEachOpiskeluoikeusAndHenkilö(
    application: KoskiApplication,
    filters: SQLActionBuilder,
    oppijaOids: Seq[String])(f: (LaajatOppijaHenkilöTiedot, Seq[KoskiOpiskeluoikeusRow]) => Unit,
  ): Unit =
    oppijaOids.foreach { oid =>
      retryWithInterval(5, 5.minutes.toMillis) {
        application.opintopolkuHenkilöFacade.findOppijaByOid(oid)
      }.map { henkilö =>
        val henkilöFilter = SQLHelpers.concat(filters, sql"AND oppija_oid = ANY(${henkilö.kaikkiOidit})")
        val opiskeluoikeudet = QueryMethods.runDbSync(
          getDb(application),
          SQLHelpers.concat(sql"SELECT * FROM opiskeluoikeus ", henkilöFilter).as[KoskiOpiskeluoikeusRow]
        )
        f(henkilö, opiskeluoikeudet)
      }
    }

  protected def forEachOpiskeluoikeus(
    application: KoskiApplication,
    filters: SQLActionBuilder,
    oppijaOids: Seq[String])(f: KoskiOpiskeluoikeusRow => Unit,
  ): Unit =
    oppijaOids.foreach { oid =>
      QueryMethods.runDbSync(application.henkilöCache.db, application.henkilöCache.getCachedAction(oid))
        .map { henkilö =>
          val oids = List(henkilö.henkilöRow.oid) ++
            henkilö.henkilöRow.masterOid.toList ++
            henkilö.henkilöRow.masterOid.map(application.henkilöCache.resolveLinkedOids).toList.flatten
          val henkilöFilter = SQLHelpers.concat(filters, sql"AND oppija_oid = ANY(${oids})")
          val opiskeluoikeudet = QueryMethods.runDbSync(
            getDb(application),
            SQLHelpers.concat(sql"SELECT * FROM opiskeluoikeus ", henkilöFilter).as[KoskiOpiskeluoikeusRow]
          )
          opiskeluoikeudet.foreach(f)
        }
    }

  private def includeMitätöidyt(implicit session: KoskiSpecificSession): Boolean = mitätöidyt.contains(true)

  protected def hasAccessToAllKoulutusmuodot(implicit session: KoskiSpecificSession) =
    allowedKoulutusmuodotForUser(session).size == MassaluovutusQueryOrganisaationOpiskeluoikeudet.allowedKoulutusmuodot.size

  protected def allowedKoulutusmuodotForUser(session: KoskiSpecificSession): List[String] =
    MassaluovutusQueryOrganisaationOpiskeluoikeudet.allowedKoulutusmuodot.intersect(session.allowedOpiskeluoikeudetJaPäätasonSuoritukset.toOpiskeluoikeudenTyypit).toList
}

object MassaluovutusQueryOrganisaationOpiskeluoikeudet {
  def allowedKoulutusmuodot: Set[String] = Set(
    "aikuistenperusopetus",
    "ammatillinenkoulutus",
    "diatutkinto",
    "ebtutkinto",
    "esiopetus",
    "europeanschoolofhelsinki",
    "ibtutkinto",
    "internationalschool",
    "korkeakoulutus",
    "lukiokoulutus",
    "luva",
    "muukuinsaanneltykoulutus",
    "perusopetukseenvalmistavaopetus",
    "perusopetuksenlisaopetus",
    "perusopetus",
    "taiteenperusopetus",
    "tuva",
    "vapaansivistystyonkoulutus",
  )
}
