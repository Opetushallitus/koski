import { Page } from '@playwright/test'
import { expect } from '../base'

export class KoskiFixtures {
  constructor(private readonly page: Page) {}

  async reset(reloadRaportointikanta = false) {
    const params = new URLSearchParams({
      reloadRaportointikanta: reloadRaportointikanta ? 'true' : 'false'
    })
    const request = await this.page.request.post(
      `/koski/fixtures/reset?${params.toString()}`
    )
    expect(request.ok()).toBeTruthy()
  }
}
