package fi.oph.koski.tiedonsiirto

import java.sql.Timestamp
import java.time.LocalDateTime

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.Tables.{Tiedonsiirto, TiedonsiirtoWithAccessCheck, TiedonsiirtoYhteenveto}
import fi.oph.koski.db._
import fi.oph.koski.koskiuser.KoskiUser
import org.json4s.JsonAST.JValue
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos}
import fi.oph.koski.util.Timing

class TiedonsiirtoRepository(val db: DB, mailer: TiedonsiirtoFailureMailer) extends GlobalExecutionContext with KoskiDatabaseMethods with Timing {
  val maxResults = 10000

  def create(kayttajaOid: String, tallentajaOrganisaatioOid: String, oppija: Option[JValue], oppilaitos: Option[JValue], error: Option[TiedonsiirtoError], lahdejarjestelma: Option[String]) {
    val (data, virheet) = error.map(e => (Some(e.data), Some(e.virheet))).getOrElse((None, None))

    runDbSync {
      Tiedonsiirto.map { row => (row.kayttajaOid, row.tallentajaOrganisaatioOid, row.oppija, row.oppilaitos, row.data, row.virheet, row.lahdejarjestelma) } += (kayttajaOid, tallentajaOrganisaatioOid, oppija, oppilaitos, data, virheet, lahdejarjestelma)
    }

    if (error.isDefined) {
      mailer.sendMail(tallentajaOrganisaatioOid)
    }
  }

  def find(organisaatiot: Option[List[String]])(implicit koskiUser: KoskiUser): Seq[TiedonsiirtoRow] = timed("findByOrganisaatio") {
    val monthAgo = Timestamp.valueOf(LocalDateTime.now.minusMonths(1))
    var tableQuery = TiedonsiirtoWithAccessCheck(koskiUser)
    organisaatiot.foreach { org =>
      tableQuery = tableQuery.filter(_.tallentajaOrganisaatioOid inSetBind org)
    }
    runDbSync(tableQuery.filter(_.aikaleima > monthAgo).sortBy(_.id.desc).take(maxResults).result)
  }

  def yhteenveto(koskiUser: KoskiUser): Seq[TiedonsiirtoYhteenvetoRow] = timed("yhteenveto") {
    runDbSync(Tables.TiedonsiirtoYhteenvetoWithAccessCheck(koskiUser).result)
  }
}

case class TiedonsiirtoError(data: JValue, virheet: JValue)