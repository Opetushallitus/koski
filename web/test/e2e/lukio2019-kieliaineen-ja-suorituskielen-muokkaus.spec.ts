import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Lukio-v2:n toinen muokkausominaisuus (arvosanan jälkeen): kieliaineen
 * kielen ja oppiaineen suorituskielen editointi - ks. kieliaineKoodistoUri-
 * ja suorituskieli-kytkentä LukionOppiaineet.tsx:ssä. Ajetaan sekä vanhaa
 * että uutta käyttöliittymää vasten samoilla assertioilla, kuten
 * lukio2019-nayttaminen.spec.ts:ssä ja lukio2019-arvosanan-muokkaus.spec.ts:ssä.
 */
const VARIANTIT: Array<{
  nimi: string
  luoKatselu: (page: Page, otsikko: string | RegExp) => LukioKatselu
}> = [
  {
    nimi: 'vanha',
    luoKatselu: (page, otsikko) => new VanhaLukioKatselu(page, otsikko)
  },
  {
    nimi: 'uusi',
    luoKatselu: (page) => new UusiLukioKatselu(page)
  }
]

for (const variantti of VARIANTIT) {
  test.describe(`Lukiokoulutus 2019: kieliaineen ja suorituskielen muokkaus, ${variantti.nimi} käyttöliittymä`, () => {
    test.use({ storageState: virkailija('pää') })

    // Uusilukio, Ulla (250605A518Y)
    const oid = '1.2.246.562.24.00000000015'
    let katselu: LukioKatselu

    test.beforeEach(async ({ fixtures, page }) => {
      await fixtures.reset()
      katselu = variantti.luoKatselu(page, 'Lukion oppimäärä')
      await katselu.goto(oid)
      await katselu.aloitaMuokkaus()
      await katselu.poistaViimeisinTila()
    })

    test('Kieliaineen kieli voidaan muuttaa ja tallentaa', async () => {
      const ai = katselu.oppiaineRivi('AI')
      await katselu.muokkaaKieliaineenKieltä(ai, 'Ruotsin kieli ja kirjallisuus')
      await katselu.tallenna()

      await expect(katselu.oppiaineenNimi(ai)).toHaveText(
        'Äidinkieli ja kirjallisuus, Ruotsin kieli ja kirjallisuus'
      )
    })

    test('Suorituskieli voidaan muuttaa ja tallentaa', async () => {
      const ai = katselu.oppiaineRivi('AI')
      await katselu.muokkaaOppiaineenSuorituskieltä(ai, 'arabia')
      await katselu.tallenna()

      await katselu.aloitaMuokkaus()
      await expect(katselu.oppiaineenSuorituskieliArvo(ai)).toHaveValue(
        'arabia'
      )
    })
  })
}
