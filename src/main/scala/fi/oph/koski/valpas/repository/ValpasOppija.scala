package fi.oph.koski.valpas.repository

import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}

case class ValpasOppija(
  henkilö: ValpasHenkilö,
  oikeutetutOppilaitokset: Set[String],
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeus]
)

case class ValpasHenkilö(
  oid: String,
  hetu: Option[String],
  syntymäaika: Option[String],
  etunimet: String,
  sukunimi: String
)

case class ValpasOpiskeluoikeus(
  oid: String,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  toimipiste: Option[ValpasToimipiste],
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  ryhmä: Option[String],
  viimeisinTila: Koodistokoodiviite
)

case class ValpasOppilaitos(
  oid: String,
  nimi: LocalizedString
)

case class ValpasToimipiste(
  oid: String,
  nimi: LocalizedString
)
