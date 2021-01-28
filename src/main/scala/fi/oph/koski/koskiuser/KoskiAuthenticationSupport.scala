package fi.oph.koski.koskiuser

import fi.oph.koski.sso.KoskiSSOSupport

trait KoskiAuthenticationSupport extends AuthenticationSupport with KoskiSSOSupport
