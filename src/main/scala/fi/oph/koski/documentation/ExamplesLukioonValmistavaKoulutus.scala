package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._

object ExamplesLukioonValmistavaKoulutus {
  val luvaTodistus = Oppija(
    MockOppijat.luva.vainHenkilötiedot,
    List(
      LukioonValmistavanKoulutuksenOpiskeluoikeus(
        alkamispäivä = Some(date(2008, 8, 15)),
        päättymispäivä = Some(date(2016, 6, 4)),
        oppilaitos = jyväskylänNormaalikoulu,
        koulutustoimija = None,
        läsnäolotiedot = None,
        tila = None,
        suoritukset = List(LukioonValmistavanKoulutuksenSuoritus(
          tila = tilaValmis,
          vahvistus = vahvistus(),
          toimipiste = jyväskylänNormaalikoulu,
          koulutusmoduuli = LukioonValmistavaKoulutus(),
          osasuoritukset = Some(List(
            luvaKurssinSuoritus("STK", "Suomi toisena kielenä ja kirjallisuus", 2.0f),
            luvaKurssinSuoritus("STK", "Yhteiskuntatietous ja kulttuurintuntemus", 1.0f),
            luvaKurssinSuoritus("STK", "Opinto-ohjaus", 1.0f),
            kurssisuoritus(valtakunnallinenKurssi("KU1")).copy(arviointi = kurssinArviointi(7))
          ))
        ))
      )
    )
  )
  val examples = List(Example("lukioon valmistava koulutus", "Oppija on suorittanut lukioon valmistavan koulutuksen (LUVA)", luvaTodistus, 200))

  private def luvaKurssinSuoritus(koodi: String, kuvaus: String, laajuusKursseissa: Float) = LukioonValmistavanKurssinSuoritus(
    tila = tilaValmis,
    koulutusmoduuli = LukioonValmistavanKoulutuksenKurssi(
      tunniste = PaikallinenKoodi(koodi, LocalizedString.finnish(kuvaus)),
      laajuus = laajuus(laajuusKursseissa)
    ),
    arviointi = kurssinArviointi("S")
  )

}
