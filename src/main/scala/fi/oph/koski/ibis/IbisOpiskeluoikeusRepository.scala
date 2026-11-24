package fi.oph.koski.ibis

import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudet
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus

case class IbisTunnisteenOpiskeluoikeus(iboTunniste: String, oppijaOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)

// Haku tehdään ilman käyttöoikeusrajausta, koska IBIS-tuonnin tekee Opetushallituksen pääkäyttäjä.
class IbisOpiskeluoikeusRepository(val db: DB) extends QueryMethods {
  private val koulutusmuodot = Set("ibtutkinto", "internationalschool", "ibdiploma")

  // Haku ei välitä kirjainkoosta, ja tunnisteet palautetaan pienillä kirjaimilla.
  def findByIboTunnisteet(tunnisteet: Set[String]): Seq[IbisTunnisteenOpiskeluoikeus] = {
    val haettavat = tunnisteet.map(_.toLowerCase)
    runDbSync(
      KoskiOpiskeluOikeudet
        .filter(_.koulutusmuoto inSetBind koulutusmuodot)
        .filterNot(_.mitätöity)
        .filterNot(_.poistettu)
        .map(row => ((row.data +>> "iboTunniste").toLowerCase, row))
        .filter { case (tunniste, _) => tunniste inSetBind haettavat }
        .result
    ).map { case (tunniste, row) => IbisTunnisteenOpiskeluoikeus(tunniste, row.oppijaOid, row.toOpiskeluoikeusUnsafe(KoskiSpecificSession.systemUser)) }
  }
}
