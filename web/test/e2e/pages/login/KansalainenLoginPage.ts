import { Locator, Page } from '@playwright/test'
import { expect } from '../../base'

export class KansalainenLoginPage {
  readonly hetuInput: Locator
  readonly loginButton: Locator

  constructor(private readonly page: Page) {
    this.hetuInput = page.getByLabel('Henkilötunnus')
    this.loginButton = page.getByRole('button', { name: 'Kirjaudu sisään' })
  }

  async goto() {
    await this.page.goto('/koski')
    await expect(this.page).toHaveURL(/\/koski$/)
    await this.loginButton.click()
    await expect(this.page).toHaveURL(/\/koski\/login\/oppija\/local$/)
  }

  /**
   * Kirjautuu Koski-palvelun kansalaisnäkymään käyttämällä henkilötunnusta.
   * @param hetu Henkilötunnus
   */
  async loginWithHetu(hetu: string) {
    await this.goto()
    await expect(this.page).toHaveURL(/\/koski\/login\/oppija\/local$/)
    await this.hetuInput.type(hetu)
    await this.loginButton.click()
    await expect(this.page).toHaveURL(/\/koski\/omattiedot$/)
  }

  /**
   * Kirjautuu Koski-palvelun kansalaisnäkymään käyttämällä henkilötunnusta.
   * @param hetu Henkilötunnus
   */
  async apiLoginWithHetu(hetu: string) {
    throw new Error('Not implemented')
  }

  /**
   * Kirjautuu Koski-palvelun kansalaisnäkymästä.
   */
  async apiLogout() {
    throw new Error('Not implemented')
  }
}
