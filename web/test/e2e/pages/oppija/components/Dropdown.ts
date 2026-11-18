import { Locator, Page } from '@playwright/test'
import { expect } from '../../../base'

export class Dropdown {
  readonly page: Page
  readonly container: Locator
  readonly textInput: Locator
  readonly options: Locator

  static fromTestId(page: Page, testId: string) {
    return new this(page, page.getByTestId(testId))
  }

  // Käytetään kun pudotusvalikon ulkokehyksellä ei ole data-testid:tä, mutta
  // sen sisällä olevalla combobox-syötteellä on oma, näkyvä nimi (esim.
  // placeholderista tuleva aria-label). Kehystä itseään ei voi hakea roolilla,
  // koska nimi on vain sisäkkäisellä elementillä, ei kehyksen omalla
  // role=combobox-elementillä; .dropdown on pudotusvalikkokomponentin oma
  // yleinen luokka (ei käyttökohtainen esi-isäluokka).
  static fromComboboxName(scope: Locator, name: string | RegExp) {
    const page = scope.page()
    return new this(
      page,
      scope
        .locator('.dropdown')
        .filter({ has: page.getByRole('combobox', { name }) })
    )
  }

  constructor(page: Page, container: Locator) {
    this.page = page
    this.container = container
    this.textInput = this.container.getByRole('combobox')
    this.options = this.container.getByRole('listitem')
  }

  async selectOptionByClick(optionName: string | RegExp) {
    await this.textInput.click()
    await this.container.getByRole('listitem', { name: optionName, exact: true }).click()
  }

  // Osa vaihtoehdoista ei näy oletuslistassa (katkaistu/rajattu alijoukko) -
  // hakuteksti suodattaa koko koodiston, ja suodatettu vaihtoehto klikataan.
  // Fill + klikkaus, ei Enter: Enter-painallus sulkee joissain konteksteissa
  // (esim. valintaikkunassa) koko dialogin sen sijaan että valitsisi
  // vaihtoehdon. optionName oletuksena sanarajalla ankkuroitu haku (\b) -
  // estää esim. "RUA2" osumasta myös "RUA20":een, jos koodisto joskus
  // sisältää molemmat.
  async filterAndSelectByClick(
    hakuteksti: string,
    optionName: string | RegExp = new RegExp(`^${hakuteksti}\\b`)
  ) {
    await this.textInput.fill(hakuteksti)
    await this.options.filter({ hasText: optionName }).first().click()
  }
}
