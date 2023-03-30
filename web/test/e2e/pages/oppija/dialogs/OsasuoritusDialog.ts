import { Locator, Page } from '@playwright/test'

export class OsasuoritusDialog {
  readonly dropdown: Locator
  readonly nimiField: Locator
  readonly submitBtn: Locator

  constructor(page: Page) {
    this.dropdown = page.getByTestId('dropdown-osasuoritus')
    this.nimiField = page.getByRole('textbox', {
      name: 'Opintokokonaisuuden nimi'
    })
    this.submitBtn = page.getByRole('button', { name: 'Lisää osasuoritus' })
  }

  async lisääUusiOsasuoritus(nimi: string, locate: (l: Locator) => Locator) {
    const dropdown = locate(this.dropdown)
    await dropdown.click()
    await dropdown.getByTestId('new-osasuoritus').click()
    await this.nimiField.fill(nimi)
    await this.submitBtn.click()
  }

  async lisääTallennettuOsasuoritus(
    nimi: string,
    locate: (l: Locator) => Locator
  ) {
    const dropdown = locate(this.dropdown)
    await dropdown.click()
    await dropdown.getByText(nimi).click()
  }
}
