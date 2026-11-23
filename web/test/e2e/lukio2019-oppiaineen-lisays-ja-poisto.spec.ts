import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

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

    test('Lisätty oppiaine on oletuksena pakollinen, ja valinnaiseksi muuttaminen näkyy nimessä', async () => {
      await katselu.lisääOppiaine('Psykologia')
      const ps = katselu.oppiaineRivi('PS')

      await expect(katselu.pakollinenCheckbox(ps)).toBeChecked()
      await expect(katselu.oppiaineenNimi(ps)).toHaveText('Psykologia')

      await katselu.pakollinenCheckbox(ps).uncheck()
      await expect(katselu.oppiaineenNimi(ps)).toHaveText(
        'Psykologia, valinnainen'
      )
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

test.describe('Lukiokoulutus 2019: matematiikan oppimäärän valinta uutta oppiainetta lisättäessä', () => {
  test.use({ storageState: virkailija('pää') })

  // Uusilukio, Ulla (250605A518Y)
  const oid = '1.2.246.562.24.00000000015'
  let katselu: UusiLukioKatselu

  test.beforeEach(async ({ fixtures, page }) => {
    await fixtures.reset()
    katselu = new UusiLukioKatselu(page)
    await katselu.goto(oid)
    await katselu.aloitaMuokkaus()
    await katselu.poistaViimeisinTila()
    // Kiinteä fixture sisältää matematiikan jo valmiiksi - poistetaan, jotta
    // "Lisää oppiaine" tarjoaa sen taas.
    await katselu.poistaOppiaine(katselu.oppiaineRivi('MA'))
  })

  test('Matematiikkaa ei lisätä ennen oppimäärän valintaa, ja lisätty oppiaine näyttää valitun oppimäärän', async ({
    page
  }) => {
    await katselu.lisääOppiaine('Matematiikka')

    await expect(katselu.oppiaineRivi('MA')).toHaveCount(0)
    await expect(katselu.matematiikanOppimääräToggle).toBeVisible()

    await katselu.valitseMatematiikanOppimäärä('Matematiikka, pitkä oppimäärä')

    const ma = katselu.oppiaineRivi('MA')
    await expect(katselu.oppiaineenNimi(ma)).toHaveText(
      'Matematiikka, pitkä oppimäärä'
    )
    await expect(katselu.matematiikanOppimääräToggle).toHaveCount(0)

    await katselu.muokkaaOppiaineenArvosanaa(ma, '9')
    await katselu.tallenna()

    await expect(katselu.oppiaineenNimi(ma)).toHaveText(
      'Matematiikka, pitkä oppimäärä'
    )
    await expect(katselu.oppiaineenArvosana(ma)).toHaveText('9')

    // Varmistetaan, että tieto on myös todella tallentunut taustajärjestelmään
    await page.reload()
    const maAfterReload = katselu.oppiaineRivi('MA')
    await expect(katselu.oppiaineenNimi(maAfterReload)).toHaveText(
      'Matematiikka, pitkä oppimäärä'
    )
    await expect(katselu.oppiaineenArvosana(maAfterReload)).toHaveText('9')
  })
})
