import { test as base } from '@playwright/test'
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
import { KoskiPerusopetusOppijaPage } from './pages/oppija/KoskiPerusopetusOppijaPage'
import { KoskiTpoOppijaPage } from './pages/oppija/KoskiTpoOppijaPage'
import { KoskiVSTOppijaPage } from './pages/oppija/KoskiVSTOppijaPage'

type Fixtures = {
  virkailijaLoginPage: VirkailijaLoginPage
  kansalainenLoginPage: KansalainenLoginPage
  oppijaPage: KoskiOppijaPage
  muksOppijaPage: KoskiMuksOppijaPage
  eshOppijaPage: KoskiEshOppijaPage
  perusopetusOppijaPage: KoskiPerusopetusOppijaPage
  oppijaHaku: KoskiOppijaHaku
  uusiOppijaPage: KoskiUusiOppijaPage
  virkailijaPage: KoskiVirkailijaPage
  kansalainenPage: KoskiKansalainenPage
  taiteenPerusopetusPage: KoskiTpoOppijaPage
  vstOppijaPage: KoskiVSTOppijaPage
  fixtures: KoskiFixtures
  makeAxeBuilder: () => AxeBuilder
}

export const test = base.extend<Fixtures>({
  eshOppijaPage: async ({ page }, use) => {
    await use(new KoskiEshOppijaPage(page))
  },
  muksOppijaPage: async ({ page }, use) => {
    await use(new KoskiMuksOppijaPage(page))
  },
  perusopetusOppijaPage: async ({ page }, use) => {
    await use(new KoskiPerusopetusOppijaPage(page))
  },
  virkailijaLoginPage: async ({ page }, use) => {
    await use(new VirkailijaLoginPage(page))
  },
  kansalainenLoginPage: async ({ page }, use) => {
    await use(new KansalainenLoginPage(page))
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
  kansalainenPage: async ({ page }, use) => {
    await use(new KoskiKansalainenPage(page))
  },
  taiteenPerusopetusPage: async ({ page }, use) => {
    await use(new KoskiTpoOppijaPage(page))
  },
  vstOppijaPage: async ({ page }, use) => {
    await use(new KoskiVSTOppijaPage(page))
  },
  fixtures: async ({ browser }, use) => {
    const ctx = await browser.newContext()
    const page = await ctx.newPage()
    await use(new KoskiFixtures(page))
    await page.close()
  },
  makeAxeBuilder: async ({ page }, use) => {
    const makeAxeBuilder = () =>
      // @ts-ignore
      new AxeBuilder({ page }).disableRules([
        'color-contrast',
        'landmark-one-main',
        'landmark-no-duplicate-banner',
        'landmark-unique',
        'link-in-text-block',
        'region'
      ])
    await use(makeAxeBuilder)
  }
})

export { expect } from '@playwright/test'
