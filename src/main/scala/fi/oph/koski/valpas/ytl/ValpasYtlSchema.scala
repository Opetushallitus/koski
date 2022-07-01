package fi.oph.koski.valpas.ytl

import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.oppivelvollisuustieto.OptionalOppivelvollisuustieto
import fi.oph.koski.schema
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.valpas.{ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult, ValpasHenkilöhakuResult, ValpasLöytyiHenkilöhakuResult}
import fi.oph.scalaschema.annotation.{Description, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object ValpasYtlSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[YtlMaksuttomuustieto]).asInstanceOf[ClassSchema])
}

@Title("Tiedot oikeudesta maksuttomaan koulutukseen")
@Description("Tiedot oppijan oikeudesta maksuttomaan koulutukseen Valppaan tietojen perusteella.")
case class YtlMaksuttomuustieto(
  oppijaOid: String,
  @Description("Hetu palautetaan vain, jos oppijan tiedot on haettu käyttäen hetukyselyä")
  hetu: Option[String] = None,
  @Description("Viimeisin päivä, jolloin oikeus maksuttomaan koulutukseen on voimassa, jos sellainen on pääteltävissä. Puuttuu, jos oppija ei ole laajennetun oppivelvollisuuden lain piirissä.")
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[LocalDate] = None,
  @Description("Tosi jos oppija on Valppaan tietojen perusteella oikeutettu maksuttomaan koulutukseen. Epätosi jos oppija ei ole lain piirissä tai oikeus maksuttomaan koulutukseen on menneisyydessä.")
  maksuttomuudenPiirissä: Option[Boolean] = None,
) {
  def withoutHetu: YtlMaksuttomuustieto = this.copy(hetu = None)
}

object YtlMaksuttomuustieto {
  def apply(tarkastelupäivä: LocalDate)(tiedot: OptionalOppivelvollisuustieto): YtlMaksuttomuustieto = YtlMaksuttomuustieto(
    oppijaOid = tiedot.oid,
    hetu = tiedot.hetu,
    oikeusMaksuttomaanKoulutukseenVoimassaAsti = tiedot.oikeusMaksuttomaanKoulutukseenVoimassaAsti,
    maksuttomuudenPiirissä = tiedot.oikeusMaksuttomaanKoulutukseenVoimassaAsti
      .map(_.isEqualOrAfter(tarkastelupäivä))
      .orElse(Some(false)),
  )

  def apply(tiedot: ValpasHenkilöhakuResult, tarkastelupäivä: LocalDate): Option[YtlMaksuttomuustieto] =
    tiedot match {
      case r: ValpasLöytyiHenkilöhakuResult => Some(YtlMaksuttomuustieto(
        oppijaOid = r.oid,
        hetu = r.hetu,
        oikeusMaksuttomaanKoulutukseenVoimassaAsti = r.maksuttomuusVoimassaAstiIänPerusteella,
        maksuttomuudenPiirissä = r.maksuttomuusVoimassaAstiIänPerusteella
          .map(_.isEqualOrAfter(tarkastelupäivä))
          .orElse(Some(false)),
      ))
      case r: ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult => r.oid.map(oid => YtlMaksuttomuustieto(
        oppijaOid = oid,
        hetu = r.hetu,
        oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
        maksuttomuudenPiirissä = Some(false),
      ))
      case _ => None
    }

  def apply(tiedot: LaajatOppijaHenkilöTiedot): YtlMaksuttomuustieto = YtlMaksuttomuustieto(
    oppijaOid = tiedot.oid,
    hetu = tiedot.hetu,
    oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
    maksuttomuudenPiirissä = Some(false),
  )

  def oidOrder: Ordering[YtlMaksuttomuustieto] = Ordering.by((t: YtlMaksuttomuustieto) => t.oppijaOid)
}
