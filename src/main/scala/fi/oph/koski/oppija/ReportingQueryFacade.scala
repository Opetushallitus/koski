package fi.oph.koski.oppija

import fi.oph.koski.db.OpiskeluOikeusRow
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{apply => _}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.schema.Henkilö.{apply => _, _}
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import rx.lang.scala.Observable

case class ReportingQueryFacade(oppijaRepository: HenkilöRepository, opiskeluOikeusRepository: OpiskeluOikeusRepository, koodisto: KoodistoViitePalvelu) extends Logging {
  def findOppijat(filters: List[QueryFilter], user: KoskiSession): Observable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = {
    query(filters)(user)
  }

  private def query(filters: List[QueryFilter])(implicit user: KoskiSession): Observable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = {
    val oikeudetPerOppijaOid: Observable[(Oid, List[OpiskeluOikeusRow])] = opiskeluOikeusRepository.streamingQuery(filters)
    oikeudetPerOppijaOid.tumblingBuffer(500).flatMap {
      oppijatJaOidit: Seq[(Oid, List[OpiskeluOikeusRow])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1).toList

        val henkilöt: Map[String, TäydellisetHenkilötiedot] = oppijaRepository.findByOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap

        val oppijat: Iterable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = oppijatJaOidit.flatMap { case (oid, opiskeluOikeudet) =>
          henkilöt.get(oid) match {
            case Some(henkilö) =>
              Some((henkilö, opiskeluOikeudet))
            case None =>
              logger(user).warn("Oppijaa " + oid + " ei löydy henkilöpalvelusta")
              None
          }
        }
        Observable.from(oppijat)
    }
  }
}