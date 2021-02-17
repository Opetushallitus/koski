package fi.oph.koski.koskiuser

import fi.oph.koski.sso.KoskiSpecificSSOSupport

trait KoskiSpecificAuthenticationSupport extends AuthenticationSupport with KoskiSpecificSSOSupport
