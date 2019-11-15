package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.sso.KoskiSessionRepository
import fi.oph.koski.userdirectory.DirectoryClient

trait UserAuthenticationContext {
  def directoryClient: DirectoryClient
  def käyttöoikeusRepository: KäyttöoikeusRepository
  def config: Config
  def koskiSessionRepository: KoskiSessionRepository
  def sessionTimeout: SessionTimeout
}
