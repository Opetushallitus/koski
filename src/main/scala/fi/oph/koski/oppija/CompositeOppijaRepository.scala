package fi.oph.koski.oppija

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._

case class CompositeOppijaRepository(main: OppijaRepository, aux: List[AuxiliaryOppijaRepository]) extends OppijaRepository {
  override def findOppijat(query: String)(implicit user: KoskiUser) = {
    (main :: aux).iterator.map(_.findOppijat(query)).find(!_.isEmpty).getOrElse(Nil)
  }

  override def findByOid(oid: String)(implicit user: KoskiUser) = main.findByOid(oid)

  override def findOrCreate(henkilö: UusiHenkilö)(implicit user: KoskiUser) = main.findOrCreate(henkilö)

  override def findByOids(oids: List[String])(implicit user: KoskiUser) = main.findByOids(oids)
}
