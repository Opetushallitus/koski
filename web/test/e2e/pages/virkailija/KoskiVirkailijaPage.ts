import { Locator, Page } from '@playwright/test'

export class KoskiVirkailijaPage {
  readonly virhepalkki: Locator

  constructor(private readonly page: Page) {
    this.virhepalkki = page.getByTestId('error')
  }

  async goto() {
    await this.page.goto(`/koski/virkailija`)
  }

  async virheilmoitus(): Promise<string> {
    return await this.virhepalkki.innerText()
  }
}
