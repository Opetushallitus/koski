package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesLukioonValmistavaKoulutus {
  val luvaTodistus = Oppija(
    MockOppijat.luva.vainHenkilötiedot,
    List(
      LukioonValmistavanKoulutuksenOpiskeluoikeus(
        alkamispäivä = Some(date(2008, 8, 15)),
        päättymispäivä = Some(date(2016, 6, 4)),
        oppilaitos = Some(jyväskylänNormaalikoulu),
        koulutustoimija = None,
        tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          LukionOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )),
        suoritukset = List(LukioonValmistavanKoulutuksenSuoritus(
          tila = tilaValmis,
          vahvistus = vahvistusPaikkakunnalla(),
          toimipiste = jyväskylänNormaalikoulu,
          koulutusmoduuli = LukioonValmistavaKoulutus(),
          osasuoritukset = Some(List(
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              ÄidinkieliJaKirjallisuus(Koodistokoodiviite("LVAIK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "AI7", koodistoUri = "oppiaineaidinkielijakirjallisuus")),
              tila = tilaValmis,
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus("STK", "Suomi toisena kielenä ja kirjallisuus", 2.0f)
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              MuutKielet(Koodistokoodiviite("LVMUUTK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "SV", koodistoUri = "kielivalikoima")),
              tila = tilaValmis,
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus("RU1", "Ruotsin alkeet", 1.0f)
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(Koodistokoodiviite("LVMALUO", "oppiaineetluva")),
              tila = tilaValmis,
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus("MAT1", "Matematiikan kertauskurssi", 1.0f)
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(Koodistokoodiviite("LVYHKU", "oppiaineetluva")),
              tila = tilaValmis,
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus("YHKU1", "Yhteiskuntatietous ja kulttuurintuntemus", 1.0f)
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(Koodistokoodiviite("LVOPO", "oppiaineetluva")),
              tila = tilaValmis,
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus("OPO1", "Opinto-ohjaus", 1.0f)
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              PaikallinenLukioonValmistavanKoulutuksenOppiaine(PaikallinenKoodi("LVATK", "Tietojenkäsittely"), "Tietojenkäsittely", pakollinen = false),
              tila = tilaValmis,
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus("ATK1", "Tietokoneen käytön peruskurssi", 1.0f)
              ))
            ),
            LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa(
              lukionKieli("A1", "EN"),
              tila = tilaValmis,
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                kurssisuoritus(valtakunnallinenKurssi("ENA1")).copy(arviointi = numeerinenArviointi(8))
              ))
            )
          ))
        )),
        lisätiedot = Some(LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(
          pidennettyPäättymispäivä = true,
          ulkomainenVaihtoopiskelija = false,
          ulkomaanjaksot = Some(List(Ulkomaanjakso(date(2012, 9, 1), Some(date(2013, 9, 1)), ruotsi, "Harjoittelua ulkomailla"))),
          oikeusMaksuttomaanAsuntolapaikkaan = true
        ))
      )
    )
  )
  val examples = List(Example("lukioon valmistava koulutus", "Oppija on suorittanut lukioon valmistavan koulutuksen (LUVA)", luvaTodistus, 200))

  private def luvaKurssinSuoritus(koodi: String, kuvaus: String, laajuusKursseissa: Float) = LukioonValmistavanKurssinSuoritus(
    tila = tilaValmis,
    koulutusmoduuli = LukioonValmistavanKoulutuksenKurssi(
      tunniste = PaikallinenKoodi(koodi, LocalizedString.finnish(kuvaus)),
      laajuus = laajuus(laajuusKursseissa),
      LocalizedString.finnish(kuvaus)
    ),
    arviointi = sanallinenArviointi("S")
  )

}
