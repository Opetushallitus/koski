package fi.oph.koski.valpas.ytl

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot
import fi.oph.koski.valpas.ValpasHenkilöhakuResult
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasYtlService(application: KoskiApplication) {
  private val raportointiDb = application.raportointiDatabase
  private val oppijaSearch = application.valpasOppijaSearchService
  private val rajapäivät = application.valpasRajapäivätService

  def haeMaksuttomuustiedot
    (oidit: Seq[String], hetut: Seq[String])
    (implicit session: ValpasSession)
  : Seq[YtlMaksuttomuustieto] = {
    haeMaksuttomuustiedotOppijaOideilla(oidit) ++ haeMaksuttomuustiedotHetuilla(hetut)
  }

  private def haeMaksuttomuustiedotOppijaOideilla
    (oidit: Seq[String])
    (implicit session: ValpasSession)
  : Seq[YtlMaksuttomuustieto] = {
    val tiedotKoskessa = Oppivelvollisuustiedot
      .queryByOidsIncludeMissing(oidit, raportointiDb)
      .map(YtlMaksuttomuustieto.apply(rajapäivät.tarkastelupäivä))

    val puuttuvatOidit = oidit.diff(tiedotKoskessa.map(_.oppijaOid))
    val tiedotOnrissa = oppijaSearch.findHenkilöOideillaIlmanOikeustarkastusta(puuttuvatOidit)

    yhdistäKoskiJaOnrTiedot(tiedotKoskessa, tiedotOnrissa).map(_.withoutHetu)
  }

  private def haeMaksuttomuustiedotHetuilla
    (hetut: Seq[String])
    (implicit session: ValpasSession)
  : Seq[YtlMaksuttomuustieto] = {
    val tiedotKoskessa = Oppivelvollisuustiedot
      .queryByHetusIncludeMissing(hetut, raportointiDb)
      .map(YtlMaksuttomuustieto.apply(rajapäivät.tarkastelupäivä))

    val puuttuvatHetut = hetut.diff(tiedotKoskessa.flatMap(_.hetu))
    val tiedotOnrissa = oppijaSearch.findHenkilöHetuillaIlmanOikeustarkastusta(puuttuvatHetut)

    yhdistäKoskiJaOnrTiedot(tiedotKoskessa, tiedotOnrissa)
  }

  private def yhdistäKoskiJaOnrTiedot(
    tiedotKoskessa: Seq[YtlMaksuttomuustieto],
    tiedotOnrissa: Seq[Either[HttpStatus, ValpasHenkilöhakuResult]],
  ): Seq[YtlMaksuttomuustieto] = {
    tiedotKoskessa ++
      tiedotOnrissa
        .flatMap(_.toOption) // TODO: Virheet voisi ottaa talteen?
        .flatMap(tiedot => YtlMaksuttomuustieto(tiedot, rajapäivät.tarkastelupäivä))
  }
}

