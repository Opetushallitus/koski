import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { formatFinnishDate } from '../../app/date/date'

test.describe('Lukiokoulutus 2019: Lukion oppiaineiden oppimäärien suoritus', () => {
  test.use({ storageState: virkailija('pää') })

  test('Valmistumistieto voidaan lisätä, vaikka suoritusta ei ole vahvistettu', async ({
    fixtures,
    virkailijaPage,
    oppijaHaku,
    oppijaPage
  }) => {
    await fixtures.reset()
    await virkailijaPage.goto()
    await (await oppijaHaku.search('010705A6119')).clickOnFirst()
    await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')
    await oppijaPage.avaaMuokkausnäkymä()
    await oppijaPage.poistaViimeisinTila()

    const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('lukion aineopinnot')
    await opiskeluoikeus.lisääTila('Valmistunut')
    await oppijaPage.tallenna()

    await expect(opiskeluoikeus.päättymispäivä).toHaveText(
      formatFinnishDate(new Date())!
    )
  })
})
