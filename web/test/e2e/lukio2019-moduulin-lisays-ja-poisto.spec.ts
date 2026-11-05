import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Lukio-v2:n neljäs muokkausominaisuus (arvosanan, kieliaineen/
 * suorituskielen ja oppiaineen lisäyksen/poiston jälkeen): valtakunnallisen
 * moduulin (kurssin) lisäys ja poisto olemassa olevan oppiaineen alle - ks.
 * UusiLukionModuuli-komponentti ja kurssirivin poistonappi
 * LukionOppiaineet.tsx:ssä. Vanhan käyttöliittymän lukio2019-kurssit.spec.ts
 * sisältää vain nämä kolme testiä (arvosanan muokkaus, lisäys, poisto) - ei
 * osaamisen tunnustamista, joten sitä ei retrofitata tähän. Ajetaan sekä
 * vanhaa että uutta käyttöliittymää vasten samoilla assertioilla.
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
  test.describe(`Lukiokoulutus 2019: moduulin lisäys ja poisto, ${variantti.nimi} käyttöliittymä`, () => {
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

    test('Valtakunnallinen moduuli voidaan lisätä oppiaineelle', async () => {
      const aom = katselu.oppiaineRivi('AOM')
      const rua2 = await katselu.lisääKurssi(aom, 'RUA2', '2')
      await katselu.muokkaaKurssinArvosanaa(rua2, '9')
      await katselu.tallenna()

      await expect(rua2).toBeVisible()
      await expect(katselu.kurssinArvosana(rua2)).toHaveText('9')
    })

    test('Moduuli/kurssi voidaan poistaa', async () => {
      const ai = katselu.oppiaineRivi('AI')
      const ai2 = katselu.kurssi(ai, 'ÄI2')
      await katselu.poistaKurssi(ai2)
      await katselu.tallenna()

      await expect(katselu.kurssi(katselu.oppiaineRivi('AI'), 'ÄI2')).toHaveCount(
        0
      )
    })
  })
}
