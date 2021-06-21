package fi.oph.koski.valpas

import java.time.{LocalDate, LocalDateTime}
import java.time.LocalDate.{of => date}

import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite}
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilöLaajatTiedot, ValpasOpiskeluoikeus, ValpasOpiskeluoikeusLaajatTiedot, ValpasOppijaLaajatTiedot, ValpasOppilaitos}
import fi.oph.koski.valpas.valpasuser.ValpasRooli


class ValpasAccessResolverSpec extends ValpasTestBase {
  private val accessResolver = new ValpasAccessResolver

  private val omaOpiskeluoikeusOid = "1.2.246.562.15.37463693990"
  private val toisenOpiskeluoikeusOid = "1.2.246.562.15.37463693991"
  private val omaOppilaitos = MockOrganisaatiot.jyväskylänNormaalikoulu
  private val toisenOppilaitos = MockOrganisaatiot.stadinAmmattiopisto

  "withOpiskeluoikeusAccess: OPPILAITOS_SUORITTAMINEN-pääsy oman oppilaitoksen opiskeluoikeuteen" in {
    val testiOppija = oppija(
      suorittamisvalvovatOppilaitokset = Set(omaOppilaitos),
      opiskeluoikeudet = Seq(
        opiskeluoikeus(
          oid = omaOpiskeluoikeusOid,
          oppilaitosOid = omaOppilaitos,
          onSuorittamisValvottava = true
        ),
        opiskeluoikeus(
          oid = toisenOpiskeluoikeusOid,
          oppilaitosOid = toisenOppilaitos,
          onSuorittamisValvottava = true
        )
      ))

    val result = accessResolver.withOpiskeluoikeusAccess(ValpasRooli.OPPILAITOS_SUORITTAMINEN)(
      omaOpiskeluoikeusOid
    )(testiOppija)(defaultSession)
    result should equal(Right(testiOppija))
  }

  "withOpiskeluoikeusAccess: Ei OPPILAITOS_SUORITTAMINEN-pääsyä toisen oppilaitoksen opiskeluoikeuteen" in {
    val testiOppija = oppija(
      suorittamisvalvovatOppilaitokset = Set(omaOppilaitos),
      opiskeluoikeudet = Seq(
        opiskeluoikeus(
          oid = omaOpiskeluoikeusOid,
          oppilaitosOid = omaOppilaitos,
          onSuorittamisValvottava = true
        ),
        opiskeluoikeus(
          oid = toisenOpiskeluoikeusOid,
          oppilaitosOid = toisenOppilaitos,
          onSuorittamisValvottava = true
        )
      ))

    val result = accessResolver.withOpiskeluoikeusAccess(ValpasRooli.OPPILAITOS_SUORITTAMINEN)(
      toisenOpiskeluoikeusOid
    )(testiOppija)(defaultSession)
    result should equal(Left(ValpasErrorCategory.forbidden.opiskeluoikeus()))
  }

  "withOpiskeluoikeusAccess: Ei OPPILAITOS_HAKEUTUMINEN-pääsyä toisen oppilaitoksen opiskeluoikeuteen" in {
    val testiOppija = oppija(
      hakeutumisvalvovatOppilaitokset = Set(omaOppilaitos),
      opiskeluoikeudet = Seq(
        opiskeluoikeus(
          oid = omaOpiskeluoikeusOid,
          oppilaitosOid = omaOppilaitos,
          onHakeutumisValvottava = true
        ),
        opiskeluoikeus(
          oid = toisenOpiskeluoikeusOid,
          oppilaitosOid = toisenOppilaitos,
          onHakeutumisValvottava = true
        )
      ))

    val result = accessResolver.withOpiskeluoikeusAccess(ValpasRooli.OPPILAITOS_HAKEUTUMINEN)(
      toisenOpiskeluoikeusOid
    )(testiOppija)(defaultSession)
    result should equal(Left(ValpasErrorCategory.forbidden.opiskeluoikeus()))
  }

  def oppija(
    henkilö: ValpasHenkilöLaajatTiedot = henkilö,
    opiskeluoikeudet: Seq[ValpasOpiskeluoikeusLaajatTiedot] = Seq.empty,
    hakeutumisvalvovatOppilaitokset: Set[ValpasOppilaitos.Oid] = Set.empty,
    suorittamisvalvovatOppilaitokset: Set[ValpasOppilaitos.Oid] = Set.empty,
    onOikeusValvoaMaksuttomuutta: Boolean = false,
    onOikeusValvoaKunnalla: Boolean = false
  ) =
    ValpasOppijaLaajatTiedot(
      henkilö = henkilö,
      hakeutumisvalvovatOppilaitokset = hakeutumisvalvovatOppilaitokset,
      suorittamisvalvovatOppilaitokset = suorittamisvalvovatOppilaitokset,
      opiskeluoikeudet = opiskeluoikeudet,
      oppivelvollisuusVoimassaAsti = date(2021, 10, 1),
      oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = date(2023, 12, 31),
      onOikeusValvoaMaksuttomuutta = onOikeusValvoaMaksuttomuutta,
      onOikeusValvoaKunnalla = onOikeusValvoaKunnalla
    )

  def henkilö = ValpasHenkilöLaajatTiedot(
    oid = "1.2.246.562.24.37463693999",
    kaikkiOidit = Set("1.2.246.562.24.37463693999"),
    hetu = None,
    syntymäaika = Some(date(2005, 1, 9)),
    etunimet = "Etu",
    sukunimi = "Suku",
    turvakielto = false,
    äidinkieli = None
  )

  def opiskeluoikeus(
    oid : ValpasOpiskeluoikeus.Oid,
    tyyppi: String = "lukiokoulutus",
    oppilaitosOid:  ValpasOppilaitos.Oid,
    onHakeutumisValvottava: Boolean = false,
    onSuorittamisValvottava: Boolean = false
  ) = ValpasOpiskeluoikeusLaajatTiedot(
    oid = oid,
    onHakeutumisValvottava = onHakeutumisValvottava,
    onSuorittamisValvottava = onSuorittamisValvottava,
    tyyppi = Koodistokoodiviite(tyyppi, "opiskeluoikeudentyyppi"),
    oppilaitos = ValpasOppilaitos(
      oid = oppilaitosOid,
      nimi = Finnish("Oppilaitoksen nimi")
    ),
    toimipiste = None,
    ryhmä = None,
    alkamispäivä = "2012-08-01",
    päättymispäivä = None,
    päättymispäiväMerkittyTulevaisuuteen = None,
    tarkastelupäivänTila = Koodistokoodiviite("voimassa", "valpasopiskeluoikeudentila"),
    näytettäväPerusopetuksenSuoritus = false,
    vuosiluokkiinSitomatonOpetus = false
  )
}
