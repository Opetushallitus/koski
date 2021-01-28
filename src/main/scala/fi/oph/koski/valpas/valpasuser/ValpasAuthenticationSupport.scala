package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.valpas.sso.ValpasSSOSupport

trait ValpasAuthenticationSupport extends AuthenticationSupport with ValpasSSOSupport
