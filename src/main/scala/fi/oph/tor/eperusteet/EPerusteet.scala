package fi.oph.tor.eperusteet

case class EPerusteet(data: List[EPeruste])
case class EPeruste(id: Long, nimi: Map[String, String], diaarinumero: String, koulutukset: List[EPerusteKoulutus])
case class EPerusteKoulutus(nimi: Map[String, String], koulutuskoodiArvo: String)
case class EPerusteTunniste(id: String)