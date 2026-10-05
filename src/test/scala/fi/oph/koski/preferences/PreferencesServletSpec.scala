package fi.oph.koski.preferences

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.organisaatio.MockOrganisaatiot
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PreferencesServletSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    resetFixtures()
  }

  private val organisaatio = MockOrganisaatiot.jyväskylänNormaalikoulu
  private val koulutustoimija = MockOrganisaatiot.jyväskylänYliopisto
  private val myöntäjät = s"api/preferences/$organisaatio/myöntäjät"

  "Myöntäjän poistaminen" - {
    "onnistuu, kun nimessä on polussa erityismerkityksen saavia merkkejä" in {
      List("Maija Myöntäjä/sijainen", "Rehtori? #1 & 100% + muut").foreach { nimi =>
        tallenna(nimi)
        tallennetut() should contain(nimi)

        poista(nimi)
        tallennetut() should not contain nimi
      }
    }

    "poistaa koulutustoimijan rajaaman myöntäjän vain samalla koulutustoimijalla" in {
      tallenna("Kaisa Koulutustoimija", Some(koulutustoimija))

      poista("Kaisa Koulutustoimija")
      tallennetut(Some(koulutustoimija)) should contain("Kaisa Koulutustoimija")

      poista("Kaisa Koulutustoimija", Some(koulutustoimija))
      tallennetut(Some(koulutustoimija)) should not contain "Kaisa Koulutustoimija"
    }

    "ilman avainta palauttaa virheen" in {
      delete(myöntäjät, headers = authHeaders()) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.missing("Missing key"))
      }
    }
  }

  private def koulutustoimijaParam(koulutustoimijaOid: Option[String]) =
    koulutustoimijaOid.map("koulutustoimijaOid" -> _).toList

  private def tallenna(nimi: String, koulutustoimijaOid: Option[String] = None): Unit = {
    val body = JsonMethods.compact(
      ("key" -> nimi) ~ ("value" -> (
        ("nimi" -> nimi) ~ ("titteli" -> ("fi" -> "rehtori")) ~ ("organisaatio" -> ("oid" -> organisaatio))
      ))
    )
    val query = koulutustoimijaOid.map(oid => s"?koulutustoimijaOid=$oid").getOrElse("")
    put(s"$myöntäjät$query", body = body, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatusOk()
    }
  }

  private def poista(nimi: String, koulutustoimijaOid: Option[String] = None): Unit =
    delete(myöntäjät, params = ("key" -> nimi) :: koulutustoimijaParam(koulutustoimijaOid), headers = authHeaders()) {
      verifyResponseStatusOk()
    }

  private def tallennetut(koulutustoimijaOid: Option[String] = None): List[String] =
    get(myöntäjät, params = koulutustoimijaParam(koulutustoimijaOid), headers = authHeaders()) {
      verifyResponseStatusOk()
      JsonMethods.parse(body).children.map(h => (h \ "nimi").values.toString)
    }
}
