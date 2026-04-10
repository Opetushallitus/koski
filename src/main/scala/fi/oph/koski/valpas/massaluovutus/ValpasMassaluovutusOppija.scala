package fi.oph.koski.valpas.massaluovutus

import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Koodistokoodiviite, KoskiSchema, LocalizedString, Organisaatio, OrganisaatioWithOid}

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import fi.oph.koski.valpas.valpasrepository.ValpasKuntailmoituksenOppijanYhteystiedot
import fi.oph.scalaschema.annotation.{Description, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

trait ValpasMassaluovutusOppija {
  @Description("Oppijan yksilöivä tunniste. Jos oppijalla on olemassa useita yksilöiviä tunnisteita, palautetaan tässä kentässä oppijanumero eli oppijan hallitseva tunniste.")
  def oppijanumero: ValpasHenkilö.Oid
  @Description("Oppijan kaikki yksilöivät tunnisteet, joilla opiskeluoikeuksia on tallennettu Koski-tietovarantoon.")
  def kaikkiOidit: Option[Seq[ValpasHenkilö.Oid]]
  def etunimet: String
  def sukunimi: String
  def syntymäaika: Option[LocalDate]
  def hetu: Option[String]
  @Description("Oppijan viimeisin oppivelvollisuuden suorittamiseen kelpaava opiskeluoikeus.")
  def viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[ValpasMassaluovutusOpiskeluoikeus]
  @Description("Oppijan olemassaolevat oppivelvollisuuden keskeytykset.")
  def oppivelvollisuudenKeskeytys: Seq[ValpasMassaluovutusOppivelvollisuudenKeskeytys]
  @Description("Jos oppija on olemassa vain Oppijanumerorekisterissä, hänellä ei ole olemassa opiskeluoikeuksia Koski-tietovarannossa.")
  def vainOppijanumerorekisterissä: Boolean
  @Description("Oppijan olemassaoleva aktiivinen kuntailmoitus.")
  def aktiivinenKuntailmoitus: Option[ValpasMassaluovutusKuntailmoitus]
  @Description("Valpas-palvelun päättelemä päivämäärä, mihin saakka oppijalla on oikeus maksuttomaan koulutukseen.")
  def oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[LocalDate]
  @Description("Ensimmäinen päivämäärä, jolloin oppijalla on kotikunta Suomessa (lukuun ottamatta Ahvenanmaata) Oppijanumerorekisterin kotikuntahistorian perusteella.")
  def kotikuntaSuomessaAlkaen: Option[LocalDate]
}

@Title("Oppivelvollinen oppija")
case class ValpasMassaluovutusOppivelvollinenOppija(
  oppijanumero: ValpasHenkilö.Oid,
  kaikkiOidit: Option[Seq[ValpasHenkilö.Oid]],
  etunimet: String,
  sukunimi: String,
  syntymäaika: Option[LocalDate],
  hetu: Option[String],
  @Description("Oppijan kaikki tällä hetkellä aktiiviset oppivelvollisuuden suorittamiseen kelpaavat opiskeluoikeudet.")
  aktiivisetOppivelvollisuudenSuorittamiseenKelpaavatOpiskeluoikeudet: Seq[ValpasMassaluovutusOpiskeluoikeus],
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[ValpasMassaluovutusOpiskeluoikeus],
  oppivelvollisuudenKeskeytys: Seq[ValpasMassaluovutusOppivelvollisuudenKeskeytys],
  vainOppijanumerorekisterissä: Boolean,
  aktiivinenKuntailmoitus: Option[ValpasMassaluovutusKuntailmoitus],
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[LocalDate],
  kotikuntaSuomessaAlkaen: Option[LocalDate]
) extends ValpasMassaluovutusOppija

object ValpasMassaluovutusOppivelvollinenOppija {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[ValpasMassaluovutusOppivelvollinenOppija]).asInstanceOf[ClassSchema])
}

@Title("Ei oppivelvollisuutta suorittava oppija")
case class ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija(
  oppijanumero: ValpasHenkilö.Oid,
  kaikkiOidit: Option[Seq[ValpasHenkilö.Oid]],
  etunimet: String,
  sukunimi: String,
  syntymäaika: Option[LocalDate],
  hetu: Option[String],
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[ValpasMassaluovutusOpiskeluoikeus],
  oppivelvollisuudenKeskeytys: Seq[ValpasMassaluovutusOppivelvollisuudenKeskeytys],
  vainOppijanumerorekisterissä: Boolean,
  aktiivinenKuntailmoitus: Option[ValpasMassaluovutusKuntailmoitus],
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[LocalDate],
  kotikuntaSuomessaAlkaen: Option[LocalDate]
) extends ValpasMassaluovutusOppija

object ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija]).asInstanceOf[ClassSchema])
}

@Title("Opiskeluoikeus")
case class ValpasMassaluovutusOpiskeluoikeus(
  @KoodistoUri("suorituksentyyppi")
  suorituksenTyyppi: Koodistokoodiviite,
  @Description("Opiskeluoikeuden päätason suorituksen koulutusmoduulin tunnisteen koodistokoodiarvo Koski-palvelussa")
  koulutusmoduulinTunniste: String,
  @Description("Opiskeluoikeuden mahdollinen päättymispäivä, muotoa 2007-12-03")
  päättymispäivä: Option[String],
  @KoodistoUri("valpasopiskeluoikeudentila")
  @Description("Opiskeluoikeuden viimeisin tila Valpas-palvelussa")
  viimeisinValpasTila: Koodistokoodiviite,
  @KoodistoUri("koskiopiskeluoikeudentila")
  @Description("Opiskeluoikeuden viimeisin tila Koski-palvelussa")
  viimeisinTila: Koodistokoodiviite,
  toimipiste: LocalizedString,
)

@Title("Oppivelvollisuuden keskeytys")
case class ValpasMassaluovutusOppivelvollisuudenKeskeytys(
  @Description("Oppivelvollisuuden keskeytyksen yksilöivä tunniste")
  id: String,
  tekijäOrganisaatioOid: Organisaatio.Oid,
  @Description("Oppivelvollisuuden keskeytyksen alkupäivä, muotoa 2007-12-03")
  alku: LocalDate,
  @Description("Oppivelvollisuuden keskeytyksen mahdollinen loppupäivä, muotoa 2007-12-03")
  loppu: Option[LocalDate],
  voimassa: Boolean,
  tulevaisuudessa: Boolean,
)

@Title("Kuntailmoitus")
case class ValpasMassaluovutusKuntailmoitus(
  oppijaOid: Option[String],
  @Description("Kuntailmoituksen yksilöivä UUID-tunniste")
  id: Option[String],
  tekijä: ValpasMassaluovutusKuntailmoituksenTekijä,
  kunta: OrganisaatioWithOid,
  @Description("Kuntailmoituksen aikaleima ilman aikavyöhyketietoja, muotoa 2007-12-03T10:15:30")
  aikaleima: Option[LocalDateTime],
  hakenutMuualle: Option[Boolean],
  onUudempiaIlmoituksiaMuihinKuntiin: Option[Boolean],
  aktiivinen: Option[Boolean],
  @Description("Kuntailmoitukselle tallennetut oppijan yhteystiedot")
  oppijanYhteystiedot: Option[ValpasKuntailmoituksenOppijanYhteystiedot],
)

@Title("Kuntailmoituksen tekijä")
case class ValpasMassaluovutusKuntailmoituksenTekijä(
  organisaatio: OrganisaatioWithOid
)
