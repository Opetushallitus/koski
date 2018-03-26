package fi.oph.koski.suoritusjako


import java.util.UUID.randomUUID

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.schema._

class SuoritusjakoService(suoritusjakoRepository: SuoritusjakoRepository, oppijaFacade: KoskiOppijaFacade) extends Logging {
  def put(oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): String = {
    val uuid = randomUUID.toString.replaceAll("-", "")
    suoritusjakoRepository.put(uuid, oppijaOid, suoritusIds)
    uuid
  }

  def get(uuid: String): Either[HttpStatus, Oppija] = {
    suoritusjakoRepository.get(uuid).flatMap { suoritusjako =>
      oppijaFacade.findOppija(suoritusjako.oppijaOid)(KoskiSession.systemUser).map { oppija =>
        val suoritusIdentifiers = JsonSerializer.extract[List[SuoritusIdentifier]](suoritusjako.suoritusIds)
        val filtered = filterOpiskeluoikeudet(oppija.opiskeluoikeudet, suoritusIdentifiers)
        oppija.copy(opiskeluoikeudet = filtered)
      }
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

  private def filterSuoritukset(opiskeluoikeus: Opiskeluoikeus, suoritusIds: List[SuoritusIdentifier]): List[Suoritus] =
    opiskeluoikeus.suoritukset.filter { suoritus =>
      suoritusIds.exists(suoritusId =>
        opiskeluoikeus.oppilaitos.exists(_.oid == suoritusId.oppilaitosOid) &&
          suoritus.tyyppi.koodiarvo == suoritusId.suorituksenTyyppi &&
          suoritus.koulutusmoduuli.tunniste.koodiarvo == suoritusId.koulutusmoduulinTunniste
      )
    }

  private def withSuoritukset(opiskeluoikeus: Opiskeluoikeus, suoritukset: List[Suoritus]): Opiskeluoikeus = {
    import mojave._
    shapeless.lens[Opiskeluoikeus].field[List[Suoritus]]("suoritukset").set(opiskeluoikeus)(suoritukset)
  }
}
