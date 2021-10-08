package fi.oph.koski.raportit

import fi.oph.koski.schema.OrganisaatioWithOid

object AhvenanmaanKunnat {
  val ahvenanmaanKunnat = List(
    "035",
    "043",
    "060",
    "062",
    "065",
    "076",
    "170",
    "295",
    "318",
    "417",
    "438",
    "478",
    "736",
    "766",
    "771",
    "941"
  )

  def onAhvenanmaalainenKunta(o: OrganisaatioWithOid): Boolean =
    o.kotipaikka match {
      case Some(kotipaikka) => ahvenanmaanKunnat.contains(kotipaikka.koodiarvo)
      case None => false
    }
}
