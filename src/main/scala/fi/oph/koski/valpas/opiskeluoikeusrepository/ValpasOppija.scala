package fi.oph.koski.valpas.opiskeluoikeusrepository

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.hakukooste.{Hakukooste, Hakutoive, Harkinnanvaraisuus, Valintatila, Vastaanottotieto}
import fi.oph.scalaschema.annotation.SyntheticProperty

import java.time.{LocalDate, LocalDateTime}

trait ValpasOppija {
  def henkilö: ValpasHenkilö

  def opiskeluoikeudet: Seq[ValpasOpiskeluoikeus]

  @SyntheticProperty
  def opiskelee: Boolean = opiskeluoikeudet.exists(_.isOpiskelu)

  @SyntheticProperty
  def oppivelvollisuusVoimassaAsti: Option[LocalDate] = henkilö.syntymäaika.map(_.plusYears(18))
}

case class ValpasOppijaLaajatTiedot(
  henkilö: ValpasHenkilöLaajatTiedot,
  oikeutetutOppilaitokset: Set[ValpasOppilaitos.Oid],
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
) extends ValpasOppija

object ValpasOppijaSuppeatTiedot {
  def apply(laajatTiedot: ValpasOppijaLaajatTiedot): ValpasOppijaSuppeatTiedot = {
    ValpasOppijaSuppeatTiedot(
      ValpasHenkilöSuppeatTiedot(laajatTiedot.henkilö),
      laajatTiedot.opiskeluoikeudet.map(ValpasOpiskeluoikeusSuppeatTiedot.apply)
    )
  }
}

case class ValpasOppijaSuppeatTiedot(
  henkilö: ValpasHenkilöSuppeatTiedot,
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeusSuppeatTiedot]
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
  kaikkiOidit: Seq[ValpasHenkilö.Oid],
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
      laajatTiedot.syntymäaika,
      laajatTiedot.etunimet,
      laajatTiedot.sukunimi
    )
  }
}

case class ValpasHenkilöSuppeatTiedot(
  oid: ValpasHenkilö.Oid,
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String
) extends ValpasHenkilö

object ValpasOpiskeluoikeus {
  type Oid = String
}

trait ValpasOpiskeluoikeus {
  def oid: ValpasOpiskeluoikeus.Oid

  def onValvottava: Boolean

  @KoodistoUri("opiskeluoikeudentyyppi")
  def tyyppi: Koodistokoodiviite

  def oppilaitos: ValpasOppilaitos

  def toimipiste: Option[ValpasToimipiste]

  def ryhmä: Option[String]

  @KoodistoUri("valpasopiskeluoikeudentila")
  def tarkastelupäivänTila: Koodistokoodiviite

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

case class ValpasOpiskeluoikeusLaajatTiedot(
  oid: ValpasOpiskeluoikeus.Oid,
  onValvottava: Boolean,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  toimipiste: Option[ValpasToimipiste],
  alkamispäivä: String,
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  ryhmä: Option[String],
  tarkastelupäivänTila: Koodistokoodiviite,
  oppivelvollisuudenSuorittamiseenKelpaava: Boolean,
  näytettäväPerusopetuksenSuoritus: Boolean,
) extends ValpasOpiskeluoikeus

object ValpasOpiskeluoikeusSuppeatTiedot {
  def apply(laajatTiedot: ValpasOpiskeluoikeusLaajatTiedot): ValpasOpiskeluoikeusSuppeatTiedot = {
    ValpasOpiskeluoikeusSuppeatTiedot(
      oid = laajatTiedot.oid,
      onValvottava = laajatTiedot.onValvottava,
      tyyppi = laajatTiedot.tyyppi,
      oppilaitos = laajatTiedot.oppilaitos,
      toimipiste = laajatTiedot.toimipiste,
      ryhmä = laajatTiedot.ryhmä,
      tarkastelupäivänTila = laajatTiedot.tarkastelupäivänTila,
      alkamispäivä = laajatTiedot.alkamispäivä,
      päättymispäivä = laajatTiedot.päättymispäivä,
      päättymispäiväMerkittyTulevaisuuteen = laajatTiedot.päättymispäiväMerkittyTulevaisuuteen,
      oppivelvollisuudenSuorittamiseenKelpaava = laajatTiedot.oppivelvollisuudenSuorittamiseenKelpaava,
      näytettäväPerusopetuksenSuoritus = laajatTiedot.näytettäväPerusopetuksenSuoritus,
    )
  }
}

case class ValpasOpiskeluoikeusSuppeatTiedot(
  oid: ValpasOpiskeluoikeus.Oid,
  onValvottava: Boolean,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  toimipiste: Option[ValpasToimipiste],
  ryhmä: Option[String],
  tarkastelupäivänTila: Koodistokoodiviite,
  alkamispäivä: String,
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  oppivelvollisuudenSuorittamiseenKelpaava: Boolean,
  näytettäväPerusopetuksenSuoritus: Boolean,
) extends ValpasOpiskeluoikeus

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
