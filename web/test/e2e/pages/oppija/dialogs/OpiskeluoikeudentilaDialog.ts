import { Locator, Page } from '@playwright/test'

export class OpiskeluoikeudenTilaDialog {
  readonly avaaDialogiBtn: Locator
  readonly tilavalitsin: Locator
  readonly valittavatTilat: Locator

  constructor(page: Page) {
    this.avaaDialogiBtn = page.getByTestId('lisää-opiskeluoikeuden-tila-btn')
    this.tilavalitsin = page.getByTestId('opiskeluoikeuden-tila-valitsin')
    this.valittavatTilat = this.tilavalitsin.getByRole('listitem')
  }

  async avaa() {
    await this.avaaDialogiBtn.click()
  }
}
