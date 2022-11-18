import { test as base, Page } from '@playwright/test'
import { KoskiFixtures } from './fixtures/KoskiFixtures'
import { KoskiMuksOppijaPage } from './pages/oppija/KoskiMuksOppijaPage'
import AxeBuilder from '@axe-core/playwright'
import { KoskiKansalainenPage } from './pages/kansalainen/KoskiKansalainenPage'
import { KansalainenLoginPage } from './pages/login/KansalainenLoginPage'
import { VirkailijaLoginPage } from './pages/login/VirkailijaLoginPage'
import { KoskiOppijaHaku } from './pages/oppija/KoskiOppijaHaku'
import { KoskiOppijaPage } from './pages/oppija/KoskiOppijaPage'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { KoskiVirkailijaPage } from './pages/virkailija/KoskiVirkailijaPage'
import { KoskiEshOppijaPage } from './pages/oppija/KoskiEshOppijaPage'

type CustomPage = Page

type Fixtures = {
  customPage: CustomPage
  virkailijaLoginPage: VirkailijaLoginPage
  kansalainenLoginPage: KansalainenLoginPage
  oppijaPage: KoskiOppijaPage
  muksOppijaPage: KoskiMuksOppijaPage
  eshOppijaPage: KoskiEshOppijaPage
  oppijaHaku: KoskiOppijaHaku
  uusiOppijaPage: KoskiUusiOppijaPage
  virkailijaPage: KoskiVirkailijaPage
  kansalainenPage: KoskiKansalainenPage
  fixtures: KoskiFixtures
  makeAxeBuilder: () => AxeBuilder
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
  eshOppijaPage: async ({ customPage }, use) => {
    await use(new KoskiEshOppijaPage(customPage))
  },
  muksOppijaPage: async ({ customPage }, use) => {
    await use(new KoskiMuksOppijaPage(customPage))
  },
  virkailijaLoginPage: async ({ customPage }, use) => {
    await use(new VirkailijaLoginPage(customPage))
  },
  kansalainenLoginPage: async ({ customPage }, use) => {
    await use(new KansalainenLoginPage(customPage))
  },
  oppijaPage: async ({ customPage }, use) => {
    await use(new KoskiOppijaPage(customPage))
  },
  oppijaHaku: async ({ customPage }, use) => {
    await use(new KoskiOppijaHaku(customPage))
  },
  uusiOppijaPage: async ({ customPage }, use) => {
    await use(new KoskiUusiOppijaPage(customPage))
  },
  virkailijaPage: async ({ customPage }, use) => {
    await use(new KoskiVirkailijaPage(customPage))
  },
  kansalainenPage: async ({ customPage }, use) => {
    await use(new KoskiKansalainenPage(customPage))
  },
  fixtures: async ({ customPage }, use) => {
    // Kirjautumissivu luodaan tässä uudestaan, jotta ne käyttävät samaa kontestia kuin KoskiFixtures
    const loginPage = new VirkailijaLoginPage(customPage)
    await loginPage.apiLoginAsUser('kalle', 'kalle')
    const fixtures = new KoskiFixtures(customPage)
    await use(fixtures)
  },
  makeAxeBuilder: async ({ customPage }, use, testInfo) => {
    const makeAxeBuilder = () =>
      new AxeBuilder({ page: customPage }).disableRules([
        'color-contrast',
        'landmark-one-main',
        'link-in-text-block',
        'region'
      ])
    await use(makeAxeBuilder)
  }
})
export { expect } from '@playwright/test'
