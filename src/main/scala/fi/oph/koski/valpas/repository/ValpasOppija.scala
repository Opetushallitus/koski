package fi.oph.koski.valpas.repository

import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.hakukooste.{Hakukooste, Hakutoive}
import fi.oph.scalaschema.annotation.SyntheticProperty

import java.time.{LocalDate, LocalDateTime}

case class ValpasOppija(
  henkilö: ValpasHenkilö,
  oikeutetutOppilaitokset: Set[ValpasOppilaitos.Oid],
  valvottavatOpiskeluoikeudet: Set[ValpasOpiskeluoikeus.Oid],
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeus]
) {
  @SyntheticProperty
  def opiskelee: Boolean = opiskeluoikeudet.exists(_.isOpiskelu)

  @SyntheticProperty
  def oppivelvollisuusVoimassaAsti: Option[LocalDate] = henkilö.syntymäaika.map(_.plusYears(18))
}

object ValpasHenkilö {
  type Oid = String
}

case class ValpasHenkilö(
  oid: ValpasHenkilö.Oid,
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  turvakielto: Boolean
)

object ValpasOpiskeluoikeus {
  type Oid = String
}

case class ValpasOpiskeluoikeus(
  oid: ValpasOpiskeluoikeus.Oid,
  @KoodistoUri("opiskeluoikeudentyyppi")
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  toimipiste: Option[ValpasToimipiste],
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  ryhmä: Option[String],
  @KoodistoUri("koskiopiskeluoikeudentila")
  viimeisinTila: Koodistokoodiviite,
  @KoodistoUri("valpasopiskeluoikeudentila")
  tarkastelupäivänTila: Koodistokoodiviite
) {
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

object ValpasHakutilanne {
  type HakuOid = String
  type HakemusOid = String

  def apply(hakukooste: Hakukooste): ValpasHakutilanne =
    ValpasHakutilanne(
      hakuOid = hakukooste.hakuOid,
      hakuNimi = hakukooste.hakuNimi.toLocalizedString,
      hakemusOid = hakukooste.hakemusOid,
      aktiivinen = hakukooste.hakutoiveet.exists(_.isAktiivinen),
      hakuAlkaa = hakukooste.haunAlkamispaivamaara,
      hakutoiveet = hakukooste.hakutoiveet.map(ValpasHakutoive.apply),
    )
}

case class ValpasHakutilanne(
  hakuOid: String,
  hakuNimi: Option[LocalizedString],
  hakemusOid: String,
  aktiivinen: Boolean,
  hakuAlkaa: LocalDateTime,
  hakutoiveet: Seq[ValpasHakutoive],
)

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

