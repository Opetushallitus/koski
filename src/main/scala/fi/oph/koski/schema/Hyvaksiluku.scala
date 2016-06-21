package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.Description

case class Hyväksiluku(
  @Description("Aiemman, korvaavan suorituksen tiedot")
  osaaminen: Option[Suoritus],
  @Description("Osaamisen tunnustamisen kautta saatavan tutkinnon osan suorituksen selite")
  @OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
  selite: LocalizedString
)