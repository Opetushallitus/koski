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
    // Näkyvyysehto (SuoritusEditor.jsx: showDeleteLink) ei riipu suorituksen
    // tilasta lainkaan - tällä fixturella linkki piilotetaan, koska
    // opiskeluoikeudella on vain yksi suoritus (isSingleSuoritus). Linkki
    // (ButtonWithConfirmation.jsx) renderöityisi roolilla "link" ja nimellä
    // "Poista suoritus", kun se näytetään - tässä tilassa sitä ei siis ole.
    await expect(
      opiskeluoikeus.container.getByRole('link', { name: 'Poista suoritus' })
    ).toHaveCount(0)
  })
})
