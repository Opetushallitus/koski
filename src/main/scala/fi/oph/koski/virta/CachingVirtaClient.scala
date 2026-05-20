package fi.oph.koski.virta

import scala.xml.Elem

class CachingVirtaClient(underlying: VirtaClient, cache: VirtaDynamoDbCache) extends VirtaClient {

  override def opintotiedot(hakuehto: VirtaHakuehto): Option[Elem] = {
    val hakuehdot = List(hakuehto)
    cache.get(hakuehdot) match {
      case Some(xml) => Some(xml)
      case None =>
        val result = underlying.opintotiedot(hakuehto)
        result.foreach(xml => cache.put(hakuehdot, xml))
        result
    }
  }

  override def opintotiedotMassahaku(hakuehdot: List[VirtaHakuehto]): Option[Elem] = {
    cache.get(hakuehdot) match {
      case Some(xml) => Some(xml)
      case None =>
        val result = underlying.opintotiedotMassahaku(hakuehdot)
        result.foreach(xml => cache.put(hakuehdot, xml))
        result
    }
  }

  override def henkilötiedot(hakuehto: VirtaHakuehto, oppilaitosNumero: String): Option[Elem] =
    underlying.henkilötiedot(hakuehto, oppilaitosNumero)
}
