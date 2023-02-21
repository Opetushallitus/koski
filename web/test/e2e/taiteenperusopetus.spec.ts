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
      await expect(await uusiOppijaPage.oppimäärä.textInput.textContent()).toBe(
        'Taiteen perusopetuksen yleinen oppimäärä'
      )
      await expect(await uusiOppijaPage.suoritustyyppi.getOptions()).toHaveText(
        [
          'Taiteen perusopetuksen yleisen oppimäärän yhteisten opintojen suoritus',
          'Taiteen perusopetuksen yleisen oppimäärän teemaopintojen suoritus'
        ]
      )
    })

    test('Oppimäärän voi vaihtaa ja suoritustyypit näytetään oikein', async ({
      uusiOppijaPage
    }) => {
      await uusiOppijaPage.fill({
        oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä'
      })
      await expect(await uusiOppijaPage.suoritustyyppi.getOptions()).toHaveText(
        [
          'Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus',
          'Taiteen perusopetuksen laajan oppimäärän syventävien opintojen suoritus'
        ]
      )
    })

    test('Suoritustyypin voi asettaa ja peruste valikoituu oikein', async ({
      uusiOppijaPage
    }) => {
      await uusiOppijaPage.fill({
        oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä',
        suoritustyyppi:
          'Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus'
      })
      await expect(
        await uusiOppijaPage.page.getByTestId('tpo-peruste-input')
      ).toHaveValue('OPH-2068-2017')
    })

    test('Opiskeluoikeuden luonti onnistuu', async ({ uusiOppijaPage }) => {
      await uusiOppijaPage.fill({
        oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä',
        suoritustyyppi:
          'Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus',
        taiteenala: 'Tanssi'
      })
      await uusiOppijaPage.submitAndExpectSuccess()
    })
  })

  test.describe('Oppijan sivu', () => {
    test.beforeEach(async ({ oppijaPage }) => {
      await oppijaPage.goto('1.2.246.562.24.00000000143')
    })

    test('näyttää suorituksen tiedot oikein', async ({
      taiteenPerusopetusPage
    }) => {
      const suoritus = taiteenPerusopetusPage.$.suoritukset(0)

      expect(await suoritus.taiteenala.value.innerText()).toBe('Musiikki')
      expect(await suoritus.oppimäärä.value.innerText()).toBe(
        'Taiteen perusopetuksen laaja oppimäärä'
      )
      expect(await suoritus.koulutuksenToteutustapa.value.innerText()).toBe(
        'Itse järjestetty koulutus'
      )
      expect(await suoritus.oppilaitos.value.innerText()).toBe(
        'Varsinais-Suomen kansanopisto'
      )
      expect(await suoritus.laajuus.value.innerText()).toBe('29.6 op')

      const suoritus2 = taiteenPerusopetusPage.$.suoritukset(1)
      suoritus2.tab.click()

      expect(await suoritus2.oppimäärä.value.innerText()).toBe(
        'Taiteen perusopetuksen syventävät opinnot'
      )
      expect(await suoritus2.laajuus.value.innerText()).toBe('18.5 op')
    })
  })
})
