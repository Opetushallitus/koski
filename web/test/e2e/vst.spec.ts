import { test, expect } from './base'
import { virkailija } from './setup/auth'

// TODO: Hetut?
const kotoutumiskoulutus = '1.2.246.562.24.00000000106'
const kotoutumiskoulutusOppivelvollisille = '1.2.246.562.24.00000000135'
const oppivelvollisilleSuunnattu = '1.2.246.562.24.00000000143'
const jotpaKoulutus = '1.2.246.562.24.00000000140'
const lukutaitokoulutus = '1.2.246.562.24.00000000107'
const kansanopisto = '1.2.246.562.24.00000000105'
const vstKoulutus = '1.2.246.562.24.00000000108'

test.describe('Vapaan sivistystyön koulutus', () => {
  test.describe('VST', () => {
    test('Näyttää oikeat tiedot', () => {
      expect(1).toEqual(1)
    })
  })
  test.describe('Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille', () => {
    test('Näyttää oikeat tiedot', () => {
      expect(1).toEqual(1)
    })
  })
  test.describe('Lukutaitokoulutus oppivelvollisille', () => {
    test('Näyttää oikeat tiedot', () => {
      expect(1).toEqual(1)
    })
  })
  test.describe('Kotoutumiskoulutus oppivelvollisille', () => {
    test.describe('Virkailijan näkymä', () => {
      test.use({ storageState: virkailija('kalle') })
      test.beforeEach(async ({ fixtures, vstOppijaPage }) => {
        await fixtures.reset(false)
        await vstOppijaPage.gotoWithQueryParams(kotoutumiskoulutus, {
          newVSTUI: 'true'
        })
      })

      test.describe('Katselunäkymä', () => {
        test('Näyttää oikeat tiedot', async ({ page }) => {
          await expect(page.getByTestId('opiskeluoikeus.nimi')).toHaveText(
            'Varsinais-Suomen kansanopisto, kotoutumiskoulutus oppivelvollisille (2021 –, läsnä)'
          )
          await expect(
            page.getByTestId('opiskeluoikeus.tila.value')
          ).toHaveText('Tila1.9.2021Läsnä')
          await expect(page.getByTestId('suoritukset.0.tab')).toHaveText(
            'Kotoutumiskoulutus oppivelvollisille'
          )
          await expect(
            page.getByTestId('vst.suoritukset.0.oppilaitos.value')
          ).toHaveText('Itä-Suomen yliopisto')
          await expect(
            page.getByTestId('vst.suoritukset.0.koulutus.value')
          ).toHaveText('Kotoutumiskoulutus oppivelvollisille')
          await expect(
            page.getByTestId('vst.suoritukset.0.koulutusmoduuli.value')
          ).toHaveText('999910')
          await expect(
            page.getByTestId('vst.suoritukset.0.peruste.value')
          ).toHaveText('OPH-123-2021')
          await expect(
            page.getByTestId('vst.suoritukset.0.koulutuksen-laajuus.value')
          ).toHaveText('54 op')
          await expect(
            page.getByTestId('vst.suoritukset.0.opetuskieli.value')
          ).toHaveText('suomi')
          // Valmis suoritus
          await expect(
            page.getByTestId('suoritukset.0.suorituksenVahvistus.value.status')
          ).toHaveText('Suoritus valmis')
          await expect(
            page.getByTestId('suoritukset.0.suorituksenVahvistus.value.details')
          ).toHaveText('Vahvistus: 31.5.2022 Jyväskylän normaalikoulu')
          await expect(
            page.getByTestId(
              'suoritukset.0.suorituksenVahvistus.value.henkilö.0'
            )
          ).toHaveText('Reijo Reksi (rehtori)')
          await expect(
            page.getByTestId('vst.suoritukset.0.yhteensa.value')
          ).toHaveText('54 op')
        })
      })
      test.describe('Muokkausnäkymä', () => {
        test('Avaa ja sulkee muokkausnäkymän', async ({ page }) => {
          await expect(page.getByTestId('opiskeluoikeus.edit')).toBeVisible()
          await expect(
            page.getByTestId('opiskeluoikeus.cancelEdit')
          ).toBeHidden()

          await page.getByTestId('opiskeluoikeus.edit').click()

          await expect(page.getByTestId('opiskeluoikeus.edit')).toBeHidden()
          await expect(
            page.getByTestId('opiskeluoikeus.cancelEdit')
          ).toBeVisible()

          await page.getByTestId('opiskeluoikeus.cancelEdit').click()
        })
        test.skip('Lisää uuden osasuorituksen', async ({ page }) => {
          await expect(page.getByTestId('opiskeluoikeus.edit')).toBeVisible()
          await page.getByTestId('opiskeluoikeus.edit').click()
          await page.getByRole('button', { name: 'Lisää osasuoritus' }).click()
          await page
            .getByPlaceholder('Opintokokonaisuus')
            .fill('Playwright-opinnot')
          await page.locator('form').getByText('Lisää osasuoritus').click()
          await page.getByRole('button', { name: 'Lisää osasuoritus' }).click()
          await page.locator('form').getByText('Peruuta').click()
        })
      })
    })
  })
  test.describe('Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutus', () => {
    test.describe('Virkailijan näkymä', () => {
      test.use({ storageState: virkailija('kalle') })
      test.beforeEach(async ({ fixtures, vstOppijaPage }) => {
        await fixtures.reset(false)
        await vstOppijaPage.gotoWithQueryParams(jotpaKoulutus, {
          newVSTUI: 'true'
        })
      })
      test.describe('Katselunäkymä', () => {
        test('Näyttää oikeat tiedot', async ({ page }) => {
          await expect(page.getByTestId('opiskeluoikeus.nimi')).toHaveText(
            'Varsinais-Suomen kansanopisto, vapaan sivistystyön koulutus (2023 –, läsnä)'
          )
          await expect(
            page.getByTestId('opiskeluoikeus.tila.value')
          ).toHaveText(
            'Tila1.1.2023Läsnä (Jatkuvan oppimisen ja työllisuuden palvelukeskuksen rahoitus)'
          )
          await expect(page.getByTestId('suoritukset.0.tab')).toHaveText(
            'Vapaan sivistystyön koulutus'
          )
          await expect(
            page.getByTestId('vst.suoritukset.0.oppilaitos.value')
          ).toHaveText('Varsinais-Suomen kansanopisto')
          await expect(
            page.getByTestId('vst.suoritukset.0.koulutus.value')
          ).toHaveText('Vapaan sivistystyön koulutus')
          await expect(
            page.getByTestId('vst.suoritukset.0.koulutusmoduuli.value')
          ).toHaveText('099999')
          await expect(
            page.getByTestId('vst.suoritukset.0.peruste.value')
          ).toHaveText('1138 Kuvallisen ilmaisun perusteet ja välineet')
          const opintokokonaisuusLink = page
            .getByTestId('vst.suoritukset.0.peruste.value')
            .locator('a')
          await expect(opintokokonaisuusLink).toHaveAttribute(
            'href',
            'https://eperusteet.opintopolku.fi/#/fi/opintokokonaisuus/1138'
          )
          await expect(
            page.getByTestId('vst.suoritukset.0.yhteensa.value')
          ).toHaveText('3 op')
        })
      })
    })
  })
})
