import { expect, test } from './base'
import { kansalainen } from './setup/auth'

test.describe('Omat tiedot: lukio 2019', () => {
  test.beforeEach(async ({ fixtures }) => {
    await fixtures.apiLoginAsUser('kalle', 'kalle')
    await fixtures.reset()
    await fixtures.apiLogout()
  })

  test.describe('Lukion oppimäärä', () => {
    test.use({ storageState: kansalainen('250605A518Y') })

    test('Työpöytänäkymässä näytetään oppiaineiden kurssit suoraan auki', async ({
      kansalainenPage
    }) => {
      await kansalainenPage.goto()
      await kansalainenPage.openOpiskeluoikeus(/Lukion oppimäärä/)

      const opiskeluoikeus = kansalainenPage.getOpiskeluoikeus(/Lukion oppimäärä/)
      const ai = opiskeluoikeus
        .locator('table.omattiedot-suoritukset tr.oppiaine-header')
        .filter({ hasText: 'Äidinkieli' })

      await expect(ai.locator('.nimi')).toHaveText(
        'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus'
      )
      await expect(ai.locator('.laajuus')).toHaveText('(6 opintopistettä)')
      await expect(ai.locator('.arvosana')).toHaveText('9')

      // Työpöytänäkymässä kurssit näkyvät suoraan, ei tarvitse avata.
      const aiKurssit = ai.locator('xpath=following-sibling::tr[1]')
      const äi1 = aiKurssit
        .locator('li.kurssi')
        .filter({ has: ai.page().getByRole('button', { name: 'ÄI1', exact: true }) })
      await expect(äi1.locator('.arvosana')).toHaveText('8')
    })

    test('Mobiilinäkymässä oppiaineen kurssit avautuvat klikkaamalla', async ({
      page,
      kansalainenPage
    }) => {
      await page.setViewportSize({ width: 375, height: 800 })
      await kansalainenPage.goto()
      await kansalainenPage.openOpiskeluoikeus(/Lukion oppimäärä/)

      const opiskeluoikeus = kansalainenPage.getOpiskeluoikeus(/Lukion oppimäärä/)
      const ai = opiskeluoikeus
        .locator('table.omattiedot-suoritukset tr.oppiaine-header')
        .filter({ hasText: 'Äidinkieli' })

      await ai.click()

      const aiKurssit = ai
        .locator('xpath=following-sibling::tr[1]')
        .locator('table.kurssilista-mobile')
      const äi1 = aiKurssit.locator('tr.kurssi').filter({ hasText: /^ÄI1\s/ })
      await expect(äi1.locator('.arvosana')).toHaveText('8')
      const äi2 = aiKurssit.locator('tr.kurssi').filter({ hasText: /^ÄI2\s/ })
      await expect(äi2.locator('.arvosana')).toHaveText('8')
    })
  })

  test.describe('Lukion aineopinnot: erityinen tutkinto', () => {
    test.use({ storageState: kansalainen('010705A6119') })

    test('Erityisenä tutkintona suoritetun oppiaineen alaviite ja laajuus näytetään oikein', async ({
      kansalainenPage
    }) => {
      await kansalainenPage.goto()
      await kansalainenPage.openOpiskeluoikeus(/Lukion aineopinnot/)

      const opiskeluoikeus = kansalainenPage.getOpiskeluoikeus(/Lukion aineopinnot/)
      const bi = opiskeluoikeus
        .locator('table.omattiedot-suoritukset tr.oppiaine-header')
        .filter({ hasText: 'Biologia' })

      await expect(bi.locator('.laajuus')).toHaveText('(6 opintopistettä)')
      await expect(bi.locator('.footnote-hint')).toHaveAttribute(
        'title',
        'Erityisenä tutkintona suoritettu oppiaine'
      )

      await expect(
        opiskeluoikeus.getByText(
          '** = erityisenä tutkintona suoritettu oppiaine'
        )
      ).toBeVisible()
      await expect(opiskeluoikeus.locator('.kurssit-yhteensä')).toHaveText(
        'Arvioitujen osasuoritusten laajuus yhteensä: 167,5Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 164,0'
      )
    })
  })
})
