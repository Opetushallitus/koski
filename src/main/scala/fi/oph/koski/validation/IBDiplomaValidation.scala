package fi.oph.koski.validation

import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.schema.{Henkilö, HenkilöWithOid, IBDiplomaOpiskeluoikeus, IBOpiskeluoikeus, InternationalSchoolOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, UusiHenkilö}

object IBDiplomaValidation {
  def validateOpiskeluoikeus(
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository,
    henkilö: Option[Henkilö],
    oo: KoskeenTallennettavaOpiskeluoikeus,
  ): HttpStatus = oo match {
    case diploma: IBDiplomaOpiskeluoikeus => validateKoulutusSamallaIboTunnisteella(henkilöRepository, koskiOpiskeluoikeudet, henkilö, diploma)
    case _ => HttpStatus.ok
  }

  private def validateKoulutusSamallaIboTunnisteella(
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository,
    henkilö: Option[Henkilö],
    diploma: IBDiplomaOpiskeluoikeus,
  ): HttpStatus = {
    def samaTunniste(iboTunniste: Option[String]): Boolean = iboTunniste.exists(_.equalsIgnoreCase(diploma.iboTunniste))

    def oppijallaOnKoulutusSamallaTunnisteella(oppijaOidit: List[Henkilö.Oid]): Boolean =
      koskiOpiskeluoikeudet.findByOppijaOids(oppijaOidit)(KoskiSpecificSession.systemUser).exists {
        case ibKoulutus: IBOpiskeluoikeus => samaTunniste(ibKoulutus.iboTunniste)
        case internationalSchool: InternationalSchoolOpiskeluoikeus => samaTunniste(internationalSchool.iboTunniste)
        case _ => false
      }

    val henkilöOid = henkilö match {
      case Some(h: HenkilöWithOid) => Some(h.oid)
      case Some(h: UusiHenkilö) => henkilöRepository.opintopolku.findByHetu(h.hetu).map(_.oid)
      case _ => None
    }

    val koulutusLöytyy = henkilöOid
      .flatMap(oid => henkilöRepository.findByOid(oid, findMasterIfSlaveOid = true))
      .exists(h => oppijallaOnKoulutusSamallaTunnisteella(h.kaikkiOidit))

    HttpStatus.validate(koulutusLöytyy)(KoskiErrorCategory.badRequest.validation.ibdiploma.puuttuvaKoulutus())
  }
}
