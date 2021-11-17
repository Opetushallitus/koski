package fi.oph.koski.valpas.opiskeluoikeusrepository

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Koodistokoodiviite, KoskiSchema, LocalizedString, Maksuttomuus, OikeuttaMaksuttomuuteenPidennetty}
import fi.oph.koski.valpas.hakukooste.{Hakukooste, Hakutoive, Harkinnanvaraisuus, Valintatila, Vastaanottotieto}
import fi.oph.scalaschema.annotation.SyntheticProperty
import java.time.{LocalDate, LocalDateTime}

import org.json4s.JValue
import fi.oph.scalaschema.{AnyOfSchema, ClassSchema, SchemaToJson}

object ValpasInternalSchema {
  lazy val laajaSchemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[ValpasOppijaLaajatTiedot]).asInstanceOf[ClassSchema])
  lazy val suppeaSchemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[ValpasOppijaSuppeatTiedot]).asInstanceOf[ClassSchema])
}

trait ValpasOppija {
  def henkilö: ValpasHenkilö

  def opiskeluoikeudet: Seq[ValpasOpiskeluoikeus]

  def oppivelvollisuusVoimassaAsti: LocalDate

  @SyntheticProperty
  def opiskelee: Boolean = opiskeluoikeudet.exists(_.isOpiskelu)
}

case class ValpasOppijaLaajatTiedot(
  henkilö: ValpasHenkilöLaajatTiedot,
  hakeutumisvalvovatOppilaitokset: Set[ValpasOppilaitos.Oid],
  suorittamisvalvovatOppilaitokset: Set[ValpasOppilaitos.Oid],
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot],
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: LocalDate,
  onOikeusValvoaMaksuttomuutta: Boolean,
  onOikeusValvoaKunnalla: Boolean
) extends ValpasOppija

object ValpasOppijaSuppeatTiedot {
  def apply(laajatTiedot: ValpasOppijaLaajatTiedot): ValpasOppijaSuppeatTiedot = {
    ValpasOppijaSuppeatTiedot(
      ValpasHenkilöSuppeatTiedot(laajatTiedot.henkilö),
      laajatTiedot.opiskeluoikeudet.map(ValpasOpiskeluoikeusSuppeatTiedot.apply),
      laajatTiedot.oppivelvollisuusVoimassaAsti
    )
  }
}

case class ValpasOppijaSuppeatTiedot(
  henkilö: ValpasHenkilöSuppeatTiedot,
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeusSuppeatTiedot],
  oppivelvollisuusVoimassaAsti: LocalDate
) extends ValpasOppija

object ValpasHenkilö {
  type Oid = String
}

trait ValpasHenkilö {
  def oid: ValpasHenkilö.Oid
  def syntymäaika: Option[LocalDate]
  def etunimet: String
  def sukunimi: String
}

case class ValpasHenkilöLaajatTiedot(
  oid: ValpasHenkilö.Oid,
  kaikkiOidit: Set[ValpasHenkilö.Oid],
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  turvakielto: Boolean,
  äidinkieli: Option[String]
) extends ValpasHenkilö

object ValpasHenkilöSuppeatTiedot {
  def apply(laajatTiedot: ValpasHenkilöLaajatTiedot): ValpasHenkilöSuppeatTiedot = {
    ValpasHenkilöSuppeatTiedot(
      laajatTiedot.oid,
      laajatTiedot.kaikkiOidit,
      laajatTiedot.syntymäaika,
      laajatTiedot.etunimet,
      laajatTiedot.sukunimi
    )
  }
}

case class ValpasHenkilöSuppeatTiedot(
  oid: ValpasHenkilö.Oid,
  kaikkiOidit: Set[ValpasHenkilö.Oid],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String
) extends ValpasHenkilö

object ValpasOpiskeluoikeus {
  type Oid = String
}

trait ValpasOpiskeluoikeus {
  def oid: ValpasOpiskeluoikeus.Oid

  def onHakeutumisValvottava: Boolean

  def onSuorittamisValvottava: Boolean

  @KoodistoUri("opiskeluoikeudentyyppi")
  def tyyppi: Koodistokoodiviite

  def oppilaitos: ValpasOppilaitos

  def päätasonSuoritukset: Seq[ValpasPäätasonSuoritus]

  def perusopetusTiedot: Option[ValpasOpiskeluoikeusPerusopetusTiedot]

  def perusopetuksenJälkeinenTiedot: Option[ValpasOpiskeluoikeusTiedot]

  @SyntheticProperty
  def isOpiskelu: Boolean =
    perusopetusTiedot.exists(_.isOpiskelu) || perusopetuksenJälkeinenTiedot.exists(_.isOpiskelu)

  @SyntheticProperty
  def isOpiskeluTulevaisuudessa: Boolean =
    perusopetusTiedot.exists(_.isOpiskeluTulevaisuudessa) || perusopetuksenJälkeinenTiedot.exists(_.isOpiskeluTulevaisuudessa)

  @SyntheticProperty
  def tarkasteltavaPäätasonSuoritus: Option[ValpasPäätasonSuoritus] = {
    päätasonSuoritukset.headOption match {
      case Some(pts) if pts.suorituksenTyyppi.koodiarvo == "nayttotutkintoonvalmistavakoulutus" =>
        päätasonSuoritukset.find(oo =>
          Seq("ammatillinentutkinto", "ammatillinentutkintoosittainen").contains(oo.suorituksenTyyppi.koodiarvo)
        ) match {
          case Some(ammatillinen) => Some(ammatillinen)
          case None => päätasonSuoritukset.headOption
        }
      case _ => päätasonSuoritukset.headOption
    }
  }

  def alkamispäivä: Option[LocalDate] = {
    Seq(
      perusopetusTiedot.flatMap(_.alkamispäivä),
      perusopetuksenJälkeinenTiedot.flatMap(_.alkamispäivä),
    )
      .flatten
      .sorted
      .headOption
      .map(LocalDate.parse)
  }
}

case class ValpasOpiskeluoikeusLaajatTiedot(
  oid: ValpasOpiskeluoikeus.Oid,
  onHakeutumisValvottava: Boolean,
  onSuorittamisValvottava: Boolean,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  oppivelvollisuudenSuorittamiseenKelpaava: Boolean,
  perusopetusTiedot: Option[ValpasOpiskeluoikeusPerusopetusLaajatTiedot],
  perusopetuksenJälkeinenTiedot: Option[ValpasOpiskeluoikeusPerusopetuksenJälkeinenLaajatTiedot],
  päätasonSuoritukset: Seq[ValpasPäätasonSuoritus],
  // Option, koska tämä tieto rikastetaan mukaan vain tietyissä tilanteissa
  onTehtyIlmoitus: Option[Boolean],
  maksuttomuus: Option[Seq[Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[Seq[OikeuttaMaksuttomuuteenPidennetty]],
) extends ValpasOpiskeluoikeus

object ValpasOpiskeluoikeusSuppeatTiedot {
  def apply(laajatTiedot: ValpasOpiskeluoikeusLaajatTiedot): ValpasOpiskeluoikeusSuppeatTiedot = {
    ValpasOpiskeluoikeusSuppeatTiedot(
      oid = laajatTiedot.oid,
      onHakeutumisValvottava = laajatTiedot.onHakeutumisValvottava,
      onSuorittamisValvottava = laajatTiedot.onSuorittamisValvottava,
      tyyppi = laajatTiedot.tyyppi,
      oppilaitos = laajatTiedot.oppilaitos,
      perusopetusTiedot = laajatTiedot.perusopetusTiedot.map(ValpasOpiskeluoikeusPerusopetusSuppeatTiedot.apply),
      perusopetuksenJälkeinenTiedot = laajatTiedot.perusopetuksenJälkeinenTiedot.map(ValpasOpiskeluoikeusPerusopetuksenJälkeinenSuppeatTiedot.apply),
      päätasonSuoritukset = laajatTiedot.päätasonSuoritukset,
      onTehtyIlmoitus = laajatTiedot.onTehtyIlmoitus,
    )
  }
}

trait ValpasOpiskeluoikeusPerusopetusTiedot extends ValpasOpiskeluoikeusTiedot {

  def vuosiluokkiinSitomatonOpetus: Boolean
}

case class ValpasOpiskeluoikeusPerusopetusLaajatTiedot(
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  tarkastelupäivänTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTilanAlkamispäivä: String,
  valmistunutAiemminTaiLähitulevaisuudessa: Boolean,
  vuosiluokkiinSitomatonOpetus: Boolean,
  näytäMuunaPerusopetuksenJälkeisenäOpintona: Option[Boolean],
) extends ValpasOpiskeluoikeusPerusopetusTiedot

object ValpasOpiskeluoikeusPerusopetusSuppeatTiedot {
  def apply(laajatTiedot: ValpasOpiskeluoikeusPerusopetusLaajatTiedot): ValpasOpiskeluoikeusPerusopetusSuppeatTiedot = {
    ValpasOpiskeluoikeusPerusopetusSuppeatTiedot(
      alkamispäivä = laajatTiedot.alkamispäivä,
      päättymispäivä = laajatTiedot.päättymispäivä,
      päättymispäiväMerkittyTulevaisuuteen = laajatTiedot.päättymispäiväMerkittyTulevaisuuteen,
      tarkastelupäivänTila = laajatTiedot.tarkastelupäivänTila,
      tarkastelupäivänKoskiTila = laajatTiedot.tarkastelupäivänKoskiTila,
      valmistunutAiemminTaiLähitulevaisuudessa = laajatTiedot.valmistunutAiemminTaiLähitulevaisuudessa,
      vuosiluokkiinSitomatonOpetus = laajatTiedot.vuosiluokkiinSitomatonOpetus,
      näytäMuunaPerusopetuksenJälkeisenäOpintona = laajatTiedot.näytäMuunaPerusopetuksenJälkeisenäOpintona,
    )
  }
}

case class ValpasOpiskeluoikeusPerusopetusSuppeatTiedot(
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  tarkastelupäivänTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTila: Koodistokoodiviite,
  valmistunutAiemminTaiLähitulevaisuudessa: Boolean,
  vuosiluokkiinSitomatonOpetus: Boolean,
  näytäMuunaPerusopetuksenJälkeisenäOpintona: Option[Boolean],
) extends ValpasOpiskeluoikeusPerusopetusTiedot

object ValpasOpiskeluoikeusPerusopetuksenJälkeinenSuppeatTiedot {
  def apply(laajatTiedot: ValpasOpiskeluoikeusPerusopetuksenJälkeinenLaajatTiedot): ValpasOpiskeluoikeusPerusopetuksenJälkeinenSuppeatTiedot = {
    ValpasOpiskeluoikeusPerusopetuksenJälkeinenSuppeatTiedot(
      alkamispäivä = laajatTiedot.alkamispäivä,
      päättymispäivä = laajatTiedot.päättymispäivä,
      päättymispäiväMerkittyTulevaisuuteen = laajatTiedot.päättymispäiväMerkittyTulevaisuuteen,
      tarkastelupäivänTila = laajatTiedot.tarkastelupäivänTila,
      tarkastelupäivänKoskiTila = laajatTiedot.tarkastelupäivänKoskiTila,
      valmistunutAiemminTaiLähitulevaisuudessa = laajatTiedot.valmistunutAiemminTaiLähitulevaisuudessa,
      näytäMuunaPerusopetuksenJälkeisenäOpintona = laajatTiedot.näytäMuunaPerusopetuksenJälkeisenäOpintona,
    )
  }
}

trait ValpasOpiskeluoikeusTiedot {
  def alkamispäivä: Option[String]

  def päättymispäivä: Option[String]

  def päättymispäiväMerkittyTulevaisuuteen: Option[Boolean]

  @KoodistoUri("valpasopiskeluoikeudentila")
  def tarkastelupäivänTila: Koodistokoodiviite

  @KoodistoUri("koskiopiskeluoikeudentila")
  def tarkastelupäivänKoskiTila: Koodistokoodiviite

  def valmistunutAiemminTaiLähitulevaisuudessa: Boolean

  def näytäMuunaPerusopetuksenJälkeisenäOpintona: Option[Boolean]

  @SyntheticProperty
  def onVoimassaNytTaiTulevaisuudessa: Boolean = tarkastelupäivänTila.koodiarvo match {
    case "voimassa" | "voimassatulevaisuudessa" => true
    case _ => false
  }

  @SyntheticProperty
  def isOpiskelu: Boolean =
    tarkastelupäivänTila.koodiarvo == "voimassa"

  @SyntheticProperty
  def isOpiskeluTulevaisuudessa: Boolean =
    tarkastelupäivänTila.koodiarvo == "voimassatulevaisuudessa"
}

case class ValpasOpiskeluoikeusPerusopetuksenJälkeinenLaajatTiedot(
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  tarkastelupäivänTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTilanAlkamispäivä: String,
  valmistunutAiemminTaiLähitulevaisuudessa: Boolean,
  näytäMuunaPerusopetuksenJälkeisenäOpintona: Option[Boolean],
) extends ValpasOpiskeluoikeusTiedot

case class ValpasOpiskeluoikeusPerusopetuksenJälkeinenSuppeatTiedot(
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  tarkastelupäivänTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTila: Koodistokoodiviite,
  valmistunutAiemminTaiLähitulevaisuudessa: Boolean,
  näytäMuunaPerusopetuksenJälkeisenäOpintona: Option[Boolean],
) extends ValpasOpiskeluoikeusTiedot


case class ValpasOpiskeluoikeusSuppeatTiedot(
  oid: ValpasOpiskeluoikeus.Oid,
  onHakeutumisValvottava: Boolean,
  onSuorittamisValvottava: Boolean,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  perusopetusTiedot: Option[ValpasOpiskeluoikeusPerusopetusSuppeatTiedot],
  perusopetuksenJälkeinenTiedot: Option[ValpasOpiskeluoikeusPerusopetuksenJälkeinenSuppeatTiedot],
  päätasonSuoritukset: Seq[ValpasPäätasonSuoritus],
  // Option, koska tämä tieto rikastetaan mukaan vain tietyissä tilanteissa
  onTehtyIlmoitus: Option[Boolean],
) extends ValpasOpiskeluoikeus

case class ValpasPäätasonSuoritus(
  toimipiste: ValpasToimipiste,
  ryhmä: Option[String],
  @KoodistoUri("suorituksentyyppi")
  suorituksenTyyppi: Koodistokoodiviite
)

object ValpasOppilaitos {
  type Oid = String
}

case class ValpasOppilaitos(
  oid: ValpasOppilaitos.Oid,
  nimi: LocalizedString
)

object ValpasToimipiste {
  type Oid = String
}

case class ValpasToimipiste(
  oid: ValpasToimipiste.Oid,
  nimi: LocalizedString
)

trait ValpasHakutilanne {
  def hakuOid: String
  def hakuNimi: Option[LocalizedString]
  def hakemusOid: String
  def hakemusUrl: String
  def aktiivinenHaku: Boolean
}

object ValpasHakutilanneLaajatTiedot {
  type HakuOid = String
  type HakemusOid = String

  def apply(hakukooste: Hakukooste): ValpasHakutilanneLaajatTiedot =
    ValpasHakutilanneLaajatTiedot(
      hakuOid = hakukooste.hakuOid,
      hakuNimi = hakukooste.hakuNimi.toLocalizedString,
      hakemusOid = hakukooste.hakemusOid,
      hakemusUrl = hakukooste.hakemusUrl,
      aktiivinenHaku = hakukooste.aktiivinenHaku.getOrElse(true),
      hakuAlkaa = hakukooste.haunAlkamispaivamaara,
      muokattu = hakukooste.hakemuksenMuokkauksenAikaleima,
      hakutoiveet = hakukooste.hakutoiveet.sortBy(_.hakutoivenumero).map(ValpasHakutoive.apply),
      debugHakukooste = Some(hakukooste)
    )
}

case class ValpasHakutilanneLaajatTiedot(
  hakuOid: String,
  hakuNimi: Option[LocalizedString],
  hakemusOid: String,
  hakemusUrl: String,
  aktiivinenHaku: Boolean,
  hakuAlkaa: LocalDateTime,
  muokattu: Option[LocalDateTime],
  hakutoiveet: Seq[ValpasHakutoive],
  debugHakukooste: Option[Hakukooste]
) extends ValpasHakutilanne {
  def validate(koodistoviitepalvelu: KoodistoViitePalvelu): ValpasHakutilanneLaajatTiedot =
    this.copy(hakutoiveet = hakutoiveet.map(_.validate(koodistoviitepalvelu)))
}

object ValpasHakutilanneSuppeatTiedot {
  def apply(laajatTiedot: ValpasHakutilanneLaajatTiedot): ValpasHakutilanneSuppeatTiedot = {
    ValpasHakutilanneSuppeatTiedot(
      laajatTiedot.hakuOid,
      laajatTiedot.hakuNimi,
      laajatTiedot.hakemusOid,
      laajatTiedot.hakemusUrl,
      laajatTiedot.aktiivinenHaku,
      laajatTiedot.hakutoiveet.map(ValpasSuppeaHakutoive.apply),
      laajatTiedot.muokattu,
    )
  }
}

case class ValpasHakutilanneSuppeatTiedot(
  hakuOid: String,
  hakuNimi: Option[LocalizedString],
  hakemusOid: String,
  hakemusUrl: String,
  aktiivinenHaku: Boolean,
  hakutoiveet: Seq[ValpasSuppeaHakutoive],
  muokattu: Option[LocalDateTime],
) extends ValpasHakutilanne

object ValpasHakutoive {
  type KoulutusOid = String

  def apply(hakutoive: Hakutoive): ValpasHakutoive = {
    ValpasHakutoive(
      hakukohdeNimi = hakutoive.hakukohdeNimi.toLocalizedString,
      organisaatioNimi = hakutoive.organisaatioNimi.toLocalizedString,
      koulutusNimi = hakutoive.koulutusNimi.toLocalizedString,
      hakutoivenumero = Some(hakutoive.hakutoivenumero),
      pisteet = hakutoive.pisteet,
      alinHyvaksyttyPistemaara = hakutoive.alinHyvaksyttyPistemaara,
      valintatila = Valintatila.valpasKoodiviiteOption(hakutoive.valintatila),
      vastaanottotieto = Vastaanottotieto.valpasKoodiviiteOption(hakutoive.vastaanottotieto),
      varasijanumero = hakutoive.varasijanumero,
      harkinnanvarainen = hakutoive.harkinnanvaraisuus.exists(Harkinnanvaraisuus.isHarkinnanvarainen),
    )
  }
}

case class ValpasHakutoive(
  hakukohdeNimi: Option[LocalizedString],
  organisaatioNimi: Option[LocalizedString],
  koulutusNimi: Option[LocalizedString],
  hakutoivenumero: Option[Int],
  pisteet: Option[BigDecimal],
  alinHyvaksyttyPistemaara: Option[BigDecimal],
  @KoodistoUri("valpashaunvalintatila")
  valintatila: Option[Koodistokoodiviite],
  @KoodistoUri("valpasvastaanottotieto")
  vastaanottotieto: Option[Koodistokoodiviite],
  varasijanumero: Option[Int],
  harkinnanvarainen: Boolean,
) {
  def validate(koodistoviitepalvelu: KoodistoViitePalvelu): ValpasHakutoive =
    this.copy(valintatila = valintatila.flatMap(koodistoviitepalvelu.validate))
}

object ValpasSuppeaHakutoive {
  def apply(hakutoive: ValpasHakutoive): ValpasSuppeaHakutoive = ValpasSuppeaHakutoive(
    organisaatioNimi = hakutoive.organisaatioNimi,
    hakutoivenumero = hakutoive.hakutoivenumero,
    valintatila = hakutoive.valintatila,
    vastaanottotieto = hakutoive.vastaanottotieto,
  )
}

case class ValpasSuppeaHakutoive(
  organisaatioNimi: Option[LocalizedString],
  hakutoivenumero: Option[Int],
  @KoodistoUri("valpashaunvalintatila")
  valintatila: Option[Koodistokoodiviite],
  @KoodistoUri("valpasvastaanottotieto")
  vastaanottotieto: Option[Koodistokoodiviite],
)
