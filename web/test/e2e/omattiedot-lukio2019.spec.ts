import { Locator, Page } from '@playwright/test'
import { expect, test } from './base'
import { kansalainen } from './setup/auth'
import { KoskiKansalainenPage } from './pages/kansalainen/KoskiKansalainenPage'

/**
 * Omat tiedot (kansalaisnäkymä) lukio 2019:lle. Katselua vain - kansalainen
 * ei voi muokata omia tietojaan, joten tämä ei ole muokkaustilan vaihe kuten
 * valtaosa muista lukio-v2-speceistä, vaan sama "porttaa olemassa oleva testi
 * kahdelle käyttöliittymälle" -tyyppinen retrofit kuin alkuperäinen
 * lukio2019-nayttaminen.spec.ts.
 *
 * Vain kaksi kolmesta alkuperäisestä testistä on porttikelpoista. Vanhan
 * käyttöliittymän kansalaisnäkymällä on oma, virkailijanäkymästä täysin
 * erillinen komponenttipuu (table.omattiedot-suoritukset,
 * tr.oppiaine-header ym. - ei sama kuin vanhan virkailijanäkymän
 * LukioOpiskeluoikeus.ts:n .oppiaine-rivi), jossa kurssit avautuvat
 * mobiilissa klikkaamalla. Lukio-v2 taas käyttää kansalaisnäkymässä TÄSMÄLLEEN
 * samaa LukionOppiaineet-komponenttia kuin virkailijanäkymässä (vahvistettu:
 * data-koodiarvo, .LukionOppiaineet__-luokat, .footnote-hint ja
 * .kurssit-yhteensä kaikki toimivat ilman uutta sovelluskoodia, ks.
 * useKansalainenUiAdapter/opiskeluoikeusEditors), eikä sillä ole erillistä
 * mobiilin avaa/sulje-tilaa lainkaan (kurssit ovat aina auki, kuten
 * virkailijanäkymässäkin) - "Mobiilinäkymässä oppiaineen kurssit avautuvat
 * klikkaamalla" -testiä ei siksi voi portata, sille ei ole vastaavaa
 * käyttäytymistä uudessa arkkitehtuurissa.
 *
 * Myös laajuuden tekstimuoto eroaa: vanha kansalaisnäkymä näyttää
 * "(6 opintopistettä)", vanha JA uusi virkailijanäkymä (ja siis uusi
 * kansalaisnäkymäkin, sama komponentti) näyttävät pelkän "6" - sarakeotsikko
 * kertoo yksikön jo kertaalleen.
 */
type OmatTiedotKatselu = {
  nimi: string
  goto: (page: Page) => Promise<void>
  oppiaineRivi: (page: Page, koodi: string, nimiSisältää: string) => Locator
  oppiaineenArvosana: (rivi: Locator) => Locator
  oppiaineenLaajuus: (rivi: Locator) => Locator
  oppiaineenLaajuusTeksti: (arvo: string) => string
  kurssi: (rivi: Locator, koodi: string) => Locator
  erityinenTutkintoLegend: (page: Page) => Locator
  kurssitYhteensä: (page: Page) => Locator
  // Formatoitu eri tavalla kuin muut laajuustekstit - "yhteensä"-luvut
  // sisältävät desimaalin aina (ks. removeFloatingPointDrift-kutsuva koodi),
  // kun taas pelkkä kokonaisluku formatoidaan ilman trailing ,0:aa.
  kurssitYhteensäTeksti: string
}

const VANHA: OmatTiedotKatselu = {
  nimi: 'Vanha käyttöliittymä',
  goto: async (page) => {
    await page.goto('/koski/omattiedot')
  },
  oppiaineRivi: (page, _koodi, nimiSisältää) =>
    page
      .locator('table.omattiedot-suoritukset tr.oppiaine-header')
      .filter({ hasText: nimiSisältää }),
  oppiaineenArvosana: (rivi) => rivi.locator('.arvosana'),
  oppiaineenLaajuus: (rivi) => rivi.locator('.laajuus'),
  oppiaineenLaajuusTeksti: (arvo) => `(${arvo} opintopistettä)`,
  kurssi: (rivi, koodi) =>
    rivi
      .locator('xpath=following-sibling::tr[1]')
      .locator('li.kurssi')
      .filter({
        has: rivi.page().getByRole('button', { name: koodi, exact: true })
      }),
  erityinenTutkintoLegend: (page) =>
    page.getByText('** = erityisenä tutkintona suoritettu oppiaine'),
  kurssitYhteensä: (page) => page.locator('.kurssit-yhteensä'),
  kurssitYhteensäTeksti:
    'Arvioitujen osasuoritusten laajuus yhteensä: 167,5Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 164,0'
}

const UUSI: OmatTiedotKatselu = {
  nimi: 'Uusi käyttöliittymä',
  goto: async (page) => {
    const params = new URLSearchParams({ 'lukio-v2': 'true' })
    await page.goto(`/koski/omattiedot?${params.toString()}`)
  },
  oppiaineRivi: (page, koodi) => page.locator(`[data-koodiarvo="${koodi}"]`),
  oppiaineenArvosana: (rivi) => rivi.locator('.LukionOppiaineet__arvosana'),
  oppiaineenLaajuus: (rivi) =>
    rivi.locator('.LukionOppiaineet__laajuus').first(),
  oppiaineenLaajuusTeksti: (arvo) => arvo,
  kurssi: (rivi, koodi) =>
    rivi.locator('li.kurssi').filter({
      has: rivi
        .page()
        .locator('.tunniste', { hasText: new RegExp(`^${koodi}$`) })
    }),
  erityinenTutkintoLegend: (page) =>
    page.getByText(/erityisenä tutkintona suoritettu oppiaine/i),
  kurssitYhteensä: (page) => page.locator('.kurssit-yhteensä'),
  kurssitYhteensäTeksti:
    'Arvioitujen osasuoritusten laajuus yhteensä: 167,5Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 164'
}

for (const katselu of [VANHA, UUSI]) {
  test.describe(`Omat tiedot: lukio 2019, ${katselu.nimi}`, () => {
    test.beforeEach(async ({ fixtures }) => {
      await fixtures.apiLoginAsUser('kalle', 'kalle')
      await fixtures.reset()
      await fixtures.apiLogout()
    })

    test.describe('Lukion oppimäärä', () => {
      test.use({ storageState: kansalainen('250605A518Y') })

      test('Oppiaineiden kurssit näytetään suoraan auki', async ({
        page
      }) => {
        await katselu.goto(page)
        const kansalainenPage = new KoskiKansalainenPage(page)
        await kansalainenPage.openOpiskeluoikeus(/Lukion oppimäärä/)

        const ai = katselu.oppiaineRivi(page, 'AI', 'Äidinkieli')
        await expect(katselu.oppiaineenArvosana(ai)).toHaveText('9')
        await expect(katselu.oppiaineenLaajuus(ai)).toHaveText(
          katselu.oppiaineenLaajuusTeksti('6')
        )

        // Kurssit näkyvät suoraan ilman erillistä avaamista, sekä vanhassa
        // että uudessa käyttöliittymässä (uudella ei edes ole muuta tilaa).
        const äi1 = katselu.kurssi(ai, 'ÄI1')
        await expect(äi1.locator('.arvosana')).toHaveText('8')
      })
    })

    test.describe('Lukion aineopinnot: erityinen tutkinto', () => {
      test.use({ storageState: kansalainen('010705A6119') })

      test('Erityisenä tutkintona suoritetun oppiaineen alaviite ja laajuus näytetään oikein', async ({
        page
      }) => {
        await katselu.goto(page)
        const kansalainenPage = new KoskiKansalainenPage(page)
        await kansalainenPage.openOpiskeluoikeus(/Lukion aineopinnot/)

        const bi = katselu.oppiaineRivi(page, 'BI', 'Biologia')
        await expect(katselu.oppiaineenLaajuus(bi)).toHaveText(
          katselu.oppiaineenLaajuusTeksti('6')
        )
        await expect(bi.locator('.footnote-hint')).toBeVisible()

        await expect(katselu.erityinenTutkintoLegend(page)).toBeVisible()

        await expect(katselu.kurssitYhteensä(page)).toHaveText(
          katselu.kurssitYhteensäTeksti
        )
      })
    })
  })
}
