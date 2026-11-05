import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Lukio-v2:n kolmas muokkausominaisuus (arvosanan ja kieliaineen/
 * suorituskielen jälkeen): valtakunnallisen oppiaineen lisäys ja poisto - ks.
 * UusiLukionOppiaine-komponentti ja oppiainerivin poistonappi
 * LukionOppiaineet.tsx:ssä. Ensimmäinen listamuokkausominaisuus, ei vain
 * olemassa olevan kentän editointi. Ajetaan sekä vanhaa että uutta
 * käyttöliittymää vasten samoilla assertioilla, kuten
 * lukio2019-nayttaminen.spec.ts:ssä ja aiemmissa muokkausspekeissä.
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
  test.describe(`Lukiokoulutus 2019: oppiaineen lisäys ja poisto, ${variantti.nimi} käyttöliittymä`, () => {
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

    test('Valtakunnallisen oppiaineen lisääminen vaatii arvosanan, koska suoritus on valmis', async () => {
      await katselu.lisääOppiaine('Psykologia')

      await expect(katselu.tallennaBtn).toBeDisabled()
      await expect(katselu.arvosanaVaadittuVirhe).toBeVisible()
      await expect(katselu.oppiaineenNimi(katselu.oppiaineRivi('PS'))).toHaveText(
        'Psykologia'
      )
    })

    test('Valtakunnallinen oppiaine voidaan lisätä ja tallentaa arvosanan kanssa', async () => {
      await katselu.lisääOppiaine('Psykologia')
      const ps = katselu.oppiaineRivi('PS')
      await katselu.muokkaaOppiaineenArvosanaa(ps, '9')
      await katselu.tallenna()

      await expect(katselu.oppiaineenNimi(ps)).toHaveText('Psykologia')
      await expect(katselu.oppiaineenArvosana(ps)).toHaveText('9')
    })

    test('Valtakunnallinen oppiaine voidaan poistaa', async () => {
      await katselu.lisääOppiaine('Psykologia')
      const ps = katselu.oppiaineRivi('PS')
      await katselu.muokkaaOppiaineenArvosanaa(ps, '9')
      await katselu.tallenna()

      await katselu.aloitaMuokkaus()
      await katselu.poistaOppiaine(katselu.oppiaineRivi('PS'))
      await katselu.tallenna()

      await expect(katselu.oppiaineRivi('PS')).toHaveCount(0)
    })
  })
}
