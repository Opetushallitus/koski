package fi.oph.koski.ibis

import fi.oph.koski.henkilo.OpintopolkuHenkilöFacade
import fi.oph.koski.schema.{IBDiplomaOpiskeluoikeus, IBOpiskeluoikeus, Oppilaitos}

case class IbisYhdistettyOppija(
  oppijaOid: String,
  oppilaitos: Oppilaitos,
  diplomanOid: Option[String],
)

class IbisService(repository: IbisOpiskeluoikeusRepository, henkilöt: OpintopolkuHenkilöFacade) {
  def yhdistäOppijoihin(rivit: List[IbisRecord]): List[(IbisRecord, Either[IbisKonversiovirhe, IbisYhdistettyOppija])] = {
    val tunnisteet = rivit.map(_.iboTunniste.toLowerCase)
    val löydetyt = masterOideilla(repository.findByIboTunnisteet(tunnisteet.toSet))
    rivit.zip(tunnisteet).map { case (rivi, tunniste) =>
      if (tunnisteet.count(_ == tunniste) > 1) {
        rivi -> Left(IbisKonversiovirhe(rivi.iboTunniste, "Sama IBO-tunniste on tiedostossa usealla rivillä"))
      } else {
        rivi -> IbisService.yhdistäOppijaan(rivi.iboTunniste, löydetyt.filter(_.iboTunniste == tunniste))
      }
    }
  }

  // Saman henkilön opiskeluoikeudet voivat olla linkitetyillä oideilla, joten ne yhdistetään master-oidiin.
  private def masterOideilla(löydetyt: Seq[IbisTunnisteenOpiskeluoikeus]): Seq[IbisTunnisteenOpiskeluoikeus] =
    if (löydetyt.isEmpty) {
      löydetyt
    } else {
      val masterit = henkilöt.findMasterOppijat(löydetyt.map(_.oppijaOid).distinct.toList)
      löydetyt.map(löydetty => löydetty.copy(oppijaOid = masterit.get(löydetty.oppijaOid).map(_.oid).getOrElse(löydetty.oppijaOid)))
    }
}

object IbisService {
  def yhdistäOppijaan(iboTunniste: String, löydetyt: Seq[IbisTunnisteenOpiskeluoikeus]): Either[IbisKonversiovirhe, IbisYhdistettyOppija] = {
    val ibKoulutukset = löydetyt.filter(_.opiskeluoikeus.isInstanceOf[IBOpiskeluoikeus])
    val valmistuneetIbKoulutukset = ibKoulutukset.filter(_.opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.exists(_.tila.koodiarvo == "valmistunut"))
    val diplomat = löydetyt.filter(_.opiskeluoikeus.isInstanceOf[IBDiplomaOpiskeluoikeus])

    if ((valmistuneetIbKoulutukset ++ diplomat).map(_.oppijaOid).distinct.size > 1) {
      Left(IbisKonversiovirhe(iboTunniste, "Tunnisteella löytyy usean oppijan opiskeluoikeuksia"))
    } else if (diplomat.size > 1) {
      Left(IbisKonversiovirhe(iboTunniste, "Tunnisteella löytyy useampi IB Diploma -opiskeluoikeus"))
    } else {
      (valmistuneetIbKoulutukset.maxByOption(_.opiskeluoikeus.päättymispäivä.map(_.toEpochDay)), diplomat.headOption) match {
        case (Some(ibKoulutus), diploma) =>
          Right(IbisYhdistettyOppija(ibKoulutus.oppijaOid, ibKoulutus.opiskeluoikeus.getOppilaitos, diploma.flatMap(_.opiskeluoikeus.oid)))
        case (None, Some(diploma)) =>
          Right(IbisYhdistettyOppija(diploma.oppijaOid, diploma.opiskeluoikeus.getOppilaitos, diploma.opiskeluoikeus.oid))
        case (None, None) if ibKoulutukset.nonEmpty =>
          Left(IbisKonversiovirhe(iboTunniste, "Tunnisteella löytyy IB-koulutuksen opiskeluoikeus, mutta se ei ole valmistunut"))
        case (None, None) =>
          Left(IbisKonversiovirhe(iboTunniste, "Tunnisteella ei löydy valmistunutta IB-koulutuksen opiskeluoikeutta eikä IB Diploma -opiskeluoikeutta"))
      }
    }
  }
}
