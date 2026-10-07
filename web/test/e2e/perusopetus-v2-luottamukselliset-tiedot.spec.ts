import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Ilman luottamuksellisten tietojen roolia backend poistaa arkaluonteiset
 * kentät (@SensitiveData) jo ennen käyttöliittymää. Näkymän on toimittava
 * ilman niitä, eikä niistä saa jäädä jälkiä (esim. alaviitemerkintöjä).
 *
 * epäluotettava-tallentaja ja jyvas-eiluottoa ovat Jyväskylän normaalikoulun
 * käyttäjiä ilman luottamuksellisten tietojen oikeutta.
 */

const tommiUrl =
  '1.2.246.562.24.00000000051?opiskeluoikeudenTyyppi=perusopetus'
const kaisaUrl =
  '1.2.246.562.24.00000000007?opiskeluoikeudenTyyppi=perusopetus'

const lisätietojenNimet = (page: Page) =>
  page.locator('.EditorContainer__lisatiedot .KeyValueRow__name')

// Biologia on Kaisan päättötodistuksella yksilöllistetty (*), liikunta
// painotettu (**).
const biologianAlaviite = 'oo.0.suoritukset.0.osasuoritukset.11.footnote'
const liikunnanAlaviite = 'oo.0.suoritukset.0.osasuoritukset.19.footnote'

test.describe('Perusopetuksen uusi käyttöliittymä: luottamukselliset tiedot', () => {
  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test.describe('Luottamuksellisten tietojen oikeudella', () => {
    test.use({ storageState: virkailija('kalle') })

    test('Arkaluonteiset lisätiedot näytetään', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(tommiUrl)
      await expect(lisätietojenNimet(page)).toContainText([
        'Pidennetty oppivelvollisuus',
        'Erityisen tuen jaksot',
        'Vaikeimmin kehitysvammainen',
        'Kuljetusetu'
      ])
    })
  })

  test.describe('Ilman luottamuksellisten tietojen oikeutta', () => {
    test.describe('epäluotettava-tallentaja', () => {
      test.use({ storageState: virkailija('epäluotettava-tallentaja') })

      test('Lisätiedoista näytetään vain ei-arkaluonteiset', async ({
        page,
        oppijaPage
      }) => {
        await oppijaPage.goto(tommiUrl)
        await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toHaveText(
          'Perusopetus'
        )
        await expect(lisätietojenNimet(page)).toHaveText([
          'Kotiopetusjaksot',
          'Ulkomaanjaksot',
          'Majoitusetu'
        ])
      })
    })

    test.describe('jyvas-eiluottoa', () => {
      test.use({ storageState: virkailija('jyvas-eiluottoa') })

      test('Yksilöllistetyn oppimäärän merkintää ei näytetä', async ({
        page,
        oppijaPage
      }) => {
        await oppijaPage.goto(kaisaUrl)
        await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

        await expect(
          page.getByTestId('oo.0.suoritukset.0.osasuoritukset.11.nimi')
        ).toContainText('Biologia')
        await expect(page.getByTestId(biologianAlaviite)).toHaveCount(0)
        // Painotettu opetus ei ole arkaluonteinen tieto.
        await expect(page.getByTestId(liikunnanAlaviite)).toHaveText('**')
      })
    })
  })
})
