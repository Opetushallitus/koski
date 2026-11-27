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

test.describe('Lukiokoulutus 2019: kurssin tietojen muokkausmodaali', () => {
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
  })

  test('Kurssin suorituskieli ja pakollisuus voidaan muokata ja tallentaa', async ({
    page
  }) => {
    const ai1 = katselu.kurssi(katselu.oppiaineRivi('AI'), 'ÄI1')

    await (await katselu.kurssinPakollinenCheckbox(ai1)).uncheck()
    await katselu.muokkaaKurssinSuorituskieltä(ai1, 'ruotsi')

    await katselu.tallenna()
    await katselu.aloitaMuokkaus()

    const ai1Again = katselu.kurssi(katselu.oppiaineRivi('AI'), 'ÄI1')
    await expect(
      await katselu.kurssinPakollinenCheckbox(ai1Again)
    ).not.toBeChecked()
    await expect(
      page
        .locator('[role="dialog"]')
        .locator('[data-testid$="suorituskieli.edit.input"]')
    ).toHaveValue('ruotsi')
  })

  test('Kurssin laajuus, arviointipäivä ja kuvaus voidaan muokata ja tallentaa', async () => {
    const ai1 = katselu.kurssi(katselu.oppiaineRivi('AI'), 'ÄI1')
    const modaali = await katselu.avaaKurssinMuokkausModaali(ai1)
    await modaali.locator('.LaajuusField input').fill('5')
    await modaali
      .locator('[data-testid$="arviointipaiva.edit.edit.input"]')
      .fill('1.9.2020')
    await modaali.getByRole('button', { name: 'Sulje' }).click()

    const paiRivi = await katselu.lisääPaikallinenOppiaine({
      koodi: 'PAI',
      nimi: 'Paikallinen oppiaine',
      kuvaus: 'Oppiaineen kuvaus',
      arvosana: '9'
    })
    const paKurssi = await katselu.lisääPaikallinenKurssi(paiRivi, {
      tunniste: 'PA',
      nimi: 'Paikallinen kurssi',
      kuvaus: 'Kurssin alkuperäinen kuvaus',
      laajuus: '2'
    })
    await katselu.muokkaaKurssinArvosanaa(paKurssi, '9')
    const kuvausModaali = await katselu.avaaKurssinMuokkausModaali(paKurssi)
    await kuvausModaali
      .locator('[data-testid$="kuvaus.edit.input"]')
      .fill('Kurssin muokattu kuvaus')
    await kuvausModaali.getByRole('button', { name: 'Sulje' }).click()

    await katselu.tallenna()
    await katselu.aloitaMuokkaus()

    const ai1Again = katselu.kurssi(katselu.oppiaineRivi('AI'), 'ÄI1')
    const modaaliAgain = await katselu.avaaKurssinMuokkausModaali(ai1Again)
    await expect(modaaliAgain.locator('.LaajuusField input')).toHaveValue('5')
    await expect(
      modaaliAgain.locator('[data-testid$="arviointipaiva.edit.edit.input"]')
    ).toHaveValue('1.9.2020')
    await modaaliAgain.getByRole('button', { name: 'Sulje' }).click()

    const paKurssiAgain = katselu.kurssi(katselu.oppiaineRivi('PAI'), 'PA')
    const kuvausModaaliAgain =
      await katselu.avaaKurssinMuokkausModaali(paKurssiAgain)
    await expect(
      kuvausModaaliAgain.locator('[data-testid$="kuvaus.edit.input"]')
    ).toHaveValue('Kurssin muokattu kuvaus')
  })
})

// Vanhassa käyttöliittymässä (onKeskeneräisiäOsasuorituksia) "Merkitse valmiiksi"
// on estetty, jos suorituksessa on arvioimattomia osasuorituksia millä tahansa
// tasolla - sekä oppiaineen että sen kurssien/opintojaksojen tulee olla arvioitu.
// Uudessa käyttöliittymässä sama este toteutuu tallennusvaiheen validointina
// (lukio-v2/validointi.ts), ei painikkeen poistamisena käytöstä - ei vanhan
// käyttöliittymän omaa testiä olemassa, josta portata.
test.describe('Lukiokoulutus 2019: kurssin arviointi vaaditaan valmiilta suoritukselta', () => {
  test.use({ storageState: virkailija('pää') })

  // Uusilukio, Ulla (250605A518Y)
  const oid = '1.2.246.562.24.00000000015'
  let katselu: UusiLukioKatselu

  test.beforeEach(async ({ fixtures, page }) => {
    await fixtures.reset()
    katselu = new UusiLukioKatselu(page)
    await katselu.goto(oid)
    await katselu.aloitaMuokkaus()
  })

  test('Arvioimaton kurssi estää tallennuksen valmiiksi merkityllä suorituksella, arvosanan lisääminen sallii tallennuksen', async () => {
    const aom = katselu.oppiaineRivi('AOM')
    const rua2 = await katselu.lisääKurssi(aom, 'RUA2', '2')

    await expect(katselu.tallennaBtn).toBeDisabled()

    await katselu.muokkaaKurssinArvosanaa(rua2, '9')

    await expect(katselu.tallennaBtn).toBeEnabled()
  })
})
