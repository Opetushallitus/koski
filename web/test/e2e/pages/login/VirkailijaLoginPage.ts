import { expect, Locator, Page } from '@playwright/test'

export class VirkailijaLoginPage {
  readonly usernameInput: Locator
  readonly passwordInput: Locator
  readonly loginButton: Locator

  constructor(private readonly page: Page) {
    // TODO: Refaktoroi käyttämään aria-attribuutteja tai data-testid -attribuuttia
    this.usernameInput = page.getByLabel('Tunnus')
    // TODO: Refaktoroi käyttämään aria-attribuutteja tai data-testid -attribuuttia
    this.passwordInput = page.getByLabel('Salasana')
    this.loginButton = page.getByRole('button', { name: 'Kirjaudu sisään' })
  }

  async goto() {
    await this.page.goto('/koski/login')
  }

  /**
   * Kirjautuu Koski-palveluun käyttämällä sisäänkirjautumislomaketta.
   * @param username Username
   * @param password Password
   */
  async loginAsUser(username: string, password: string) {
    await this.usernameInput.type(username)
    await this.passwordInput.type(password)
    await this.loginButton.click()
    // Uudelleenohjaus /koski/virkailija
    await expect(this.page).toHaveURL(/koski\/virkailija/)
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
   * Kirjautuu ulos Koski-palvelusta tekemällä POST-pyynnön resurssiin /koski/user/logout.
   */
   async apiLogout() {
    const request = await this.page.request.post('/koski/user/logout')
    expect(request.ok()).toBeTruthy()
  }
}
