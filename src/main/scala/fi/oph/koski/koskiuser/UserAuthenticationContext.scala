package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.huoltaja.HuoltajaServiceVtj
import fi.oph.koski.luovutuspalvelu.v2clientlist.{LuovutuspalveluV2ClientListService, LuovutuspalveluV2XRoadConfigService}
import fi.oph.koski.sso.KoskiSessionRepository
import fi.oph.koski.userdirectory.DirectoryClient

trait UserAuthenticationContext {
  def directoryClient: DirectoryClient
  def käyttöoikeusRepository: KäyttöoikeusRepository
  def config: Config
  def koskiSessionRepository: KoskiSessionRepository
  def sessionTimeout: SessionTimeout
  def huoltajaServiceVtj: HuoltajaServiceVtj
  def luovutuspalveluV2ClientListService: LuovutuspalveluV2ClientListService
  def luovutuspalveluV2XRoadConfigService: LuovutuspalveluV2XRoadConfigService
}
