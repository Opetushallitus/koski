package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesLukioonValmistavaKoulutus {
  val luvaTodistus = Oppija(
    asUusiOppija(MockOppijat.luva),
    List(
      LukioonValmistavanKoulutuksenOpiskeluoikeus(
        oppilaitos = Some(jyväskylänNormaalikoulu),
        koulutustoimija = None,
        tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          LukionOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )),
        suoritukset = List(LukioonValmistavanKoulutuksenSuoritus(
          oppimäärä = nuortenOpetussuunnitelma,
          vahvistus = vahvistusPaikkakunnalla(),
          toimipiste = jyväskylänNormaalikoulu,
          koulutusmoduuli = LukioonValmistavaKoulutus(perusteenDiaarinumero = Some("56/011/2015")),
          suorituskieli = suomenKieli,
          osasuoritukset = Some(List(
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              LukioonValmistavaÄidinkieliJaKirjallisuus(Koodistokoodiviite("LVAIK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "AI7", koodistoUri = "oppiaineaidinkielijakirjallisuus")),
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus(valtakunnallinenLuvaKurssi("LVS1").copy(laajuus = laajuus(2.0f)))
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              MuutKielet(Koodistokoodiviite("LVPOAK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "SV", koodistoUri = "kielivalikoima")),
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus(valtakunnallinenLuvaKurssi("LVKA1"))
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(Koodistokoodiviite("LVMALUO", "oppiaineetluva")),
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus(valtakunnallinenLuvaKurssi("LVLUMA1"))
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(Koodistokoodiviite("LVYHKU", "oppiaineetluva")),
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus(valtakunnallinenLuvaKurssi("LVHY1"))
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(Koodistokoodiviite("LVOPO", "oppiaineetluva")),
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus(valtakunnallinenLuvaKurssi("LVOP1"))
              ))
            ),
            LukioonValmistavanKoulutuksenOppiaineenSuoritus(
              PaikallinenLukioonValmistavanKoulutuksenOppiaine(PaikallinenKoodi("LVATK", "Tietojenkäsittely"), "Tietojenkäsittely", pakollinen = false),
              arviointi = arviointi("S"),
              osasuoritukset = Some(List(
                luvaKurssinSuoritus(paikallinenLuvaKurssi("ATK1", "Tietokoneen käytön peruskurssi", "Kurssilla opiskellaan tietokoneen, toimisto-ohjelmien sekä internetin ja sähköpostin peruskäyttöä."))
              ))
            ),
            LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa(
              lukionKieli("A1", "EN"),
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
          oikeusMaksuttomaanAsuntolapaikkaan = true,
          sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2013, 9, 1), Some(date(2013, 12, 12)))))
        ))
      )
    )
  )
  val examples = List(Example("lukioon valmistava koulutus", "Oppija on suorittanut lukioon valmistavan koulutuksen (LUVA)", luvaTodistus, 200))

  private def luvaKurssinSuoritus(kurssi: LukioonValmistavanKoulutuksenKurssi) = LukioonValmistavanKurssinSuoritus(
    koulutusmoduuli = kurssi,
    arviointi = sanallinenArviointi("S")
  )

  private def valtakunnallinenLuvaKurssi(kurssi: String) =
    ValtakunnallinenLukioonValmistavanKoulutuksenKurssi(Koodistokoodiviite(koodistoUri = "lukioonvalmistavankoulutuksenkurssit2015", koodiarvo = kurssi), laajuus(1.0f))

  private def paikallinenLuvaKurssi(koodi: String, nimi: String, kuvaus: String) =
    PaikallinenLukioonValmistavanKoulutuksenKurssi(PaikallinenKoodi(koodi, LocalizedString.finnish(nimi)), laajuus(1.0f), LocalizedString.finnish(kuvaus))
}
