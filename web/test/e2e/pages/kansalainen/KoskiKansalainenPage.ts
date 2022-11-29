import { Page } from '@playwright/test'

export class KoskiKansalainenPage {
  constructor(private readonly page: Page) {}

  async goto() {
    await this.page.goto(`/koski`)
  }
}
