package fi.oph.koski.valvira

import java.sql.{Date, Timestamp}

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.jsonMethods.{parse => parseJson}
import fi.oph.koski.db.Tables.OpiskeluOikeudet
import fi.oph.koski.db.{DatabaseExecutionContext, KoskiDatabaseMethods}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.log.Logging
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JString}

import scala.util.{Failure, Success, Try}


class ValviraRepository(val db: DB) extends DatabaseExecutionContext with KoskiDatabaseMethods with Logging {

  def opiskeluoikeudetByOppijaOids(oppijaOids: Seq[Henkilö.Oid]): Either[HttpStatus, List[ValviraOpiskeluoikeus]] = Try {
    runDbSync(
      OpiskeluOikeudet
        .filter(_.oppijaOid inSet oppijaOids)
        .filter(_.data.+>("suoritukset").@>(parseJson(s"""[{"tyyppi":{"koodiarvo":"ammatillinentutkinto"}}]""")))
        .map(r => (r.data, r.aikaleima, r.versionumero, r.alkamispäivä, r.päättymispäivä))
        .result
    )
      .map(mergeJson)
      .map(onlyAmmatillisenTutkinnonSuoritukset)
      .map(JsonSerializer.extract[ValviraOpiskeluoikeus](_, ignoreExtras = true))
  } match {
    case Success(opiskeluoikeudet) if opiskeluoikeudet.nonEmpty => Right(opiskeluoikeudet.toList)
    case Success(_) => Left(KoskiErrorCategory.notFound())
    case Failure(exception) => {
      logger.error(exception)(s"Valvira-datan luonti epäonnistui oppijoille ${oppijaOids.mkString(",")}: ${exception.toString}")
      Left(KoskiErrorCategory.internalError())
    }
  }

  private def mergeJson(t: (JValue, Timestamp, Int, Date, Option[Date])) = {
    val (json, timestamp, versionumero, alkamispäivä, päättymispäivä) = t
    json.merge(parseJson(s"""{"aikaleima":"${timestamp.toLocalDateTime}","versionumero":"$versionumero","alkamispäivä":"${alkamispäivä.toLocalDate}"${päättymispäiväIfDefined(päättymispäivä)}}"""))
  }

  private def päättymispäiväIfDefined(päättymispäivä: Option[Date]) =
    päättymispäivä.map(d => s""","päättymispäivä":"${d.toLocalDate}"""").getOrElse("")

  private def onlyAmmatillisenTutkinnonSuoritukset(json: JValue): JValue = {
    json.transformField { case ("suoritukset", suoritukset) =>
      ("suoritukset", JArray(suoritukset.filter(_ \ "tyyppi" \ "koodiarvo" == JString("ammatillinentutkinto"))))
    }
  }
}
