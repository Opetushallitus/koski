package fi.oph.tor.virta

import fi.oph.tor.henkilo.Hetu
import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.schema.{UusiHenkilö, TaydellisetHenkilötiedot}

// Wrapper that implements OppijaRepository on top of Virta
// TODO: actually fetch data from Virta. Now always returns a fake henkilö for a valid hetu
case class VirtaOppijaRepository(v: VirtaClient) extends OppijaRepository {
  override def findOppijat(query: String) = Hetu.validFormat(query) match {
    case Left(_) =>
      Nil
    case Right(hetu) =>
      List(TaydellisetHenkilötiedot("?", hetu, "-", "-", "-", None, None))
  }

  override def findOrCreate(henkilö: UusiHenkilö) = Left(TorErrorCategory.notImplemented.readOnly("Virta-järjestelmään ei voi lisätä henkilöitä"))

  override def findByOid(oid: String) = None

  override def findByOids(oids: List[String]) = Nil
}
