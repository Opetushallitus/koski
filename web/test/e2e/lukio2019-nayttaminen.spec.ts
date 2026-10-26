import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { LukioOpiskeluoikeus } from './pages/oppija/components/LukioOpiskeluoikeus'

test.describe('Lukiokoulutus 2019: virkailija', () => {
  test.use({ storageState: virkailija('pää') })

  // Pelkkä näyttötesti, mutta fixturet resetoidaan silti: sama oppija on
  // jaettu lukio2019-oppiaineen-muokkaus.spec.ts:n kanssa, joka tallentaa
  // muutoksia - ei saa olettaa, missä järjestyksessä tiedostot ajetaan.
  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test.describe('Lukion päättötodistus', () => {
    let opiskeluoikeus: LukioOpiskeluoikeus

    test.beforeEach(async ({ virkailijaPage, oppijaHaku, oppijaPage }) => {
      await virkailijaPage.goto()
      await (await oppijaHaku.search('250605A518Y')).clickOnFirst()
      await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')
      opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('Lukion oppimäärä')
    })

    test('Tutkinto ja oppilaitos näytetään oppijan suorituksissa', async ({
      oppijaPage
    }) => {
      await expect(opiskeluoikeus.tutkinnonNimi).toHaveText('Lukion oppimäärä')
      await expect(
        oppijaPage.getOppilaitosNimi('Lukion oppimäärä')
      ).toHaveText('Jyväskylän normaalikoulu')
    })

    test('Opiskeluoikeuden tiedot näytetään', async () => {
      await expect(opiskeluoikeus.opiskeluoikeudenTiedot).toContainText(
        'Opiskeluoikeuden voimassaoloaika: 1.8.2019 — 5.9.2021'
      )

      await opiskeluoikeus.odotaTilaHistoria([
        { päivä: '5.9.2021', tila: 'Valmistunut (valtionosuusrahoitteinen koulutus)' },
        { päivä: '1.8.2019', tila: 'Läsnä (valtionosuusrahoitteinen koulutus)' }
      ])

      await expect(opiskeluoikeus.kenttä('oppimääräSuoritettu')).toHaveText(
        'kyllä'
      )
    })

    test('Suorituksen tiedot näytetään', async () => {
      // Tunniste, koodiarvo ja diaarinumero renderöityvät vierekkäisinä
      // <span>-elementteinä ilman välilyöntiä niiden välissä.
      await expect(opiskeluoikeus.kenttä('koulutusmoduuli')).toHaveText(
        'Lukion oppimäärä309902OPH-2263-2019'
      )
      await expect(opiskeluoikeus.kenttä('oppimäärä')).toHaveText(
        'Lukio suoritetaan nuorten opetussuunnitelman mukaan'
      )
      await expect(opiskeluoikeus.kenttä('toimipiste')).toHaveText(
        'Jyväskylän normaalikoulu'
      )
      await expect(opiskeluoikeus.kenttä('suorituskieli')).toHaveText('suomi')
      await expect(opiskeluoikeus.kenttä('ryhmä')).toHaveText('AH')
      await expect(opiskeluoikeus.kenttä('koulusivistyskieli')).toHaveText(
        'suomi'
      )

      const taydentavat = opiskeluoikeus.omanÄidinkielenOpinnot
      await expect(opiskeluoikeus.kenttä('arvosana', taydentavat)).toHaveText(
        '8'
      )
      await expect(
        opiskeluoikeus.kenttä('arviointipäivä', taydentavat)
      ).toHaveText('4.9.2021')
      await expect(opiskeluoikeus.kenttä('kieli', taydentavat)).toHaveText(
        'saame, lappi'
      )
      await expect(opiskeluoikeus.kenttä('laajuus', taydentavat)).toHaveText(
        '3 op'
      )

      await expect(opiskeluoikeus.tilaVahvistus.locator('.tila')).toHaveText(
        'Suoritus valmis'
      )
      await expect(
        opiskeluoikeus.tilaVahvistus.locator('.vahvistus.inline')
      ).toHaveText('15.5.2020 Jyväskylä Reijo Reksi, rehtori')
    })

    test('Oppiaineiden ja kurssien arvosanat näytetään', async () => {
      const ai = opiskeluoikeus.oppiaineRivi('AI')
      await expect(ai.locator('.nimi')).toHaveText(
        'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus'
      )

      await expect(
        opiskeluoikeus.kurssi(ai, 'ÄI1').locator('.arvosana')
      ).toHaveText('8')
      await expect(
        opiskeluoikeus.kurssi(ai, 'ÄI2').locator('.arvosana')
      ).toHaveText('8')
      await expect(
        opiskeluoikeus.kurssi(ai, 'ÄI3').locator('.arvosana')
      ).toHaveText('8')

      await expect(ai.locator('.laajuus:not(.arvioitu)')).toHaveText('6')
      await expect(ai.locator('.laajuus.arvioitu')).toHaveText('6')
      await expect(ai.locator('td.arvosana')).toHaveText('9')
    })
  })

  test.describe('Lukion oppiaineiden oppimäärien suoritus', () => {
    test.describe('Laajuuksien erottelu', () => {
      let opiskeluoikeus: LukioOpiskeluoikeus

      test.beforeEach(async ({ virkailijaPage, oppijaHaku, oppijaPage }) => {
        await virkailijaPage.goto()
        await (await oppijaHaku.search('010105A1995')).clickOnFirst()
        await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')
        opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('lukion aineopinnot')
      })

      test('Arvioitu ja hyväksytysti arvioitu laajuus näytetään erikseen oppiaineelle', async () => {
        // Varmistetaan, että sarakkeiden otsikot todella tarkoittavat sitä,
        // mitä alla luetut arvot olettavat - sarake-erottelu nojaa CSS-luokkaan
        // (.laajuus vs. .laajuus.arvioitu), ei suoraan käyttäjän näkemään otsikkoon.
        await expect(
          opiskeluoikeus.oppiaineet.locator('th.laajuus:not(.arvioitu)')
        ).toHaveText('Arvioitu (opintopistettä)')
        await expect(
          opiskeluoikeus.oppiaineet.locator('th.laajuus.arvioitu')
        ).toHaveText('Hyväksytysti arvioitu (opintopistettä)')

        const fy = opiskeluoikeus.oppiaineRivi('FY')
        await expect(fy.locator('.laajuus:not(.arvioitu)')).toHaveText('6')
        await expect(fy.locator('.laajuus.arvioitu')).toHaveText('4')
      })
    })

    test.describe('Perustiedot', () => {
      let opiskeluoikeus: LukioOpiskeluoikeus

      test.beforeEach(async ({ virkailijaPage, oppijaHaku, oppijaPage }) => {
        await virkailijaPage.goto()
        await (await oppijaHaku.search('010705A6119')).clickOnFirst()
        await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')
        opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('lukion aineopinnot')
      })

      test('Tutkinto ja oppilaitos näytetään, suoritustyyppi näkyy otsikossa', async ({
        oppijaPage
      }) => {
        await expect(opiskeluoikeus.tutkinnonNimi).toHaveText(
          'Lukion aineopinnot'
        )
        await expect(
          oppijaPage.getOppilaitosNimi('lukion aineopinnot')
        ).toHaveText('Jyväskylän normaalikoulu')

        await expect(
          opiskeluoikeus.container.locator('h3 .koulutus')
        ).toHaveText('Lukion aineopinnot')
      })

      test('Opiskeluoikeuden tiedot näytetään', async () => {
        // Toisin kuin "Lukion oppimäärä" -suoritustyypillä, tällä ei ole
        // oppimääräSuoritettu-kenttää lainkaan.
        await expect(
          opiskeluoikeus.kenttä('oppimääräSuoritettu')
        ).toHaveCount(0)

        await opiskeluoikeus.odotaTilaHistoria([
          { päivä: '5.9.2021', tila: 'Valmistunut (valtionosuusrahoitteinen koulutus)' },
          { päivä: '1.8.2019', tila: 'Läsnä (valtionosuusrahoitteinen koulutus)' }
        ])
      })
    })
  })
})
