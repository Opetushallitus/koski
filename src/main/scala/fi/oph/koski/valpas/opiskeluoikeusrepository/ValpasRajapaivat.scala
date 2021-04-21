package fi.oph.koski.valpas.opiskeluoikeusrepository

import com.typesafe.config.Config
import fi.oph.koski.config.Environment

import java.time.LocalDate
import java.time.LocalDate.{of => date}

trait ValpasRajapäivät {
  def tarkasteluPäivä: LocalDate

  def lakiVoimassaPeruskoulustaValmistuneillaAlku: LocalDate = date(2021, 1, 1)
  def keväänValmistumisjaksoAlku: LocalDate = date(0, 5, 15).withYear(keväänVuosi)
  def keväänValmistumisjaksoLoppu: LocalDate = date(0, 5, 31).withYear(keväänVuosi)
  // TODO: Lue tuotannossa konfiguraatioista tms. rajapäivä kuntailmoitusten lähettämiselle ja lisää siihen 1 kuukausi:
  // se on oikea päivä jolloin keväällä valmistuneiden tietoja ei saa enää hakeutumisen valvoja nähdä.
  def keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä: LocalDate =
    date(0, 9, 30).withYear(keväänVuosi)
  def keväänUlkopuolellaValmistumisjaksoAlku: LocalDate = tarkasteluPäivä.minusMonths(2)

  // Tämä päättää tutkittavan kevään nykyhetken vuoden mukaan: Tämä yksinkertainen logiikka toistaiseksi
  // riittää, koska edellisenä keväänä valmistuvat eivät näy enää kyseisen vuoden syyskuun lopun jälkeen.
  private val keväänVuosi = tarkasteluPäivä.getYear
}

class OikeatValpasRajapäivät extends ValpasRajapäivät {
  override def tarkasteluPäivä: LocalDate = LocalDate.now
}

class MockValpasRajapäivät(val tarkasteluPäivä: LocalDate = date(2021, 9, 5)) extends ValpasRajapäivät

object ValpasRajapäivät {
  // käytetään oletuksena tuotantototeutusta myös paikallisesti: kun Valppaan mock-data resetoidaan, tämä vaihdetaan
  // mock-versioksi.
  private val default = new OikeatValpasRajapäivät()

  private var mockImplementation: ValpasRajapäivät = default

  def enableMock(rajapäivät: MockValpasRajapäivät): Unit = mockImplementation = rajapäivät

  def disableMock(): Unit = mockImplementation = default

  def getCurrent(): ValpasRajapäivät = mockImplementation

  def apply(config: Config): () => ValpasRajapäivät = {
    val allowMock = !Environment.isServerEnvironment(config)
    () => {
      if (allowMock) {
        mockImplementation
      } else {
        default
      }
    }
  }
}
