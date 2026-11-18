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
  test.describe(`Lukiokoulutus 2019: paikallinen oppiaine, ${variantti.nimi} käyttöliittymä`, () => {
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

    test('Paikallisen oppiaineen lisääminen vaatii pakolliset tiedot ennen tallennusta', async () => {
      const uusiOppiaine = await katselu.lisääPaikallinenOppiaine({
        koodi: '',
        nimi: '',
        kuvaus: '',
        arvosana: '9'
      })

      await expect(uusiOppiaine).toHaveCount(1)
      await expect(katselu.tallennaBtn).toBeDisabled()
      await expect(katselu.editBarMessage).toHaveText(
        'Korjaa virheelliset tiedot.'
      )
    })

    test('Paikallinen oppiaine voidaan lisätä pakolliset tiedot täytettynä, ja sen alle voidaan lisätä osasuoritus/kurssi', async () => {
      await katselu.lisääPaikallinenOppiaine({
        koodi: 'PAI',
        nimi: 'Paikallinen oppiaine',
        kuvaus: 'Pakollinen kuvaus',
        arvosana: '9'
      })

      await expect(katselu.tallennaBtn).toBeEnabled()
      await katselu.tallenna()

      await expect(katselu.oppiaineenNimi(katselu.oppiaineRivi('PAI'))).toHaveText(
        'Paikallinen oppiaine'
      )

      await katselu.aloitaMuokkaus()
      const paiUudelleen = katselu.oppiaineRivi('PAI')
      const kurssi = await katselu.lisääPaikallinenKurssi(paiUudelleen, {
        tunniste: 'PA',
        nimi: 'Paikallinen kurssi',
        kuvaus: 'Kurssin kuvaus',
        laajuus: '2'
      })
      await katselu.muokkaaKurssinArvosanaa(kurssi, '9')
      await katselu.tallenna()

      const paaOppiaineRivi = katselu.oppiaineRivi('PAI')
      const paaKurssi = katselu.kurssi(paaOppiaineRivi, 'PA')
      await expect(paaKurssi).toBeVisible()
      await expect(katselu.kurssinArvosana(paaKurssi)).toHaveText('9')
    })

    test('Paikallinen oppiaine voidaan poistaa', async () => {
      await katselu.lisääPaikallinenOppiaine({
        koodi: 'PAI',
        nimi: 'Paikallinen oppiaine',
        kuvaus: 'Pakollinen kuvaus',
        arvosana: '9'
      })
      await katselu.tallenna()

      await katselu.aloitaMuokkaus()
      await katselu.poistaOppiaine(katselu.oppiaineRivi('PAI'))
      await katselu.tallenna()

      await expect(katselu.oppiaineRivi('PAI')).toHaveCount(0)
    })
  })
}
