package fi.oph.tor.opintooikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.PostgresDriverWithJsonSupport.jsonMethods._

import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db._
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.Oppija
import fi.oph.tor.user.UserContext
import org.json4s._

class PostgresOpintoOikeusRepository(db: DB) extends OpintoOikeusRepository with Futures with GlobalExecutionContext {
  protected class OpintoOikeusTable(tag: Tag) extends Table[OpintoOikeusRow](tag, "opintooikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val oppijaOid: Rep[String] = column[String]("oppija_oid", O.NotNull)
    def data = column[JValue]("data")

    def * = (id, oppijaOid, data) <> (OpintoOikeusRow.tupled, OpintoOikeusRow.unapply)
  }
  protected val OpintoOikeudet = TableQuery[OpintoOikeusTable]

  // Note: this is a naive implementation. All filtering should be moved to query-level instead of in-memory-level

  private def findAllRows(implicit userContext: UserContext): Seq[OpintoOikeusRow] = {
    await(db.run(OpintoOikeudet.result)).filter(withAccess)
  }

  override def filterOppijat(oppijat: Seq[Oppija])(implicit userContext: UserContext) = {
    val all = findAllRows
    oppijat.filter { oppija => all.exists(opintoOikeus => opintoOikeus.oppijaOid == oppija.oid)}
  }

  override def findByOppijaOid(oid: String)(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    await(
      db.run(OpintoOikeudet.filter(_.oppijaOid === oid).result)
    ).map(_.toOpintoOikeus).filter(withAccess)
  }

  override def create(oppijaOid: String, opintoOikeus: OpintoOikeus) = {
    Right(await(db.run(OpintoOikeudet.returning(OpintoOikeudet.map(_.id)) += new OpintoOikeusRow(oppijaOid, opintoOikeus))))
  }

  private def findAll(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    findAllRows.map(_.toOpintoOikeus)
  }

  private def withAccess(oikeus: OpintoOikeus)(implicit userContext: UserContext) = {
    userContext.hasReadAccess(oikeus.oppilaitosOrganisaatio)
  }

  private def withAccess(oikeus: OpintoOikeusRow)(implicit userContext: UserContext) = {
    userContext.hasReadAccess(oikeus.toOpintoOikeus.oppilaitosOrganisaatio)
  }
}

case class OpintoOikeusRow(id: Int, oppijaOid: String, data: JValue) {
  lazy val toOpintoOikeus = {
    Json.fromJValue[OpintoOikeus](data)
  }

  def this(oppijaOid: String, opintoOikeus: OpintoOikeus) = {
    this(0, oppijaOid, Json.toJValue(opintoOikeus))
  }
}