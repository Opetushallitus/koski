package fi.oph.koski.suoritusjako


import java.util.UUID.randomUUID

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.schema._

class SuoritusjakoService(suoritusjakoRepository: SuoritusjakoRepository, oppijaFacade: KoskiOppijaFacade) extends Logging {
  def put(oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): Either[HttpStatus, String] = {
    assertSuorituksetExist(oppijaOid, suoritusIds) match {
      case Right(_) =>
        val uuid = randomUUID.toString.replaceAll("-", "")
        suoritusjakoRepository.put(uuid, oppijaOid, suoritusIds)
        Right(uuid)
      case Left(status) => Left(status)
    }
  }

  def get(uuid: String): Either[HttpStatus, Oppija] = {
    suoritusjakoRepository.get(uuid).flatMap { suoritusjako =>
      oppijaFacade.findOppija(suoritusjako.oppijaOid)(KoskiSession.systemUser).map { oppija =>
        val suoritusIdentifiers = JsonSerializer.extract[List[SuoritusIdentifier]](suoritusjako.suoritusIds)
        val filtered = filterOpiskeluoikeudet(oppija.opiskeluoikeudet, suoritusIdentifiers)
        OmatTiedotEditorModel.piilotaArvosanatKeskeneräisistäSuorituksista(oppija.copy(opiskeluoikeudet = filtered))
      }
    }
  }

  def validateSuoritusjakoUuid(uuid: String): Either[HttpStatus, String] = {
    if (uuid.matches("^[0-9a-f]{32}$")) {
      Right(uuid)
    } else {
      Left(KoskiErrorCategory.badRequest.format())
    }
  }

  private def filterOpiskeluoikeudet(opiskeluoikeus: Seq[Opiskeluoikeus], suoritusIds: List[SuoritusIdentifier]): Seq[Opiskeluoikeus] = {
    opiskeluoikeus.flatMap { oo =>
      filterSuoritukset(oo, suoritusIds) match {
        case Nil => Nil
        case filtered => List(withSuoritukset(oo, filtered))
      }
    }
  }

  private def assertSuorituksetExist(oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): Either[HttpStatus, Boolean] = {
    oppijaFacade.findOppija(oppijaOid)(KoskiSession.systemUser).map { oppija =>
      suoritusIds.map(suoritusId =>
        oppija.opiskeluoikeudet.exists(oo =>
          oo.suoritukset.exists(isMatchingSuoritus(oo, _, suoritusId))
        )
      )
    } match {
      case Right(matches) if matches.isEmpty => Left(KoskiErrorCategory.badRequest.format())
      case Right(matches) if matches.exists(!_) => Left(KoskiErrorCategory.notFound.suoritustaEiLöydy())
      case Right(_) => Right(true)
      case Left(status) => Left(status)
    }
  }

  private def filterSuoritukset(opiskeluoikeus: Opiskeluoikeus, suoritusIds: List[SuoritusIdentifier]): List[Suoritus] =
    opiskeluoikeus.suoritukset.filter { suoritus =>
      suoritusIds.exists(suoritusId => isMatchingSuoritus(opiskeluoikeus, suoritus, suoritusId))
    }

  private def isMatchingSuoritus(opiskeluoikeus: Opiskeluoikeus, suoritus: PäätasonSuoritus, suoritusId: SuoritusIdentifier): Boolean =
    opiskeluoikeus.oppilaitos.exists(_.oid == suoritusId.oppilaitosOid) &&
      suoritus.tyyppi.koodiarvo == suoritusId.suorituksenTyyppi &&
      suoritus.koulutusmoduuli.tunniste.koodiarvo == suoritusId.koulutusmoduulinTunniste

  private def withSuoritukset(opiskeluoikeus: Opiskeluoikeus, suoritukset: List[Suoritus]): Opiskeluoikeus = {
    import mojave._
    shapeless.lens[Opiskeluoikeus].field[List[Suoritus]]("suoritukset").set(opiskeluoikeus)(suoritukset)
  }
}
