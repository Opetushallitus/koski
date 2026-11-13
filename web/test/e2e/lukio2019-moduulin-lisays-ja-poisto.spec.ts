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

/**
 * Kurssin tietojen muokkausmodaali (klikkaus kurssin tunnisteeseen
 * muokkaustilassa) - Suorituskieli, Pakollinen, Tunnustettu, Laajuus,
 * Arvosana, Arviointipäivä, Kuvaus (vain paikallinen opintojakso). Kaikki
 * olemassa vanhassa käyttöliittymässä geneerisesti (KurssiPopup.jsx:n
 * PropertiesEditor näyttää kaikki kurssin skeemakentät paitsi
 * arviointi/koodistoUri/tunniste/koodiarvo/nimi), mutta koskaan
 * testaamatta kummassakaan käyttöliittymässä. Siksi uusi, ei portattu
 * ominaisuus - ei VARIANTIT-rinnakkaisajoa, sama periaate kuin
 * lukio2019-moduulin-osaamisen-tunnustaminen.spec.ts:ssä. Arvosana on
 * tietoisesti eri tavalla kuin vanhassa käyttöliittymässä, jossa se pysyy
 * aina rivin omana kenttänään (div.arvosana) eikä ole osa popupia - uudessa
 * käyttöliittymässä se on muokattavissa tästä modaalista, rivillä näkyy
 * siitä silti aina pelkkä katselu (ks. KurssinTietojenMuokkausModaalin
 * kommentti).
 */
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

  // Laajuus ja Arviointipäivä - samasta modaalista löytyvät, aiemmin
  // tavoittamattomat kentät (ei muokattavissa minkään olemassa olevan
  // kurssin osalta ennen tätä, vain lisäyshetkellä/arvosanaa asetettaessa).
  // Kuvaus koskee vain paikallista opintojaksoa, siksi oma paikallinen
  // oppiaine+kurssi tähän testiin - valtakunnallisella moduulilla ei ole
  // kuvaus-kenttää lainkaan.
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
