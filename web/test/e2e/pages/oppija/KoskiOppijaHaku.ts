import { Page, Locator } from '@playwright/test'
import { expect } from '../../base'

export class KoskiOppijaHaku {
  readonly page: Page
  readonly hakuInput: Locator
  readonly hakutulokset: Locator
  readonly eiHakutuloksia: Locator

  constructor(page: Page) {
    this.page = page
    this.hakuInput = page.getByPlaceholder(
      'henkilötunnus, nimi tai oppijanumero'
    )
    this.hakutulokset = page.getByRole('list', { name: 'Hakutulokset' })
    this.eiHakutuloksia = page.getByRole('status', { name: 'Ei hakutuloksia' })
  }

  /**
   * Hakee Koskesta oppijoita
   * @param searchString Hakuehto
   * @returns expectNotEmpty, expectEmpty ja clickOnFirst -handlerit
   */
  async search(searchString: string) {
    await expect(this.page).toHaveURL(/\/koski\/(virkailija|oppija)/)
    await this.hakuInput.fill(searchString)
    return {
      expectNotEmpty: async () => {
        await expect(this.hakutulokset).toBeVisible()
        await expect(this.hakutulokset).not.toBeEmpty()
      },
      expectEmpty: async () => {
        await expect(this.eiHakutuloksia).toBeVisible()
      },
      searchResults: async () => {
        await expect(this.hakutulokset).toBeVisible()
        return this.hakutulokset.allTextContents()
      },
      clickOnFirst: async () => {
        await expect(this.hakutulokset).toBeVisible()
        await expect(this.hakutulokset).not.toBeEmpty()
        await this.hakutulokset.first().getByRole("link").click()
        await expect(this.page).toHaveURL(/\/koski\/oppija\//)
      }
    }
  }
}
