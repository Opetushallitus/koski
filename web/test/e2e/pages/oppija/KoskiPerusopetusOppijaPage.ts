import { Page, Locator, expect } from '@playwright/test'
import { KoskiOppijaPage } from './KoskiOppijaPage'

export class KoskiPerusopetusOppijaPage extends KoskiOppijaPage {
  readonly avaaKaikkiOsasuorituksetBtn: Locator
  readonly suljeKaikkiOsasuorituksetBtn: Locator
  readonly opiskeluoikeudet?: Locator

  constructor(page: Page) {
    super(page)

    this.avaaKaikkiOsasuorituksetBtn = page.getByRole('button', {
      name: 'Avaa kaikki'
    })
    this.suljeKaikkiOsasuorituksetBtn = page.getByRole('button', {
      name: 'Sulje kaikki'
    })
  }

  async expectSuoritusUrl(suoritus: string, hyväksyttyPostfix: string = '.*') {
    await expect(this.page).toHaveURL(
      new RegExp(
        `koski\\/oppija\\/1\\.2\\..*\\?1\\.2\\..*\\.suoritus=${suoritus}${hyväksyttyPostfix}$`
      ),
      { timeout: 20000 }
    )
  }
}
