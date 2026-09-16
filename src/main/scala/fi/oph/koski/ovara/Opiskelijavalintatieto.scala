package fi.oph.koski.ovara

import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}

case class Opiskelijavalintatieto(
  hakemukset: List[OpiskelijavalintaHakemus]
)

case class OpiskelijavalintaHakemus(
  hakemusOid: String,
  haunKohdejoukko: Option[Koodistokoodiviite],
  hakutapa: Option[Koodistokoodiviite],
  haku: OpiskelijavalintaHaku,
  hakutoiveet: List[OpiskelijavalintaHakutoive]
)

case class OpiskelijavalintaHaku(
  oid: String,
  nimi: LocalizedString
)

case class OpiskelijavalintaHakutoive(
  hakukohde: OpiskelijavalintaOrganisaatio,
  tarjoaja: Option[OpiskelijavalintaOrganisaatio],
  koulutuksenAlkamiskausi: Option[Koodistokoodiviite],
  koulutuksenAlkamisvuosi: Option[String],
  valinnanTila: Option[Koodistokoodiviite],
  vastaanotonTila: Option[Koodistokoodiviite],
  ilmoittautumisenTila: Option[Koodistokoodiviite],
  johtaaTutkintoon: Option[Boolean]
)

case class OpiskelijavalintaOrganisaatio(
  oid: String,
  nimi: LocalizedString
)
