import { expect, test } from './base'
import { virkailija, virkailijaPath } from './setup/auth'
import { KoskiFixtures } from './fixtures/KoskiFixtures'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'

test.describe('Vapaan sivistyön VST osaamismerkki', () => {
  test.use({ storageState: virkailija('pää') })
  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test.describe('Uuden opiskeluoikeuden luomisen jälkeen', () => {
    test.beforeEach(async ({ browser, virkailijaPage, oppijaHaku }) => {
      const ctx = await browser.newContext({
        storageState: virkailijaPath('pää')
      })
      const page = await ctx.newPage()
      const uusiOppijaPage = new KoskiUusiOppijaPage(page)
      await uusiOppijaPage.goTo('210610A426P')
      await uusiOppijaPage.fill({
        oppilaitos: 'Varsinais-Suomen kansanopisto',
        etunimet: 'Merkki',
        sukunimi: 'Merkkimestari',
        opiskeluoikeus: 'Vapaan sivistystyön koulutus',
        suoritustyyppi: 'Vapaan sivistystyön osaamismerkki',
        osaamismerkki: '1001 Digitaalinen tiedonhaku',
        opiskeluoikeudenTila: 'Hyväksytysti suoritettu'
      })
      await uusiOppijaPage.submitAndExpectSuccess()
      await page.close()
    })

    test('Opiskeluoikeus löytyy etsimällä', async ({
      virkailijaPage,
      oppijaHaku
    }) => {
      await virkailijaPage.goto()
      const search = await oppijaHaku.search('210610A426P')
      await search.clickOnFirst()
    })
  })
})
