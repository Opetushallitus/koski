package fi.oph.koski.valpas.massaluovutus

import fi.oph.koski.valpas.oppija.ValpasKuntailmoitusSuppeatTiedot
import fi.oph.koski.valpas.rouhinta.{RouhintaOpiskeluoikeus, ValpasRouhintaOppivelvollinen}
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoituksenOppijanYhteystiedot, ValpasOppivelvollisuudenKeskeytys}

object ValpasMassaluovutusConverter {

  def toOppivelvollinenOppija(
    oppivelvollinen: ValpasRouhintaOppivelvollinen,
    aktiivisetOpiskeluoikeudet: Seq[RouhintaOpiskeluoikeus]
  ): ValpasMassaluovutusOppivelvollinenOppija = ValpasMassaluovutusOppivelvollinenOppija(
    oppijanumero = oppivelvollinen.oppijanumero,
    kaikkiOidit = oppivelvollinen.kaikkiOidit,
    etunimet = oppivelvollinen.etunimet,
    sukunimi = oppivelvollinen.sukunimi,
    syntymäaika = oppivelvollinen.syntymäaika,
    hetu = oppivelvollinen.hetu,
    aktiivisetOppivelvollisuudenSuorittamiseenKelpaavatOpiskeluoikeudet = aktiivisetOpiskeluoikeudet
      .map(toOpiskeluoikeus),
    viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus =
      oppivelvollinen
        .viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus
        .map(toOpiskeluoikeus),
    oppivelvollisuudenKeskeytys = oppivelvollinen
      .oppivelvollisuudenKeskeytys
      .map(toOppivelvollisuudenKeskeytys),
    vainOppijanumerorekisterissä = oppivelvollinen.vainOppijanumerorekisterissä,
    aktiivinenKuntailmoitus = oppivelvollinen.aktiivinenKuntailmoitus.map(
      toKuntailmoitus(_, oppivelvollinen.aktiivisenKuntailmoituksenOppijanYhteystiedot)
    ),
    oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
    kotikuntaSuomessaAlkaen = None
  )

  def toEiOppivelvollisuuttaSuorittavaOppija(
    oppivelvollinen: ValpasRouhintaOppivelvollinen
  ): ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija =
    ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija(
      oppijanumero = oppivelvollinen.oppijanumero,
      kaikkiOidit = oppivelvollinen.kaikkiOidit,
      etunimet = oppivelvollinen.etunimet,
      sukunimi = oppivelvollinen.sukunimi,
      syntymäaika = oppivelvollinen.syntymäaika,
      hetu = oppivelvollinen.hetu,
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus =
        oppivelvollinen
          .viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus
          .map(toOpiskeluoikeus),
      oppivelvollisuudenKeskeytys = oppivelvollinen
        .oppivelvollisuudenKeskeytys
        .map(toOppivelvollisuudenKeskeytys),
      vainOppijanumerorekisterissä = oppivelvollinen.vainOppijanumerorekisterissä,
      aktiivinenKuntailmoitus = oppivelvollinen.aktiivinenKuntailmoitus.map(
        toKuntailmoitus(_, oppivelvollinen.aktiivisenKuntailmoituksenOppijanYhteystiedot)
      ),
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
      kotikuntaSuomessaAlkaen = None
    )

  private def toOpiskeluoikeus(oo: RouhintaOpiskeluoikeus): ValpasMassaluovutusOpiskeluoikeus =
    ValpasMassaluovutusOpiskeluoikeus(
      suorituksenTyyppi = oo.suorituksenTyyppi,
      koulutusmoduulinTunniste = oo.koulutusmoduulinTunniste,
      päättymispäivä = oo.päättymispäivä,
      viimeisinValpasTila = oo.viimeisinValpasTila,
      viimeisinTila = oo.viimeisinTila,
      toimipiste = oo.toimipiste
    )

  private def toOppivelvollisuudenKeskeytys(
    keskeytys: ValpasOppivelvollisuudenKeskeytys
  ): ValpasMassaluovutusOppivelvollisuudenKeskeytys =
    ValpasMassaluovutusOppivelvollisuudenKeskeytys(
      id = keskeytys.id,
      tekijäOrganisaatioOid = keskeytys.tekijäOrganisaatioOid,
      alku = keskeytys.alku,
      loppu = keskeytys.loppu,
      voimassa = keskeytys.voimassa,
      tulevaisuudessa = keskeytys.tulevaisuudessa
    )

  private def toKuntailmoitus(
    ilmoitus: ValpasKuntailmoitusSuppeatTiedot,
    oppijanYhteystiedot: Option[ValpasKuntailmoituksenOppijanYhteystiedot]
  ): ValpasMassaluovutusKuntailmoitus =
    ValpasMassaluovutusKuntailmoitus(
      oppijaOid = ilmoitus.oppijaOid,
      id = ilmoitus.id,
      tekijä = ValpasMassaluovutusKuntailmoituksenTekijä(ilmoitus.tekijä.organisaatio),
      kunta = ilmoitus.kunta,
      aikaleima = ilmoitus.aikaleima,
      hakenutMuualle = ilmoitus.hakenutMuualle,
      onUudempiaIlmoituksiaMuihinKuntiin = ilmoitus.onUudempiaIlmoituksiaMuihinKuntiin,
      aktiivinen = ilmoitus.aktiivinen,
      oppijanYhteystiedot = oppijanYhteystiedot
    )
}
