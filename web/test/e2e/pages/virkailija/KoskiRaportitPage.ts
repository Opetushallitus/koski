import { Locator, Page } from '@playwright/test'
import { expect } from '../../base'

export class KoskiRaportitPage {
  readonly muodostaButton: Locator
  readonly taustallaOhje: Locator
  readonly raportit: Locator
  readonly raporttiRivit: Locator

  constructor(private readonly page: Page) {
    this.muodostaButton = page.getByTestId('massaluovutus.start')
    this.taustallaOhje = page.getByTestId('massaluovutus.taustalla')
    this.raportit = page.getByTestId('massaluovutus.raportit')
    this.raporttiRivit = this.raportit.locator('tbody tr')
  }

  async gotoAmmatillinenSuoritustiedot() {
    await this.page.goto(
      '/koski/raportit/ammatillinen-koulutus/ammatillinentutkintosuoritustietojentarkistus'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async syötäAikajakso(alku: string, loppu: string) {
    await this.page.locator('#dateinput-alku').fill(alku)
    await this.page.locator('#dateinput-loppu').fill(loppu)
    await expect(this.muodostaButton).toBeEnabled()
  }

  async muodosta() {
    await this.muodostaButton.click()
  }

  latauslinkki(rivi: Locator): Locator {
    return rivi.getByRole('link', { name: 'Lataa Excel-tiedosto' })
  }

  salasana(rivi: Locator): Locator {
    return rivi.locator('td.password')
  }
}
