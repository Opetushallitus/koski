package fi.oph.koski.valpas.repository

import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.hakukooste.{Hakukooste, Hakutoive}
import fi.oph.scalaschema.annotation.SyntheticProperty
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.valpas.repository.ValpasOpiskeluoikeus.Oid

trait ValpasOppija {
  def henkilö: ValpasHenkilö
  def valvottavatOpiskeluoikeudet: Set[ValpasOpiskeluoikeus.Oid]
  def opiskeluoikeudet: Seq[ValpasOpiskeluoikeus]
}

case class ValpasOppijaLaajatTiedot(
  henkilö: ValpasHenkilöLaajatTiedot,
  oikeutetutOppilaitokset: Set[ValpasOppilaitos.Oid],
  valvottavatOpiskeluoikeudet: Set[ValpasOpiskeluoikeus.Oid],
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot]
) extends ValpasOppija {
  @SyntheticProperty
  def opiskelee: Boolean = opiskeluoikeudet.exists(_.isOpiskelu)

  @SyntheticProperty
  def oppivelvollisuusVoimassaAsti: Option[LocalDate] = henkilö.syntymäaika.map(_.plusYears(18))
}

object ValpasOppijaSuppeatTiedot {
  def apply(laajatTiedot: ValpasOppijaLaajatTiedot): ValpasOppijaSuppeatTiedot = {
    ValpasOppijaSuppeatTiedot(
      ValpasHenkilöSuppeatTiedot(laajatTiedot.henkilö),
      laajatTiedot.valvottavatOpiskeluoikeudet,
      laajatTiedot.opiskeluoikeudet.map(ValpasOpiskeluoikeusSuppeatTiedot.apply)
    )
  }
}

case class ValpasOppijaSuppeatTiedot(
  henkilö: ValpasHenkilöSuppeatTiedot,
  valvottavatOpiskeluoikeudet: Set[ValpasOpiskeluoikeus.Oid],
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeusSuppeatTiedot]
) extends ValpasOppija {
  @SyntheticProperty
  def opiskelee: Boolean = opiskeluoikeudet.exists(_.isOpiskelu)

  @SyntheticProperty
  def oppivelvollisuusVoimassaAsti: Option[LocalDate] = henkilö.syntymäaika.map(_.plusYears(18))
}

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
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  turvakielto: Boolean
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
  @KoodistoUri("opiskeluoikeudentyyppi")
  def tyyppi: Koodistokoodiviite
  def oppilaitos: ValpasOppilaitos
  def toimipiste: Option[ValpasToimipiste]
  def ryhmä: Option[String]

  @KoodistoUri("valpasopiskeluoikeudentila")
  def tarkastelupäivänTila: Koodistokoodiviite
}

case class ValpasOpiskeluoikeusLaajatTiedot(
  oid: ValpasOpiskeluoikeus.Oid,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  toimipiste: Option[ValpasToimipiste],
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  ryhmä: Option[String],
  tarkastelupäivänTila: Koodistokoodiviite
) extends ValpasOpiskeluoikeus {
  @SyntheticProperty
  def isOpiskelu: Boolean =
    tarkastelupäivänTila.koodiarvo == "voimassa"
}

object ValpasOpiskeluoikeusSuppeatTiedot {
  def apply(laajatTiedot: ValpasOpiskeluoikeusLaajatTiedot): ValpasOpiskeluoikeusSuppeatTiedot = {
    ValpasOpiskeluoikeusSuppeatTiedot(
      laajatTiedot.oid,
      laajatTiedot.tyyppi,
      laajatTiedot.oppilaitos,
      laajatTiedot.toimipiste,
      laajatTiedot.ryhmä,
      laajatTiedot.tarkastelupäivänTila
    )
  }
}

case class ValpasOpiskeluoikeusSuppeatTiedot(
  oid: ValpasOpiskeluoikeus.Oid,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  toimipiste: Option[ValpasToimipiste],
  ryhmä: Option[String],
  tarkastelupäivänTila: Koodistokoodiviite
) extends ValpasOpiskeluoikeus {
  @SyntheticProperty
  def isOpiskelu: Boolean =
    tarkastelupäivänTila.koodiarvo == "voimassa"
}

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
  def aktiivinen: Boolean
}

object ValpasHakutilanneLaajatTiedot {
  type HakuOid = String
  type HakemusOid = String

  def apply(hakukooste: Hakukooste): ValpasHakutilanneLaajatTiedot =
    ValpasHakutilanneLaajatTiedot(
      hakuOid = hakukooste.hakuOid,
      hakuNimi = hakukooste.hakuNimi.toLocalizedString,
      hakemusOid = hakukooste.hakemusOid,
      aktiivinen = hakukooste.hakutoiveet.exists(_.isAktiivinen),
      hakuAlkaa = hakukooste.haunAlkamispaivamaara,
      hakutoiveet = hakukooste.hakutoiveet.map(ValpasHakutoive.apply),
    )
}

case class ValpasHakutilanneLaajatTiedot(
  hakuOid: String,
  hakuNimi: Option[LocalizedString],
  hakemusOid: String,
  aktiivinen: Boolean,
  hakuAlkaa: LocalDateTime,
  hakutoiveet: Seq[ValpasHakutoive]
) extends ValpasHakutilanne

object ValpasHakutilanneSuppeatTiedot {
  def apply(laajatTiedot: ValpasHakutilanneLaajatTiedot): ValpasHakutilanneSuppeatTiedot = {
    ValpasHakutilanneSuppeatTiedot(
      laajatTiedot.hakuOid,
      laajatTiedot.hakuNimi,
      laajatTiedot.hakemusOid,
      laajatTiedot.aktiivinen
    )
  }
}

case class ValpasHakutilanneSuppeatTiedot(
  hakuOid: String,
  hakuNimi: Option[LocalizedString],
  hakemusOid: String,
  aktiivinen: Boolean
) extends ValpasHakutilanne

object ValpasHakutoive {
  type KoulutusOid = String

  def apply(hakutoive: Hakutoive): ValpasHakutoive = {
    ValpasHakutoive(
      hakukohdeNimi = hakutoive.hakukohdeNimi.toLocalizedString,
      koulutusNimi = hakutoive.koulutusNimi.toLocalizedString,
      hakutoivenumero = Some(hakutoive.hakutoivenumero),
      pisteet = hakutoive.pisteet,
      minValintapisteet = hakutoive.alinValintaPistemaara
    )
  }
}

case class ValpasHakutoive(
  hakukohdeNimi: Option[LocalizedString],
  koulutusNimi: Option[LocalizedString],
  hakutoivenumero: Option[Int],
  pisteet: Option[BigDecimal],
  minValintapisteet: Option[BigDecimal]
) {
  @SyntheticProperty
  def hyväksytty: Option[Boolean] = (pisteet, minValintapisteet) match {
    case (Some(p), Some(min)) => Some(p >= min)
    case _ => None
  }
}

