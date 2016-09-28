package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._

object ExamplesValma {
  val valmaTodistus = Oppija(
    MockOppijat.valma.vainHenkilötiedot,
    List(
      AmmatillinenOpiskeluoikeus(
        alkamispäivä = Some(date(2009, 9, 14)),
        päättymispäivä = Some(date(2016, 6, 4)),
        tila = AmmatillinenOpiskeluoikeudenTila(List(
          AmmatillinenOpiskeluoikeusjakso(date(2009, 9, 14), opiskeluoikeusLäsnä, None),
          AmmatillinenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut, None)
        )),
        oppilaitos = stadinAmmattiopisto,
        suoritukset = List(AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenSuoritus(
          tila = tilaValmis,
          vahvistus = vahvistus(date(2016, 6, 4), stadinAmmattiopisto, helsinki),
          toimipiste = stadinAmmattiopisto,
          koulutusmoduuli = AmmatilliseenPeruskoulutukseenValmentavaKoulutus(),
          osasuoritukset = Some(List(
            valmaKurssinSuoritus("AKO", "Ammatilliseen koulutukseen orientoituminen ja työelämän perusvalmiuksien hankkiminen", 10f, arviointiHyväksytty, pakollinen = true),
            valmaKurssinSuoritus("OV", "Opiskeluvalmiuksien vahvistaminen", 10f, arviointiHyväksytty, pakollinen = false),
            valmaKurssinSuoritus("TOV", "Työssäoppimiseen ja oppisopimuskoulutukseen valmentautuminen", 15f, arviointiHyväksytty, pakollinen = false),
            valmaKurssinSuoritus("ATH", "Arjen taitojen ja hyvinvoinnin vahvistaminen", 10f, arviointiHyväksytty, pakollinen = false),
            valmaKurssinSuoritus("APT", "Ammatillisen perustutkinnon tutkinnon osat tai osa-alueet", 15f, arviointiKiitettävä, pakollinen = false, tunnustettu = tunnustettu, näyttö = Some(näyttö("Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki", Some(näytönArviointi))))
          ))
        )),
        tavoite = tavoiteTutkinto
      )
    )
  )

  val examples = List(Example("ammatilliseen peruskoulutukseen valmentava koulutus", "Oppija on suorittanut ammatilliseen peruskoulutukseen valmentavan koulutuksen (VALMA)", valmaTodistus, 200))

  lazy val tunnustettu: Some[OsaamisenTunnustaminen] = Some(OsaamisenTunnustaminen(
    osaaminen = Some(AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100209", Some("Asennuksen ja automaation perustyöt"), "tutkinnonosat", Some(1)), true, None),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      tutkinto = Some(AmmatillinenTutkintoKoulutus(
        tunniste = Koodistokoodiviite("351101", Some("Kone- ja metallialan perustutkinto"), "koulutus"),
        perusteenDiaarinumero = Some("39/011/2014"))
      ),
      vahvistus = vahvistus(date(2015, 10, 3), stadinAmmattiopisto, helsinki)
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
    AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsanSuoritus(
      tila = tilaValmis,
      koulutusmoduuli = AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa(
        tunniste = PaikallinenKoodi(koodi, LocalizedString.finnish(kuvaus)),
        laajuus = Some(LaajuusOsaamispisteissä(laajuusOsaamispisteissä)),
        pakollinen = pakollinen
      ),
      arviointi = arviointi,
      tunnustettu = tunnustettu,
      näyttö = näyttö
    )
}
