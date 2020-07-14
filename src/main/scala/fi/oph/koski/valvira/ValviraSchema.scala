package fi.oph.koski.valvira

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.{Koodistokoodiviite, KoskiSchema, LocalizedString}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

object ValviraSchema {
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[ValviraOppija]).asInstanceOf[ClassSchema])
}

object ValviraOppija {
  def apply(hetu: String, opiskeluoikeudet: List[ValviraOpiskeluoikeus]): ValviraOppija = {
    new ValviraOppija(Henkilo(hetu), opiskeluoikeudet)
  }
}

case class ValviraOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[ValviraOpiskeluoikeus]
)

case class Henkilo(hetu: String)

case class ValviraOpiskeluoikeus(
  versionumero: Int,
  aikaleima: LocalDateTime,
  oppilaitos: Oppilaitos,
  tila: Opiskeluoikeusjaksot,
  suoritukset: List[AmmatillisenTutkinnonSuoritus],
  alkamispäivä: LocalDate,
  päättymispäivä: Option[LocalDate]
)

case class Opiskeluoikeusjaksot(opiskeluoikeusjaksot: List[Opiskeluoikeusjakso])

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[Koodistokoodiviite],
  nimi: Option[LocalizedString]
)

case class Opiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
)

case class AmmatillisenTutkinnonSuoritus(
  koulutusmoduuli: Koulutusmoduuli,
  tutkintonimike: Option[List[Koodistokoodiviite]],
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[Vahvistus]
)

case class Koulutusmoduuli(
  tunniste: Koodistokoodiviite,
  perusteenDiaarinumero: String,
  perusteenNimi: Option[LocalizedString],
  koulutustyyppi: Koodistokoodiviite
)

case class Vahvistus(
  päivä: LocalDate
)
