package fi.oph.tor.opintooikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db._
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.Oppija
import fi.oph.tor.user.UserContext
import org.json4s._

class PostgresOpintoOikeusRepository(config: DatabaseConfig) extends OpintoOikeusRepository with Futures with GlobalExecutionContext {
  val db = TorDatabase.init(config).db

  class OpintoOikeusTable(tag: Tag) extends Table[OpintoOikeusRow](tag, "opintooikeus") {
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def data = column[JValue]("data")

    def * = (id, data) <> (OpintoOikeusRow.tupled, OpintoOikeusRow.unapply)
  }
  val OpintoOikeudet = TableQuery[OpintoOikeusTable]


  override def filterOppijat(oppijat: List[Oppija])(implicit userContext: UserContext) = {
    val all = findAll
    oppijat.filter { oppija => all.exists(_.oppijaOid == oppija.oid) }
  }

  override def findBy(oppija: Oppija)(implicit userContext: UserContext) = {
    findAll.filter(_.oppijaOid == oppija.oid).toList
  }

  private def findAll: Seq[OpintoOikeus] = {
    await(db.run(OpintoOikeudet.result)).map(_.toOpintoOikeus)
  }

  override def create(opintoOikeus: OpintoOikeus) = {
    await(db.run(OpintoOikeudet += new OpintoOikeusRow(opintoOikeus)))
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