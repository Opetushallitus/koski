import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { LukioOpiskeluoikeus } from './pages/oppija/components/LukioOpiskeluoikeus'

test.describe('Lukiokoulutus 2019: oppiaineen muokkaus', () => {
  test.use({ storageState: virkailija('pää') })

  let opiskeluoikeus: LukioOpiskeluoikeus
  let ai: ReturnType<LukioOpiskeluoikeus['oppiaineRivi']>

  test.beforeEach(
    async ({ fixtures, virkailijaPage, oppijaHaku, oppijaPage }) => {
      await fixtures.reset()
      await virkailijaPage.goto()
      await (await oppijaHaku.search('250605A518Y')).clickOnFirst()
      await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')
      await oppijaPage.avaaMuokkausnäkymä()
      opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('Lukion oppimäärä')
      ai = opiskeluoikeus.oppiaineRivi('AI')
    }
  )

  test('Kieliaineen kieli voidaan muuttaa ja tallentaa', async ({
    oppijaPage
  }) => {
    // Muokkausnäkymässä .nimi näyttää vain kiinteän etuliitteen - kielivalinta
    // on erillinen pudotusvalikko, ei osa nimeä.
    await expect(ai.locator('.nimi')).toHaveText('Äidinkieli ja kirjallisuus,')

    const kieli = opiskeluoikeus.kieliaineenKieliDropdown(ai)
    await expect(kieli.textInput).toHaveValue('Suomen kieli ja kirjallisuus')

    await kieli.selectOptionByClick('Ruotsin kieli ja kirjallisuus')
    await expect(kieli.textInput).toHaveValue('Ruotsin kieli ja kirjallisuus')

    await oppijaPage.tallenna()

    // Katselunäkymässä kielivalinta on osa oppiaineen koko nimeä.
    await expect(ai.locator('.nimi')).toHaveText(
      'Äidinkieli ja kirjallisuus, Ruotsin kieli ja kirjallisuus'
    )
  })

  test('Suorituskieli voidaan muuttaa ja tallentaa', async ({
    oppijaPage
  }) => {
    const suorituskieli = opiskeluoikeus.suorituskieliDropdown(ai)
    await expect(suorituskieli.textInput).toHaveValue('Ei valintaa')

    await suorituskieli.selectOptionByClick('arabia')
    await expect(suorituskieli.textInput).toHaveValue('arabia')

    await oppijaPage.tallenna()

    await oppijaPage.avaaMuokkausnäkymä()
    await expect(
      opiskeluoikeus.suorituskieliDropdown(ai).textInput
    ).toHaveValue('arabia')
  })

  test('Oppiaineen arvosana voidaan muuttaa ja tallentaa', async ({
    oppijaPage
  }) => {
    const arvosana = opiskeluoikeus.arvosanaDropdown(ai)
    await expect(arvosana.textInput).toHaveValue('9')

    await arvosana.selectOptionByClick('8')
    await expect(arvosana.textInput).toHaveValue('8')

    await oppijaPage.tallenna()

    await expect(ai.locator('td.arvosana')).toHaveText('8')
  })

  test('Valtakunnallisen oppiaineen lisääminen vaatii arvosanan, koska suoritus on valmis', async ({
    oppijaPage
  }) => {
    await opiskeluoikeus.lisääOppiaine('Psykologia')

    await expect(oppijaPage.tallennaBtn).toBeDisabled()
    await expect(
      opiskeluoikeus.oppiaineet.getByText(
        'Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.'
      )
    ).toBeVisible()
    await expect(opiskeluoikeus.oppiaineRivi('PS').locator('.nimi')).toHaveText(
      'Psykologia'
    )
  })

  test('Valtakunnallinen oppiaine voidaan lisätä ja tallentaa arvosanan kanssa', async ({
    oppijaPage
  }) => {
    await opiskeluoikeus.lisääOppiaine('Psykologia')
    const ps = opiskeluoikeus.oppiaineRivi('PS')
    await opiskeluoikeus.arvosanaDropdown(ps).selectOptionByClick('9')

    await oppijaPage.tallenna()

    await expect(
      opiskeluoikeus.oppiaineRivi('PS').locator('.nimi')
    ).toHaveText('Psykologia')
  })

  test('Valtakunnallinen oppiaine voidaan poistaa', async ({ oppijaPage }) => {
    await opiskeluoikeus.lisääOppiaine('Psykologia')
    const ps = opiskeluoikeus.oppiaineRivi('PS')
    await opiskeluoikeus.arvosanaDropdown(ps).selectOptionByClick('9')
    await oppijaPage.tallenna()

    await oppijaPage.avaaMuokkausnäkymä()
    await opiskeluoikeus.poistaOppiaine(opiskeluoikeus.oppiaineRivi('PS'))
    await oppijaPage.tallenna()

    await expect(opiskeluoikeus.oppiaineRivi('PS')).toHaveCount(0)
  })
})
