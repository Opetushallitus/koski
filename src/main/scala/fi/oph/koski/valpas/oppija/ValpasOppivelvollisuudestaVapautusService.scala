package fi.oph.koski.valpas.oppija

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.schema.{Henkilö, OrganisaatioWithOid}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.util.ChunkReader
import fi.oph.koski.valpas.kuntailmoitus.ValpasKunnat
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.koski.valpas.valpasuser.ValpasSession
import slick.jdbc.GetResult

import java.time.{LocalDate, LocalDateTime}

class ValpasOppivelvollisuudestaVapautusService(application: KoskiApplication) extends Logging {
  val db = new ValpasOppivelvollisuudestaVapautusRepository(application.valpasDatabase.db)
  private val organisaatioService = application.organisaatioService
  private val rajapäivätService = application.valpasRajapäivätService
  private val accessResolver = new ValpasAccessResolver
  private val raportitService = new RaportitService(application)

  private val VAPAUTUS_SALLITTU_AIKAISINTAAN = if (Environment.isMockEnvironment(application.config)) {
    LocalDate.of(1999, 8, 1)
  } else {
    LocalDate.of(2022, 8, 1)
  }

  def mapVapautetutOppijat[T](oppijat: Seq[T], toOids: T => Seq[String])(map: (T, OppivelvollisuudestaVapautus) => T): Seq[T] = {
    val oppijaOids = oppijat.flatMap(toOids)
    val vapautukset = db.getOppivelvollisuudestaVapautetutOppijat(oppijaOids)
    val aktiivisetKunnat = organisaatioService.aktiivisetKunnat()
    val dueTime = raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika

    oppijat.map { oppija =>
      vapautukset
        .flatMap(OppivelvollisuudestaVapautus.apply(aktiivisetKunnat, rajapäivätService, dueTime))
        .find(v => toOids(oppija).contains(v.oppijaOid)) match {
          case Some(vapautus) => map(oppija, vapautus)
          case None => oppija
        }
    }
  }

  def lisääOppivelvollisuudestaVapautus(vapautus: UusiOppivelvollisuudestaVapautus)(implicit session: ValpasSession): Either[HttpStatus, Unit] =
    withValidation(Some(vapautus.vapautettu), vapautus.kuntakoodi) { () =>
      db.lisääOppivelvollisuudestaVapautus(vapautus.oppijaOid, session.oid, vapautus.vapautettu, vapautus.kuntakoodi)
    }

  def mitätöiOppivelvollisuudestaVapautus(vapautus: OppivelvollisuudestaVapautuksenMitätöinti)(implicit session: ValpasSession): Either[HttpStatus, Unit] =
    withValidation(None, vapautus.kuntakoodi) { () =>
      if (db.mitätöiOppivelvollisuudestaVapautus(vapautus.oppijaOid, vapautus.kuntakoodi)) {
        Right(Unit)
      } else {
        Left(ValpasErrorCategory.notFound.vapautustaEiLöydy())
      }
    }

  def pohjatiedot(implicit session: ValpasSession): OppivelvollisuudestaVapautuksenPohjatiedot = OppivelvollisuudestaVapautuksenPohjatiedot(
    aikaisinPvm = VAPAUTUS_SALLITTU_AIKAISINTAAN,
    kunnat = ValpasKunnat.getUserKunnat(organisaatioService).sortBy(_.nimi.map(_.get(session.lang)))
  )

  def kaikkiVapautuksetIterator(pageSize: Int): ChunkReader[RawOppivelvollisuudestaVapautus] =
    new ChunkReader(pageSize, chunk => db.readPage(chunk.offset, chunk.pageSize, rajapäivätService.tarkastelupäivä))

  private def withValidation[T](aloitusPvm: Option[LocalDate], kotipaikkaKoodiarvo: String)(fn: () => T)(implicit session: ValpasSession): Either[HttpStatus, T] =
    if (accessResolver.accessToKuntaOrg(kotipaikkaKoodiarvo)) {
      if (aloitusPvm.exists(_.isBefore(VAPAUTUS_SALLITTU_AIKAISINTAAN))) {
        Left(ValpasErrorCategory.badRequest.validation.virheellinenPäivämäärä(s"Aikaisin sallittu oppivelvollisuudesta vapautuksen alkamispäivä on ${VAPAUTUS_SALLITTU_AIKAISINTAAN.format(finnishDateFormat)}"))
      } else {
        Right(fn())
      }
    } else {
      Left(ValpasErrorCategory.forbidden.organisaatio())
    }
}

case class UusiOppivelvollisuudestaVapautus(
  oppijaOid: Oid,
  vapautettu: LocalDate,
  kuntakoodi: String,
)

case class OppivelvollisuudestaVapautuksenMitätöinti(
  oppijaOid: Oid,
  kuntakoodi: String,
)

class ValpasOppivelvollisuudestaVapautusRepository(val db: DB) extends QueryMethods with Logging {
  def getOppivelvollisuudestaVapautetutOppijat(oppijaOids: Seq[String]): Seq[RawOppivelvollisuudestaVapautus] =
    runDbSync(sql"""
      SELECT
        oppija_oid,
        vapautettu,
        kunta_koodiarvo,
        mitatoity,
        aikaleima
      FROM oppivelvollisuudesta_vapautetut
        WHERE oppija_oid = any($oppijaOids)
          AND (
            mitatoity IS NULL
            OR mitatoity + interval '1 day' > now()
          )
    """.as[RawOppivelvollisuudestaVapautus])

  def lisääOppivelvollisuudestaVapautus(oppijaOid: Oid, virkailijaOid: Oid, vapautettu: LocalDate, kotipaikkaKoodiarvo: String): Unit = {
    runDbSync(sql"""
      INSERT INTO oppivelvollisuudesta_vapautetut
        (oppija_oid, virkailija_oid, vapautettu, kunta_koodiarvo)
        VALUES ($oppijaOid, $virkailijaOid, $vapautettu, $kotipaikkaKoodiarvo)
    """.asUpdate)
  }

  def mitätöiOppivelvollisuudestaVapautus(oppijaOid: Oid, kotipaikkaKoodiarvo: String): Boolean =
    runDbSync(sql"""
      UPDATE oppivelvollisuudesta_vapautetut
        SET mitatoity = now()
        WHERE oppija_oid = $oppijaOid
          AND kunta_koodiarvo = $kotipaikkaKoodiarvo
          AND mitatoity IS NULL
    """.asUpdate) > 0

  def readPage(offset: Int, pageSize: Int, today: LocalDate): Seq[RawOppivelvollisuudestaVapautus] =
    runDbSync(sql"""
      SELECT
        oppija_oid,
        min(vapautettu) as vapautettu,
        '' as kunta_koodiarvo,
        NULL as mitatoity,
        aikaleima
      FROM oppivelvollisuudesta_vapautetut
      WHERE
        mitatoity IS NULL
        AND vapautettu <= $today
      GROUP BY oppija_oid, aikaleima
      ORDER BY oppija_oid
      OFFSET $offset
      LIMIT $pageSize
    """.as[RawOppivelvollisuudestaVapautus])

  def deleteAll(): Unit =
    runDbSync(sql"""TRUNCATE oppivelvollisuudesta_vapautetut""".asUpdate)

  private implicit def getOppivelvollisuudestaVapautus: GetResult[RawOppivelvollisuudestaVapautus] = GetResult(r =>
    RawOppivelvollisuudestaVapautus(
      oppijaOid = r.rs.getString("oppija_oid"),
      vapautettu = r.getLocalDate("vapautettu"),
      kunta = r.rs.getString("kunta_koodiarvo"),
      mitätöity = Option(r.rs.getTimestamp("mitatoity")).map(_.toLocalDateTime),
      aikaleima = r.rs.getTimestamp("aikaleima").toLocalDateTime,
    )
  )
}

case class RawOppivelvollisuudestaVapautus(
  oppijaOid: Henkilö.Oid,
  vapautettu: LocalDate,
  kunta: String,
  mitätöity: Option[LocalDateTime],
  aikaleima: LocalDateTime,
)

case class OppivelvollisuudestaVapautus(
  oppijaOid: Henkilö.Oid,
  vapautettu: LocalDate,
  tulevaisuudessa: Boolean,
  mitätöitymässä: Boolean,
  kunta: Option[OrganisaatioWithOid],
) {
  def poistaTurvakiellonAlaisetTiedot: OppivelvollisuudestaVapautus = copy(kunta = None)
  def oppivelvollisuusVoimassaAsti: LocalDate = vapautettu.minusDays(1)
  def oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: LocalDate = oppivelvollisuusVoimassaAsti
}

object OppivelvollisuudestaVapautus {
  def apply(
    aktiivisetKunnat: Seq[OrganisaatioWithOid],
    rajapäivätService: ValpasRajapäivätService,
    raportointikantaDueTime: LocalDateTime
  )(vapautus: RawOppivelvollisuudestaVapautus): Option[OppivelvollisuudestaVapautus] = {
    val mitätöity = vapautus.mitätöity.isDefined
    val mitätöintiPäivittynytRaportointikantaan = vapautus.mitätöity.exists(_.isBefore(raportointikantaDueTime))
    val raportointikantaanEiVieläOlePäivitettyVapautusta = vapautus.aikaleima.isAfter(raportointikantaDueTime)

    if (mitätöity && (mitätöintiPäivittynytRaportointikantaan || raportointikantaanEiVieläOlePäivitettyVapautusta)) {
      None
    } else {
      Some(OppivelvollisuudestaVapautus(
        oppijaOid = vapautus.oppijaOid,
        vapautettu = vapautus.vapautettu,
        tulevaisuudessa = rajapäivätService.tarkastelupäivä.isBefore(vapautus.vapautettu),
        mitätöitymässä = vapautus.mitätöity.isDefined,
        kunta = aktiivisetKunnat.find(_.kotipaikka.exists(_.koodiarvo == vapautus.kunta)),
      ))
    }
  }
}

case class OppivelvollisuudestaVapautuksenPohjatiedot(
  aikaisinPvm: LocalDate,
  kunnat: Seq[OrganisaatioWithOid],
)
