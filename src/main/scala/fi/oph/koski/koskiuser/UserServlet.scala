package fi.oph.koski.koskiuser

import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class UserServlet(implicit val application: UserAuthenticationContext) extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with NoCache {
  get("/") {
    renderEither[UserWithAccessRights](getUser.right.map { user =>
      koskiSessionOption.map { session => {
        UserWithAccessRights(
          name = user.name,
          oid = user.oid,
          hasWriteAccess = session.hasAnyWriteAccess,
          hasLocalizationWriteAccess = session.hasLocalizationWriteAccess,
          hasGlobalReadAccess = session.hasGlobalReadAccess,
          hasAnyReadAccess = session.hasAnyReadAccess,
          hasHenkiloUiWriteAccess = session.hasHenkiloUiWriteAccess,
          hasAnyInvalidateAccess = session.hasAnyTiedonsiirronMitätöintiAccess,
          isViranomainen = session.hasGlobalKoulutusmuotoReadAccess,
          hasRaportitAccess = session.hasRaportitAccess,
          hasKelaUiAccess = session.hasKelaAccess,
          varhaiskasvatuksenJärjestäjäKoulutustoimijat = session.varhaiskasvatusKoulutustoimijat.toList,
          hasOneKoulutustoimijaWriteAccess = session.getKoulutustoimijatWithWriteAccess.size == 1
        )
      }
      }.getOrElse(UserWithAccessRights(user.name, user.oid))
    })
  }
}

case class UserWithAccessRights(
  name: String,
  oid: String,
  hasWriteAccess: Boolean = false,
  hasLocalizationWriteAccess: Boolean = false,
  hasGlobalReadAccess: Boolean = false,
  hasAnyReadAccess: Boolean = false,
  hasHenkiloUiWriteAccess: Boolean = false,
  hasAnyInvalidateAccess: Boolean = false,
  isViranomainen: Boolean = false,
  hasRaportitAccess: Boolean = false,
  hasKelaUiAccess: Boolean = false,
  varhaiskasvatuksenJärjestäjäKoulutustoimijat: List[String] = Nil,
  hasOneKoulutustoimijaWriteAccess: Boolean = false
)

