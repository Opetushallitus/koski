import { test as base, Page } from '@playwright/test'
import { KoskiFixtures } from './fixtures/KoskiFixtures'
import { KoskiLoginPage } from './pages/login/KoskiLoginPage'
import { KoskiMuksOppijaPage } from './pages/oppija/KoskiMuksOppijaPage'
import { KoskiOppijaHaku } from './pages/oppija/KoskiOppijaHaku'
import { KoskiOppijaPage } from './pages/oppija/KoskiOppijaPage'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { KoskiVirkailijaPage } from './pages/virkailija/KoskiVirkailijaPage'

type Fixtures = {
  customPage: Page
  loginPage: KoskiLoginPage
  oppijaPage: KoskiOppijaPage
  muksOppijaPage: KoskiMuksOppijaPage
  oppijaHaku: KoskiOppijaHaku
  uusiOppijaPage: KoskiUusiOppijaPage
  virkailijaPage: KoskiVirkailijaPage
  fixtures: KoskiFixtures
}

export const test = base.extend<Fixtures>({
  customPage: [
    async ({ browser }, use) => {
      const ctx = await browser.newContext()
      const page = await ctx.newPage()
      use(page)
    },
    { scope: 'test' }
  ],
  loginPage: async ({ customPage }, use) => {
    await use(new KoskiLoginPage(customPage))
  },
  oppijaPage: async ({ customPage }, use) => {
    await use(new KoskiOppijaPage(customPage))
  },
  oppijaHaku: async ({ customPage }, use) => {
    await use(new KoskiOppijaHaku(customPage))
  },
  muksOppijaPage: async ({ customPage }, use) => {
    await use(new KoskiMuksOppijaPage(customPage))
  },
  uusiOppijaPage: async ({ customPage }, use) => {
    await use(new KoskiUusiOppijaPage(customPage))
  },
  virkailijaPage: async ({ customPage }, use) => {
    await use(new KoskiVirkailijaPage(customPage))
  },
  fixtures: async ({ customPage }, use) => {
    // Kirjautumissivu luodaan tässä uudestaan, jotta ne käyttävät samaa kontestia kuin KoskiFixtures
    const loginPage = new KoskiLoginPage(customPage)
    await loginPage.apiLoginAsUser('kalle', 'kalle')
    const fixtures = new KoskiFixtures(customPage)
    await use(fixtures)
  }
})
export { expect } from '@playwright/test'
