import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { luoLukio } from './fragments/LukioLuonti'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Lukio-v2:n seitsemäs muokkausominaisuus (arvosanan, kieliaineen/
 * suorituskielen, oppiaineen ja moduulin lisäyksen/poiston, päätason
 * suorituksen poiston ja paikallisen oppiaineen jälkeen): erityinen tutkinto,
 * sekä oppiaine- että suoritustasolla - ks. isErityinenTutkinto/
 * oppiaineenArvioituLaajuus LukionOppiaineet.tsx:ssä. Ensimmäinen testi
 * ("alaviite ja laajuus näytetään oikein") on puhdas katselutesti, joka
 * paljasti todellisen, aiemmin huomaamatta jääneen katseluvirheen uudesta
 * käyttöliittymästä: erityisenä tutkintona suoritetulla oppiaineella ei
 * yleensä ole yhtään kurssia, jolloin laajuus piti lukea
 * koulutusmoduuli.laajuudesta kurssien summan sijaan.
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
  test.describe(`Lukiokoulutus 2019: erityinen tutkinto, ${variantti.nimi} käyttöliittymä`, () => {
    test.use({ storageState: virkailija('pää') })

    // Uusilukionaineopiskelija, Urho (010705A6119) - "lukion aineopinnot",
    // eri testioppija kuin muissa lukio-v2-speceissä.
    const oid = '1.2.246.562.24.00000000016'

    test.beforeEach(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test('Erityisenä tutkintona suoritetun oppiaineen alaviite ja laajuus näytetään oikein', async ({
      page
    }) => {
      const katselu = variantti.luoKatselu(page, 'lukion aineopinnot')
      await katselu.goto(oid)

      const bi = katselu.oppiaineRivi('BI')
      await expect(katselu.oppiaineenFootnoteHint(bi)).toHaveText('**')
      await expect(katselu.oppiaineenLaajuusArvioitu(bi)).toHaveText('6')
      await expect(
        katselu.oppiaineenLaajuusHyväksytystiArvioitu(bi)
      ).toHaveText('6')

      const ai = katselu.oppiaineRivi('ÄI')
      await expect(katselu.oppiaineenFootnoteHint(ai)).toHaveCount(0)

      await expect(katselu.erityinenTutkintoLegend).toBeVisible()
    })

    test('Erityinen tutkinto voidaan merkitä oppiaineen tasolla', async ({
      page
    }) => {
      const katselu = variantti.luoKatselu(page, 'lukion aineopinnot')
      await katselu.goto(oid)
      await katselu.aloitaMuokkaus()

      await katselu.lisääOppiaine('Psykologia')
      const ps = katselu.oppiaineRivi('PS')
      await katselu.erityinenTutkintoCheckbox(ps).check()
      await katselu.laajuusInput(ps).fill('6')
      await katselu.muokkaaOppiaineenArvosanaa(ps, '9')

      await katselu.tallenna()

      const psAgain = katselu.oppiaineRivi('PS')
      await expect(katselu.oppiaineenFootnoteHint(psAgain)).toHaveText('**')
      await expect(katselu.oppiaineenLaajuusArvioitu(psAgain)).toHaveText('6')
      await expect(
        katselu.oppiaineenLaajuusHyväksytystiArvioitu(psAgain)
      ).toHaveText('6')

      // Lipun asettaminen yhdelle oppiaineelle ei vaikuta muihin.
      const ai = katselu.oppiaineRivi('ÄI')
      await expect(katselu.oppiaineenFootnoteHint(ai)).toHaveCount(0)
    })

    test.describe('suoritustason lippu', () => {
      test.use({ storageState: virkailija('kalle') })

      test('Erityinen tutkinto voidaan merkitä suorituksen tasolla', async ({
        uusiOppijaPage,
        page
      }) => {
        // Suoritustason lippu vaikuttaa kaikkiin oppiaineisiin (ks.
        // LukioOppiaineet.tsx:n isErityinenTutkinto) - käytetään siksi juuri
        // luotua, tyhjää aikuisten oppimäärää, ei valmiiksi täytettyä
        // kiinteää fixtuuria. luoLukio käyttää vanhaa "Uusi opiskelija"
        // -lomaketta UI-variantista riippumatta ja laskeutuu vanhalla
        // käyttöliittymällä - navigoidaan sen jälkeen uudelleen oikealla
        // katselu.goto():lla, joka lisää tarvittaessa lukio-v2-lipun.
        await luoLukio(uusiOppijaPage, {
          hetu: '130307A208A',
          oppimäärä: 'Lukion oppimäärä',
          peruste:
            'OPH-2267-2019 Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019'
        })
        const luotuOid = page.url().match(/1\.2\.246\.562\.24\.\d+/)?.[0]
        if (!luotuOid) throw new Error('Luodun opiskeluoikeuden oid puuttuu')

        const katselu = variantti.luoKatselu(page, 'Lukion oppimäärä')
        await katselu.goto(luotuOid)
        await katselu.aloitaMuokkaus()
        await katselu.erityinenTutkintoCheckbox().check()

        await katselu.lisääOppiaine('Biologia')
        const bi = katselu.oppiaineRivi('BI')
        await katselu.laajuusInput(bi).fill('12')
        await katselu.muokkaaOppiaineenArvosanaa(bi, '9')

        await katselu.tallenna()

        const biAgain = katselu.oppiaineRivi('BI')
        await expect(katselu.oppiaineenFootnoteHint(biAgain)).toHaveText('**')
        await expect(katselu.oppiaineenLaajuusArvioitu(biAgain)).toHaveText(
          '12'
        )
        await expect(
          katselu.oppiaineenLaajuusHyväksytystiArvioitu(biAgain)
        ).toHaveText('12')
      })
    })
  })
}
