package fi.oph.tor.tor

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import fi.oph.tor.opiskeluoikeus._
import fi.oph.tor.oppija._
import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser
import fi.vm.sade.utils.slf4j.Logging
import rx.lang.scala.Observable

class TodennetunOsaamisenRekisteri(oppijaRepository: OppijaRepository,
                                   opiskeluOikeusRepository: OpiskeluOikeusRepository) extends Logging {

  def findOppijat(filters: List[QueryFilter])(implicit user: TorUser): Observable[TorOppija] = {
    opiskeluOikeusRepository.query(filters).tumblingBuffer(100).flatMap {
      oikeudet =>
        val henkilötAndOpiskeluoikeudet = oppijaRepository.findByOids(oikeudet.map(_._1).toList).zip(oikeudet).map {
          case (h, (oid, oo)) =>
            assert(h.oid == oid)
            TorOppija(h, oo)
        }
        Observable.from(henkilötAndOpiskeluoikeudet)
    }
  }

  def findOppijat(query: String)(implicit user: TorUser): Seq[FullHenkilö] = {
    val oppijat: List[FullHenkilö] = oppijaRepository.findOppijat(query)
    val filtered = opiskeluOikeusRepository.filterOppijat(oppijat)
    filtered.sortBy(oppija => (oppija.sukunimi, oppija.etunimet))
  }

  def createOrUpdate(oppija: TorOppija)(implicit user: TorUser): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val oppijaOid: Either[HttpStatus, PossiblyUnverifiedOppijaOid] = oppija.henkilö match {
      case h:NewHenkilö => oppijaRepository.findOrCreate(oppija.henkilö).right.map(VerifiedOppijaOid(_))
      case h:HenkilöWithOid => Right(UnverifiedOppijaOid(h.oid, oppijaRepository))
    }

    oppijaOid.right.flatMap { oppijaOid: PossiblyUnverifiedOppijaOid =>
      val opiskeluOikeusCreationResults: Seq[Either[HttpStatus, CreateOrUpdateResult]] = oppija.opiskeluoikeudet.map { opiskeluOikeus =>
        val result = opiskeluOikeusRepository.createOrUpdate(oppijaOid, opiskeluOikeus)
        result match {
          case Right(result) =>
            val (verb, content) = result match {
              case _:Updated => ("Päivitetty", Json.write(opiskeluOikeus))
              case _:Created => ("Luotu", Json.write(opiskeluOikeus))
              case _:NotChanged => ("Päivitetty", "ei muutoksia")
            }
            logger.info(verb + " opiskeluoikeus " + result.oid + " (versio " + result.versionumero + ")" + " oppijalle " + oppijaOid + " tutkintoon " + opiskeluOikeus.suoritus.koulutusmoduulitoteutus.koulutusmoduuli.tunniste + " oppilaitoksessa " + opiskeluOikeus.oppilaitos.oid + ": " + content)
          case _ =>
        }
        result
      }

      opiskeluOikeusCreationResults.find(_.isLeft) match {
        case Some(Left(error)) => Left(error)
        case _ => Right(HenkilönOpiskeluoikeusVersiot(OidHenkilö(oppijaOid.oppijaOid), opiskeluOikeusCreationResults.toList.map {
          case Right(result:CreateOrUpdateResult) => OpiskeluoikeusVersio(result.oid, result.versionumero)
        }))
      }
    }
  }

  def findTorOppija(oid: String)(implicit user: TorUser): Either[HttpStatus, TorOppija] = {
    oppijaRepository.findByOid(oid) match {
      case Some(oppija) =>
        opiskeluOikeusRepository.findByOppijaOid(oppija.oid) match {
          case Nil => notFound(oid)
          case opiskeluoikeudet => Right(TorOppija(oppija, opiskeluoikeudet))
        }
      case None => notFound(oid)
    }
  }

  private def notFound(oid: String): Left[HttpStatus, Nothing] = {
    Left(HttpStatus.notFound(s"Oppija with oid: $oid not found"))
  }
}

case class HenkilönOpiskeluoikeusVersiot(henkilö: OidHenkilö, opiskeluoikeudet: List[OpiskeluoikeusVersio])
case class OpiskeluoikeusVersio(id: OpiskeluOikeus.Id, versionumero: Int)