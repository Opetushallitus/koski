import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Lukio-v2:n ensimmäinen muokkausominaisuus: arvosanan editointi olemassa
 * oleville oppiaineille ja kursseille/moduuleille (ei lisäys/poisto, ei muita
 * kenttiä) - ks. ParasArvosanaEdit-kytkentä LukionOppiaineet.tsx:ssä. Ajetaan
 * sekä vanhaa että uutta käyttöliittymää vasten samoilla assertioilla, kuten
 * lukio2019-nayttaminen.spec.ts:ssäkin.
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
  test.describe(`Lukiokoulutus 2019: arvosanan muokkaus, ${variantti.nimi} käyttöliittymä`, () => {
    test.use({ storageState: virkailija('pää') })

    // Uusilukio, Ulla (250605A518Y)
    const oid = '1.2.246.562.24.00000000015'
    let katselu: LukioKatselu

    test.beforeEach(async ({ fixtures, page }) => {
      await fixtures.reset()
      katselu = variantti.luoKatselu(page, 'Lukion oppimäärä')
      await katselu.goto(oid)
      await katselu.aloitaMuokkaus()
      // Suoritus on jo "valmis": arvosanan muutos leimaisi arviointipäivän
      // tämänpäiväiseksi, joka ylittäisi opiskeluoikeuden päättymispäivän.
      // Poistetaan viimeisin tila ensin, kuten vanhan käyttöliittymän
      // lukio2019-kurssit.spec.ts:ssäkin.
      await katselu.poistaViimeisinTila()
    })

    test('Oppiaineen arvosana voidaan muuttaa ja tallentaa', async () => {
      const ai = katselu.oppiaineRivi('AI')
      await katselu.muokkaaOppiaineenArvosanaa(ai, '8')
      await katselu.tallenna()

      await expect(katselu.oppiaineenArvosana(ai)).toHaveText('8')
    })

    test('Moduulin arvosanaa voidaan muuttaa', async () => {
      const ma = katselu.oppiaineRivi('MA')
      const mab4 = katselu.kurssi(ma, 'MAB4')
      await katselu.muokkaaKurssinArvosanaa(mab4, '6')
      await katselu.tallenna()

      await expect(katselu.kurssinArvosana(mab4)).toHaveText('6')
    })
  })
}
