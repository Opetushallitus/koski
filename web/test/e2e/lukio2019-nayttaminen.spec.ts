import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { LukioOpiskeluoikeus } from './pages/oppija/components/LukioOpiskeluoikeus'

test.describe('Lukiokoulutus 2019: virkailija', () => {
  test.use({ storageState: virkailija('pää') })

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

      // Tila-historia on kaksi erillistä opiskeluoikeusjaksoa: varmistetaan
      // päivämäärä ja tila per jakso erikseen, jotta pari ei voi mennä sekaisin.
      const jaksot = opiskeluoikeus.kenttä('tila').locator('.opiskeluoikeusjakso')
      await expect(jaksot.nth(0).locator('label.date')).toHaveText('5.9.2021')
      await expect(jaksot.nth(0).locator('label.tila')).toHaveText(
        'Valmistunut (valtionosuusrahoitteinen koulutus)'
      )
      await expect(jaksot.nth(1).locator('label.date')).toHaveText('1.8.2019')
      await expect(jaksot.nth(1).locator('label.tila')).toHaveText(
        'Läsnä (valtionosuusrahoitteinen koulutus)'
      )

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

      // Lukion oppimäärää täydentävät oman äidinkielen opinnot
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
  })
})
