package fi.oph.tor.organisaatio

case class OrganisaatioHierarkia(oid: String, nimi: String, organisaatiotyypit: List[String], children: List[OrganisaatioHierarkia])
