package fi.oph.koski.todistus.tiedote

import fi.oph.koski.json.JsonSerializer
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TiedotuspalveluClientSpec extends AnyFreeSpec with Matchers {
  private implicit val formats: DefaultFormats.type = DefaultFormats

  "TiedotuspalveluClient" - {
    "serialisoi opiskeluoikeusOidin kielitutkintotodistuksen tiedotepyyntöön" in {
      val opiskeluoikeusOid = "1.2.246.562.15.12345678901"
      val request = KielitutkintoTodistusTiedoteRequest(
        oppijanumero = "1.2.246.562.24.12345678901",
        opiskeluoikeusOid = opiskeluoikeusOid,
        idempotencyKey = s"$opiskeluoikeusOid-initial",
        todistusBucket = Some("koski-tiedotuspalvelu-local"),
        todistusKey = Some("todistukset/tiedote.pdf"),
        kituExamineeDetails = Some(KituExamineeDetails(
          sukunimi = "Meikäläinen",
          etunimet = "Matti Johannes",
          katuosoite = Some("Esimerkkikatu 123"),
          postinumero = Some("00100"),
          postitoimipaikka = Some("Helsinki"),
          maa = Some(KituKoodiarvo("FIN", "maatjavaltiot1")),
          email = Some("matti.meikalainen@example.com"),
          todistuskieli = Some(KituKoodiarvo("FI", "kieli"))
        ))
      )

      val json = parse(JsonSerializer.writeWithRoot(request))

      (json \ "oppijanumero").extract[String] should equal("1.2.246.562.24.12345678901")
      (json \ "opiskeluoikeusOid").extract[String] should equal(opiskeluoikeusOid)
      (json \ "idempotencyKey").extract[String] should equal(s"$opiskeluoikeusOid-initial")
      (json \ "todistusBucket").extract[String] should equal("koski-tiedotuspalvelu-local")
      (json \ "todistusKey").extract[String] should equal("todistukset/tiedote.pdf")
      (json \ "kituExamineeDetails" \ "sukunimi").extract[String] should equal("Meikäläinen")
    }
  }
}
