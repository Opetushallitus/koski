package fi.oph.koski.koskiuser

import com.typesafe.config.Config

object KäyttöoikeusRyhmät {
  def apply(config: Config): KäyttöoikeusRyhmät = KäyttöoikeusRyhmät(config.getInt("käyttöoikeusryhmät.readWrite"))
}

case class KäyttöoikeusRyhmät(readWrite: Int)
