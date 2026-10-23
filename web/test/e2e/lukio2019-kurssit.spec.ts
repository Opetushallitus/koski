import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { LukioOpiskeluoikeus } from './pages/oppija/components/LukioOpiskeluoikeus'

test.describe('Lukiokoulutus 2019: moduulin/kurssin muokkaus', () => {
  test.use({ storageState: virkailija('pää') })

  let opiskeluoikeus: LukioOpiskeluoikeus

  test.beforeEach(
    async ({ fixtures, virkailijaPage, oppijaHaku, oppijaPage }) => {
      await fixtures.reset()
      await virkailijaPage.goto()
      await (await oppijaHaku.search('250605A518Y')).clickOnFirst()
      await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')
      await oppijaPage.avaaMuokkausnäkymä()
      opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('Lukion oppimäärä')
    }
  )

  test('Moduulin arvosanaa voidaan muuttaa', async ({ oppijaPage }) => {
    // Suoritus on valmis: moduulin arvosanan muutos leimaa arviointipäivän
    // tämänpäiväiseksi, joka ei voi olla opiskeluoikeuden päättymispäivän
    // jälkeen. Siksi viimeisin tila poistetaan ensin (= suoritus kesken).
    await oppijaPage.poistaViimeisinTila()

    const ma = opiskeluoikeus.oppiaineRivi('MA')
    const mab4 = opiskeluoikeus.kurssi(ma, 'MAB4')
    await opiskeluoikeus.kurssinArvosanaDropdown(mab4).selectOptionByClick('6')

    await oppijaPage.tallenna()

    const maAgain = opiskeluoikeus.oppiaineRivi('MA')
    const mab4Again = opiskeluoikeus.kurssi(maAgain, 'MAB4')
    await expect(mab4Again.locator('.arvosana')).toHaveText('6')
  })

  test('Valtakunnallinen moduuli voidaan lisätä oppiaineelle', async ({
    oppijaPage
  }) => {
    await oppijaPage.poistaViimeisinTila()

    const aom = opiskeluoikeus.oppiaineRivi('AOM')
    const rua2 = await opiskeluoikeus.lisääValtakunnallinenKurssi(
      aom,
      'RUA2',
      '2'
    )
    await opiskeluoikeus.kurssinArvosanaDropdown(rua2).selectOptionByClick('9')

    await oppijaPage.tallenna()

    await expect(
      opiskeluoikeus.kurssi(opiskeluoikeus.oppiaineRivi('AOM'), 'RUA2')
    ).toBeVisible()
  })

  test('Moduuli/kurssi voidaan poistaa', async ({ oppijaPage }) => {
    const ai = opiskeluoikeus.oppiaineRivi('AI')
    const ai2 = opiskeluoikeus.kurssi(ai, 'ÄI2')
    await opiskeluoikeus.poistaKurssi(ai2)

    await oppijaPage.tallenna()

    await expect(
      opiskeluoikeus.kurssi(opiskeluoikeus.oppiaineRivi('AI'), 'ÄI2')
    ).toHaveCount(0)
  })
})
