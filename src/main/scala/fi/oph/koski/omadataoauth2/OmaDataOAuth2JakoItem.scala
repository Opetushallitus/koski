package fi.oph.koski.omadataoauth2

import fi.oph.koski.schema.LocalizedString

import java.time.LocalDateTime

case class OmaDataOAuth2JakoItem(
  codeSHA256: String,
  clientId: String,
  clientName: LocalizedString,
  creationDate: LocalDateTime,
  expirationDate: LocalDateTime,
  scope: String
)
