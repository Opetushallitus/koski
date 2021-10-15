package fi.oph.koski.valpas.kela

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import fi.oph.koski.schema
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

object ValpasKelaSchema {
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[ValpasKelaOppija]).asInstanceOf[ClassSchema])
}

case class ValpasKelaOppija(
  henkilö: ValpasKelaHenkilö,
  oppivelvollisuudenKeskeytykset: Seq[ValpasKelaOppivelvollisuudenKeskeytys]
)

case class ValpasKelaHenkilö(
  oid: ValpasHenkilö.Oid,
  hetu: Option[String],
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: Option[LocalDate],
)

case class ValpasKelaOppivelvollisuudenKeskeytys(
  uuid: String,
  alku: LocalDate,
  loppu: Option[LocalDate],
  luotu: LocalDateTime,
  peruttu: Boolean,
)
