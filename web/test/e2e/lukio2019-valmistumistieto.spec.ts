import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { formatFinnishDate } from '../../app/date/date'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Portattu vanhan käyttöliittymän lukio2019-oppimaaran-suoritus.spec.ts:n
 * testistä "Valmistumistieto voidaan lisätä, vaikka suoritusta ei ole
 * vahvistettu" - jäi aiemmin kokonaan porttaamatta. Paljasti todellisen,
 * aiemmin huomaamatta jääneen eron: EditorContainerin oletusarvo
 * (suorituksetVahvistettu = kaikilla suorituksilla on vahvistus) esti
 * "Valmistunut"-tilan valitsemisen uudessa käyttöliittymässä, vaikka vanha
 * käyttöliittymä on aina sallinut sen riippumatta vahvistuksesta - korjattu
 * LukioEditor.tsx:ssä antamalla suorituksetVahvistettu={true} vakiona, kuten
 * vanha käyttöliittymä käyttäytyy.
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
  test.describe(`Lukiokoulutus 2019: valmistumistieto, ${variantti.nimi} käyttöliittymä`, () => {
    test.use({ storageState: virkailija('pää') })

    // Uusilukionaineopiskelija, Urho (010705A6119) - "lukion aineopinnot",
    // jonka ainoan suorituksen vahvistus puuttuu, ja jonka olemassa oleva
    // "Valmistunut"-tila (2021) poistetaan tässä testissä, ennen kuin uusi
    // lisätään.
    const oid = '1.2.246.562.24.00000000016'

    test('Valmistumistieto voidaan lisätä, vaikka suoritusta ei ole vahvistettu', async ({
      fixtures,
      page
    }) => {
      await fixtures.reset()
      const katselu = variantti.luoKatselu(page, 'lukion aineopinnot')
      await katselu.goto(oid)
      await katselu.aloitaMuokkaus()
      await katselu.poistaViimeisinTila()

      // Luettu ennen dialogin avaamista, jotta päivämäärä ei voi ehtiä
      // vaihtua keskiyön rajalla tämän ja dialogin oman "tänään"-oletusarvon
      // välillä.
      const tänään = formatFinnishDate(new Date())!

      await katselu.lisääTila('Valmistunut')
      await katselu.tallenna()

      await expect(katselu.opiskeluoikeudenTiedot).toContainText(tänään)
    })
  })
}
