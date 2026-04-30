import { expect, test } from './base'
import { virkailija } from './setup/auth'

const organisaatiohistoriallinenOid = '1.2.246.562.24.00000000098'
const kaisaOid = '1.2.246.562.24.00000000007'

const perusopetusV2Url = (oid: string) =>
  `${oid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: organisaatiohistoria', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Näyttää organisaatiohistorian laajennettavana osiona', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(perusopetusV2Url(organisaatiohistoriallinenOid))

    const toggle = page.getByTestId(
      'oo.0.opiskeluoikeus.organisaatiohistoria.toggle'
    )
    const value = page.getByTestId(
      'oo.0.opiskeluoikeus.organisaatiohistoria.value'
    )

    await expect(toggle).toBeVisible()
    await expect(toggle).toContainText('Opiskeluoikeuden organisaatiohistoria')
    await expect(value).toBeHidden()

    await toggle.click()

    await expect(value).toBeVisible()
    await expect(value).toContainText('Muutospäivä')
    await expect(value).toContainText('2013')
    await expect(value).toContainText('Jyväskylän normaalikoulu')
    await expect(value).toContainText('Helsingin kaupunki')
  })

  test('Ei näytä osiota, kun opiskeluoikeudella ei ole organisaatiohistoriaa', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(perusopetusV2Url(kaisaOid))

    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.organisaatiohistoria.toggle')
    ).toBeHidden()
  })
})
