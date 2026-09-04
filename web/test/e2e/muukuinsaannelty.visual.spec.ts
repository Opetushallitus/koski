import { expect, test } from './base'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'
import { virkailija } from './setup/auth'

test.describe('Muu kuin säännelty koulutus – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.use({ storageState: virkailija('muks') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Kaksitasoiset osasuoritukset muokkaus- ja katselutilassa', async ({
    page,
    virkailijaPage,
    oppijaHaku,
    muksOppijaPage
  }) => {
    await virkailijaPage.goto()
    const hakutulokset = await oppijaHaku.search('200600A515B')
    await hakutulokset.clickOnFirst()
    await muksOppijaPage.avaaMuokkausnäkymä()

    const osasuoritus =
      await muksOppijaPage.lisääOsasuoritus('Hedelmäasetelmat')
    await osasuoritus.syötäLaajuus(6)
    await osasuoritus.valitseArvosana('5')
    await osasuoritus.syötäArviointipäivä('1.2.2027')

    const alaosasuoritus = await osasuoritus.lisääAlaosasuoritus(
      'Fotorealistiset omenat'
    )
    await alaosasuoritus.avaa()
    await alaosasuoritus.syötäLaajuus(6)
    await alaosasuoritus.valitseArvosana('4')
    await alaosasuoritus.syötäArviointipäivä('2.2.2027')

    await expect(osasuoritus.arviointipäiväInput).toHaveValue('1.2.2027')
    await expect(alaosasuoritus.arviointipäiväInput).toHaveValue('2.2.2027')
    await takeFullPageScreenshot(page, 'muks-muokkaus-avattu.png')

    await muksOppijaPage.tallenna()
    await osasuoritus.avaa()
    await alaosasuoritus.avaa()

    await expect(osasuoritus.arviointipäivä).toHaveText('1.2.2027')
    await expect(alaosasuoritus.arviointipäivä).toHaveText('2.2.2027')
    await takeFullPageScreenshot(page, 'muks-katselu-avattu.png')
  })
})
