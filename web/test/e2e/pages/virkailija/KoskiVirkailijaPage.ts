import { Locator, Page } from '@playwright/test'
import { expect } from '../../base'

export class KoskiVirkailijaPage {
  readonly virhepalkki: Locator

  constructor(private readonly page: Page) {
    this.virhepalkki = page.getByTestId('error')
  }

  async goto() {
    await this.page.goto(`/koski/virkailija`)
    await expect(this.page).toHaveURL(/\/koski\/virkailija/)
  }

  async virheilmoitus(): Promise<string> {
    return await this.virhepalkki.innerText()
  }
}
