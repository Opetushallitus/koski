package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.TelmaExampleData.{arviointiHyväksytty, _}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.finnish
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesTelma {
  val telmaTodistus = Oppija(
    MockOppijat.telma.henkilö,
    List(
      AmmatillinenOpiskeluoikeus(
        päättymispäivä = Some(date(2016, 6, 4)),
        tila = AmmatillinenOpiskeluoikeudenTila(List(
          AmmatillinenOpiskeluoikeusjakso(date(2009, 9, 14), opiskeluoikeusLäsnä, None),
          AmmatillinenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut, None)
        )),
        oppilaitos = Some(stadinAmmattiopisto),
        suoritukset = List(TelmaKoulutuksenSuoritus(
          vahvistus = vahvistus(date(2016, 6, 4), stadinAmmattiopisto, Some(helsinki)),
          toimipiste = stadinAmmattiopisto,
          koulutusmoduuli = TelmaKoulutus(perusteenDiaarinumero = Some("6/011/2015")),
          suorituskieli = suomenKieli,
          osasuoritukset = Some(List(
            telmaKurssinSuoritus("TV", "Toimintakyvyn vahvistaminen", 18f, pakollinen = true,
              sanallinenArvionti("Opiskelija selviytyy arkielämään liittyvistä toimista, osaa hyödyntää apuvälineitä, palveluita ja tukea sekä on valinnut itselleen sopivan tavan viettää vapaa-aikaa.")),
            telmaKurssinSuoritus("OV", "Opiskeluvalmiuksien vahvistaminen", 15f, pakollinen = true,
              sanallinenArvionti("Opiskelija osaa opiskella työskennellä itsenäisesti, mutta ryhmässä toimimisessa tarvitsee joskus apua. Hän viestii vuorovaikutustilanteissa hyvin, osaa käyttää tietotekniikkaa ja matematiikan perustaitoja arkielämässä.")),
            telmaKurssinSuoritus("TYV", "Työelämään valmentautuminen", 20f, pakollinen = true,
              sanallinenArvionti("Opiskelijalla on käsitys itsestä työntekijänä, mutta työyhteisön säännöt vaativat vielä harjaantumista.")),
            telmaKurssinSuoritus("TIV", "Tieto- ja viestintätekniikka sekä sen hyödyntäminen", 2f, pakollinen = false, arviointiHyväksytty, tunnustettu("Yhteisten tutkinnon osien osa-alue on suoritettu x- perustutkinnon perusteiden (2015) osaamistavoitteiden mukaisesti"), näyttö = Some(näyttö(date(2016, 6, 1), "Elokuvien jälkieditointi", "FinBio Oy"))),
            telmaKurssinSuoritus("UV", "Uimaliikunta ja vesiturvallisuus", 5f, pakollinen = false, arvointiTyydyttävä, tunnustettu("Koulutuksen osa on tunnustettu Vesikallion urheiluopiston osaamistavoitteiden mukaisesti"),
              kuvaus = Some(
                """|Kurssilla harjoitellaan vedessä liikkumista ja perehdytään vesiturvallisuuden perusteisiin.
                   |Kurssilla käytäviä asioita:
                   |  - uinnin hengitystekniikka
                   |  - perehdytystä uinnin eri tekniikoihin
                   |  - allasturvallisuuden perustiedot
                """.stripMargin)
            ),
            TelmaKoulutuksenOsanSuoritus(
              koulutusmoduuli = autonLisävarustetyöt(false),
              arviointi = arviointiHyväksytty
            )
          ))
        ))
      )
    )
  )
  val examples = List(Example("työhön ja itsenäiseen elämään valmentava koulutus", "Oppija on suorittanut työhön ja itsenäiseen elämään valmentava koulutuksen (TELMA)", telmaTodistus, 200))

  def tunnustettu(selite: String): Some[OsaamisenTunnustaminen] = Some(OsaamisenTunnustaminen(None, selite))

  private def telmaKurssinSuoritus(koodi: String, nimi: String, laajuusOsaamispisteissä: Float, pakollinen: Boolean, arviointi: Option[List[AmmatillinenArviointi]], tunnustaminen: Option[OsaamisenTunnustaminen] = None, näyttö: Option[Näyttö] = None, kuvaus: Option[String] = None) = {
    TelmaKoulutuksenOsanSuoritus(
      koulutusmoduuli = PaikallinenTelmaKoulutuksenOsa(
        tunniste = PaikallinenKoodi(koodi, finnish(nimi)),
        kuvaus = finnish(kuvaus.getOrElse(nimi)),
        laajuus = Some(LaajuusOsaamispisteissä(laajuusOsaamispisteissä)),
        pakollinen = pakollinen
      ),
      arviointi = arviointi,
      tunnustettu = tunnustaminen
    )
  }
}

object TelmaExampleData {
  lazy val arviointiHyväksytty: Some[List[AmmatillinenArviointi]] = Some(List(AmmatillinenArviointi(
    arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
    päivä = date(2013, 3, 20))))

  lazy val arvointiTyydyttävä: Some[List[AmmatillinenArviointi]] = Some(List(AmmatillinenArviointi(
    arvosana = Koodistokoodiviite("2", Some("H2"), "arviointiasteikkoammatillinent1k3", None),
    päivä = date(2013, 3, 20))))

  def sanallinenArvionti(teksti: String): Option[List[AmmatillinenArviointi]] = arviointiHyväksytty.map(_.map(a => a.copy(kuvaus = Some(teksti))))
}
