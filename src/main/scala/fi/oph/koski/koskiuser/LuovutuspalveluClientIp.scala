package fi.oph.koski.koskiuser

import jakarta.servlet.http.HttpServletRequest

import scala.jdk.CollectionConverters._

object LuovutuspalveluClientIp {
  // ALB lisää X-Forwarded-For-headerin loppuun sen osoitteen, josta yhteys siihen avattiin, joten IP-rajaus tehdään
  // headerin viimeisellä arvolla. Header voi tulla myös useana rivinä, jotka ovat yhdessä yksi pilkuilla eroteltu lista.
  // Viimeistä arvoa käytetään sellaisenaan, vaikka se olisi tyhjä. Ilman headeria osoitetta ei ole: getRemoteAddria ei
  // käytetä, koska Jettyn ForwardedRequestCustomizer voi muodostaa sen muista forward-headereista.
  def apply(request: HttpServletRequest): Option[String] =
    request.getHeaders("X-Forwarded-For").asScala
      .flatMap(_.split(",", -1))
      .map(_.trim)
      .toList
      .lastOption
}
