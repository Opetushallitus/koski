package fi.oph.common.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.huoltaja.HuoltajaServiceVtj
import fi.oph.koski.sso.KoskiSessionRepository
import fi.oph.koski.userdirectory.DirectoryClient

trait UserAuthenticationContext {
  def directoryClient: DirectoryClient
  def käyttöoikeusRepository: KäyttöoikeusRepository
  def config: Config
  def koskiSessionRepository: KoskiSessionRepository
  def sessionTimeout: SessionTimeout
  def huoltajaServiceVtj: HuoltajaServiceVtj
}
