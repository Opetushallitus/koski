import { Locator } from '@playwright/test'
import { Dropdown } from './Dropdown'

export class LukioOpiskeluoikeus {
  readonly container: Locator
  readonly tutkinnonNimi: Locator
  readonly opiskeluoikeudenTiedot: Locator
  readonly omanÄidinkielenOpinnot: Locator
  readonly tilaVahvistus: Locator
  readonly oppiaineet: Locator

  constructor(container: Locator) {
    this.container = container
    this.tutkinnonNimi = this.kenttä('koulutusmoduuli').locator('.tunniste')
    this.opiskeluoikeudenTiedot = this.container.locator(
      '.opiskeluoikeuden-tiedot'
    )
    this.omanÄidinkielenOpinnot = this.kenttä('omanÄidinkielenOpinnot')
    this.tilaVahvistus = this.container.locator('.suoritus > .tila-vahvistus')
    // Oppiaineiden/kurssien taulukko on oma React-komponenttinsa, ei
    // PropertiesEditorin kautta renderöity - ei siis data-testid:tä.
    // Sama taulukkoluokka on yhteinen "Lukion oppimäärä"- ja
    // "Lukion oppiaineiden oppimäärien suoritus" -suoritustyypeille.
    this.oppiaineet = this.container.locator('table.suoritukset.oppiaineet')
  }

  oppiaineRivi(koodi: string): Locator {
    return this.oppiaineet.locator(`.oppiaine-rivi.${koodi}`)
  }

  // Kurssi tunnistetaan sen näkyvästä koodista (esim. "ÄI1") roolin ja nimen
  // kautta, ei sijainnista listassa - kurssien järjestys ei ole osa
  // testattavaa käyttäytymistä.
  kurssi(oppiaineRivi: Locator, koodi: string): Locator {
    return oppiaineRivi.locator('li.kurssi').filter({
      has: oppiaineRivi.page().getByRole('button', { name: koodi, exact: true })
    })
  }

  // Kieliaineen kieli (esim. "Suomen kieli ja kirjallisuus") ei ole oma
  // PropertiesEditor-rivi, vaan osa oppiaineen otsikkoa - ei data-testid:tä
  // ympärillä, CSS-skoppaus sama kuin vanhassa Mocha-testissä.
  kieliaineenKieliDropdown(oppiaineRivi: Locator): Dropdown {
    return new Dropdown(
      oppiaineRivi.page(),
      oppiaineRivi
        .locator('.title .properties .dropdown-wrapper')
        .getByTestId('enum-editor-dropdown')
    )
  }

  suorituskieliDropdown(oppiaineRivi: Locator): Dropdown {
    return new Dropdown(
      oppiaineRivi.page(),
      this.kenttä('suorituskieli', oppiaineRivi).getByTestId(
        'enum-editor-dropdown'
      )
    )
  }

  arvosanaDropdown(oppiaineRivi: Locator): Dropdown {
    return new Dropdown(
      oppiaineRivi.page(),
      oppiaineRivi.locator('td.arvosana').getByTestId('enum-editor-dropdown')
    )
  }

  // Yleinen PropertiesEditor (web/app/editor/PropertiesEditor.jsx) merkitsee joka
  // skeemakentän arvon data-testid:llä "<kenttäavain>-value" - käytetään sitä
  // suoraan CSS-luokkien sijaan. .first(), koska sama kenttäavain (esim.
  // arvosana) voi toistua sisäkkäisissä osasuorituksissa; DOM-järjestyksessä
  // oma kenttä tulee aina ennen sisäkkäisiä.
  kenttä(avain: string, juuri: Locator = this.container): Locator {
    return juuri.getByTestId(`${avain}-value`).first()
  }
}
