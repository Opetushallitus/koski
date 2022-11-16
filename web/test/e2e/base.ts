import { test as base } from '@playwright/test'
import { KoskiFixtures } from './fixtures/KoskiFixtures'
import { KoskiLoginPage } from './pages/login/KoskiLoginPage'
import { KoskiOppijaHaku } from './pages/oppija/KoskiOppijaHaku'
import { KoskiOppijaPage } from './pages/oppija/KoskiOppijaPage'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { KoskiVirkailijaPage } from './pages/virkailija/KoskiVirkailijaPage'

type Fixtures = {
  loginPage: KoskiLoginPage
  oppijaPage: KoskiOppijaPage
  oppijaHaku: KoskiOppijaHaku
  uusiOppijaPage: KoskiUusiOppijaPage
  virkailijaPage: KoskiVirkailijaPage
  fixtures: KoskiFixtures
}

export const test = base.extend<Fixtures>({
  loginPage: async ({ page }, use) => {
    await use(new KoskiLoginPage(page))
  },
  oppijaPage: async ({ page }, use) => {
    await use(new KoskiOppijaPage(page))
  },
  oppijaHaku: async ({ page }, use) => {
    await use(new KoskiOppijaHaku(page))
  },
  uusiOppijaPage: async ({ page }, use) => {
    await use(new KoskiUusiOppijaPage(page))
  },
  virkailijaPage: async ({ page }, use) => {
    await use(new KoskiVirkailijaPage(page))
  },
  fixtures: async ({ browser }, use) => {
    const context = await browser.newContext()
    const page = await context.newPage()
    // Kirjautumissivu luodaan tässä uudestaan, jotta ne käyttävät samaa kontestia kuin KoskiFixtures
    const loginPage = new KoskiLoginPage(page)
    await loginPage.apiLoginAsUser('kalle', 'kalle')
    const fixtures = new KoskiFixtures(page)
    await use(fixtures)
  }
})
export { expect } from '@playwright/test'
