import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Portattu vanhan käyttöliittymän lukio2019-oppiaineen-muokkaus.spec.ts:n
 * testistä "Alkutilassa ei ole tallentamattomia muutoksia" - tämä jäi
 * aiemmin porttaamatta uutta käyttöliittymää vastaan ("todennäköisesti
 * katettu implisiittisesti", ei koskaan varmistettu).
 *
 * Tällä fixtuurilla on 12 osasuoritusta "Lukion oppimäärä" -suorituksen
 * alla - 9 oppiainetta (LukionOppiaineenSuoritus2019) ja 3 lukiodiplomit/
 * muut suoritukset/teemaopinnot -riviä (MuidenLukioOpintojenSuoritus2019,
 * eri sisarluokka). Molemmat rivityypit renderöityvät nyt (LukionOppiaineet.tsx),
 * joten molemmat käyttöliittymät näyttävät kaikki 12.
 */
const VARIANTIT: Array<{
  nimi: string
  luoKatselu: (page: Page, otsikko: string | RegExp) => LukioKatselu
  oppiaineidenMäärä: number
}> = [
  {
    nimi: 'vanha',
    luoKatselu: (page, otsikko) => new VanhaLukioKatselu(page, otsikko),
    oppiaineidenMäärä: 12
  },
  {
    nimi: 'uusi',
    luoKatselu: (page) => new UusiLukioKatselu(page),
    oppiaineidenMäärä: 12
  }
]

for (const variantti of VARIANTIT) {
  test.describe(`Lukiokoulutus 2019: alkutila, ${variantti.nimi} käyttöliittymä`, () => {
    test.use({ storageState: virkailija('pää') })

    // Uusilukio, Ulla (250605A518Y)
    const oid = '1.2.246.562.24.00000000015'

    test('Alkutilassa ei ole tallentamattomia muutoksia', async ({
      fixtures,
      page
    }) => {
      await fixtures.reset()
      const katselu = variantti.luoKatselu(page, 'Lukion oppimäärä')
      await katselu.goto(oid)
      await katselu.aloitaMuokkaus()

      await expect(katselu.editBarMessage).toHaveText(
        'Ei tallentamattomia muutoksia'
      )
      await expect(katselu.tallennaBtn).toBeDisabled()
      await expect(katselu.oppiaineRivit).toHaveCount(
        variantti.oppiaineidenMäärä
      )
    })
  })
}
