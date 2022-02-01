package fi.oph.koski.valpas.ytl

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.oppivelvollisuustieto.{Oppivelvollisuustiedot, OptionalOppivelvollisuustieto}
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.koski.valpas.{ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult, ValpasHenkilöhakuResult, ValpasLöytyiHenkilöhakuResult}

import java.time.LocalDate

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
    val tiedotOnrissa = puuttuvatOidit.map(oppijaSearch.findHenkilöOidillaIlmanOikeustarkastusta)

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
    val tiedotOnrissa = puuttuvatHetut.map(oppijaSearch.findHenkilöHetullaIlmanOikeustarkastusta)

    yhdistäKoskiJaOnrTiedot(tiedotKoskessa, tiedotOnrissa)
  }

  private def yhdistäKoskiJaOnrTiedot(
    tiedotKoskessa: Seq[YtlMaksuttomuustieto],
    tiedotOnrissa: Seq[Either[HttpStatus, ValpasHenkilöhakuResult]],
  ): Seq[YtlMaksuttomuustieto] = {
    tiedotKoskessa ++
      tiedotOnrissa
        .flatMap(_.toOption) // TODO: Virheet voisi ottaa talteen?
        .flatMap(tiedot => YtlMaksuttomuustieto(tiedot))
  }
}

case class YtlMaksuttomuustieto(
  oppijaOid: String,
  hetu: Option[String] = None,
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[LocalDate] = None,
  maksuttomuudenPiirissä: Option[Boolean] = None,
) {
  def withoutHetu: YtlMaksuttomuustieto = this.copy(hetu = None)
}

object YtlMaksuttomuustieto {
  def apply(tarkastelupäivä: LocalDate)(tiedot: OptionalOppivelvollisuustieto): YtlMaksuttomuustieto = YtlMaksuttomuustieto(
    oppijaOid = tiedot.oid,
    hetu = tiedot.hetu,
    oikeusMaksuttomaanKoulutukseenVoimassaAsti = tiedot.oikeusMaksuttomaanKoulutukseenVoimassaAsti,
    maksuttomuudenPiirissä = tiedot.oikeusMaksuttomaanKoulutukseenVoimassaAsti
      .map(_.isEqualOrAfter(tarkastelupäivä))
      .orElse(Some(false)),
  )

  def apply(tiedot: ValpasHenkilöhakuResult): Option[YtlMaksuttomuustieto] =
    tiedot match {
      case r: ValpasLöytyiHenkilöhakuResult => Some(YtlMaksuttomuustieto(
        oppijaOid = r.oid,
        hetu = r.hetu,
        oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
        maksuttomuudenPiirissä = None,
      ))
      case r: ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult => r.oid.map(oid => YtlMaksuttomuustieto(
        oppijaOid = oid,
        hetu = r.hetu,
        oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
        maksuttomuudenPiirissä = Some(false),
      ))
      case _ => None
    }

  def oidOrder: Ordering[YtlMaksuttomuustieto] = Ordering.by((t: YtlMaksuttomuustieto) => t.oppijaOid)
}
