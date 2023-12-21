import { test, expect } from './base'
import { virkailija } from './setup/auth'

const keskeneräinenOpiskelija = '1.2.246.562.24.00000000142'
const hyväksytystiSuorittanutOpiskelija = '1.2.246.562.24.00000000143'

test.describe('Taiteen perusopetus', () => {
  test.beforeEach(async ({ fixtures, page }) => {
    page.once('dialog', (dialog) => {
      dialog.accept()
    })
    await fixtures.reset()
  })

  test.describe('Itse järjestetty opiskeluoikeus', () => {
    test.use({ storageState: virkailija('kalle') })

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
        expect(await uusiOppijaPage.oppimäärä.textInput.textContent()).toBe(
          'Taiteen perusopetuksen yleinen oppimäärä'
        )
        await expect(
          await uusiOppijaPage.suoritustyyppi.getOptions()
        ).toHaveText([
          'Taiteen perusopetuksen yleisen oppimäärän yhteisten opintojen suoritus',
          'Taiteen perusopetuksen yleisen oppimäärän teemaopintojen suoritus'
        ])
      })

      test('Oppimäärän voi vaihtaa ja suoritustyypit näytetään oikein', async ({
        uusiOppijaPage
      }) => {
        await uusiOppijaPage.fill({
          oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä'
        })
        await expect(
          await uusiOppijaPage.suoritustyyppi.getOptions()
        ).toHaveText([
          'Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus',
          'Taiteen perusopetuksen laajan oppimäärän syventävien opintojen suoritus'
        ])
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
          uusiOppijaPage.page.getByTestId('tpo-peruste-input')
        ).toHaveValue('OPH-2068-2017')
      })

      test('Opiskeluoikeuden luonti onnistuu', async ({ uusiOppijaPage }) => {
        await uusiOppijaPage.fill({
          oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä',
          suoritustyyppi:
            'Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus',
          taiteenala: 'Tanssi'
        })
      })
    })
  })

  test.describe('Hankintakoulutuksena järjestetty opiskeluoikeus', () => {
    test.use({ storageState: virkailija('hki-tallentaja') })

    test.describe('Uuden opiskeluoikeuden luonti', () => {
      test.beforeEach(async ({ uusiOppijaPage }) => {
        await uusiOppijaPage.goTo('230872-7258')
        await uusiOppijaPage.page
          .getByTestId('hankintakoulutus-checkbox__label')
          .click()
        await uusiOppijaPage.fill({
          etunimet: 'Tero',
          sukunimi: 'Taiteilija',
          oppilaitos: 'Varsinais-Suomen kansanopisto',
          opiskeluoikeus: 'Taiteen perusopetus'
        })
      })

      test('Opiskeluoikeuden luonti onnistuu', async ({ uusiOppijaPage }) => {
        await uusiOppijaPage.fill({
          oppimäärä: 'Taiteen perusopetuksen laaja oppimäärä',
          suoritustyyppi:
            'Taiteen perusopetuksen laajan oppimäärän perusopintojen suoritus',
          taiteenala: 'Kuvataide'
        })
        await uusiOppijaPage.submitAndExpectSuccess()
      })
    })
  })

  test.describe(`Oppijan sivu ${hyväksytystiSuorittanutOpiskelija}`, () => {
    test.use({ storageState: virkailija('kalle') })
    test.beforeEach(async ({ taiteenPerusopetusPage }) => {
      await taiteenPerusopetusPage.goto(hyväksytystiSuorittanutOpiskelija)
      await taiteenPerusopetusPage.selectOpiskeluoikeus('taiteenperusopetus')
    })

    test('Näyttää suorituksen tiedot oikein', async ({
      taiteenPerusopetusPage: page
    }) => {
      expect(await page.suoritustieto('taiteenala')).toBe('Musiikki')
      expect(await page.suoritustieto('oppimäärä')).toBe(
        'Taiteen perusopetuksen laaja oppimäärä'
      )
      expect(await page.suoritustieto('koulutuksenToteutustapa')).toBe(
        'Itse järjestetty koulutus'
      )
      expect(await page.suoritustieto('oppilaitos')).toBe(
        'Varsinais-Suomen kansanopisto'
      )
      expect(await page.suoritustieto('laajuus')).toBe('29,6 op')

      await page.selectSuoritus(1)

      expect(await page.suoritustieto('laajuus')).toBe('18,5 op')
    })

    test('Näyttää osasuorituksien tiedot oikein', async ({
      taiteenPerusopetusPage: page
    }) => {
      for (let osasuoritusIndex = 0; osasuoritusIndex < 3; osasuoritusIndex++) {
        await page.openOsasuoritus(osasuoritusIndex)
        expect(await page.osasuoritustieto('nimi')).toBe('Musiikin kurssi')
        expect(await page.osasuoritustieto('laajuus')).toBe(
          ['10 op', '10 op', '9,6 op'][osasuoritusIndex]
        )
        expect(await page.osasuoritustieto('arvosana')).toBe('Hyväksytty')

        expect(await page.osasuoritusProperty('arvosana')).toBe('Hyväksytty')
        expect(await page.osasuoritusProperty('date')).toBe('1.1.2021')
        if (osasuoritusIndex === 0) {
          expect(await page.osasuoritusProperty('tunnustettu')).toBe(
            'Tunnustettu paikallinen opintokokonaisuus'
          )
        }
      }
    })
  })

  test.describe(`Oppijan sivu ${keskeneräinenOpiskelija}`, () => {
    test.use({ storageState: virkailija('kalle') })
    test.beforeEach(async ({ taiteenPerusopetusPage }) => {
      await taiteenPerusopetusPage.goto(keskeneräinenOpiskelija)
      await taiteenPerusopetusPage.edit()
    })

    test.describe('Osasuoritusten käsittely', () => {
      test('Uuden osasuorituksen lisääminen', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.addNewOsasuoritus('Säveltapailu')
        expect(await page.osasuoritustieto('nimi')).toBe('Säveltapailu')
      })

      test('Luodut osasuoritukset tallentuvat uudelleenkäytettäväksi, osasuoritus on poistettavissa', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.addNewOsasuoritus('Nuotinnus')
        await page.removeOsasuoritus(0)

        expect(
          await page.$.suoritukset(0).addOsasuoritus.select.options()
        ).toEqual(['Lisää osasuoritus', 'Nuotinnus'])

        await page.addOsasuoritus('Nuotinnus')
        expect(await page.osasuoritustieto('nimi')).toBe('Nuotinnus')
      })

      test('Tallennetut osasuoritukset on poistettavissa', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.addNewOsasuoritus('Musiikkihistoria')

        expect(
          await page.$.suoritukset(0).addOsasuoritus.select.options()
        ).toEqual(['Lisää osasuoritus', 'Musiikkihistoria'])

        await page.deleteStoredOsasuoritus('Musiikkihistoria')

        expect(
          await page.$.suoritukset(0).addOsasuoritus.select.options()
        ).toEqual(['Lisää osasuoritus'])
      })

      test('Osasuorituksen laajuuden asetus toimii', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.addNewOsasuoritus('Säestys')
        await page.addNewOsasuoritus('Kuorolaulu')

        page.osasuoritusIndex = 0
        await page.setOsasuorituksenLaajuus(3.2)

        page.osasuoritusIndex = 1
        await page.setOsasuorituksenLaajuus(3.1)

        expect(await page.suoritustieto('laajuus')).toEqual('6,3 op')
      })

      test('Osasuorituksen arvosanan asetus toimii', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.addNewOsasuoritus('Säestys')
        await page.setOsasuorituksenArvosana(
          'arviointiasteikkotaiteenperusopetus_hyvaksytty'
        )
        expect(await page.osasuoritustieto('arvosana')).toEqual('Hyväksytty')
      })
    })

    test.describe('Opiskeluoikeuden tilan muokkaus', () => {
      test.use({ storageState: virkailija('kalle') })
      test.beforeEach(async ({ taiteenPerusopetusPage }) => {
        await taiteenPerusopetusPage.goto(keskeneräinenOpiskelija)
      })

      test('Opiskeluoikeuden tilan näyttäminen', async ({
        taiteenPerusopetusPage: page
      }) => {
        expect(await page.opiskeluoikeudenTila(0)).toEqual('1.1.2021 Läsnä')
        await page.edit()
        expect(await page.opiskeluoikeudenTila(0)).toEqual('1.1.2021 Läsnä')
      })

      test('Opiskeluoikeuden tilan lisääminen: läsnä', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.edit()
        await page.addOpiskeluoikeudenTila('2.1.2021', 'lasna')
        expect(await page.opiskeluoikeudenTila(0)).toEqual('1.1.2021 Läsnä')
        expect(await page.opiskeluoikeudenTila(1)).toEqual('2.1.2021 Läsnä')
      })

      test('Opiskeluoikeuden tilan lisääminen: väliaikaisesti keskeytynyt', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.edit()
        await page.addOpiskeluoikeudenTila(
          '2.1.2021',
          'valiaikaisestikeskeytynyt'
        )
        expect(await page.opiskeluoikeudenTila(0)).toEqual('1.1.2021 Läsnä')
        expect(await page.opiskeluoikeudenTila(1)).toEqual(
          '2.1.2021 Väliaikaisesti keskeytynyt'
        )
      })

      test('Opiskeluoikeuden tilan poistaminen', async ({
        taiteenPerusopetusPage: page
      }) => {
        const removeFirstTilaBtn =
          page.$.opiskeluoikeus.tila.edit.items(0).remove

        await page.edit()
        await page.addOpiskeluoikeudenTila('2.1.2021', 'lasna')
        expect(await removeFirstTilaBtn.isVisible()).toEqual(false)

        await page.removeOpiskeluoikeudenTila(1)
        expect(await removeFirstTilaBtn.isVisible()).toEqual(true)
      })

      test('Opiskelutilaa ei voi asettaa suoritetuksi ilman suoritusten vahvistuksia', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.edit()
        await page.$.opiskeluoikeus.tila.edit.add.click()
        expect(
          await page.$.opiskeluoikeus.tila.edit.modal.tila.isDisabled(
            'hyvaksytystisuoritettu'
          )
        ).toBe(true)
      })
    })

    test.describe('Suorituksen vahvistus', () => {
      test.use({ storageState: virkailija('kalle') })
      test.beforeEach(async ({ taiteenPerusopetusPage }) => {
        await taiteenPerusopetusPage.goto(keskeneräinenOpiskelija)
      })

      test('Suoritusta ei voi vahvistaa ellei ole tarpeeksi hyväksyttyjä osasuorituksia kasassa', async ({
        taiteenPerusopetusPage: page
      }) => {
        const vahvistusBtn =
          page.$.suoritukset(0).suorituksenVahvistus.edit.merkitseValmiiksi

        await page.edit()
        expect(await vahvistusBtn.isDisabled()).toBe(true)

        await page.addNewOsasuoritus('Osasuoritus')
        expect(await vahvistusBtn.isDisabled()).toBe(true)

        await page.setOsasuorituksenArvosana(
          'arviointiasteikkotaiteenperusopetus_hyvaksytty'
        )
        expect(await vahvistusBtn.isDisabled()).toBe(true)

        await page.setOsasuorituksenLaajuus(11.1)
        expect(await vahvistusBtn.isDisabled()).toBe(false)
      })

      test('Suorituksen vahvistus uudella henkilöllä', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.edit()

        expect(await page.suorituksenTila()).toEqual('SUORITUS KESKEN')

        await page.addNewOsasuoritus('Osasuoritus')
        await page.setOsasuorituksenArvosana(
          'arviointiasteikkotaiteenperusopetus_hyvaksytty'
        )
        await page.setOsasuorituksenLaajuus(11.1)

        await page.vahvistaSuoritusUudellaHenkilöllä(
          'Teemu Rex',
          'rehtori',
          '1.2.2021'
        )

        expect(await page.suorituksenTila()).toEqual('SUORITUS VALMIS')
        expect(await page.suorituksenVahvistus()).toEqual(
          'Vahvistus: 1.2.2021 Varsinais-Suomen kansanopisto'
        )
        expect(await page.suorituksenVahvistushenkilö(0)).toEqual(
          'Teemu Rex (rehtori)'
        )

        await page.selectSuoritus(1)
        expect(await page.suorituksenTila()).toEqual('SUORITUS KESKEN')
      })

      test('Suorituksen vahvistuksen poisto + lisäys tallennetulla henkilöllä', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.edit()

        await page.addNewOsasuoritus('Osasuoritus')
        await page.setOsasuorituksenArvosana(
          'arviointiasteikkotaiteenperusopetus_hyvaksytty'
        )
        await page.setOsasuorituksenLaajuus(11.1)

        await page.vahvistaSuoritusUudellaHenkilöllä(
          'Amos Rex',
          'rehtori',
          '1.2.2021'
        )
        expect(await page.suorituksenTila()).toEqual('SUORITUS VALMIS')

        await page.poistaSuorituksenVahvistus()
        expect(await page.suorituksenTila()).toEqual('SUORITUS KESKEN')

        await page.vahvistaSuoritusTallennetullaHenkilöllä(
          'Amos Rex',
          '1.2.2021'
        )
        expect(await page.suorituksenTila()).toEqual('SUORITUS VALMIS')
        expect(await page.suorituksenVahvistus()).toEqual(
          'Vahvistus: 1.2.2021 Varsinais-Suomen kansanopisto'
        )
        expect(await page.suorituksenVahvistushenkilö(0)).toEqual(
          'Amos Rex (rehtori)'
        )
      })

      test('Opiskeluoikeuden tilaa ei voi merkitä hyvväksytysti suoritetuksi vain yhdellä vahvistetulla päätason suorituksella', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.edit()

        await page.addNewOsasuoritus('Osasuoritus')
        await page.setOsasuorituksenArvosana(
          'arviointiasteikkotaiteenperusopetus_hyvaksytty'
        )
        await page.setOsasuorituksenLaajuus(11.1)
        await page.vahvistaSuoritusUudellaHenkilöllä(
          'Amos Rex',
          'rehtori',
          '1.2.2021'
        )

        const ooTilaEditor = page.$.opiskeluoikeus.tila.edit
        await ooTilaEditor.add.click()
        await ooTilaEditor.modal.tila.isDisabled('hyvaksytystisuoritettu')
      })

      test('Suoritusten vahvistuksen jälkeen opiskeluoikeuden tilan voi merkitä suoritetuksi', async ({
        taiteenPerusopetusPage: page
      }) => {
        await page.edit()

        await page.addNewOsasuoritus('Osasuoritus')
        await page.setOsasuorituksenArvosana(
          'arviointiasteikkotaiteenperusopetus_hyvaksytty'
        )
        await page.setOsasuorituksenLaajuus(11.1)
        await page.vahvistaSuoritusUudellaHenkilöllä(
          'Amos Rex',
          'rehtori',
          '1.2.2021'
        )

        await page.selectSuoritus(1)

        await page.addNewOsasuoritus('Osasuoritus')
        await page.setOsasuorituksenArvosana(
          'arviointiasteikkotaiteenperusopetus_hyvaksytty'
        )
        await page.setOsasuorituksenLaajuus(11.1)
        await page.vahvistaSuoritusTallennetullaHenkilöllä(
          'Amos Rex',
          '1.2.2021'
        )

        await page.addOpiskeluoikeudenTila('1.2.2021', 'hyvaksytystisuoritettu')
      })
    })
  })
})
