package fi.oph.koski.validation

import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.schema.{Henkilö, HenkilöWithOid, IBDiplomaOpiskeluoikeus, IBOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, UusiHenkilö}

object IBDiplomaValidation {
  def validateOpiskeluoikeus(
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository,
    henkilö: Option[Henkilö],
    oo: KoskeenTallennettavaOpiskeluoikeus,
  ): HttpStatus = oo match {
    case diploma: IBDiplomaOpiskeluoikeus => validateIBKoulutuksenOpiskeluoikeusSamallaOppijalla(henkilöRepository, koskiOpiskeluoikeudet, henkilö, diploma)
    case _ => HttpStatus.ok
  }

  // Oppijalla on oltava IB-koulutuksen opiskeluoikeus, jolla on kirjainkoosta riippumatta sama iboTunniste (vrt. EB-tutkinnon
  // vaatimus ESH S7 -suorituksesta). Valmistumista ei vaadita, jotta diplomaa voi korjata, vaikka IB-koulutuksen
  // opiskeluoikeus olisi avattu uudelleen.
  // TODO TOR-747: IB-koulutuksen opiskeluoikeuden iboTunniste on vapaaehtoinen, joten tunnisteettomille ei voi tallentaa diplomaa.
  // Tuonnin raportin on listattava nämä.
  private def validateIBKoulutuksenOpiskeluoikeusSamallaOppijalla(
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository,
    henkilö: Option[Henkilö],
    diploma: IBDiplomaOpiskeluoikeus,
  ): HttpStatus = {
    def oppijallaOnIBKoulutuksenOpiskeluoikeus(oppijaOidit: List[Henkilö.Oid]): Boolean =
      koskiOpiskeluoikeudet.findByOppijaOids(oppijaOidit)(KoskiSpecificSession.systemUser).exists {
        case ib: IBOpiskeluoikeus => ib.iboTunniste.exists(_.equalsIgnoreCase(diploma.iboTunniste))
        case _ => false
      }

    val henkilöOid = henkilö match {
      case Some(h: HenkilöWithOid) => Some(h.oid)
      case Some(h: UusiHenkilö) => henkilöRepository.opintopolku.findByHetu(h.hetu).map(_.oid)
      case _ => None
    }

    val ibKoulutuksenOpiskeluoikeusLöytyy = henkilöOid
      .flatMap(oid => henkilöRepository.findByOid(oid, findMasterIfSlaveOid = true))
      .exists(h => oppijallaOnIBKoulutuksenOpiskeluoikeus(h.kaikkiOidit))

    HttpStatus.validate(ibKoulutuksenOpiskeluoikeusLöytyy)(KoskiErrorCategory.badRequest.validation.ibdiploma.puuttuvaKoulutus())
  }
}
