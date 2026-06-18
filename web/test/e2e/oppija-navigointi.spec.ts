import { expect, test } from './base'
import { virkailija } from './setup/auth'

test.describe('Oppijanavigointi', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Oppijasivun Opiskelijat-linkki avaa oppijataulukon ilman aiempaa listapolkua', async ({
    oppijaPage,
    virkailijaPage
  }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000001')
    await expect(oppijaPage.oppijaHeading).toContainText('Eero')

    await oppijaPage.opiskelijatLink.click()

    await expect(virkailijaPage.oppijataulukko).toBeVisible()
  })
})
