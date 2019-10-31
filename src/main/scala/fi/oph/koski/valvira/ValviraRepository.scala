package fi.oph.koski.valvira

import java.sql.Timestamp

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.jsonMethods.{parse => parseJson}
import fi.oph.koski.db.Tables.OpiskeluOikeudet
import fi.oph.koski.db.{DatabaseExecutionContext, KoskiDatabaseMethods}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JString}

import scala.util.{Success, Try}


class ValviraRepository(val db: DB) extends DatabaseExecutionContext with KoskiDatabaseMethods {

  def opiskeluoikeudetByOppijaOids(oppijaOids: Seq[Henkilö.Oid]): Either[HttpStatus, List[ValviraOpiskeluoikeus]] = Try {
    runDbSync(
      OpiskeluOikeudet
        .filter(_.oppijaOid inSet oppijaOids)
        .filter(_.data.+>("suoritukset").@>(parseJson(s"""[{"tyyppi":{"koodiarvo":"ammatillinentutkinto"}}]""")))
        .map(r => (r.data, r.aikaleima, r.versionumero))
        .result
    )
      .map(mergeAikaleimaAndVersionumero)
      .map(onlyAmmatillisenTutkinnonSuoritukset)
      .map(JsonSerializer.extract[ValviraOpiskeluoikeus](_, ignoreExtras = true))
  } match {
    case Success(opiskeluoikeudet) if opiskeluoikeudet.nonEmpty => Right(opiskeluoikeudet.toList)
    case Success(_) => Left(KoskiErrorCategory.notFound())
    case _ => Left(KoskiErrorCategory.internalError())
  }

  private def mergeAikaleimaAndVersionumero(t: (JValue, Timestamp, Int)) = {
    val (json, timestamp, versionumero) = t
    json.merge(parseJson(s"""{"aikaleima":"${timestamp.toLocalDateTime}","versionumero":"$versionumero"}"""))
  }

  private def onlyAmmatillisenTutkinnonSuoritukset(json: JValue): JValue = {
    json.transformField { case ("suoritukset", suoritukset) =>
      ("suoritukset", JArray(suoritukset.filter(_ \ "tyyppi" \ "koodiarvo" == JString("ammatillinentutkinto"))))
    }
  }
}
