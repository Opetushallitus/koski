import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Ajetaan jokainen testi sekä vanhaa (mallipohjainen editori) että uutta
 * (lukio-v2/, feature flag "lukio-v2") käyttöliittymää vasten samoilla
 * assertioilla - ks. pages/oppija/components/LukioKatselu.ts. Uusi
 * käyttöliittymä toteuttaa toistaiseksi vain katselun, joten vain
 * näyttämiseen liittyvät skenaariot on retrofitattu tähän (muokkaus/luonti/
 * poisto pysyvät omissa, vain vanhaa käyttöliittymää ajavissa
 * spec-tiedostoissaan).
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
  test.describe(`Lukiokoulutus 2019: ${variantti.nimi} käyttöliittymä`, () => {
    test.use({ storageState: virkailija('pää') })

    test.beforeEach(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test.describe('Lukion päättötodistus', () => {
      // Uusilukio, Ulla (250605A518Y)
      const oid = '1.2.246.562.24.00000000015'
      let katselu: LukioKatselu

      test.beforeEach(async ({ page }) => {
        katselu = variantti.luoKatselu(page, 'Lukion oppimäärä')
        await katselu.goto(oid)
      })

      test('Tutkinto ja oppilaitos näytetään oppijan suorituksissa', async () => {
        await expect(katselu.koulutusNimi).toHaveText('Lukion oppimäärä')
        await expect(katselu.koulutusKoodi).toHaveText('309902')
        await expect(katselu.toimipiste).toHaveText('Jyväskylän normaalikoulu')
      })

      test('Opiskeluoikeuden tiedot näytetään', async () => {
        await expect(katselu.opiskeluoikeudenTiedot).toContainText(
          'Opiskeluoikeuden voimassaoloaika: 1.8.2019 — 5.9.2021'
        )

        await katselu.odotaTilaHistoria([
          { päivä: '5.9.2021', tila: 'Valmistunut' },
          { päivä: '1.8.2019', tila: 'Läsnä' }
        ])

        await expect(katselu.oppimääräSuoritettu).toHaveText('kyllä')
      })

      test('Suorituksen tiedot näytetään', async () => {
        await expect(katselu.oppimäärä).toHaveText(
          'Lukio suoritetaan nuorten opetussuunnitelman mukaan'
        )
        await expect(katselu.suorituskieli).toHaveText('suomi')
        await expect(katselu.ryhmä).toHaveText('AH')
        await expect(katselu.koulusivistyskieli).toHaveText('suomi')

        const taydentavat = katselu.omanÄidinkielenOpinnot()
        await expect(taydentavat.arvosana).toHaveText('8')
        await expect(taydentavat.arviointipäivä).toHaveText('4.9.2021')
        await expect(taydentavat.kieli).toHaveText('saame, lappi')
        await expect(taydentavat.laajuus).toHaveText('3 op')

        const vahvistus = katselu.vahvistus()
        await expect(vahvistus.tila).toHaveText('Suoritus valmis')
        await expect(vahvistus.päivä).toContainText('15.5.2020')
        await expect(vahvistus.paikka).toContainText('Jyväskylä')
        await expect(vahvistus.henkilöTeksti).toContainText('Reijo Reksi')
        await expect(vahvistus.henkilöTeksti).toContainText('rehtori')
      })

      test('Oppiaineiden ja kurssien arvosanat näytetään', async () => {
        const ai = katselu.oppiaineRivi('AI')

        await expect(
          katselu.kurssinArvosana(katselu.kurssi(ai, 'ÄI1'))
        ).toHaveText('8')
        await expect(
          katselu.kurssinArvosana(katselu.kurssi(ai, 'ÄI2'))
        ).toHaveText('8')
        await expect(
          katselu.kurssinArvosana(katselu.kurssi(ai, 'ÄI3'))
        ).toHaveText('8')

        await expect(katselu.oppiaineenLaajuusArvioitu(ai)).toHaveText('6')
        await expect(
          katselu.oppiaineenLaajuusHyväksytystiArvioitu(ai)
        ).toHaveText('6')
        await expect(katselu.oppiaineenArvosana(ai)).toHaveText('9')
        await expect(katselu.oppiaineenTilaTitle(ai)).toHaveAttribute(
          'title',
          'Suoritus valmis'
        )
      })
    })

    test.describe('Lukion oppiaineiden oppimäärien suoritus', () => {
      test.describe('Laajuuksien erottelu', () => {
        // Vajaa-Suoritus, Lukiolainen (010105A1995)
        const oid = '1.2.246.562.24.00000000161'
        let katselu: LukioKatselu

        test.beforeEach(async ({ page }) => {
          katselu = variantti.luoKatselu(page, 'lukion aineopinnot')
          await katselu.goto(oid)
        })

        test('Arvioitu ja hyväksytysti arvioitu laajuus näytetään erikseen oppiaineelle', async () => {
          await expect(katselu.oppiaineetOtsikkoArvioitu).toHaveText(
            'Arvioitu (opintopistettä)'
          )
          await expect(
            katselu.oppiaineetOtsikkoHyväksytystiArvioitu
          ).toHaveText('Hyväksytysti arvioitu (opintopistettä)')

          const fy = katselu.oppiaineRivi('FY')
          await expect(katselu.oppiaineenLaajuusArvioitu(fy)).toHaveText('6')
          await expect(
            katselu.oppiaineenLaajuusHyväksytystiArvioitu(fy)
          ).toHaveText('4')
        })
      })

      test.describe('Perustiedot', () => {
        // Uusilukionaineopiskelija, Urho (010705A6119)
        const oid = '1.2.246.562.24.00000000016'
        let katselu: LukioKatselu

        test.beforeEach(async ({ page }) => {
          katselu = variantti.luoKatselu(page, 'lukion aineopinnot')
          await katselu.goto(oid)
        })

        test('Tutkinto ja oppilaitos näytetään, suoritustyyppi näkyy otsikossa', async () => {
          await expect(katselu.koulutusNimi).toHaveText('Lukion aineopinnot')
          await expect(katselu.toimipiste).toHaveText('Jyväskylän normaalikoulu')
          await expect(katselu.suoritustyyppiOtsikko).toHaveText(
            'Lukion aineopinnot'
          )
        })

        test('Opiskeluoikeuden tiedot näytetään', async () => {
          // Toisin kuin "Lukion oppimäärä" -suoritustyypillä, tällä ei ole
          // oppimääräSuoritettu-kenttää lainkaan.
          await expect(katselu.oppimääräSuoritettu).toHaveCount(0)

          await katselu.odotaTilaHistoria([
            { päivä: '5.9.2021', tila: 'Valmistunut' },
            { päivä: '1.8.2019', tila: 'Läsnä' }
          ])
        })
      })
    })
  })
}
