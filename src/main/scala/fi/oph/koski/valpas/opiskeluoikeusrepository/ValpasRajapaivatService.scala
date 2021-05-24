package fi.oph.koski.valpas.opiskeluoikeusrepository

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import com.typesafe.config.Config
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService.defaultMockTarkastelupäivä

trait ValpasRajapäivätService extends Logging {
  def tarkastelupäivä: LocalDate

  def lakiVoimassaVanhinSyntymäaika: LocalDate
  def ilmoitustenEnsimmäinenTallennuspäivä: LocalDate
  def lakiVoimassaPeruskoulustaValmistuneillaAlku: LocalDate
  def keväänValmistumisjaksoAlku: LocalDate
  def keväänValmistumisjaksoLoppu: LocalDate
  def keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäivä: LocalDate
  def keväänUlkopuolellaValmistumisjaksoAlku(tarkastelupäivä: LocalDate = tarkastelupäivä): LocalDate

  def keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä: LocalDate
  def perusopetussuorituksenNäyttämisenAikaraja: LocalDate

  def kuntailmoituksenVoimassoloaikaOpiskeluoikeudenJatkuessaPäivinä: Long
}

object ValpasRajapäivätService {
  val UseMockPath = "valpas.rajapäivät.useMock"
  val LakiVoimassaVanhinSyntymäaikaPath = "valpas.rajapäivät.lakiVoimassaVanhinSyntymäaika"
  val LakiVoimassaPeruskoulustaValmistuneillaAlkuPath = "valpas.rajapäivät.lakiVoimassaPeruskoulustaValmistuneillaAlku"
  val KeväänValmistumisjaksoPituusPäivinäPath = "valpas.rajapäivät.keväänValmistumisjaksoPituusPäivinä"
  val IlmoitustenEnsimmäinenTallennuspäiväPath = "valpas.rajapäivät.ilmoitustenEnsimmäinenTallennuspäivä"
  val KeväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäiväPath =
    "valpas.rajapäivät.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä"

  def keväänValmistumisjaksoLoppuPath(vuosi: Int) = s"valpas.rajapäivät.${vuosi}.keväänValmistumisjaksoLoppu"

  def keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäiväPath(vuosi: Int) =
    s"valpas.rajapäivät.${vuosi}.keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäivä"

  val tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinäPath =
    "valpas.rajapäivät.tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinä"

  val kuntailmoituksenVoimassoloaikaOpiskeluoikeudenJatkuessaPäivinäPath =
    "valpas.rajapäivät.kuntailmoituksenVoimassoloaikaOpiskeluoikeudenJatkuessaPäivinä"

  def apply(config: Config) = {
    if (config.getBoolean(UseMockPath)) {
      new MockValpasRajapäivätService(new ConfigValpasRajapäivätService(config))
    } else {
      new ConfigValpasRajapäivätService(config)
    }
  }
}

object MockValpasRajapäivätService {
  val defaultMockTarkastelupäivä: LocalDate = date(2021, 9, 5)
}

class MockValpasRajapäivätService(defaultService: ConfigValpasRajapäivätService) extends ValpasRajapäivätService {
  private var mockTarkastelupäivä: Option[LocalDate] = None

  def asetaMockTarkastelupäivä(tarkastelupäivä: LocalDate = defaultMockTarkastelupäivä): Unit = {
    this.mockTarkastelupäivä = Some(tarkastelupäivä)
  }
  def poistaMockTarkastelupäivä(): Unit = {
    this.mockTarkastelupäivä = None
  }
  def tarkastelupäivä: LocalDate = mockTarkastelupäivä match {
    case Some(päivä) => päivä
    case _ => defaultService.tarkastelupäivä
  }

  def lakiVoimassaVanhinSyntymäaika: LocalDate = defaultService.lakiVoimassaVanhinSyntymäaika
  def ilmoitustenEnsimmäinenTallennuspäivä: LocalDate = defaultService.ilmoitustenEnsimmäinenTallennuspäivä
  def lakiVoimassaPeruskoulustaValmistuneillaAlku: LocalDate = defaultService.lakiVoimassaPeruskoulustaValmistuneillaAlku
  def keväänValmistumisjaksoAlku: LocalDate = defaultService.keväänValmistumisjaksoAlku
  def keväänValmistumisjaksoLoppu: LocalDate =
    defaultService.konfiguraatioOletuksenaEdellinenVuosi(
      ValpasRajapäivätService.keväänValmistumisjaksoLoppuPath,
      tarkastelupäivä.getYear
    )
  def keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäivä: LocalDate =
    defaultService.konfiguraatioOletuksenaEdellinenVuosi(
      ValpasRajapäivätService.keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäiväPath,
      tarkastelupäivä.getYear
    )

  def keväänUlkopuolellaValmistumisjaksoAlku(tarkastelupäivä: LocalDate): LocalDate =
    defaultService.keväänUlkopuolellaValmistumisjaksoAlku(tarkastelupäivä)

  def keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä: LocalDate = {
    defaultService.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
  }

  def perusopetussuorituksenNäyttämisenAikaraja: LocalDate =
    tarkastelupäivä.plusDays(defaultService.tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinä)

  def kuntailmoituksenVoimassoloaikaOpiskeluoikeudenJatkuessaPäivinä: Long =
    defaultService.kuntailmoituksenVoimassoloaikaOpiskeluoikeudenJatkuessaPäivinä
}

class ConfigValpasRajapäivätService(config: Config) extends ValpasRajapäivätService {
  def tarkastelupäivä: LocalDate = LocalDate.now

  val lakiVoimassaVanhinSyntymäaika: LocalDate =
    LocalDate.parse(config.getString(ValpasRajapäivätService.LakiVoimassaVanhinSyntymäaikaPath))

  val ilmoitustenEnsimmäinenTallennuspäivä: LocalDate =
    LocalDate.parse(config.getString(ValpasRajapäivätService.IlmoitustenEnsimmäinenTallennuspäiväPath))

  val lakiVoimassaPeruskoulustaValmistuneillaAlku: LocalDate =
    LocalDate.parse(config.getString(ValpasRajapäivätService.LakiVoimassaPeruskoulustaValmistuneillaAlkuPath))

  val keväänValmistumisjaksoLoppu: LocalDate = konfiguraatioOletuksenaEdellinenVuosi(
    ValpasRajapäivätService.keväänValmistumisjaksoLoppuPath,
    tarkastelupäivä.getYear
  )

  private val keväänValmistumisjaksoPituusPäivinä: Long =
    config.getLong(ValpasRajapäivätService.KeväänValmistumisjaksoPituusPäivinäPath)

  val keväänValmistumisjaksoAlku: LocalDate = keväänValmistumisjaksoLoppu.minusDays(keväänValmistumisjaksoPituusPäivinä)

  val keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäivä: LocalDate =
    konfiguraatioOletuksenaEdellinenVuosi(
      ValpasRajapäivätService.keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäiväPath,
      tarkastelupäivä.getYear
    )

  def keväänUlkopuolellaValmistumisjaksoAlku(tarkastelupäivä: LocalDate): LocalDate =
    tarkastelupäivä.minusMonths(2)

  def keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä: LocalDate =
    LocalDate.parse(config.getString(ValpasRajapäivätService.KeväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäiväPath))
      .withYear(keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäivä.getYear)

  def konfiguraatioOletuksenaEdellinenVuosi(configPathVuodesta: Int => String, tarkasteluvuosi: Int) =
    OletuksenaEdellinenVuosiKonfiguraattori(2021, config, (msg: String) => logger.error(msg), configPathVuodesta).hae(tarkasteluvuosi)

  def perusopetussuorituksenNäyttämisenAikaraja: LocalDate =
    tarkastelupäivä.plusDays(tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinä)

  val tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinä: Long =
    config.getLong(ValpasRajapäivätService.tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinäPath)

  val kuntailmoituksenVoimassoloaikaOpiskeluoikeudenJatkuessaPäivinä: Long =
    config.getLong(ValpasRajapäivätService.kuntailmoituksenVoimassoloaikaOpiskeluoikeudenJatkuessaPäivinäPath)
}

case class OletuksenaEdellinenVuosiKonfiguraattori(
  aloitusVuosi: Int,
  config: Config,
  logError: String => Unit,
  configPathVuodesta: Int => String
) {
  def hae(tarkasteluvuosi: Int): LocalDate = {
    val vuodet: Seq[Int] = (tarkasteluvuosi to aloitusVuosi by -1) :+ aloitusVuosi

    val määritellyt = vuodet.view.map(vuosi => (vuosi, config.hasPath(configPathVuodesta(vuosi))))

    määritellyt.find(m => m._2) match {
      case Some((vuosi, _)) if vuosi == tarkasteluvuosi => {
        LocalDate.parse(config.getString(configPathVuodesta(vuosi)))
      }
      case Some((vuosi, _)) => {
        val edellisenVuodenPäivämääräTälläVuodella = LocalDate.parse(config.getString(configPathVuodesta(vuosi))).withYear(tarkasteluvuosi)
        logError(s"${configPathVuodesta(tarkasteluvuosi)} ei määritelty, käytetään oletusta ${edellisenVuodenPäivämääräTälläVuodella}")
        edellisenVuodenPäivämääräTälläVuodella
      }
      case _ => {
        throw new InternalError(s"Tarvittava konfiguraatioparametri ${configPathVuodesta(aloitusVuosi)} ei määritelty")
      }
    }
  }
}
