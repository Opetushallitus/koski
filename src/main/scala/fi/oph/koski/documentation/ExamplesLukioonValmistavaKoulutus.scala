package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.Lukio2019ExampleData.{lukionKieli2019, moduulinSuoritusOppiaineissa, numeerinenArviointi, numeerinenLukionOppiaineenArviointi, vieraanKielenModuuliOppiaineissa}
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesLukioonValmistavaKoulutus {
  val lukioonValmistavanKoulutuksenSuoritus = LukioonValmistavanKoulutuksenSuoritus(
    oppimäärä = nuortenOpetussuunnitelma,
    vahvistus = vahvistusPaikkakunnalla(),
    toimipiste = jyväskylänNormaalikoulu,
    koulutusmoduuli = LukioonValmistavaKoulutus(perusteenDiaarinumero = Some("56/011/2015"), laajuus = Some(laajuus(2.0f))),
    suorituskieli = suomenKieli,
    osasuoritukset = Some(List(
      LukioonValmistavanKoulutuksenOppiaineenSuoritus(
        LukioonValmistavaÄidinkieliJaKirjallisuus(Koodistokoodiviite("LVAIK", "oppiaineetluva"), kieli = Koodistokoodiviite(koodiarvo = "AI7", koodistoUri = "oppiaineaidinkielijakirjallisuus")),
        arviointi = arviointi("S"),
        osasuoritukset = Some(List(
          luvaKurssinSuoritus(valtakunnallinenLuvaKurssi("LVS1").copy(laajuus = Some(laajuus(2.0f))))
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
          kurssisuoritus(valtakunnallinenKurssi("ENA1")).copy(arviointi = LukioExampleData.numeerinenArviointi(8))
        ))
      )
    ))
  )

  val lukioonValmistavanKoulutuksenSuoritus2019 = lukioonValmistavanKoulutuksenSuoritus.copy(
    koulutusmoduuli = LukioonValmistavaKoulutus(perusteenDiaarinumero = Some("OPH-4958-2020"), laajuus = Some(laajuus2019(3.0))),
    osasuoritukset = Some(List(
      LukioonValmistavanKoulutuksenOppiaineenSuoritus(
        PaikallinenLukioonValmistavanKoulutuksenOppiaine(PaikallinenKoodi("LVATK", "Tietojenkäsittely"), "Tietojenkäsittely", pakollinen = false),
        arviointi = arviointi("S"),
        osasuoritukset = Some(List(
          luvaKurssinSuoritus(paikallinenLuvaKurssi("ATK1", "Tietokoneen käytön peruskurssi", "Kurssilla opiskellaan tietokoneen, toimisto-ohjelmien sekä internetin ja sähköpostin peruskäyttöä.")
            .copy(laajuus = Some(laajuus2019(2.0)))
          )
        ))
      ),
      LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019(
        lukionKieli2019("AOM", "SV"),
        arviointi = numeerinenLukionOppiaineenArviointi(9),
        osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA4").copy(laajuus = laajuus2019(1))).copy(arviointi = numeerinenArviointi(7))
        ))
      )
    ))
  )

  val lukioonValmistavanKoulutuksenOpiskeluoikeus = LukioonValmistavanKoulutuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    tila = LukionOpiskeluoikeudenTila(List(
      LukionOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      LukionOpiskeluoikeusjakso(date(2016, 8, 1), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    suoritukset = List(lukioonValmistavanKoulutuksenSuoritus),
    lisätiedot = Some(LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(
      pidennettyPäättymispäivä = true,
      ulkomainenVaihtoopiskelija = false,
      ulkomaanjaksot = Some(List(Ulkomaanjakso(date(2012, 9, 1), Some(date(2013, 9, 1)), ruotsi, "Harjoittelua ulkomailla"))),
      oikeusMaksuttomaanAsuntolapaikkaan = None,
      sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2013, 9, 1), Some(date(2013, 12, 12)))))
    ))
  )

  val lukioonValmistavanKoulutuksenOpiskeluoikeus2019 = lukioonValmistavanKoulutuksenOpiskeluoikeus.copy(
    tila = LukionOpiskeluoikeudenTila(List(
      LukionOpiskeluoikeusjakso(date(2016, 8, 15), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      LukionOpiskeluoikeusjakso(date(2021, 8, 1), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    suoritukset = List(lukioonValmistavanKoulutuksenSuoritus2019),
  )

  val luvaTodistus = Oppija(
    asUusiOppija(KoskiSpecificMockOppijat.luva),
    List(
      lukioonValmistavanKoulutuksenOpiskeluoikeus
    )
  )

  val luvaTodistus2019 = Oppija(
    asUusiOppija(KoskiSpecificMockOppijat.luva2019),
    List(
      lukioonValmistavanKoulutuksenOpiskeluoikeus2019
    )
  )

  val examples = List(
    Example("lukioon valmistava koulutus", "Oppija on suorittanut lukioon valmistavan koulutuksen (LUVA)", luvaTodistus, 200),
    Example("lukioon valmistava koulutus, Lukion 2019-opsilla", "Oppija on suorittanut lukioon valmistavan koulutuksen (LUVA). Mukana lukion 2019 opsin mukaisia osasuorituksia.", luvaTodistus2019, 200)
  )

  def luvaKurssinSuoritus(kurssi: LukioonValmistavanKoulutuksenKurssi) = LukioonValmistavanKurssinSuoritus(
    koulutusmoduuli = kurssi,
    arviointi = sanallinenArviointi("S")
  )

  def valtakunnallinenLuvaKurssi(kurssi: String) =
    ValtakunnallinenLukioonValmistavanKoulutuksenKurssi(Koodistokoodiviite(koodistoUri = "lukioonvalmistavankoulutuksenkurssit2015", koodiarvo = kurssi), Some(laajuus(1.0f)))

  def valtakunnallinenLuvaKurssi2019(kurssi: String) =
    ValtakunnallinenLukioonValmistavanKoulutuksenKurssi(Koodistokoodiviite(koodistoUri = "lukioonvalmistavankoulutuksenmoduulit2019", koodiarvo = kurssi), Some(laajuus(1.0f)))

  def paikallinenLuvaKurssi(koodi: String, nimi: String, kuvaus: String) =
    PaikallinenLukioonValmistavanKoulutuksenKurssi(PaikallinenKoodi(koodi, LocalizedString.finnish(nimi)), Some(laajuus(1.0f)), LocalizedString.finnish(kuvaus))

  def laajuus2019(arvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(arvo = arvo, yksikkö = laajuusOpintopisteissä)
}
