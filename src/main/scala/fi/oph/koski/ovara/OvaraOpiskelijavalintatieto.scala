package fi.oph.koski.ovara

case class OvaraOpiskelijavalintatieto(
  oppijanumero: String,
  hetu: Option[String],
  syntymaaika: Option[String],
  sukunimi: Option[String],
  etunimet: Option[String],
  hakemukset: List[OvaraHakemus]
)

case class OvaraHakemus(
  hakemusOid: String,
  haku: OvaraHaku,
  haunKohdejoukko: Option[String],
  hakutapa: Option[String],
  hakutoiveet: List[OvaraHakutoive]
)

case class OvaraHaku(oid: String, nimi: OvaraNimi)

case class OvaraNimi(fi: Option[String], sv: Option[String], en: Option[String])

case class OvaraHakutoive(
  hakukohde: OvaraOrganisaatio,
  tarjoaja: Option[OvaraOrganisaatio],
  koulutuksenAlkamiskausiUri: Option[String],
  koulutuksenAlkamisvuosi: Option[String],
  valinnanTila: Option[String],
  vastaanotonTila: Option[String],
  ilmoittautumisenTila: Option[String]
)

case class OvaraOrganisaatio(oid: String, nimi: OvaraNimi)
