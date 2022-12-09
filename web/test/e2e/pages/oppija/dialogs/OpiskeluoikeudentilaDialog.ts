import { Locator, Page } from '@playwright/test'

export class OpiskeluoikeudenTilaDialog {
  readonly avaaDialogiBtn: Locator
  readonly vahvistaBtn: Locator
  readonly tilavalitsin: Locator
  readonly valittavatTilat: Locator
  readonly lisaaOpiskeluoikeusjaksoInput: Locator

  constructor(page: Page) {
    this.avaaDialogiBtn = page.getByTestId('lisää-opiskeluoikeuden-tila-btn')
    this.vahvistaBtn = page.getByTestId('dialog-vahvista')
    this.tilavalitsin = page.getByTestId('opiskeluoikeuden-tila-valitsin')
    this.valittavatTilat = this.tilavalitsin.getByRole('listitem')
    this.lisaaOpiskeluoikeusjaksoInput = page
      .getByTestId('lisaa-opiskeluoikeusjakso')
      .locator('input[type="text"]')
  }

  async avaa() {
    await this.avaaDialogiBtn.click()
  }

  async vahvista() {
    await this.vahvistaBtn.click()
  }

  async syötäPäivämäärä(pvm: string) {
    await this.lisaaOpiskeluoikeusjaksoInput.click()
    await this.lisaaOpiskeluoikeusjaksoInput.fill(pvm)
  }

  async lisääTila(pvm: string, tila: string) {
    await this.avaa()
    await this.tilavalitsin.getByText(tila).click()
    await this.syötäPäivämäärä(pvm)
    await this.vahvista()
  }
}
