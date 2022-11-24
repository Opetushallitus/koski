package fi.oph.koski.valpas.kansalainen

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema.{Koodistokoodiviite, Maksuttomuus, OikeuttaMaksuttomuuteenPidennetty, OrganisaatioWithOid}
import fi.oph.koski.valpas.opiskeluoikeusrepository._
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoituksenOppijanYhteystiedot, ValpasKuntailmoituksenTekijäLaajatTiedot, ValpasKuntailmoitusLaajatTiedot, ValpasOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.yhteystiedot.ValpasYhteystiedot
import fi.oph.koski.valpas.oppija.{OpiskeluoikeusLisätiedot, OppijaHakutilanteillaLaajatTiedot}
import fi.oph.koski.valpas.oppivelvollisuudestavapautus.OppivelvollisuudestaVapautus

import java.time.{LocalDate, LocalDateTime}

case class KansalaisnäkymänTiedot(
  omatTiedot: Option[KansalainenOppijatiedot],
  huollettavat: Seq[KansalainenOppijatiedot],
  huollettavatIlmanTietoja: Seq[KansalainenOppijaIlmanTietoja],
)

case class KansalainenOppijatiedot(
  oppija: KansalainenOppija,
  hakutilanteet: Seq[ValpasHakutilanneLaajatTiedot],
  hakutilanneError: Option[String],
  yhteystiedot: Seq[ValpasYhteystiedot],
  kuntailmoitukset: Seq[KansalainenKuntailmoitus],
  oppivelvollisuudenKeskeytykset: Seq[ValpasOppivelvollisuudenKeskeytys],
  lisätiedot: Seq[OpiskeluoikeusLisätiedot],
  oppivelvollisuudestaVapautus: Option[OppivelvollisuudestaVapautus],
) {
  def poistaTurvakiellonAlaisetTiedot: KansalainenOppijatiedot =
    this.copy(
      oppija = oppija.poistaTurvakiellonAlaisetTiedot,
      hakutilanteet = Seq.empty,
      hakutilanneError = None,
      yhteystiedot = Seq.empty,
      kuntailmoitukset = kuntailmoitukset.map(_.poistaTurvakiellonAlaisetTiedot),
      oppivelvollisuudestaVapautus = oppivelvollisuudestaVapautus.map(_.poistaTurvakiellonAlaisetTiedot),
    )
}

object KansalainenOppijatiedot {
  def apply(tiedot: OppijaHakutilanteillaLaajatTiedot): KansalainenOppijatiedot = KansalainenOppijatiedot(
    oppija = KansalainenOppija(tiedot.oppija),
    hakutilanteet = tiedot.hakutilanteet,
    hakutilanneError = tiedot.hakutilanneError,
    yhteystiedot = tiedot.yhteystiedot,
    kuntailmoitukset = tiedot.kuntailmoitukset.map(KansalainenKuntailmoitus.apply),
    oppivelvollisuudenKeskeytykset = tiedot.oppivelvollisuudenKeskeytykset,
    lisätiedot = tiedot.lisätiedot,
    oppivelvollisuudestaVapautus = tiedot.oppija.oppivelvollisuudestaVapautus,
  )
}

case class KansalainenOppija(
  henkilö: ValpasHenkilöLaajatTiedot,
  opiskeluoikeudet: Seq[KansalainenOpiskeluoikeus],
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: LocalDate,
  opiskelee: Boolean,
) {
  def poistaTurvakiellonAlaisetTiedot: KansalainenOppija =
    this.copy(
      henkilö = henkilö.copy(kotikunta = None),
      opiskeluoikeudet = opiskeluoikeudet.map(_.poistaTurvakiellonAlaisetTiedot),
    )
}

object KansalainenOppija {
  def apply(oppija: ValpasOppijaLaajatTiedot): KansalainenOppija = {
    KansalainenOppija(
      henkilö = oppija.henkilö,
      opiskeluoikeudet = oppija.opiskeluoikeudet.map(KansalainenOpiskeluoikeus.apply),
      oppivelvollisuusVoimassaAsti = oppija.oppivelvollisuusVoimassaAsti,
      oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti,
      opiskelee = oppija.opiskelee,
    )
  }
}

case class KansalainenOpiskeluoikeus(
  oid: ValpasOpiskeluoikeus.Oid,
  tyyppi: Option[Koodistokoodiviite],
  oppilaitos: Option[ValpasOppilaitos],
  oppivelvollisuudenSuorittamiseenKelpaava: Boolean,
  perusopetusTiedot: Option[ValpasOpiskeluoikeusPerusopetusTiedot],
  perusopetuksenJälkeinenTiedot: Option[ValpasOpiskeluoikeusTiedot],
  muuOpetusTiedot: Option[ValpasOpiskeluoikeusTiedot],
  päätasonSuoritukset: Seq[ValpasPäätasonSuoritus],
  tarkasteltavaPäätasonSuoritus: Option[ValpasPäätasonSuoritus],
  maksuttomuus: Option[Seq[Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[Seq[OikeuttaMaksuttomuuteenPidennetty]],
) {
  def poistaTurvakiellonAlaisetTiedot: KansalainenOpiskeluoikeus =
    this.copy(
      tyyppi = None,
      oppilaitos = None,
      päätasonSuoritukset = Seq.empty,
      tarkasteltavaPäätasonSuoritus = None,
    )
}

object KansalainenOpiskeluoikeus {
  def apply(oo: ValpasOpiskeluoikeusLaajatTiedot): KansalainenOpiskeluoikeus = KansalainenOpiskeluoikeus(
    oid = oo.oid,
    tyyppi = Some(oo.tyyppi),
    oppilaitos = Some(oo.oppilaitos),
    oppivelvollisuudenSuorittamiseenKelpaava = oo.oppivelvollisuudenSuorittamiseenKelpaava,
    perusopetusTiedot = oo.perusopetusTiedot,
    perusopetuksenJälkeinenTiedot = oo.perusopetuksenJälkeinenTiedot,
    muuOpetusTiedot = oo.muuOpetusTiedot,
    päätasonSuoritukset = oo.päätasonSuoritukset,
    tarkasteltavaPäätasonSuoritus = oo.tarkasteltavaPäätasonSuoritus,
    maksuttomuus = oo.maksuttomuus,
    oikeuttaMaksuttomuuteenPidennetty = oo.oikeuttaMaksuttomuuteenPidennetty,
  )
}

case class KansalainenKuntailmoitus(
  kunta: Option[OrganisaatioWithOid],
  aikaleima: LocalDateTime,
  tekijä: Option[KansalainenKuntailmoituksenTekijä],
  @KoodistoUri("kieli")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("SV")
  yhteydenottokieli: Option[Koodistokoodiviite],
  oppijanYhteystiedot: Option[ValpasKuntailmoituksenOppijanYhteystiedot],
  hakenutMuualle: Option[Boolean],
  aktiivinen: Option[Boolean],
  tietojaKarsittu: Option[Boolean],
) {
  def poistaTurvakiellonAlaisetTiedot: KansalainenKuntailmoitus =
    this.copy(
      kunta = None,
      tekijä = None,
      yhteydenottokieli = None,
      oppijanYhteystiedot = None,
      hakenutMuualle = None,
      tietojaKarsittu = Some(true),
    )
}

object KansalainenKuntailmoitus {
  def apply(ilmoitus: ValpasKuntailmoitusLaajatTiedot): KansalainenKuntailmoitus =
    KansalainenKuntailmoitus(
      kunta = Some(ilmoitus.kunta),
      aikaleima = ilmoitus.aikaleima.get,
      tekijä = Some(KansalainenKuntailmoituksenTekijä(ilmoitus.tekijä)),
      yhteydenottokieli = ilmoitus.yhteydenottokieli,
      oppijanYhteystiedot = ilmoitus.oppijanYhteystiedot,
      hakenutMuualle = ilmoitus.hakenutMuualle,
      aktiivinen = ilmoitus.aktiivinen,
      tietojaKarsittu = ilmoitus.tietojaKarsittu,
    )
}

case class KansalainenKuntailmoituksenTekijä(
  organisaatio: OrganisaatioWithOid,
)

object KansalainenKuntailmoituksenTekijä {
  def apply(tekijä: ValpasKuntailmoituksenTekijäLaajatTiedot): KansalainenKuntailmoituksenTekijä =
    KansalainenKuntailmoituksenTekijä(
      organisaatio = tekijä.organisaatio,
    )
}

case class KansalainenOppijaIlmanTietoja(
  nimi: String,
  hetu: Option[String],
)
