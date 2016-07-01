package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.TelmaExampleData.{arviointiHyväksytty, _}
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._

object ExamplesTelma {
  val telmaTodistus = Oppija(
    MockOppijat.telma.vainHenkilötiedot,
    List(
      AmmatillinenOpiskeluoikeus(
        alkamispäivä = Some(date(2009, 9, 14)),
        päättymispäivä = Some(date(2016, 6, 4)),
        tila = AmmatillinenOpiskeluoikeudenTila(List(
          AmmatillinenOpiskeluoikeusjakso(date(2009, 9, 14), opiskeluoikeusLäsnä, None),
          AmmatillinenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut, None)
        )),
        oppilaitos = stadinAmmattiopisto,
        suoritukset = List(TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenSuoritus(
          tila = tilaValmis,
          vahvistus = vahvistus(date(2016, 6, 4), stadinAmmattiopisto, helsinki),
          toimipiste = stadinAmmattiopisto,
          koulutusmoduuli = TyöhönJaItsenäiseenElämäänValmentavaKoulutus(),
          osasuoritukset = Some(List(
            telmaKurssinSuoritus("TV", "Toimintakyvyn vahvistaminen", 18f, pakollinen = true,
              sanallinenArvionti("Opiskelija selviytyy arkielämään liittyvistä toimista, osaa hyödyntää apuvälineitä, palveluita ja tukea sekä on valinnut itselleen sopivan tavan viettää vapaa-aikaa.")),
            telmaKurssinSuoritus("OV", "Opiskeluvalmiuksien vahvistaminen", 15f, pakollinen = true,
              sanallinenArvionti("Opiskelija osaa opiskella työskennellä itsenäisesti, mutta ryhmässä toimimisessa tarvitsee joskus apua. Hän viestii vuorovaikutustilanteissa hyvin, osaa käyttää tietotekniikkaa ja matematiikan perustaitoja arkielämässä.")),
            telmaKurssinSuoritus("TYV", "Työelämään valmentautuminen", 20f, pakollinen = true,
              sanallinenArvionti("Opiskelijalla on käsitys itsestä työntekijänä, mutta työyhteisön säännöt vaativat vielä harjaantumista.")),
            telmaKurssinSuoritus("TIV", "Tieto- ja viestintätekniikka sekä sen hyödyntäminen", 2f, pakollinen = false, arviointiHyväksytty, tunnustettu("Yhteisten tutkinnon osien osa-alue on suoritettu x- perustutkinnon perusteiden (2015) osaamistavoitteiden mukaisesti")),
            telmaKurssinSuoritus("UV", "Uimaliikunta ja vesiturvallisuus", 5f, pakollinen = false, arvointiTyydyttävä, tunnustettu("Koulutuksen osa on tunnustettu Vesikallion urheiluopiston osaamistavoitteiden mukaisesti"))
          ))
        )),
        tavoite = tavoiteTutkinto
      )
    )
  )
  val examples = List(Example("työhön ja itsenäiseen elämään valmentava koulutus", "Oppija on suorittanut työhön ja itsenäiseen elämään valmentava koulutuksen (TELMA)", telmaTodistus, 200))

  def tunnustettu(selite: String): Some[OsaamisenTunnustaminen] = Some(OsaamisenTunnustaminen(None, selite))

  private def telmaKurssinSuoritus(koodi: String, kuvaus: String, laajuusOsaamispisteissä: Float, pakollinen: Boolean, arviointi: Option[List[TelmaArviointi]], tunnustaminen: Option[OsaamisenTunnustaminen] = None) =
    TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsanSuoritus(
      tila = tilaValmis,
      koulutusmoduuli = TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa(
        tunniste = PaikallinenKoodi(koodi, LocalizedString.finnish(kuvaus)),
        laajuus = Some(LaajuusOsaamispisteissä(laajuusOsaamispisteissä)),
        pakollinen = pakollinen
      ),
      arviointi = arviointi,
      tunnustettu = tunnustaminen
    )
}

object TelmaExampleData {
  lazy val arviointiHyväksytty: Some[List[TelmaArviointi]] = Some(List(TelmaArviointi(
    arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
    päivä = date(2013, 3, 20))))

  lazy val arvointiTyydyttävä: Some[List[TelmaArviointi]] = Some(List(TelmaArviointi(
    arvosana = Koodistokoodiviite("2", Some("H2"), "arviointiasteikkoammatillinent1k3", None),
    päivä = date(2013, 3, 20))))

  def sanallinenArvionti(teksti: String): Option[List[TelmaArviointi]] = arviointiHyväksytty.map(_.map(a => a.copy(kuvaus = Some(teksti))))
}
