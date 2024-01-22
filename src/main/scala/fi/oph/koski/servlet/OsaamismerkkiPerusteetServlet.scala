package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoKoodi
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.schema.Koodistokoodiviite

import scala.concurrent.duration.{Duration, DurationInt}

class OsaamismerkkiPerusteetServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with Unauthenticated with Cached with LanguageSupport {

  get("/kuva/:koodiarvo") {
    renderEither[Osaamismerkkikuva](
      application.koodistoViitePalvelu.validate(Koodistokoodiviite(params("koodiarvo"), "osaamismerkit"))
        .map(k => KoodistoKoodi.koodiUri(k.koodistoUri, k.koodiarvo))
        .flatMap(koodiUri => {
          application.ePerusteet.findOsaamismerkkiRakenteet().find(_.koodiUri == koodiUri)
        })
        .flatMap(_.kategoria.liite.map(l =>
          Osaamismerkkikuva(l.mime, l.binarydata)
        ))
        .toRight(KoskiErrorCategory.notFound())
    )
  }

  // TODO:TOR-2049: Route kategorian (ja sen lokalisoidun nimen) haulle, jos ne tarvitaan käliin

  override def cacheDuration: Duration = 1.hours
}

case class Osaamismerkkikuva(
  mimetype: String,
  base64data: String
)
