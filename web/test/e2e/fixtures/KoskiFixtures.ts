import { Page, expect } from '@playwright/test'

export class KoskiFixtures {
  constructor(private readonly page: Page) {}

  async reset() {
    const request = await this.page.request.post('/koski/fixtures/reset')
    expect(request.ok()).toBeTruthy()
  }
}
