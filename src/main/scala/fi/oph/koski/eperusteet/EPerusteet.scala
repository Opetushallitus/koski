package fi.oph.koski.eperusteet

case class EPerusteet(
  data: List[EPeruste]
)

case class EPeruste(
  id: Long,
  nimi: Map[String, String],
  diaarinumero: String,
  koulutukset: List[EPerusteKoulutus],
  koulutusvienti: Option[Boolean]
)

case class EPerusteKoulutus(
  nimi: Map[String, String],
  koulutuskoodiArvo: String
)

case class EPerusteTunniste(
  id: Long,
  nimi: Map[String, String]
)
