import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  LukioKatselu,
  UusiLukioKatselu,
  VanhaLukioKatselu
} from './pages/oppija/components/LukioKatselu'

/**
 * Lukio-v2:n viides muokkausominaisuus (arvosanan, kieliaineen/
 * suorituskielen, oppiaineen ja moduulin lisäyksen/poiston jälkeen):
 * päätason suorituksen poisto (mitätöinti) - ks. RemovePaatasonSuoritus
 * (yhteinen, uudelleenkäytetty komponentti) ja LukioEditor.tsx:n
 * removePäätasonSuoritus. Vanhan käyttöliittymän lukio2019-poisto.spec.ts
 * sisältää vain yhden testin - negatiivisen tapauksen, jossa linkkiä ei
 * näytetä yhden suorituksen opiskeluoikeudelle. Positiivista poistopolkua ei
 * voi portata, koska mikään testioppija ei omaa 2+ lukio-suoritusta, eikä
 * sellaista voi luoda testin aikana - backend hylkää aina toisen päätason
 * suorituksen lisäyksen lukio-v2:n tukemille suoritustyypeille (Lukion
 * oppimäärä sallii vain yhden suorituksen ylipäätään, Lukion aineopinnot
 * sallii vain yhden kappaleen), vahvistettu rakentamalla ja ajamalla lisäys-
 * ominaisuus livenä ja havaitsemalla backendin hylkäys ennen sen poistamista
 * - ei siis puuttuva ominaisuus vaan mahdoton tälle skoopille.
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
  test.describe(`Lukiokoulutus 2019: päätason suorituksen poistaminen, ${variantti.nimi} käyttöliittymä`, () => {
    test.use({ storageState: virkailija('pää') })

    // Uusilukio, Ulla (250605A518Y)
    const oid = '1.2.246.562.24.00000000015'

    test('Mitätöintilinkkiä ei näytetä, kun opiskeluoikeudella on vain yksi suoritus', async ({
      fixtures,
      page
    }) => {
      await fixtures.reset()
      const katselu = variantti.luoKatselu(page, 'Lukion oppimäärä')
      await katselu.goto(oid)
      await katselu.aloitaMuokkaus()

      await expect(katselu.poistaSuoritusLinkki).toHaveCount(0)
    })
  })
}
