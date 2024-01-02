package fi.oph.koski.valpas.oppija

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.util.DateOrdering.localDateTimeOrdering
import fi.oph.koski.valpas.db.ValpasSchema.OpiskeluoikeusLisätiedotRow
import fi.oph.koski.valpas.hakukooste.Hakukooste
import fi.oph.koski.valpas.opiskeluoikeusrepository._
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoitusLaajatTiedot, ValpasOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.yhteystiedot.ValpasYhteystiedot

case class OppijaHakutilanteillaLaajatTiedot(
  oppija: ValpasOppijaLaajatTiedot,
  hakutilanteet: Seq[ValpasHakutilanneLaajatTiedot],
  hakutilanneError: Option[String],
  yhteystiedot: Seq[ValpasYhteystiedot],
  kuntailmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot],
  oppivelvollisuudenKeskeytykset: Seq[ValpasOppivelvollisuudenKeskeytys],
  onOikeusTehdäKuntailmoitus: Option[Boolean],
  onOikeusMitätöidäOppivelvollisuudestaVapautus: Option[Boolean],
  lisätiedot: Seq[OpiskeluoikeusLisätiedot],
) {
  def validate(koodistoviitepalvelu: KoodistoViitePalvelu): OppijaHakutilanteillaLaajatTiedot =
    this.copy(hakutilanteet = hakutilanteet.map(_.validate(koodistoviitepalvelu)))

  def withLisätiedot(lisätiedot: Seq[OpiskeluoikeusLisätiedotRow]): OppijaHakutilanteillaLaajatTiedot = {
    this.copy(
      lisätiedot = lisätiedot.map(l => OpiskeluoikeusLisätiedot(
        oppijaOid = l.oppijaOid,
        opiskeluoikeusOid = l.opiskeluoikeusOid,
        oppilaitosOid = l.oppilaitosOid,
        muuHaku = l.muuHaku
      ))
    )
  }

  def withEiKatseltavanMinimitiedot: OppijaHakutilanteillaLaajatTiedot = copy(
    oppija = oppija.withEiKatseltavanMinimitiedot,
    hakutilanteet = Seq.empty,
    hakutilanneError = None,
    yhteystiedot = Seq.empty,
    kuntailmoitukset = Seq.empty,
    oppivelvollisuudenKeskeytykset = Seq.empty,
    onOikeusTehdäKuntailmoitus = Some(false),
    onOikeusMitätöidäOppivelvollisuudestaVapautus = Some(false),
    lisätiedot = Seq.empty,
  )
}

object OppijaHakutilanteillaLaajatTiedot {
  def apply(oppija: ValpasOppijaLaajatTiedot, yhteystietoryhmänNimi: LocalizedString, haut: Either[HttpStatus, Seq[Hakukooste]]): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot(
      oppija = oppija,
      hakutilanteet = haut.map(_.map(ValpasHakutilanneLaajatTiedot.apply)).getOrElse(Seq()),
      // TODO: Pitäisikö virheet mankeloida jotenkin eikä palauttaa sellaisenaan fronttiin?
      hakutilanneError = haut.left.toOption.flatMap(_.errorString),
      yhteystiedot = haut.map(uusimmatIlmoitetutYhteystiedot(yhteystietoryhmänNimi)).getOrElse(Seq.empty),
      kuntailmoitukset = Seq.empty,
      oppivelvollisuudenKeskeytykset = Seq.empty,
      onOikeusTehdäKuntailmoitus = None,
      onOikeusMitätöidäOppivelvollisuudestaVapautus = None,
      lisätiedot = Seq.empty
    )
  }

  def apply(oppija: ValpasOppijaLaajatTiedot, kuntailmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot]): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot(
      oppija = oppija,
      hakutilanteet = Seq.empty,
      hakutilanneError = None,
      yhteystiedot = Seq.empty,
      kuntailmoitukset = kuntailmoitukset,
      oppivelvollisuudenKeskeytykset = Seq.empty,
      onOikeusTehdäKuntailmoitus = None,
      onOikeusMitätöidäOppivelvollisuudestaVapautus = None,
      lisätiedot = Seq.empty
    )
  }

  def apply(oppija: ValpasOppijaLaajatTiedot): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot(
      oppija = oppija,
      hakutilanteet = Seq.empty,
      hakutilanneError = None,
      yhteystiedot = Seq.empty,
      kuntailmoitukset = Seq.empty,
      oppivelvollisuudenKeskeytykset = Seq.empty,
      onOikeusTehdäKuntailmoitus = None,
      onOikeusMitätöidäOppivelvollisuudestaVapautus = None,
      lisätiedot = Seq.empty
    )
  }

  private def uusimmatIlmoitetutYhteystiedot(yhteystietoryhmänNimi: LocalizedString)(hakukoosteet: Seq[Hakukooste]): Seq[ValpasYhteystiedot] =
    hakukoosteet
      .sortBy(hk => hk.hakemuksenMuokkauksenAikaleima.getOrElse(hk.haunAlkamispaivamaara))
      .lastOption
      .map(haku => List(
        ValpasYhteystiedot.oppijanIlmoittamatYhteystiedot(haku, yhteystietoryhmänNimi),
      ))
      .getOrElse(List.empty)
}

case class OpiskeluoikeusLisätiedot(
  oppijaOid: ValpasHenkilö.Oid,
  opiskeluoikeusOid: ValpasOpiskeluoikeus.Oid,
  oppilaitosOid: ValpasOppilaitos.Oid,
  muuHaku: Boolean
)
