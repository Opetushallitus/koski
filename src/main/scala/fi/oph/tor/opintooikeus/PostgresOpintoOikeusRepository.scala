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
    def data = column[JValue]("data")

    def * = (id, data) <> (OpintoOikeusRow.tupled, OpintoOikeusRow.unapply)
  }
  protected val OpintoOikeudet = TableQuery[OpintoOikeusTable]

  override def filterOppijat(oppijat: Seq[Oppija])(implicit userContext: UserContext) = {
    val all = findAll
    oppijat.filter { oppija => all.exists(opintoOikeus => opintoOikeus.oppijaOid == oppija.oid) }
  }

  override def findByOppijaOid(oid: String)(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    await(
      db.run(OpintoOikeudet.filter(_.data @> parse(s"""{"oppijaOid": "$oid"}""")).result)
    ).map(_.toOpintoOikeus).filter(withAccess)
  }

  override def create(opintoOikeus: OpintoOikeus) = {
    Right(await(db.run(OpintoOikeudet.returning(OpintoOikeudet.map(_.id)) += new OpintoOikeusRow(opintoOikeus))))
  }

  private def findAll(implicit userContext: UserContext): Seq[OpintoOikeus] = {
    await(
      db.run(OpintoOikeudet.result)
    ).map(_.toOpintoOikeus).filter(withAccess)
  }

  private def withAccess(oikeus: OpintoOikeus)(implicit userContext: UserContext) = {
    userContext.hasReadAccess(oikeus.oppilaitosOrganisaatio)
  }
}

case class OpintoOikeusRow(id: Int, data: JValue) {
  def toOpintoOikeus = {
    Json.fromJValue[OpintoOikeus](data)
  }

  def this(opintoOikeus: OpintoOikeus) = {
    this(0, Json.toJValue(opintoOikeus))
  }
}