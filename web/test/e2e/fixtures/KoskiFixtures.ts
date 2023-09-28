import { expect, Page } from '@playwright/test'

export class KoskiFixtures {
  constructor(private readonly page: Page) {}

  /**
   * Resetoi Koski-fixturet.
   * @param reloadRaportointikanta Raportointikannan reload
   * @param reloadYTR YTR:n reload
   */
  async reset(reloadRaportointikanta = false, reloadYTR = false) {
    const params = new URLSearchParams({
      reloadRaportointikanta: reloadRaportointikanta ? 'true' : 'false',
      reloadYTR: reloadYTR ? 'true' : 'false'
    })
    const request = await this.page.request.post(
      `/koski/fixtures/reset?${params.toString()}`
    )
    expect(await request.json()).not.toEqual([
      {
        key: 'forbidden.vainVirkailija',
        message: 'Sallittu vain virkailija-käyttäjille'
      }
    ])
    expect(request.ok()).toBeTruthy()
  }

  async teardown() {
    // TODO: Teardown voisi resetoida fixturet
    await Promise.resolve(true)
  }
}
