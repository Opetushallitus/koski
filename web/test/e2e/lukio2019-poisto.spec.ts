import { expect, test } from './base'
import { virkailija } from './setup/auth'

test.describe('Lukiokoulutus 2019: päätason suorituksen poistaminen', () => {
  test.use({ storageState: virkailija('pää') })

  test('Mitätöintilinkkiä ei näytetä, kun opiskeluoikeudella on vain yksi suoritus', async ({
    fixtures,
    virkailijaPage,
    oppijaHaku,
    oppijaPage
  }) => {
    await fixtures.reset()
    await virkailijaPage.goto()
    await (await oppijaHaku.search('250605A518Y')).clickOnFirst()
    await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')
    await oppijaPage.avaaMuokkausnäkymä()

    const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('Lukion oppimäärä')
    await expect(
      opiskeluoikeus.container.getByRole('link', { name: 'Poista suoritus' })
    ).toHaveCount(0)
  })
})
