import { expect, test } from './base'
import { kansalainen, virkailija } from './setup/auth'

const tapaukset = [
  {
    nimi: 'Avoimet opinnot ilman maksuja',
    hetu: '250668-293Y',
    otsikko: /Avoimen opinnot.*2009—2009/,
    maksullinen: false
  },
  {
    nimi: 'Tutkinto ilman maksuja',
    hetu: '100869-192W',
    otsikko: /Dipl.ins., konetekniikka/,
    maksullinen: false
  },
  {
    nimi: 'Opinnot, joilla on maksuja',
    hetu: '250668-293Y',
    otsikko: /Avoimen opinnot.*2015—2016/,
    maksullinen: true
  }
]

for (const näkymä of ['virkailija', 'kansalainen'] as const) {
  for (const tapaus of tapaukset) {
    test.describe(`Lukuvuosimaksut: ${näkymä}, ${tapaus.nimi}`, () => {
      test.use({
        storageState:
          näkymä === 'virkailija' ? virkailija('pää') : kansalainen(tapaus.hetu)
      })

      test('Lisätiedoissa näkyvät vain olemassa olevat maksut', async ({
        page,
        virkailijaPage,
        oppijaHaku,
        oppijaPage,
        kansalainenPage
      }) => {
        if (näkymä === 'virkailija') {
          await virkailijaPage.goto()
          await (await oppijaHaku.search(tapaus.hetu)).clickOnFirst()
          await oppijaPage.selectOpiskeluoikeus('korkeakoulutus')
        } else {
          await kansalainenPage.goto()
          await page
            .locator('button.opiskeluoikeus-button')
            .filter({ hasText: tapaus.otsikko })
            .click()
        }

        const opiskeluoikeus =
          näkymä === 'virkailija'
            ? page.locator('div.opiskeluoikeus').filter({
                has: page.getByRole('heading', {
                  level: 3,
                  name: tapaus.otsikko
                })
              })
            : page
                .locator('.opiskeluoikeus-container')
                .filter({
                  has: page.locator('button.opiskeluoikeus-button').filter({
                    hasText: tapaus.otsikko
                  })
                })
                .locator('div.opiskeluoikeus')
        await expect(opiskeluoikeus).toBeVisible()
        const lisätiedot = opiskeluoikeus.locator(
          '.expandable-container.lisätiedot'
        )
        await lisätiedot.getByText('Lisätiedot', { exact: true }).click()
        await expect(lisätiedot.locator(':scope > .value')).toBeVisible()

        const maksut = lisätiedot.locator('.maksettavatLukuvuosimaksut')
        if (tapaus.maksullinen) {
          await expect(maksut).toBeVisible()
          await expect(maksut).toContainText('Maksettavat lukuvuosimaksut')
          await expect(maksut).toContainText('20.10.2015')
          await expect(maksut).toContainText('12.4.2016')
          await expect(maksut).toContainText('4000')
        } else {
          await expect(maksut).toHaveCount(0)
          await expect(lisätiedot).not.toContainText(
            'Maksettavat lukuvuosimaksut'
          )
        }
      })
    })
  }
}
