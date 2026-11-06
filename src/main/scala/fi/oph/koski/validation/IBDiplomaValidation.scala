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
    case diploma: IBDiplomaOpiskeluoikeus => validateKoulutusSamallaOppijalla(henkilöRepository, koskiOpiskeluoikeudet, henkilö, diploma)
    case _ => HttpStatus.ok
  }

  // Diploma yhdistetään koulutukseen iboTunnisteella: oppijalla on oltava IB-koulutuksen tai International schoolin
  // opiskeluoikeus, jolla on sama tunniste (vrt. EB-tutkinnon vaatimus ESH S7 -suorituksesta).
  // TODO TOR-747: Koulutuksen opiskeluoikeuden iboTunniste on vapaaehtoinen, joten tunnisteettomille ei voi tallentaa diplomaa.
  // Tuonnin raportin on listattava nämä. Pitääkö lisäksi vaatia, että koulutuksen opiskeluoikeus on valmistunut?
  private def validateKoulutusSamallaOppijalla(
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository,
    henkilö: Option[Henkilö],
    diploma: IBDiplomaOpiskeluoikeus,
  ): HttpStatus = {
    def oppijallaOnKoulutus(oppijaOidit: List[Henkilö.Oid]): Boolean =
      koskiOpiskeluoikeudet.findByOppijaOids(oppijaOidit)(KoskiSpecificSession.systemUser).exists {
        case ib: IBOpiskeluoikeus => ib.iboTunniste.contains(diploma.iboTunniste)
        case is: InternationalSchoolOpiskeluoikeus => is.iboTunniste.contains(diploma.iboTunniste)
        case _ => false
      }

    val henkilöOid = henkilö match {
      case Some(h: HenkilöWithOid) => Some(h.oid)
      case Some(h: UusiHenkilö) => henkilöRepository.opintopolku.findByHetu(h.hetu).map(_.oid)
      case _ => None
    }

    val koulutusLöytyy = henkilöOid
      .flatMap(oid => henkilöRepository.findByOid(oid, findMasterIfSlaveOid = true))
      .exists(h => oppijallaOnKoulutus(h.kaikkiOidit))

    HttpStatus.validate(koulutusLöytyy)(KoskiErrorCategory.badRequest.validation.ibdiploma.puuttuvaKoulutus())
  }
}
