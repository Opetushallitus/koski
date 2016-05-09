package fi.oph.tor.virta

import fi.oph.tor.henkilo.Hetu
import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.oppija.{MockOppijaRepository, OppijaRepository}
import fi.oph.tor.schema.UusiHenkilö

// Wrapper that implements OppijaRepository on top of Virta
case class VirtaOppijaRepository(v: VirtaClient) extends OppijaRepository {
  val mockOppijat = new MockOppijaRepository(Nil)

  override def findOppijat(query: String) = Hetu.validFormat(query) match {
    case Left(_) =>
      Nil
    case Right(hetu) =>
      mockOppijat.findOppijat(query) match {
        case Nil =>
          val hakuehto: VirtaHakuehtoHetu = VirtaHakuehtoHetu(hetu)
          val organisaatiot = v.fetchVirtaData(hakuehto).toSeq.flatMap(_ \\ "Opiskeluoikeus" \ "Myontaja").map(_.text)
          val opiskelijaNodes = organisaatiot.flatMap(v.fetchHenkilöData(hakuehto, _)).flatMap(_ \\ "Opiskelija")
          opiskelijaNodes
            .map { opiskelijaNode => ((opiskelijaNode \ "Sukunimi").text, (opiskelijaNode \ "Etunimet").text) }
            .find { case (suku, etu) => !suku.isEmpty && !etu.isEmpty }
            .map { case (suku, etu) => mockOppijat.addOppija(suku, etu, hetu) }
            .toList
        case oppijat => oppijat
      }

  }

  override def findOrCreate(henkilö: UusiHenkilö) = Left(TorErrorCategory.notImplemented.readOnly("Virta-järjestelmään ei voi lisätä henkilöitä"))

  override def findByOid(oid: String) = mockOppijat.findByOid(oid)

  override def findByOids(oids: List[String]) = mockOppijat.findByOids(oids)
}
