package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{KoskiDatabaseMethods, PreferenceRow, Tables}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{KoskiSession, RequiresAuthentication}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{KoskiSchema, Organisaatiohenkilö, PerusopetuksenPaikallinenValinnainenOppiaine}
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.JValue





case class KeyValue(key: String, value: JValue)
