package fi.oph.koski.sure

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.JsonFiles
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.JsonAST.JBool
import org.json4s.jackson.JsonMethods
import org.scalatest.{FreeSpec, Matchers}

class SureSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers {

}

