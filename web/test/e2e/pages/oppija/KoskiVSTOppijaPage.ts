import { Page, Locator, expect } from '@playwright/test'
import { KoskiOppijaPage } from './KoskiOppijaPage'

export class KoskiVSTOppijaPage extends KoskiOppijaPage {
  readonly __NEW_UI_opiskeluoikeuksienTiedot: Locator

  constructor(page: Page) {
    super(page)
    this.__NEW_UI_opiskeluoikeuksienTiedot = page.getByTestId('opiskeluoikeuksientiedot')
  }

  async expectSuoritusUrl(suoritus: string, hyväksyttyPostfix = '.*') {
    await expect(this.page).toHaveURL(
      new RegExp(
        `koski\\/oppija\\/1\\.2\\..*\\?1\\.2\\..*\\.suoritus=${suoritus}${hyväksyttyPostfix}$`
      ),
      { timeout: 30000 }
    )
  }
}
