package fi.oph.koski.organisaatio

import fi.oph.koski.schema.{Finnish, LocalizedString}

object Opetushallitus {
  val organisaatioOid = "1.2.246.562.10.00000000001"
  val koulutustoimijaOid = "1.2.246.562.10.48587687889"
  val nimi: LocalizedString = Finnish("Opetushallitus", sv = Some("Utbildningsstyrelsen"), en = Some("Finnish National Agency for Education"))
}

object Tuntematon {
  val koulutustoimijaOid = "1.2.246.562.10.2013120314194853405606"
  val oppilaitosOid = "1.2.246.562.10.57118763579"
}
