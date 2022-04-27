package fi.oph.koski.valpas

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema.{Henkilö, Koodistokoodiviite, LocalizedString, Organisaatio, OrganisaatioWithOid}
import fi.oph.koski.valpas.opiskeluoikeusrepository.{HakeutumisvalvontaTieto, ValpasHakutilanne, ValpasHakutilanneLaajatTiedot, ValpasHakutoive, ValpasHenkilö, ValpasHenkilöLaajatTiedot, ValpasOpiskeluoikeus, ValpasOpiskeluoikeusLaajatTiedot, ValpasOpiskeluoikeusMuuOpetusLaajatTiedot, ValpasOpiskeluoikeusPerusopetuksenJälkeinenLaajatTiedot, ValpasOpiskeluoikeusPerusopetusLaajatTiedot, ValpasOpiskeluoikeusPerusopetusTiedot, ValpasOpiskeluoikeusTiedot, ValpasOppija, ValpasOppijaLaajatTiedot, ValpasOppilaitos, ValpasPäätasonSuoritus}
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoituksenTekijäLaajatTiedot, ValpasKuntailmoitusLaajatTiedot, ValpasOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

case class OppijaHakutilanteillaSuppeatTiedot(
  oppija: ValpasOppijaSuppeatTiedot,
  hakutilanteet: Seq[ValpasHakutilanneSuppeatTiedot],
  hakutilanneError: Option[String],
  kuntailmoitukset: Seq[ValpasKuntailmoitusSuppeatTiedot],
  oppivelvollisuudenKeskeytykset: Seq[ValpasOppivelvollisuudenKeskeytys],
  lisätiedot: Seq[OpiskeluoikeusLisätiedot],
)

object OppijaHakutilanteillaSuppeatTiedot {
  def apply(laajatTiedot: OppijaHakutilanteillaLaajatTiedot)
  : OppijaHakutilanteillaSuppeatTiedot = {
    OppijaHakutilanteillaSuppeatTiedot(
      oppija = ValpasOppijaSuppeatTiedot(laajatTiedot.oppija),
      hakutilanteet = laajatTiedot.hakutilanteet.map(ValpasHakutilanneSuppeatTiedot.apply),
      hakutilanneError = laajatTiedot.hakutilanneError,
      kuntailmoitukset = laajatTiedot.kuntailmoitukset.map(ValpasKuntailmoitusSuppeatTiedot.apply),
      oppivelvollisuudenKeskeytykset = laajatTiedot.oppivelvollisuudenKeskeytykset,
      lisätiedot = laajatTiedot.lisätiedot,
    )
  }
}

case class OppijaKuntailmoituksillaSuppeatTiedot (
  oppija: ValpasOppijaSuppeatTiedot,
  kuntailmoitukset: Seq[ValpasKuntailmoitusSuppeatTiedot],
)

object OppijaKuntailmoituksillaSuppeatTiedot {
  def apply(laajatTiedot: OppijaHakutilanteillaLaajatTiedot)
  : OppijaKuntailmoituksillaSuppeatTiedot = {
    OppijaKuntailmoituksillaSuppeatTiedot(
      oppija = ValpasOppijaSuppeatTiedot(laajatTiedot.oppija),
      kuntailmoitukset = laajatTiedot.kuntailmoitukset.map(ValpasKuntailmoitusSuppeatTiedot.apply),
    )
  }
}

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
      muuOpetusTiedot = laajatTiedot.muuOpetusTiedot.map(ValpasOpiskeluoikeusMuuOpetusSuppeatTiedot.apply),
      päätasonSuoritukset = laajatTiedot.päätasonSuoritukset,
      onTehtyIlmoitus = laajatTiedot.onTehtyIlmoitus,
    )
  }
}

case class ValpasOpiskeluoikeusSuppeatTiedot(
  oid: ValpasOpiskeluoikeus.Oid,
  onHakeutumisValvottava: Boolean,
  onSuorittamisValvottava: Boolean,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  perusopetusTiedot: Option[ValpasOpiskeluoikeusPerusopetusSuppeatTiedot],
  perusopetuksenJälkeinenTiedot: Option[ValpasOpiskeluoikeusPerusopetuksenJälkeinenSuppeatTiedot],
  muuOpetusTiedot: Option[ValpasOpiskeluoikeusMuuOpetusSuppeatTiedot],
  päätasonSuoritukset: Seq[ValpasPäätasonSuoritus],
  // Option, koska tämä tieto rikastetaan mukaan vain tietyissä tilanteissa
  onTehtyIlmoitus: Option[Boolean],
) extends ValpasOpiskeluoikeus

object ValpasOpiskeluoikeusPerusopetusSuppeatTiedot {
  def apply(laajatTiedot: ValpasOpiskeluoikeusPerusopetusLaajatTiedot): ValpasOpiskeluoikeusPerusopetusSuppeatTiedot = {
    ValpasOpiskeluoikeusPerusopetusSuppeatTiedot(
      alkamispäivä = laajatTiedot.alkamispäivä,
      päättymispäivä = laajatTiedot.päättymispäivä,
      päättymispäiväMerkittyTulevaisuuteen = laajatTiedot.päättymispäiväMerkittyTulevaisuuteen,
      tarkastelupäivänTila = laajatTiedot.tarkastelupäivänTila,
      tarkastelupäivänKoskiTila = laajatTiedot.tarkastelupäivänKoskiTila,
      tarkastelupäivänKoskiTilanAlkamispäivä = laajatTiedot.tarkastelupäivänKoskiTilanAlkamispäivä,
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
  tarkastelupäivänKoskiTilanAlkamispäivä: String,
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
      tarkastelupäivänKoskiTilanAlkamispäivä = laajatTiedot.tarkastelupäivänKoskiTilanAlkamispäivä,
      valmistunutAiemminTaiLähitulevaisuudessa = laajatTiedot.valmistunutAiemminTaiLähitulevaisuudessa,
      näytäMuunaPerusopetuksenJälkeisenäOpintona = laajatTiedot.näytäMuunaPerusopetuksenJälkeisenäOpintona,
    )
  }
}

object ValpasOpiskeluoikeusMuuOpetusSuppeatTiedot {
  def apply(laajatTiedot: ValpasOpiskeluoikeusMuuOpetusLaajatTiedot): ValpasOpiskeluoikeusMuuOpetusSuppeatTiedot = {
    ValpasOpiskeluoikeusMuuOpetusSuppeatTiedot(
      alkamispäivä = laajatTiedot.alkamispäivä,
      päättymispäivä = laajatTiedot.päättymispäivä,
      päättymispäiväMerkittyTulevaisuuteen = laajatTiedot.päättymispäiväMerkittyTulevaisuuteen,
      tarkastelupäivänTila = laajatTiedot.tarkastelupäivänTila,
      tarkastelupäivänKoskiTila = laajatTiedot.tarkastelupäivänKoskiTila,
      tarkastelupäivänKoskiTilanAlkamispäivä = laajatTiedot.tarkastelupäivänKoskiTilanAlkamispäivä,
      valmistunutAiemminTaiLähitulevaisuudessa = laajatTiedot.valmistunutAiemminTaiLähitulevaisuudessa,
      näytäMuunaPerusopetuksenJälkeisenäOpintona = laajatTiedot.näytäMuunaPerusopetuksenJälkeisenäOpintona,
    )
  }
}

case class ValpasOpiskeluoikeusPerusopetuksenJälkeinenSuppeatTiedot(
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  tarkastelupäivänTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTilanAlkamispäivä: String,
  valmistunutAiemminTaiLähitulevaisuudessa: Boolean,
  näytäMuunaPerusopetuksenJälkeisenäOpintona: Option[Boolean],
) extends ValpasOpiskeluoikeusTiedot

case class ValpasOpiskeluoikeusMuuOpetusSuppeatTiedot(
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  päättymispäiväMerkittyTulevaisuuteen: Option[Boolean],
  tarkastelupäivänTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTila: Koodistokoodiviite,
  tarkastelupäivänKoskiTilanAlkamispäivä: String,
  valmistunutAiemminTaiLähitulevaisuudessa: Boolean,
  näytäMuunaPerusopetuksenJälkeisenäOpintona: Option[Boolean],
) extends ValpasOpiskeluoikeusTiedot

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

case class ValpasKuntailmoitusSuppeatTiedot(
  oppijaOid: Option[String],
  id: Option[String], // Oikeasti UUID - scala-schemasta puuttuu tuki UUID-tyypille
  tekijä: ValpasKuntailmoituksenTekijäSuppeatTiedot,
  kunta: OrganisaatioWithOid,
  aikaleima: Option[LocalDateTime],
  // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn ilmoituksen tiedoista,
  // tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa.
  hakenutMuualle: Option[Boolean],
  // Option, koska relevantti kenttä vain haettaessa ilmoituksia tietylle kunnalle
  onUudempiaIlmoituksiaMuihinKuntiin: Option[Boolean],
  aktiivinen: Option[Boolean],
)

object ValpasKuntailmoitusSuppeatTiedot {
  def apply(laajatTiedot: ValpasKuntailmoitusLaajatTiedot): ValpasKuntailmoitusSuppeatTiedot = {
    ValpasKuntailmoitusSuppeatTiedot(
      oppijaOid = laajatTiedot.oppijaOid,
      id = laajatTiedot.id,
      tekijä = ValpasKuntailmoituksenTekijäSuppeatTiedot(laajatTiedot.tekijä),
      kunta = laajatTiedot.kunta,
      aikaleima = laajatTiedot.aikaleima,
      hakenutMuualle = laajatTiedot.hakenutMuualle,
      onUudempiaIlmoituksiaMuihinKuntiin = laajatTiedot.onUudempiaIlmoituksiaMuihinKuntiin,
      aktiivinen = laajatTiedot.aktiivinen,
    )
  }
}

case class ValpasKuntailmoituksenTekijäSuppeatTiedot(
  organisaatio: OrganisaatioWithOid
)

object ValpasKuntailmoituksenTekijäSuppeatTiedot {
  def apply(laajatTiedot: ValpasKuntailmoituksenTekijäLaajatTiedot): ValpasKuntailmoituksenTekijäSuppeatTiedot = {
    ValpasKuntailmoituksenTekijäSuppeatTiedot(laajatTiedot.organisaatio)
  }
}

class ValpasOppijaSuppeatTiedotService(
  valpasOppijaService: ValpasOppijaService
) {
  def getHakeutumisvalvottavatOppijatSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value, haeHakutilanteet: Seq[Henkilö.Oid] = Seq.empty)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    valpasOppijaService.getHakeutumisvalvottavatOppijatLaajatTiedot(oppilaitosOid, hakeutumisvalvontaTieto, haeHakutilanteet)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  def getSuorittamisvalvottavatOppijatSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    valpasOppijaService.getSuorittamisvalvottavatOppijatLaajatTiedot(oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  def getKunnanOppijatSuppeatTiedot
    (kuntaOid: Organisaatio.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaKuntailmoituksillaSuppeatTiedot]] =
    valpasOppijaService.getKunnanOppijatLaajatTiedot(kuntaOid)
      .map(_.map(OppijaKuntailmoituksillaSuppeatTiedot.apply))

  def getHakeutumisenvalvonnanKunnalleTehdytIlmoituksetSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    valpasOppijaService.getOppilaitoksenKunnalleTekemätIlmoituksetLaajatTiedot(ValpasRooli.OPPILAITOS_HAKEUTUMINEN, oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  def getSuorittamisvalvonnanKunnalleTehdytIlmoituksetSuppeatTiedot
    (oppilaitosOid: ValpasOppilaitos.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] = {
    valpasOppijaService.getOppilaitoksenKunnalleTekemätIlmoituksetLaajatTiedot(ValpasRooli.OPPILAITOS_SUORITTAMINEN, oppilaitosOid)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))
  }
}
