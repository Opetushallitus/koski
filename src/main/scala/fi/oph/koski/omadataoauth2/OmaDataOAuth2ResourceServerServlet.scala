package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresOmaDataOAuth2}
import fi.oph.koski.log.KoskiAuditLogMessageField.{omaDataKumppani, omaDataOAuth2Scope, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.{OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, OAUTH2_KATSOMINEN_KAIKKI_TIEDOT, OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiOperation, Logging}
import fi.oph.koski.schema.{Opiskeluoikeus, Oppija, TäydellisetHenkilötiedot}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeus
import fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOpiskeluoikeus
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue
import org.scalatra.ContentEncodingSupport
import fi.oph.koski.schema

import scala.util.chaining._
import java.time.LocalDate


class OmaDataOAuth2ResourceServerServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet
  with Logging with ContentEncodingSupport with NoCache with RequiresOmaDataOAuth2 {
  // in: access token
  // out: data, jos käyttäjällä oikeudet kyseiseen access tokeniin.
  //      TAI OAuth2-protokollan mukainen virheilmoitus
  post("/") {
    request.header("X-Auth").map(_.split(" ")) match {
      case Some(Array("Bearer", token)) =>
        renderRequestedData(token)
      case _ =>
        renderInvalidRequestError("Request missing valid token parameters", msg => logger.warn(msg))
    }
  }

  private def renderRequestedData(token: String): Unit = {
    application.omaDataOAuth2Service.getByAccessToken(
      accessToken = token,
      expectedClientId = koskiSession.user.username,
      allowedScopes = koskiSession.omaDataOAuth2Scopes
    ) match {
      case Right(AccessTokenInfo(_, oppijaOid, scope)) =>
        renderOpinnot(oppijaOid, scope)
      case Left(error) =>
        val errorResult = AccessTokenErrorResponse(error)
        response.setStatus(errorResult.httpStatus)
        renderObject(errorResult)
    }
  }

  private def renderOpinnot(oppijaOid: String, scope: String): Unit = {
    val overrideSession = KoskiSpecificSession.suoritusjakoKatsominenUser(request)

    scope.split(" ").filter(_.startsWith("OPISKELUOIKEUDET_")).toSeq match {
      case Seq("OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT") =>
        renderSuoritetutTutkinnot(oppijaOid, scope, overrideSession)
      case Seq("OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT") =>
        renderAktiivisetJaPäättyneetOpinnot(oppijaOid, scope, overrideSession)
      case Seq("OPISKELUOIKEUDET_KAIKKI_TIEDOT") =>
        renderKaikkiTiedot(oppijaOid, scope, overrideSession)
      case _ =>
        renderServerError(s"Internal error, unable to handle OPISKELUOIKEUDET scope defined in ${scope}", msg => logger.error(msg))
    }
  }

  private def renderSuoritetutTutkinnot(oppijaOid: String, scope: String, overrideSession: KoskiSpecificSession): Unit = {
    application.suoritetutTutkinnotService.findSuoritetutTutkinnotOppijaLaajatHenkilötiedot(
      oppijaOid,
      merkitseSuoritusjakoTehdyksi = false
    )(overrideSession) match {
      case Right(oppija) =>
        auditLogKatsominen(OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT, koskiSession.user.username, koskiSession, oppijaOid, scope)

        val result = OmaDataOAuth2SuoritetutTutkinnot(
          henkilö = OmaDataOAuth2Henkilötiedot(oppija.henkilö, scope),
          opiskeluoikeudet = oppija.opiskeluoikeudet
        )

        response.setStatus(200)
        renderObject(result)
      case Left(httpStatus) =>
        renderServerError(httpStatus, "Internal error", msg => logger.warn(msg))
    }
  }

  private def renderAktiivisetJaPäättyneetOpinnot(oppijaOid: String, scope: String, overrideSession: KoskiSpecificSession): Unit = {
    application.aktiivisetJaPäättyneetOpinnotService.findOppijaLaajatHenkilötiedot(
      oppijaOid,
      merkitseSuoritusjakoTehdyksi = false
    )(overrideSession) match {
      case Right(oppija) =>
        auditLogKatsominen(OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, koskiSession.user.username, koskiSession, oppijaOid, scope)

        val result = OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet(
          henkilö = OmaDataOAuth2Henkilötiedot(oppija.henkilö, scope),
          opiskeluoikeudet = oppija.opiskeluoikeudet
        )

        response.setStatus(200)
        renderObject(result)
      case Left(httpStatus) =>
        renderServerError(httpStatus, "Internal error", msg => logger.warn(msg))
    }
  }

  private def renderKaikkiTiedot(oppijaOid: String, scope: String, overrideSession: KoskiSpecificSession): Unit = {
    application.oppijaFacade.findOppija(oppijaOid)(overrideSession).flatMap(_.warningsToLeft) match {
      case Right(Oppija(henkilö: TäydellisetHenkilötiedot, opiskeluoikeudet: Seq[Opiskeluoikeus])) =>
        auditLogKatsominen(OAUTH2_KATSOMINEN_KAIKKI_TIEDOT, koskiSession.user.username, koskiSession, oppijaOid, scope)

        val result = OmaDataOAuth2KaikkiOpiskeluoikeudet(
          henkilö = OmaDataOAuth2Henkilötiedot(henkilö, scope),
          opiskeluoikeudet = opiskeluoikeudet.toList
        )

        response.setStatus(200)
        renderObject(result)
      case Right(_) =>
        renderServerError("Internal error, datatype not recognized", msg => logger.error(msg))
      case Left(httpStatus) =>
        renderServerError(httpStatus, "Internal error", msg => logger.warn(msg))
    }
  }

  private def auditLogKatsominen(
    operation: KoskiOperation.KoskiOperation,
    expectedClientId: String,
    koskiSession: KoskiSpecificSession,
    oppijaOid: String,
    scope: String
  ): Unit = {
    AuditLog.log(KoskiAuditLogMessage(operation, koskiSession, Map(
      oppijaHenkiloOid -> oppijaOid,
      omaDataKumppani -> expectedClientId,
      omaDataOAuth2Scope -> scope
    )))
  }

  private def renderServerError(message: String, log: String => Unit): Unit = {
    val error = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, message)
    log(error.getLoggedErrorMessage)
    val errorResult = AccessTokenErrorResponse(error)
    response.setStatus(errorResult.httpStatus)
    renderObject(errorResult)
  }

  private def renderServerError(httpStatus: HttpStatus, message: String, log: String => Unit): Unit = {
    val error = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, httpStatus.errorString.getOrElse(message))
    log(error.getLoggedErrorMessage)
    val errorResult = AccessTokenErrorResponse(error)
    response.setStatus(httpStatus.statusCode)
    renderObject(errorResult)
  }

  private def renderInvalidRequestError(message: String, log: String => Unit): Unit = {
    val error = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, message)
    log(error.getLoggedErrorMessage)
    val errorResult = AccessTokenErrorResponse(error)
    response.setStatus(errorResult.httpStatus)
    renderObject(errorResult)
  }
}

object OmaDataOAuth2Henkilötiedot {
  def apply(laajatTiedot: LaajatOppijaHenkilöTiedot, scope: String): OmaDataOAuth2Henkilötiedot = {
    val scopes = scope.split(" ").filter(_.startsWith("HENKILOTIEDOT_"))

    def withNimi(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_NIMI") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          sukunimi = Some(laajatTiedot.sukunimi),
          etunimet = Some(laajatTiedot.etunimet),
          kutsumanimi = Some(laajatTiedot.kutsumanimi)
        )
      } else {
        henkilö
      }
    }

    def withOppijanumero(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_OPPIJANUMERO") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          oid = Some(laajatTiedot.oid)
        )
      } else {
        henkilö
      }
    }

    def withHetu(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_HETU") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          hetu = laajatTiedot.hetu
        )
      } else {
        henkilö
      }
    }

    def withSyntymäaika(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_SYNTYMAAIKA") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          syntymäaika = laajatTiedot.syntymäaika
        )
      } else {
        henkilö
      }
    }

    OmaDataOAuth2Henkilötiedot()
      .pipe(withNimi)
      .pipe(withOppijanumero)
      .pipe(withHetu)
      .pipe(withSyntymäaika)
  }

  def apply(täydellisetTiedot: TäydellisetHenkilötiedot, scope: String): OmaDataOAuth2Henkilötiedot = {
    val scopes = scope.split(" ").filter(_.startsWith("HENKILOTIEDOT_"))

    def withNimi(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_NIMI") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          sukunimi = Some(täydellisetTiedot.sukunimi),
          etunimet = Some(täydellisetTiedot.etunimet),
          kutsumanimi = Some(täydellisetTiedot.kutsumanimi)
        )
      } else {
        henkilö
      }
    }

    def withOppijanumero(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_OPPIJANUMERO") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          oid = Some(täydellisetTiedot.oid)
        )
      } else {
        henkilö
      }
    }

    def withHetu(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_HETU") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          hetu = täydellisetTiedot.hetu
        )
      } else {
        henkilö
      }
    }

    def withSyntymäaika(henkilö: OmaDataOAuth2Henkilötiedot): OmaDataOAuth2Henkilötiedot = {
      if (scopes.contains("HENKILOTIEDOT_SYNTYMAAIKA") || scopes.contains("HENKILOTIEDOT_KAIKKI_TIEDOT")) {
        henkilö.copy(
          syntymäaika = täydellisetTiedot.syntymäaika
        )
      } else {
        henkilö
      }
    }

    OmaDataOAuth2Henkilötiedot()
      .pipe(withNimi)
      .pipe(withOppijanumero)
      .pipe(withHetu)
      .pipe(withSyntymäaika)
  }

}

case class OmaDataOAuth2Henkilötiedot(
  oid: Option[String] = None,
  sukunimi: Option[String] = None,
  etunimet: Option[String] = None,
  kutsumanimi: Option[String] = None,
  hetu: Option[String] = None,
  syntymäaika: Option[LocalDate] = None
)


object OmaDataOAuth2KaikkiOpiskeluoikeudet {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[OmaDataOAuth2KaikkiOpiskeluoikeudet]).asInstanceOf[ClassSchema])
}

case class OmaDataOAuth2KaikkiOpiskeluoikeudet(
  henkilö: OmaDataOAuth2Henkilötiedot,
  opiskeluoikeudet: List[Opiskeluoikeus]
)

object OmaDataOAuth2SuoritetutTutkinnot {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[OmaDataOAuth2SuoritetutTutkinnot]).asInstanceOf[ClassSchema])
}

case class OmaDataOAuth2SuoritetutTutkinnot(
  henkilö: OmaDataOAuth2Henkilötiedot,
  opiskeluoikeudet: List[SuoritetutTutkinnotOpiskeluoikeus]
)

object OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet]).asInstanceOf[ClassSchema])
}

case class OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet(
  henkilö: OmaDataOAuth2Henkilötiedot,
  opiskeluoikeudet: List[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]
)
