import { Page } from '@playwright/test'
import { expect, test } from './base'
import { kansalainen, virkailija } from './setup/auth'

// IB-Diploma Dani (040701A002J): IB-koulutuksen opiskeluoikeus ja IB Diploma.
const dani = '1.2.246.562.24.00000000234'

const oppiaineidenSarake = (page: Page, prefix: string, sarake: string) =>
  page.getByTestId(new RegExp(`^${prefix}\\.oppiaineet\\.\\d+\\.${sarake}$`))

const oppiaineet = [
  'Language A: literature, suomi',
  'B-language, englanti',
  'History',
  'Biology',
  'Mathematics: analysis and approaches',
  'Physics',
  'Chemistry'
]

test.describe('IB Diploma', () => {
  test.describe('OPH-pääkäyttäjä', () => {
    test.use({ storageState: virkailija('pää') })

    test.beforeAll(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test('näyttää diploman tiedot ilman muokkausta', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(dani)
      await oppijaPage.selectOpiskeluoikeus('ibdiploma')

      await expect(
        page.getByTestId('opiskeluoikeustyyppi-ibdiploma')
      ).not.toContainText(/2016|valmistunut/i)
      await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toHaveText(
        'International Baccalaureate Organization, IB Diploma'
      )
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.iboTunniste')
      ).toHaveText('dip123')

      const suoritus = 'oo.0.suoritukset.0'
      await expect(page.getByTestId(`${suoritus}.koulutus`)).toHaveText(
        'IB-tutkinto (International Baccalaureate)'
      )
      await expect(page.getByTestId(`${suoritus}.toimipiste`)).toHaveText(
        'International Baccalaureate Organization'
      )
      await expect(
        page.getByTestId(`${suoritus}.examinationSession`)
      ).toHaveText('Toukokuu 2016')
      await expect(
        page.getByTestId(`${suoritus}.extendedEssay.oppiaine`)
      ).toHaveText('History')
      await expect(
        page.getByTestId(`${suoritus}.extendedEssay.arvosana`)
      ).toHaveText('B Good')
      await expect(
        page.getByTestId(`${suoritus}.theoryOfKnowledge.arvosana`)
      ).toHaveText('A Excellent')
      await expect(page.getByTestId(`${suoritus}.combinedGrade`)).toHaveText(
        '2'
      )
      await expect(page.getByTestId(`${suoritus}.totalPoints`)).toHaveText('37')
      await expect(page.getByTestId(`${suoritus}.result`)).toHaveText(
        'Diploma awarded'
      )
      await expect(
        page.getByTestId(`${suoritus}.suorituksenVahvistus.value.status`)
      ).toHaveText('Suoritus valmis')
      await expect(
        page.getByTestId(`${suoritus}.suorituksenVahvistus.value.details`)
      ).toHaveText(
        'Vahvistus: 6.7.2016 International Baccalaureate Organization'
      )

      await expect(oppiaineidenSarake(page, suoritus, 'nimi')).toHaveText(
        oppiaineet
      )
      await expect(oppiaineidenSarake(page, suoritus, 'taso')).toHaveText([
        'HL',
        'HL',
        'HL',
        'SL',
        'SL',
        'SL',
        'SL'
      ])
      await expect(oppiaineidenSarake(page, suoritus, 'arvosana')).toHaveText([
        '7',
        '6',
        '6',
        '6',
        '5',
        '5',
        '5'
      ])
      await expect(
        oppiaineidenSarake(page, suoritus, 'examinationSession')
      ).toHaveText([
        'Toukokuu 2016',
        'Toukokuu 2016',
        'Toukokuu 2016',
        'Toukokuu 2016',
        'Toukokuu 2016',
        'Toukokuu 2016',
        'Marraskuu 2015'
      ])

      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
      ).toHaveCount(0)
      await expect(
        page.getByTestId(/^oo\.0\.opiskeluoikeus\.tila/)
      ).toHaveCount(0)
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.invalidate.button')
      ).toBeVisible()
    })
  })

  test.describe('Kansalainen', () => {
    test.use({ storageState: kansalainen('040701A002J') })

    test.beforeEach(async ({ fixtures }) => {
      await fixtures.apiLoginAsUser('kalle', 'kalle')
      await fixtures.reset()
      await fixtures.apiLogout()
    })

    test('näkee diploman omissa tiedoissa ilman muokkausta', async ({
      page,
      kansalainenPage
    }) => {
      await kansalainenPage.goto()
      await expect(
        page
          .getByTestId(/^oo\.\d+\.opiskeluoikeus\.nimi$/)
          .filter({ hasText: 'IB Diploma' })
      ).toHaveText('IB Diploma')
      await page
        .getByTestId(/^oo\.\d+\.opiskeluoikeus\.expand$/)
        .filter({ hasText: 'IB Diploma' })
        .click()

      const suoritus = 'oo\\.\\d+\\.suoritukset\\.0'
      await expect(
        page.getByTestId(new RegExp(`^${suoritus}\\.examinationSession$`))
      ).toHaveText('Toukokuu 2016')
      await expect(
        page.getByTestId(new RegExp(`^${suoritus}\\.result$`))
      ).toHaveText('Diploma awarded')
      await expect(oppiaineidenSarake(page, suoritus, 'nimi')).toHaveText(
        oppiaineet
      )

      // Koulutuksen koodiarvoa ei näytetä kansalaiselle.
      await expect(page.getByText('301102')).toHaveCount(0)
      await expect(
        page.getByTestId(/opiskeluoikeus\.voimassaoloaika$/)
      ).toHaveCount(0)
      await expect(page.getByTestId(/opiskeluoikeus\.edit$/)).toHaveCount(0)
    })
  })
})
