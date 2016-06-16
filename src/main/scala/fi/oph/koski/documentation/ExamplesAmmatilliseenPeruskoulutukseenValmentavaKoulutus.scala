package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData.{arviointiHyväksytty, arviointiKiitettävä, stadinAmmattiopisto}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._

object ExamplesAmmatilliseenPeruskoulutukseenValmentavaKoulutus {
  val valmaTodistus = Oppija(
    MockOppijat.valma.vainHenkilötiedot,
    List(
      AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOpiskeluoikeus(
        alkamispäivä = Some(date(2009, 9, 14)),
        päättymispäivä = Some(date(2016, 6, 4)),
        oppilaitos = stadinAmmattiopisto,
        suoritukset = List(AmmatilliseenPerustutkintoonValmentavanKoulutuksenSuoritus(
          tila = tilaValmis,
          vahvistus = vahvistus,
          toimipiste = stadinAmmattiopisto,
          koulutusmoduuli = AmmatilliseenPerustutkintoonValmentavaKoulutus(),
          osasuoritukset = Some(List(
            valmaKurssinSuoritus("AKO", "Ammatilliseen koulutukseen orientoituminen ja työelämän perusvalmiuksien hankkiminen", 10f, arviointiHyväksytty, pakollinen = true),
            valmaKurssinSuoritus("OV", "Opiskeluvalmiuksien vahvistaminen", 10f, arviointiHyväksytty, pakollinen = false),
            valmaKurssinSuoritus("TOV", "Työssäoppimiseen ja oppisopimuskoulutukseen valmentautuminen", 15f, arviointiHyväksytty, pakollinen = false),
            valmaKurssinSuoritus("ATH", "Arjen taitojen ja hyvinvoinnin vahvistaminen", 10f, arviointiHyväksytty, pakollinen = false),
            valmaKurssinSuoritus("APT", "Ammatillisen perustutkinnon tutkinnon osat tai osa-alueet", 10f, arviointiKiitettävä, pakollinen = false)
          ))
        ))
      )
    )
  )
  val examples = List(Example("ammatilliseen peruskoulutukseen valmentava koulutus", "Oppija on suorittanut ammatilliseen peruskoulutukseen valmentavan koulutuksen (VALMA)", valmaTodistus, 200))

  private def valmaKurssinSuoritus(koodi: String, kuvaus: String, laajuusOsaamispisteissä: Float, arviointi: Option[List[AmmatillinenArviointi]], pakollinen: Boolean) = AmmatilliseenPerustutkintoonValmentavanKoulutuksenOsanSuoritus(
    tila = tilaValmis,
    koulutusmoduuli = AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa(
      tunniste = PaikallinenKoodi(koodi, LocalizedString.finnish(kuvaus), "stadin-valma-kurssit"),
      laajuus = Some(LaajuusOsaamispisteissä(laajuusOsaamispisteissä)),
      pakollinen = pakollinen
    ),
    arviointi = arviointi
  )
}
