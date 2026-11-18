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
