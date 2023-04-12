package fi.oph.koski.valvira

import java.sql.{Date, Timestamp}
import fi.oph.koski.db.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.jsonMethods.{parse => parseJson}
import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudet
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.log.Logging
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JBool, JString}

import scala.util.{Failure, Success, Try}


class ValviraRepository(val db: DB) extends QueryMethods with Logging {

  def opiskeluoikeudetByOppijaOids(oppijaOids: Seq[Henkilö.Oid]): Either[HttpStatus, List[ValviraOpiskeluoikeus]] = Try {
    runDbSync(
      KoskiOpiskeluOikeudet
        .filter(_.oppijaOid inSet oppijaOids)
        .filterNot(_.mitätöity)
        .filter(_.data.+>("suoritukset").@>(parseJson(s"""[{"tyyppi":{"koodiarvo":"ammatillinentutkinto"}}]""")))
        .map(r => (r.data, r.aikaleima, r.versionumero, r.alkamispäivä, r.päättymispäivä))
        .result
    )
      .map(mergeJson)
      .map(onlyValviraTutkinnonSuoritukset)
      .filter(sisältääSuorituksia)
      .filterNot(onKoulutusvientiä)
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

  private def onlyValviraTutkinnonSuoritukset(json: JValue): JValue = {
    json.transformField { case ("suoritukset", suoritukset) =>
      ("suoritukset", JArray(suoritukset.filter(onValviraSuoritus)))
    }
  }

  private def onValviraSuoritus(suoritus:JValue): Boolean = {
    suoritus \ "tyyppi" \ "koodiarvo" == JString("ammatillinentutkinto") &&
      valviranTutkinnot.contains(suoritus \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo")
  }

  private lazy val valviranTutkinnot = List(JString("371101"), JString("374111"), JString("371171"))

  private def sisältääSuorituksia(json: JValue): Boolean = {
    json \ "suoritukset" != JArray(Nil)
  }

  private def onKoulutusvientiä(json: JValue): Boolean = {
    json \ "lisätiedot" \ "koulutusvienti" == JBool.True
  }

}
