import { Page } from '@playwright/test'

export class KoskiVirkailijaPage {
  constructor(private readonly page: Page) {}

  async goto() {
    await this.page.goto(`/koski/virkailija`)
  }
}
