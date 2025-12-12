package fi.oph.koski.valpas.massaluovutus

import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString, Organisaatio, OrganisaatioWithOid}

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import fi.oph.koski.valpas.oppija.ValpasKuntailmoitusSuppeatTiedot
import fi.oph.koski.valpas.rouhinta.{RouhintaOpiskeluoikeus, ValpasRouhintaOppivelvollinen}
import fi.oph.koski.valpas.valpasrepository.ValpasOppivelvollisuudenKeskeytys

case class ValpasMassaluovutusOppija(
  oppijanumero: ValpasHenkilö.Oid,
  kaikkiOidit: Option[Seq[ValpasHenkilö.Oid]],
  etunimet: String,
  sukunimi: String,
  syntymäaika: Option[LocalDate],
  hetu: Option[String],
  aktiivisetOppivelvollisuudenSuorittamiseenKelpaavatOpiskeluoikeudet: Option[Seq[ValpasMassaluovutusOpiskeluoikeus]] = None,
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[ValpasMassaluovutusOpiskeluoikeus],
  oppivelvollisuudenKeskeytys: Seq[ValpasMassaluovutusOppivelvollisuudenKeskeytys],
  vainOppijanumerorekisterissä: Boolean,
  aktiivinenKuntailmoitus: Option[ValpasMassaluovutusKuntailmoitus],
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[LocalDate],
  kotikuntaSuomessaAlkaen: Option[LocalDate]
)

object ValpasMassaluovutusOppija {
  def apply(oppivelvollinen: ValpasRouhintaOppivelvollinen): ValpasMassaluovutusOppija = {
    ValpasMassaluovutusOppija(
      oppijanumero = oppivelvollinen.oppijanumero,
      kaikkiOidit = oppivelvollinen.kaikkiOidit,
      etunimet = oppivelvollinen.etunimet,
      sukunimi = oppivelvollinen.sukunimi,
      syntymäaika = oppivelvollinen.syntymäaika,
      hetu = oppivelvollinen.hetu,
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus =
        oppivelvollinen.viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus.map(ValpasMassaluovutusOpiskeluoikeus.apply),
      oppivelvollisuudenKeskeytys = oppivelvollinen.oppivelvollisuudenKeskeytys.map(ValpasMassaluovutusOppivelvollisuudenKeskeytys.apply),
      vainOppijanumerorekisterissä = oppivelvollinen.vainOppijanumerorekisterissä,
      aktiivinenKuntailmoitus = oppivelvollinen.aktiivinenKuntailmoitus.map(ValpasMassaluovutusKuntailmoitus.apply),
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
      kotikuntaSuomessaAlkaen = None
    )
  }

  def apply(oppivelvollinen: ValpasRouhintaOppivelvollinen, aktiivisetOpiskeluoikeudet: Seq[RouhintaOpiskeluoikeus]): ValpasMassaluovutusOppija = {
    ValpasMassaluovutusOppija(
      oppijanumero = oppivelvollinen.oppijanumero,
      kaikkiOidit = oppivelvollinen.kaikkiOidit,
      etunimet = oppivelvollinen.etunimet,
      sukunimi = oppivelvollinen.sukunimi,
      syntymäaika = oppivelvollinen.syntymäaika,
      hetu = oppivelvollinen.hetu,
      aktiivisetOppivelvollisuudenSuorittamiseenKelpaavatOpiskeluoikeudet =
        Some(aktiivisetOpiskeluoikeudet.map(ValpasMassaluovutusOpiskeluoikeus.apply)),
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus =
        oppivelvollinen.viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus.map(ValpasMassaluovutusOpiskeluoikeus.apply),
      oppivelvollisuudenKeskeytys = oppivelvollinen.oppivelvollisuudenKeskeytys.map(ValpasMassaluovutusOppivelvollisuudenKeskeytys.apply),
      vainOppijanumerorekisterissä = oppivelvollinen.vainOppijanumerorekisterissä,
      aktiivinenKuntailmoitus = oppivelvollinen.aktiivinenKuntailmoitus.map(ValpasMassaluovutusKuntailmoitus.apply),
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
      kotikuntaSuomessaAlkaen = None
    )
  }
}

case class ValpasMassaluovutusOpiskeluoikeus(
  suorituksenTyyppi: Koodistokoodiviite,
  koulutusmoduulinTunniste: String,
  päättymispäivä: Option[String],
  viimeisinValpasTila: Koodistokoodiviite,
  viimeisinTila: Koodistokoodiviite,
  toimipiste: LocalizedString,
)

object ValpasMassaluovutusOpiskeluoikeus {
  def apply(oo: RouhintaOpiskeluoikeus): ValpasMassaluovutusOpiskeluoikeus = ValpasMassaluovutusOpiskeluoikeus(
    suorituksenTyyppi = oo.suorituksenTyyppi,
    koulutusmoduulinTunniste = oo.koulutusmoduulinTunniste,
    päättymispäivä = oo.päättymispäivä,
    viimeisinValpasTila = oo.viimeisinValpasTila,
    viimeisinTila = oo.viimeisinTila,
    toimipiste = oo.toimipiste
  )
}

case class ValpasMassaluovutusOppivelvollisuudenKeskeytys(
  id: String,
  tekijäOrganisaatioOid: Organisaatio.Oid,
  alku: LocalDate,
  loppu: Option[LocalDate],
  voimassa: Boolean,
  tulevaisuudessa: Boolean,
)

object ValpasMassaluovutusOppivelvollisuudenKeskeytys {
  def apply(keskeytys: ValpasOppivelvollisuudenKeskeytys): ValpasMassaluovutusOppivelvollisuudenKeskeytys = ValpasMassaluovutusOppivelvollisuudenKeskeytys(
    id = keskeytys.id,
    tekijäOrganisaatioOid = keskeytys.tekijäOrganisaatioOid,
    alku = keskeytys.alku,
    loppu = keskeytys.loppu,
    voimassa = keskeytys.voimassa,
    tulevaisuudessa = keskeytys.tulevaisuudessa
  )
}

case class ValpasMassaluovutusKuntailmoitus(
  oppijaOid: Option[String],
  id: Option[String], // Oikeasti UUID - scala-schemasta puuttuu tuki UUID-tyypille
  tekijä: ValpasMassaluovutusKuntailmoituksenTekijä,
  kunta: OrganisaatioWithOid,
  aikaleima: Option[LocalDateTime],
  hakenutMuualle: Option[Boolean],
  onUudempiaIlmoituksiaMuihinKuntiin: Option[Boolean],
  aktiivinen: Option[Boolean],
)

object ValpasMassaluovutusKuntailmoitus {
  def apply(ilmoitus: ValpasKuntailmoitusSuppeatTiedot): ValpasMassaluovutusKuntailmoitus = ValpasMassaluovutusKuntailmoitus(
    oppijaOid = ilmoitus.oppijaOid,
    id = ilmoitus.id,
    tekijä = ValpasMassaluovutusKuntailmoituksenTekijä(ilmoitus.tekijä.organisaatio),
    kunta = ilmoitus.kunta,
    aikaleima = ilmoitus.aikaleima,
    hakenutMuualle = ilmoitus.hakenutMuualle,
    onUudempiaIlmoituksiaMuihinKuntiin = ilmoitus.onUudempiaIlmoituksiaMuihinKuntiin,
    aktiivinen = ilmoitus.aktiivinen
  )
}

case class ValpasMassaluovutusKuntailmoituksenTekijä(
  organisaatio: OrganisaatioWithOid
)
