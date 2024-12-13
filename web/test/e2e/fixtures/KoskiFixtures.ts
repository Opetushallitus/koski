import { expect, Page } from '@playwright/test'
import { Oppija } from '../../../app/types/fi/oph/koski/schema/Oppija'
import { Raw } from '../../../app/util/schema'
import { HenkilönOpiskeluoikeusVersiot } from '../../../app/types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'

export class KoskiFixtures {
  constructor(private readonly page: Page) { }

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

  async putOppija(oppija: Raw<Oppija>): Promise<HenkilönOpiskeluoikeusVersiot> {
    const response = await this.page.request.put('/koski/api/oppija', { data: oppija })
    const body = await response.body()
    return JSON.parse(body.toString()) as HenkilönOpiskeluoikeusVersiot
  }

  async teardown() {
    // TODO: Teardown voisi resetoida fixturet
    await Promise.resolve(true)
  }

  /**
   * Kirjautuu Koski-palveluun tekemällä POST-pyynnön resurssiin /koski/user/login.
   * @param username Username
   * @param password Password
   */
  async apiLoginAsUser(username: string, password: string) {
    const request = await this.page.request.post('/koski/user/login', {
      data: {
        username,
        password
      }
    })
    expect(request.ok()).toBeTruthy()
  }

  /**
   * Kirjautuu ulos Koski-palvelusta tekemällä pyynnön resurssiin /koski/user/logout.
   */
  async apiLogout() {
    const request = await this.page.request.get('/koski/user/logout')
    expect(request.ok()).toBeTruthy()
  }
}
