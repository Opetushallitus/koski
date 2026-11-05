import { Page } from '@playwright/test'
import { expect, test } from './base'
import { luoLukio } from './fragments/LukioLuonti'
import { virkailija } from './setup/auth'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Lukion 2019 opiskeluoikeuden luonti. Luontidialogi (KoskiUusiOppijaPage,
 * /koski/uusioppija) on sama molemmille käyttöliittymille - ei lukio-
 * spesifinen, ei edes koulutusmuotokohtainen - ja luonnin jälkeen päädytään
 * aina /koski/oppija/<oid>:iin ilman lukio-v2-lippua. Neljä ensimmäistä
 * testiä navigoidaan siksi uudelleen luodun oid:n kanssa
 * katselu.goto(oid, ...):lla, sama temppu kuin
 * lukio2019-erityinen-tutkinto.spec.ts:n suoritustason lippu -testissä -
 * tuore, tyhjä suoritus renderöityy jo nyt oikein lukio-v2:lla ilman uutta
 * sovelluskoodia.
 *
 * Viides testi ("peruste-valikko ei näytä 2019-OPS vaihtoehtoja") ei koskaan
 * lähetä lomaketta eikä avaa syntyvää suoritusta - se jää oman, ei-
 * variantoidun testinsä varaan tiedoston loppuun, koulutusmuoto-/UI-
 * riippumattomana regressiotestinä luontilomakkeelle itselleen.
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

const luoJaAvaa = async (
  page: Page,
  variantti: (typeof VARIANTIT)[number],
  otsikko: string | RegExp,
  tiedot: { hetu: string; oppimäärä: string; peruste: string }
): Promise<LukioKatselu> => {
  const uusiOppijaPage = new KoskiUusiOppijaPage(page)
  await luoLukio(uusiOppijaPage, tiedot)

  const oid = page.url().match(/1\.2\.246\.562\.24\.\d+/)?.[0]
  if (!oid) throw new Error('Luodun opiskeluoikeuden oid puuttuu')

  const katselu = variantti.luoKatselu(page, otsikko)
  await katselu.goto(oid)
  return katselu
}

for (const variantti of VARIANTIT) {
  test.describe(`Lukiokoulutus 2019: opiskeluoikeuden luonti, ${variantti.nimi} käyttöliittymä`, () => {
    test.use({ storageState: virkailija('kalle') })

    test.beforeEach(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test('Nuorten lukion oppimäärä voidaan luoda, oppiaineita ei esitäytetä', async ({
      page
    }) => {
      const katselu = await luoJaAvaa(page, variantti, 'Lukion oppimäärä', {
        hetu: '140981-334Y',
        oppimäärä: 'Lukion oppimäärä',
        peruste: 'OPH-2263-2019 Lukion opetussuunnitelman perusteet 2019'
      })

      await expect(katselu.koulutusNimi).toHaveText('Lukion oppimäärä')
      await expect(katselu.oppimäärä).toHaveText(
        'Lukio suoritetaan nuorten opetussuunnitelman mukaan'
      )
      await expect(katselu.toimipiste).toHaveText('Jyväskylän normaalikoulu')
      await expect(katselu.suorituskieli).toHaveText('suomi')

      await expect(katselu.oppiaineRivit).toHaveCount(0)
    })

    test('Aikuisten lukion oppimäärä voidaan luoda', async ({ page }) => {
      const katselu = await luoJaAvaa(page, variantti, 'Lukion oppimäärä', {
        hetu: '170491-517B',
        oppimäärä: 'Lukion oppimäärä',
        peruste:
          'OPH-2267-2019 Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019'
      })

      await expect(katselu.koulutusNimi).toHaveText('Lukion oppimäärä')
      await expect(katselu.oppimäärä).toHaveText(
        'Lukio suoritetaan aikuisten opetussuunnitelman mukaan'
      )
      await expect(katselu.toimipiste).toHaveText('Jyväskylän normaalikoulu')
      await expect(katselu.suorituskieli).toHaveText('suomi')
    })

    test('Nuorten lukion aineopinnot voidaan luoda', async ({ page }) => {
      const katselu = await luoJaAvaa(page, variantti, 'lukion aineopinnot', {
        hetu: '260613-0652',
        oppimäärä: 'Lukion aineopinnot',
        peruste: 'OPH-2263-2019 Lukion opetussuunnitelman perusteet 2019'
      })

      await expect(katselu.koulutusNimi).toHaveText('Lukion aineopinnot')
      await expect(katselu.toimipiste).toHaveText('Jyväskylän normaalikoulu')
      await expect(katselu.suorituskieli).toHaveText('suomi')
    })

    test('Aikuisten lukion aineopinnot voidaan luoda', async ({ page }) => {
      const katselu = await luoJaAvaa(page, variantti, 'lukion aineopinnot', {
        hetu: '051009-409T',
        oppimäärä: 'Lukion aineopinnot',
        peruste:
          'OPH-2267-2019 Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019'
      })

      await expect(katselu.koulutusNimi).toHaveText('Lukion aineopinnot')
      await expect(katselu.toimipiste).toHaveText('Jyväskylän normaalikoulu')
      await expect(katselu.suorituskieli).toHaveText('suomi')
    })
  })
}

// Regressiotesti: "Lukion oppiaineen oppimäärä" on 2015-OPS:n yksittäisen
// oppiaineen suoritus, eri oppimäärä kuin "Lukion oppimäärä"/"Lukion
// aineopinnot" - sen peruste-valikon ei pidä tarjota 2019-OPS:n
// diaarinumeroita, vaikka ne näkyvät muiden lukio-oppimäärien valikoissa.
// Ei variantoitu: koskee vain yhteistä luontilomaketta, ei kummankaan
// käyttöliittymän suoritusnäkymää.
test.describe('Lukiokoulutus 2019: opiskeluoikeuden luonti', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Lukion oppiaineen oppimäärän peruste-valikko ei näytä 2019-OPS vaihtoehtoja', async ({
    uusiOppijaPage
  }) => {
    await uusiOppijaPage.goTo('230872-7258')
    await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
    await uusiOppijaPage.fill({ oppilaitos: 'Jyväskylän normaalikoulu' })
    await uusiOppijaPage.controls.opiskeluoikeus.setByLabel('Lukiokoulutus')
    await uusiOppijaPage.controls.oppimäärä.setByLabel(
      'Lukion oppiaineen oppimäärä'
    )

    const perusteet = await uusiOppijaPage.controls.peruste.options()
    expect(perusteet).not.toContain(
      'OPH-2267-2019 Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019'
    )
    expect(perusteet).not.toContain(
      'OPH-2263-2019 Lukion opetussuunnitelman perusteet 2019'
    )
  })
})
