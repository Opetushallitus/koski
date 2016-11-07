package fi.oph.koski.oppija

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._

case class CompositeOppijaRepository(main: OppijaRepository, aux: List[AuxiliaryOppijaRepository]) extends OppijaRepository {
  override def findOppijat(query: String)(implicit user: KoskiSession) = {
    (main :: aux).iterator.map(_.findOppijat(query)).find(!_.isEmpty).getOrElse(Nil)
  }

  override def findByOid(oid: String) = main.findByOid(oid)

  override def findOrCreate(henkilö: UusiHenkilö) = main.findOrCreate(henkilö)

  override def findByOids(oids: List[String]) = main.findByOids(oids)
}
