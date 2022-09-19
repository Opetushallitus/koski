package fi.oph.koski.valpas.oppija

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Henkilö, OrganisaatioWithOid}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.valpas.kuntailmoitus.ValpasKunnat
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.koski.valpas.valpasuser.ValpasSession
import slick.jdbc.GetResult

import java.time.LocalDate

class ValpasOppivelvollisuudestaVapautusService(application: KoskiApplication) extends Logging {
  val db = new ValpasOppivelvollisuudestaVapautusRepository(application.valpasDatabase.db)
  private val organisaatioService = application.organisaatioService
  private val rajapäivätService = application.valpasRajapäivätService
  private val accessResolver = new ValpasAccessResolver

  def mapVapautetutOppijat[T](oppijat: Seq[T], toOids: T => Seq[String])(map: (T, OppivelvollisuudestaVapautus) => T): Seq[T] = {
    val oppijaOids = oppijat.flatMap(toOids)
    val vapautukset = db.getOppivelvollisuudestaVapautetutOppijat(oppijaOids)
    val aktiivisetKunnat = organisaatioService.aktiivisetKunnat()
    oppijat.map { oppija =>
      vapautukset
        .map(OppivelvollisuudestaVapautus.apply(aktiivisetKunnat, rajapäivätService))
        .find(v => toOids(oppija).contains(v.oppijaOid)) match {
          case Some(vapautus) => map(oppija, vapautus)
          case None => oppija
        }
    }
  }

  def lisääOppivelvollisuudestaVapautus(vapautus: UusiOppivelvollisuudestaVapautus)(implicit session: ValpasSession): Either[HttpStatus, Unit] =
    withAccessCheckToKunta(vapautus.kuntakoodi) { () =>
      db.lisääOppivelvollisuudestaVapautus(vapautus.oppijaOid, session.oid, vapautus.vapautettu, vapautus.kuntakoodi)
    }

  def mitätöiOppivelvollisuudestaVapautus(vapautus: OppivelvollisuudestaVapautuksenMitätöinti)(implicit session: ValpasSession): Either[HttpStatus, Unit] =
    withAccessCheckToKunta(vapautus.kuntakoodi) { () =>
      if (db.mitätöiOppivelvollisuudestaVapautus(vapautus.oppijaOid, session.oid, vapautus.kuntakoodi)) {
        Right(Unit)
      } else {
        Left(ValpasErrorCategory.notFound.vapautustaEiLöydy())
      }
    }

  def pohjatiedot(implicit session: ValpasSession): OppivelvollisuudestaVapautuksenPohjatiedot = OppivelvollisuudestaVapautuksenPohjatiedot(
    aikaisinPvm = LocalDate.of(2022, 8, 1),
    kunnat = ValpasKunnat.getUserKunnat(organisaatioService).sortBy(_.nimi.map(_.get(session.lang)))
  )

  private def withAccessCheckToKunta[T](kotipaikkaKoodiarvo: String)(fn: () => T)(implicit session: ValpasSession): Either[HttpStatus, T] =
    if (accessResolver.accessToKuntaOrg(kotipaikkaKoodiarvo)) {
      Right(fn())
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
      SELECT oppija_oid, vapautettu, kunta_koodiarvo
        FROM oppivelvollisuudesta_vapautetut
        WHERE oppija_oid = any($oppijaOids)
          AND mitatoity IS NULL;
    """.as[RawOppivelvollisuudestaVapautus])

  def lisääOppivelvollisuudestaVapautus(oppijaOid: Oid, virkailijaOid: Oid, vapautettu: LocalDate, kotipaikkaKoodiarvo: String): Unit = {
    runDbSync(sql"""
      INSERT INTO oppivelvollisuudesta_vapautetut
        (oppija_oid, virkailija_oid, vapautettu, kunta_koodiarvo)
        VALUES ($oppijaOid, $virkailijaOid, $vapautettu, $kotipaikkaKoodiarvo)
    """.asUpdate)
  }

  def mitätöiOppivelvollisuudestaVapautus(oppijaOid: Oid, virkailijaOid: Oid, kotipaikkaKoodiarvo: String): Boolean =
    runDbSync(sql"""
      UPDATE oppivelvollisuudesta_vapautetut
        SET mitatoity = now()
        WHERE oppija_oid = $oppijaOid
          AND virkailija_oid = $virkailijaOid
          AND kunta_koodiarvo = $kotipaikkaKoodiarvo
          AND mitatoity IS NULL
    """.asUpdate) > 0

  def deleteAll(): Unit =
    runDbSync(sql"""TRUNCATE oppivelvollisuudesta_vapautetut""".asUpdate)

  private implicit def getOppivelvollisuudestaVapautus: GetResult[RawOppivelvollisuudestaVapautus] = GetResult(r =>
    RawOppivelvollisuudestaVapautus(
      oppijaOid = r.rs.getString("oppija_oid"),
      vapautettu = r.rs.getDate("vapautettu").toLocalDate,
      kunta = r.rs.getString("kunta_koodiarvo"),
    )
  )
}

case class RawOppivelvollisuudestaVapautus(
  oppijaOid: Henkilö.Oid,
  vapautettu: LocalDate,
  kunta: String,
)

case class OppivelvollisuudestaVapautus(
  oppijaOid: Henkilö.Oid,
  vapautettu: LocalDate,
  tulevaisuudessa: Boolean,
  kunta: Option[OrganisaatioWithOid],
) {
  def poistaTurvakiellonAlaisetTiedot: OppivelvollisuudestaVapautus = copy(kunta = None)
}

object OppivelvollisuudestaVapautus {
  def apply(aktiivisetKunnat: Seq[OrganisaatioWithOid], rajapäivätService: ValpasRajapäivätService)(vapautus: RawOppivelvollisuudestaVapautus): OppivelvollisuudestaVapautus =
    OppivelvollisuudestaVapautus(
      oppijaOid = vapautus.oppijaOid,
      vapautettu = vapautus.vapautettu,
      tulevaisuudessa = rajapäivätService.tarkastelupäivä.isBefore(vapautus.vapautettu),
      kunta = aktiivisetKunnat.find(_.kotipaikka.exists(_.koodiarvo == vapautus.kunta)),
    )
}

case class OppivelvollisuudestaVapautuksenPohjatiedot(
  aikaisinPvm: LocalDate,
  kunnat: Seq[OrganisaatioWithOid],
)
