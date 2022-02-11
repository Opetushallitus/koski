package fi.oph.koski.http

import org.http4s.{Header, RequestCookie}
import org.typelevel.ci.CIString

object OpintopolkuCsrfToken {
  // Opintopolun yhteisesti käyttämä nimi csrf-keksille ja -headerille
  val key = "CSRF"
  // CSRF-token, jota käytetään Kosken kutsuessa muita Opintopolun palveluita.
  val serviceToken: String = OpintopolkuCallerId.koski

  val serviceHttpHeader: Header.Raw = Header.Raw(CIString(key), serviceToken)
  val serviceCookie: RequestCookie = RequestCookie(key, serviceToken)
}
