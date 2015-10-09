package fi.oph.tor.organisaatio

case class Organisaatio(oid: String, nimi: String, organisaatiotyypit: List[String], children: List[Organisaatio])
