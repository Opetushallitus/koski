package fi.oph.koski.valpas.opiskeluoikeusrepository

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString, Maksuttomuus, OikeuttaMaksuttomuuteenPidennetty}
import fi.oph.koski.valpas.hakukooste._
import fi.oph.scalaschema.annotation.SyntheticProperty

import java.time.{LocalDate, LocalDateTime}

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

  def muuOpetusTiedot: Option[ValpasOpiskeluoikeusTiedot]

  @SyntheticProperty
  def isOpiskelu: Boolean = opiskeluoikeustiedot.exists(_.isOpiskelu)

  @SyntheticProperty
  def isOpiskeluTulevaisuudessa: Boolean = opiskeluoikeustiedot.exists(_.isOpiskeluTulevaisuudessa)

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

  def alkamispäivä: Option[LocalDate] =
    opiskeluoikeustiedot
      .flatMap(_.alkamispäivä)
      .sorted
      .headOption
      .map(LocalDate.parse)

  def opiskeluoikeustiedot: Seq[ValpasOpiskeluoikeusTiedot] =
    Seq(
      perusopetusTiedot,
      perusopetuksenJälkeinenTiedot,
      muuOpetusTiedot,
    ).flatten
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
  muuOpetusTiedot: Option[ValpasOpiskeluoikeusMuuOpetusLaajatTiedot],
  päätasonSuoritukset: Seq[ValpasPäätasonSuoritus],
  // Option, koska tämä tieto rikastetaan mukaan vain tietyissä tilanteissa
  onTehtyIlmoitus: Option[Boolean],
  maksuttomuus: Option[Seq[Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[Seq[OikeuttaMaksuttomuuteenPidennetty]],
) extends ValpasOpiskeluoikeus

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

case class ValpasOpiskeluoikeusMuuOpetusLaajatTiedot(
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  tarkastelupäivänTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTilanAlkamispäivä: String,
  valmistunutAiemminTaiLähitulevaisuudessa: Boolean,
  näytäMuunaPerusopetuksenJälkeisenäOpintona: Option[Boolean],
) extends ValpasOpiskeluoikeusTiedot

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
