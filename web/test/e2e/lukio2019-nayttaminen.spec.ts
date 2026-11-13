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

/**
 * Sama saavutettavuustarkistus kuin esim. ammatillinen-v2.spec.ts:ssä ja
 * ib.spec.ts:ssä (makeAxeBuilder, ks. base.ts) - ei vielä ollut lukio-v2:lle.
 * Uutta käyttöliittymää vasten vain, koska vanha skeemareflektiivinen editori
 * on oma, laajempi korjauskohteensa eikä osa tätä tarkistusta. Sekä katselu-
 * että muokkaustila: kriittinen "label"-virhe (TextEdit/LocalizedTextEdit-
 * kentiltä puuttui ohjelmallinen nimi) löytyi vain muokkaustilasta.
 */
test.describe('Lukiokoulutus 2019: saavutettavuus, uusi käyttöliittymä', () => {
  test.use({ storageState: virkailija('pää') })

  // Uusilukio, Ulla (250605A518Y)
  const oid = '1.2.246.562.24.00000000015'

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Sivulla ei saavutettavuusvirheitä, katselutila', async ({
    page,
    makeAxeBuilder
  }) => {
    const katselu = new UusiLukioKatselu(page)
    await katselu.goto(oid)

    // .oppija-haku: virkailijan sivunlaajuinen "Hae tai lisää opiskelija"
    // -haku, ei liity lukio-v2:een - sama elementti kaikilla virkailijan
    // sivuilla, ei tähän asti ollut minkään axe-testin piirissä.
    const accessibilityScanResults = await makeAxeBuilder()
      .exclude('.oppija-haku')
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })

  test('Sivulla ei saavutettavuusvirheitä, muokkaustila', async ({
    page,
    makeAxeBuilder
  }) => {
    const katselu = new UusiLukioKatselu(page)
    await katselu.goto(oid)
    await katselu.aloitaMuokkaus()

    // Kurssin tietojen muokkausmodaali ei ole auki oletuksena - avataan se
    // tässä, jotta sen oma sisältö (uusi <button>-tunniste, uusi Modal)
    // tulee myös skannatuksi, ei vain suljetun tilan sivu.
    const ai1 = katselu.kurssi(katselu.oppiaineRivi('AI'), 'ÄI1')
    await katselu.avaaKurssinMuokkausModaali(ai1)

    // .oppija-haku: virkailijan sivunlaajuinen "Hae tai lisää opiskelija"
    // -haku, ei liity lukio-v2:een - sama elementti kaikilla virkailijan
    // sivuilla, ei tähän asti ollut minkään axe-testin piirissä.
    const accessibilityScanResults = await makeAxeBuilder()
      .exclude('.oppija-haku')
      .analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})

/**
 * Vanhan käyttöliittymän kurssi/KurssiEditor.jsx näyttää kurssin lisätiedot
 * (Nimi/Laajuus/Pakollinen/Arvosana/Arviointipäivä) hiirellä osoittamalla
 * avautuvassa popupissa katselutilassa - lukio-v2:lta puuttui tämä
 * kokonaan (laajuus ei näkynyt ollenkaan, eikä mitään saanut auki
 * klikkaamatta/osoittamatta). Uusi, ei portattu ominaisuus samassa
 * mielessä kuin osaamisen tunnustaminen - ei VARIANTIT-rinnakkaisajoa,
 * koska testi kohdistuu nimenomaan uuden popupin sisältöön.
 */
test.describe('Lukiokoulutus 2019: kurssin tietopopup katselutilassa', () => {
  test.use({ storageState: virkailija('pää') })

  // Uusilukio, Ulla (250605A518Y)
  const oid = '1.2.246.562.24.00000000015'

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Kurssin osoittaminen hiirellä näyttää Nimi/Laajuus/Pakollinen/Arvosana/Arviointipäivä', async ({
    page
  }) => {
    const katselu = new UusiLukioKatselu(page)
    await katselu.goto(oid)

    const ai = katselu.oppiaineRivi('AI')
    const ai1 = katselu.kurssi(ai, 'ÄI1')
    await ai1.locator('.tunniste').hover()

    const popup = page.locator('.PositionalPopup--open')
    await expect(popup.locator('[data-testid$="popup.nimi"]')).toHaveText(
      'Tekstien tulkinta ja kirjoittaminen'
    )
    // LaajuusView kiinnittää oman testitunnisteensa aina "laajuus.value":ksi
    // riippumatta annetusta testId-propista - olemassa oleva, muuallakin
    // tunnettu oikku jaetussa komponentissa, ei korjattu tämän muutoksen
    // yhteydessä.
    await expect(popup.locator('[data-testid$="laajuus.value"]')).toHaveText(
      '2 op'
    )
    await expect(
      popup.locator('[data-testid$="popup.pakollinen"]')
    ).toHaveText('Kyllä')
    await expect(popup.locator('[data-testid$="popup.arvosana"]')).toHaveText(
      '8'
    )
    await expect(
      popup.locator('[data-testid$="popup.arviointipaiva"]')
    ).toHaveText('4.9.2021')

    // Hiiren siirtäminen pois sulkee popupin.
    await page.mouse.move(0, 0)
    await expect(popup).not.toBeVisible()
  })

  test('Muokkaustilassa kurssin osoittaminen ei avaa popupia', async ({
    page
  }) => {
    const katselu = new UusiLukioKatselu(page)
    await katselu.goto(oid)
    await katselu.aloitaMuokkaus()

    const ai = katselu.oppiaineRivi('AI')
    const ai1 = katselu.kurssi(ai, 'ÄI1')
    await ai1.locator('.tunniste').hover()

    await expect(page.locator('.PositionalPopup--open')).toHaveCount(0)
  })

  // Suorituskieli ja Kuvaus näkyvät popupissa vain kun ne on asetettu
  // (ks. KurssiInfoPopupin oma ehdollinen renderöinti) - edellinen testi ei
  // kata kumpaakaan, koska ÄI1:llä ei ole kumpaakaan oletuksena. Kuvaus
  // näkyy lisäksi vain paikalliselle opintojaksolle.
  test('Suorituskieli ja Kuvaus näkyvät popupissa, kun ne on asetettu', async ({
    page
  }) => {
    const katselu = new UusiLukioKatselu(page)
    await katselu.goto(oid)
    await katselu.aloitaMuokkaus()
    await katselu.poistaViimeisinTila()

    const ai1 = katselu.kurssi(katselu.oppiaineRivi('AI'), 'ÄI1')
    await katselu.muokkaaKurssinSuorituskieltä(ai1, 'ruotsi')

    await katselu.lisääPaikallinenOppiaine({
      koodi: 'PAI',
      nimi: 'Paikallinen oppiaine',
      kuvaus: 'Oppiaineen kuvaus',
      arvosana: '9'
    })
    await katselu.tallenna()

    await katselu.aloitaMuokkaus()
    const paiRivi = katselu.oppiaineRivi('PAI')
    const paikallinenKurssi = await katselu.lisääPaikallinenKurssi(paiRivi, {
      tunniste: 'PA',
      nimi: 'Paikallinen kurssi',
      kuvaus: 'Kurssin kuvaus',
      laajuus: '2'
    })
    await katselu.muokkaaKurssinArvosanaa(paikallinenKurssi, '9')
    await katselu.tallenna()

    const ai1Again = katselu.kurssi(katselu.oppiaineRivi('AI'), 'ÄI1')
    await ai1Again.locator('.tunniste').hover()
    const suorituskieliPopup = page.locator('.PositionalPopup--open')
    await expect(
      suorituskieliPopup.locator('[data-testid$="popup.suorituskieli"]')
    ).toHaveText('ruotsi')
    await page.mouse.move(0, 0)
    await expect(suorituskieliPopup).not.toBeVisible()

    const paKurssi = katselu.kurssi(katselu.oppiaineRivi('PAI'), 'PA')
    await paKurssi.locator('.tunniste').hover()
    const kuvausPopup = page.locator('.PositionalPopup--open')
    await expect(
      kuvausPopup.locator('[data-testid$="popup.kuvaus"]')
    ).toHaveText('Kurssin kuvaus')
  })
})
