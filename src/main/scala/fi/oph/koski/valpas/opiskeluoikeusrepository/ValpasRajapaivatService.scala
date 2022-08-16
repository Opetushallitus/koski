package fi.oph.koski.valpas.opiskeluoikeusrepository

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import com.typesafe.config.Config
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService.defaultMockTarkastelupäivä

abstract class ValpasRajapäivätService(config: Config) extends Logging {
  def tarkastelupäivä: LocalDate

  def lakiVoimassaVanhinSyntymäaika: LocalDate =
    LocalDate.parse(config.getString(ValpasRajapäivätService.LakiVoimassaVanhinSyntymäaikaPath))

  def oppivelvollisuusAlkaaIka: Integer =
    config.getInt(ValpasRajapäivätService.OppivelvollisuusAlkaaIkaPath)

  def oppivelvollisuusAlkaaPäivämäärä: LocalDate =
    LocalDate.parse(config.getString(ValpasRajapäivätService.OppivelvollisuusAlkaaPäivämääräPath))

  def oppivelvollisuusAlkaa(syntymäpäivä: LocalDate): LocalDate =
    oppivelvollisuusAlkaaPäivämäärä.withYear(syntymäpäivä.getYear + oppivelvollisuusAlkaaIka)

  // Vain ONR:ssä olevan oppivelvollisen tiedot voi näyttää 1 kk ennenkuin oppivelvollisuus on alkanut, jotta
  // hänelle voi esim. kirjata oppivelvollisuuden keskeytyksen.
  def oppijaOnTarpeeksiVanhaKeskeytysmerkintöjäVarten(syntymäaika: Option[LocalDate]): Boolean =
    syntymäaika match {
      case Some(pvm) => !tarkastelupäivä.isBefore(oppivelvollisuusAlkaa(pvm).minusMonths(1))
      case _ => false
    }

  def oppivelvollisuusVoimassaAstiIänPerusteella(syntymäpäivä: LocalDate): LocalDate = {
    // Huom! Sama logiikka on myös SQL:nä Oppivelvollisuustiedot-luokan luomassa materialized view:ssä.
    // Varmista muutosten jälkeen, että logiikka säilyy samana.
    syntymäpäivä.plusYears(oppivelvollisuusLoppuuIka.toLong).minusDays(1)
  }

  def maksuttomuusVoimassaAstiIänPerusteella(syntymäpäivä: LocalDate): LocalDate = {
    // Huom! Sama logiikka on myös SQL:nä Oppivelvollisuustiedot-luokan luomassa materialized view:ssä.
    // Varmista muutosten jälkeen, että logiikka säilyy samana.
    date(syntymäpäivä.plusYears(maksuttomuusLoppuuIka.toLong).getYear, 12, 31)
  }

  def oppivelvollisuusLoppuuIka: Integer =
    config.getInt(ValpasRajapäivätService.OppivelvollisuusLoppuuIkaPath)

  def maksuttomuusLoppuuIka: Integer =
    config.getInt(ValpasRajapäivätService.MaksuttomuusLoppuuIkaPath)

  def ilmoitustenEnsimmäinenTallennuspäivä: LocalDate =
    LocalDate.parse(config.getString(ValpasRajapäivätService.IlmoitustenEnsimmäinenTallennuspäiväPath))

  def lakiVoimassaPeruskoulustaValmistuneillaAlku: LocalDate =
    LocalDate.parse(config.getString(ValpasRajapäivätService.LakiVoimassaPeruskoulustaValmistuneillaAlkuPath))

  def keväänValmistumisjaksoAlku: LocalDate =
    konfiguraatioOletuksenaEdellinenVuosi(
      ValpasRajapäivätService.keväänValmistumisjaksoAlkuPath,
      tarkastelupäivä.getYear
    )

  def keväänValmistumisjaksoLoppu: LocalDate =
    konfiguraatioOletuksenaEdellinenVuosi(
      ValpasRajapäivätService.keväänValmistumisjaksoLoppuPath,
      tarkastelupäivä.getYear
    )

  def keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäivä: LocalDate =
    konfiguraatioOletuksenaEdellinenVuosi(
      ValpasRajapäivätService.keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäiväPath,
      tarkastelupäivä.getYear
    )

  def keväänUlkopuolellaValmistumisjaksoAlku: LocalDate =
    tarkastelupäivä.minusMonths(2)

  def keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä: LocalDate =
    LocalDate.parse(
      config.getString(ValpasRajapäivätService.KeväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäiväPath)
    ).withYear(keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäivä.getYear)

  def perusopetussuorituksenNäyttämisenAikaraja: LocalDate =
    tarkastelupäivä.plusDays(tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinä)

  def aikaisinMahdollinenOppivelvollisenSyntymäaika: LocalDate = {
    val aikaisinNykyhetkenJaIänPerusteella =
      date(
        tarkastelupäivä.minusYears(oppivelvollisuusLoppuuIka.toLong).getYear,
        1,
        1)

    if (aikaisinNykyhetkenJaIänPerusteella.isBefore(lakiVoimassaVanhinSyntymäaika)) {
      lakiVoimassaVanhinSyntymäaika
    } else {
      aikaisinNykyhetkenJaIänPerusteella
    }
  }

  def kuntailmoitusAktiivisuusKuukausina: Long =
    config.getLong(ValpasRajapäivätService.kuntailmoitusAktiivisuusKuukausina)

  def onAlle18VuotiasTarkastelupäivänä(syntymäaika: Option[LocalDate]): Boolean = {
    syntymäaika match {
      case Some(d) if d.plusYears(18).isAfter(tarkastelupäivä) => true
      case Some(_) => false
      case _ => false // Jos syntymäaika puuttuu, oletetaan täysi-ikäiseksi
    }
  }

  def nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutAikaväli: (LocalDate, LocalDate) = {
    val väli = List(keväänValmistumisjaksoAlku, keväänValmistumisjaksoLoppu).map(_.withYear(tarkastelupäivä.getYear))
    (väli(0), väli(1))
  }

  private val tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinä: Long =
    config.getLong(ValpasRajapäivätService.tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinäPath)

  private def konfiguraatioOletuksenaEdellinenVuosi(configPathVuodesta: Int => String, tarkasteluvuosi: Int) =
    OletuksenaEdellinenVuosiKonfiguraattori(2021, config, (msg: String) => logger.error(msg), configPathVuodesta).hae(tarkasteluvuosi)
}

object ValpasRajapäivätService {
  val UseMockPath = "valpas.rajapäivät.useMock"
  val LakiVoimassaVanhinSyntymäaikaPath = "valpas.rajapäivät.lakiVoimassaVanhinSyntymäaika"
  val OppivelvollisuusAlkaaIkaPath = "valpas.rajapäivät.oppivelvollisuusAlkaaIkä"
  val OppivelvollisuusAlkaaPäivämääräPath = "valpas.rajapäivät.oppivelvollisuusAlkaaPäivämäärä"
  val OppivelvollisuusLoppuuIkaPath = "valpas.rajapäivät.oppivelvollisuusLoppuuIkä"
  val MaksuttomuusLoppuuIkaPath = "valpas.rajapäivät.maksuttomuusLoppuuIkä"
  val LakiVoimassaPeruskoulustaValmistuneillaAlkuPath = "valpas.rajapäivät.lakiVoimassaPeruskoulustaValmistuneillaAlku"
  val IlmoitustenEnsimmäinenTallennuspäiväPath = "valpas.rajapäivät.ilmoitustenEnsimmäinenTallennuspäivä"
  val KeväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäiväPath =
    "valpas.rajapäivät.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä"

  def keväänValmistumisjaksoAlkuPath(vuosi: Int) = s"valpas.rajapäivät.${vuosi}.keväänValmistumisjaksoAlku"
  def keväänValmistumisjaksoLoppuPath(vuosi: Int) = s"valpas.rajapäivät.${vuosi}.keväänValmistumisjaksoLoppu"

  def keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäiväPath(vuosi: Int) =
    s"valpas.rajapäivät.${vuosi}.keväänValmistumisjaksollaValmistuneidenOppivelvollisuudenSuorittamisenTarkistuspäivä"

  val tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinäPath =
    "valpas.rajapäivät.tulevaisuuteenMerkitynPerusopetuksenSuorituksenAikaikkunaPäivinä"

  val kuntailmoitusAktiivisuusKuukausina =
    "valpas.rajapäivät.kuntailmoitusAktiivisuusKuukausina"

  def apply(config: Config) = {
    if (config.getBoolean(UseMockPath)) {
      new MockValpasRajapäivätService(config)
    } else {
      new ConfigValpasRajapäivätService(config)
    }
  }
}

object MockValpasRajapäivätService {
  val defaultMockTarkastelupäivä: LocalDate = date(2021, 9, 5)
}

class MockValpasRajapäivätService(config: Config) extends ValpasRajapäivätService(config) {
  private var mockTarkastelupäivä: Option[LocalDate] = None

  def asetaMockTarkastelupäivä(tarkastelupäivä: LocalDate = defaultMockTarkastelupäivä): Unit = {
    this.mockTarkastelupäivä = Some(tarkastelupäivä)
  }
  def poistaMockTarkastelupäivä(): Unit = {
    this.mockTarkastelupäivä = None
  }
  def tarkastelupäivä: LocalDate = mockTarkastelupäivä match {
    case Some(päivä) => päivä
    case _ => defaultMockTarkastelupäivä
  }
}

class ConfigValpasRajapäivätService(config: Config) extends ValpasRajapäivätService(config) {
  def tarkastelupäivä: LocalDate = LocalDate.now
}

// Mahdollistaa eri päivämääräarvojen käyttämisen eri vuosina, mutta ei silti hajoa kokonaan, vaikka tarkasteluvuosi
// puuttuisikin konfiguraatiosta: käyttää sen sijaan edellisen vuoden konfiguraatiosta luettua päivää ja kuukautta.
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
