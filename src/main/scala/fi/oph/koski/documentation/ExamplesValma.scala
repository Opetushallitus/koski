package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.localization.LocalizedString.finnish
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesValma {
  val valmaTodistus = Oppija(
    MockOppijat.valma.henkilö,
    List(
      AmmatillinenOpiskeluoikeus(
        päättymispäivä = Some(date(2016, 6, 4)),
        tila = AmmatillinenOpiskeluoikeudenTila(List(
          AmmatillinenOpiskeluoikeusjakso(date(2009, 9, 14), opiskeluoikeusLäsnä, None),
          AmmatillinenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut, None)
        )),
        oppilaitos = Some(stadinAmmattiopisto),
        suoritukset = List(ValmaKoulutuksenSuoritus(
          vahvistus = vahvistus(date(2016, 6, 4), stadinAmmattiopisto),
          toimipiste = stadinAmmattiopisto,
          koulutusmoduuli = ValmaKoulutus(laajuus = Some(LaajuusOsaamispisteissä(65)), perusteenDiaarinumero = Some("5/011/2015")),
          suorituskieli = suomenKieli,
          osasuoritukset = Some(List(
            valmaKurssinSuoritus("AKO", "Ammatilliseen koulutukseen orientoituminen ja työelämän perusvalmiuksien hankkiminen", 10f, Some(List(arviointiHyväksytty)), pakollinen = true),
            valmaKurssinSuoritus("OV", "Opiskeluvalmiuksien vahvistaminen", 10f, Some(List(arviointiHyväksytty)), pakollinen = false),
            valmaKurssinSuoritus("TOV", "Työssäoppimiseen ja oppisopimuskoulutukseen valmentautuminen", 15f, Some(List(arviointiHyväksytty)), pakollinen = false),
            valmaKurssinSuoritus("ATH", "Arjen taitojen ja hyvinvoinnin vahvistaminen", 10f, Some(List(arviointiHyväksytty)), pakollinen = false),
            valmaKurssinSuoritus("ATK", "Tietokoneen käyttäjän AB-kortti", 5f, Some(List(arviointiHyväksytty)), pakollinen = false),
            ValmaKoulutuksenOsanSuoritus(
              koulutusmoduuli = autonLisävarustetyöt(false),
              arviointi = Some(List(arviointiHyväksytty)),
              tunnustettu = tunnustettu
            )
          ))
        ))
      )
    )
  )

  val examples = List(Example("ammatilliseen peruskoulutukseen valmentava koulutus", "Oppija on suorittanut ammatilliseen peruskoulutukseen valmentavan koulutuksen (VALMA)", valmaTodistus, 200))

  lazy val tunnustettu: Some[OsaamisenTunnustaminen] = Some(OsaamisenTunnustaminen(
    osaaminen = Some(MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100209", Some("Asennuksen ja automaation perustyöt"), "tutkinnonosat", Some(1)), true, None),
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      tutkinto = Some(AmmatillinenTutkintoKoulutus(
        tunniste = Koodistokoodiviite("351101", Some("Kone- ja metallialan perustutkinto"), "koulutus"),
        perusteenDiaarinumero = Some("39/011/2014"))
      ),
      vahvistus = vahvistusPaikkakunnallaJaValinnaisellaTittelillä(date(2015, 10, 3), stadinAmmattiopisto, helsinki)
    )),
    selite = "Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta"))

  private def valmaKurssinSuoritus(
    koodi: String,
    kuvaus: String,
    laajuusOsaamispisteissä: Float,
    arviointi: Option[List[AmmatillinenArviointi]],
    pakollinen: Boolean,
    tunnustettu: Option[OsaamisenTunnustaminen] = None,
    näyttö: Option[Näyttö] = None) =
    ValmaKoulutuksenOsanSuoritus(
      koulutusmoduuli = PaikallinenValmaKoulutuksenOsa(
        tunniste = PaikallinenKoodi(koodi, finnish(kuvaus)),
        kuvaus = finnish(kuvaus),
        laajuus = Some(LaajuusOsaamispisteissä(laajuusOsaamispisteissä)),
        pakollinen = pakollinen
      ),
      arviointi = arviointi,
      tunnustettu = tunnustettu,
      näyttö = näyttö
    )
}
