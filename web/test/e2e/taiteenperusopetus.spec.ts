import { test, expect } from './base'
import { virkailija } from './setup/auth'

test.describe('Taiteen perusopetus', () => {
  test.use({ storageState: virkailija('kalle') })
  test.beforeEach(async ({ fixtures, page }) => {
    page.once('dialog', (dialog) => {
      dialog.accept()
    })
    await fixtures.reset()
  })

  test.describe('Uuden opiskeluoikeuden luonti', () => {
    test.beforeEach(async ({ uusiOppijaPage }) => {
      await uusiOppijaPage.goTo('230872-7258')
      await uusiOppijaPage.fill({
        etunimet: 'Tero',
        sukunimi: 'Taiteilija',
        oppilaitos: 'Varsinais-Suomen kansanopisto',
        opiskeluoikeus: 'Taiteen perusopetus'
      })
    })

    test('Lisäys ei onnistu ilman puuttuvien tietojen syöttämistä', async ({
      uusiOppijaPage
    }) => {
      await expect(uusiOppijaPage.submitBtn).toBeDisabled()
    })

    test('Vain oikeat opiskeluoikeuden tilat valittavissa', async ({
      uusiOppijaPage
    }) => {
      await expect(
        await uusiOppijaPage.opiskeluoikeudenTila.getOptions()
      ).toHaveText(['Läsnä', 'Päättynyt'])
    })

    test('Oppimäärä on esivalittu ja oikeat suorituksen tyypit ovat valittavissa', async ({
      uusiOppijaPage
    }) => {
      await expect(
        await uusiOppijaPage.oppimäärä.textInput.textContent()
      ).toBe("Taiteen perusopetuksen yleinen oppimäärä")
      await expect(
        await uusiOppijaPage.suoritustyyppi.getOptions()
      ).toHaveText(["Taiteen perusopetuksen yleisen oppimäärän yhteisten opintojen suoritus", "Taiteen perusopetuksen yleisen oppimäärän teemaopintojen suoritus"])
    })

    test('Oppimäärän voi vaihtaa ja suoritustyypit näytetään oikein', async ({
      uusiOppijaPage
    }) => {
      await uusiOppijaPage.fill({
        oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä'
      })
      await expect(
        await uusiOppijaPage.suoritustyyppi.getOptions()
      ).toHaveText(["Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus", "Taiteen perusopetuksen laajan oppimäärän syventävien opintojen suoritus"])
    })

    test('Suoritustyypin voi asettaa ja peruste valikoituu oikein', async ({
      uusiOppijaPage
    }) => {
      await uusiOppijaPage.fill({
        oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä',
        suoritustyyppi: 'Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus'
      })
      await expect(
        await uusiOppijaPage.page.getByTestId("tpo-peruste-input")
      ).toHaveValue("OPH-2068-2017")
    })

    test('Opiskeluoikeuden luonti onnistuu', async ({
      uusiOppijaPage
    }) => {
      await uusiOppijaPage.fill({
        oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä',
        suoritustyyppi: 'Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus',
        taiteenala: 'Tanssi'
      })
      await uusiOppijaPage.submitAndExpectSuccess()
    })
  })
})
