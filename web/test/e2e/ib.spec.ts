import { test, expect } from './base'
import { virkailija } from './setup/auth'

const oppijaOid_IBFinalIina = '1.2.246.562.24.00000000060'

test.describe('IB', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ oppijaPage }) => {
    await oppijaPage.goto(oppijaOid_IBFinalIina)
  })

  test('Opiskeluoikeuden tiedot näytetään oikein', async ({ ibOppijaPage }) => {
    const tiedot = ibOppijaPage.$.opiskeluoikeus

    await expect(tiedot.voimassaoloaika.elem).toContainText(
      'Opiskeluoikeuden voimassaoloaika: 1.9.2012 – 4.6.2016'
    )
    await expect(tiedot.tila.value.items(0).tila.elem).toContainText(
      'Valmistunut'
    )
    await expect(tiedot.tila.value.items(0).rahoitus.elem).toContainText(
      'Valtionosuusrahoitteinen koulutus'
    )
    await expect(tiedot.tila.value.items(1).tila.elem).toContainText('Läsnä')
    await expect(tiedot.tila.value.items(1).rahoitus.elem).toContainText(
      'Valtionosuusrahoitteinen koulutus'
    )
  })

  test.describe('Pre-IB', () => {
    test.describe('Kaikki tiedot näkyvissä', () => {
      test('Suorituksen tiedot', async ({ ibOppijaPage }) => {
        const suoritus = ibOppijaPage.$.suoritukset(0)
        await expect(suoritus.koulutus.elem).toContainText('Pre-IB')
        await expect(await suoritus.organisaatio.value()).toEqual(
          'Ressun lukio'
        )
        await expect(await suoritus.suorituskieli.value()).toEqual('englanti')

        const vahvistus = suoritus.suorituksenVahvistus.value
        await expect(vahvistus.status.elem).toContainText('Suoritus valmis')
        await expect(vahvistus.henkilö(0).elem).toContainText(
          'Reijo Reksi (rehtori)'
        )
      })
    })
  })
})
