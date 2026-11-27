import { test, expect } from './base'
import { virkailija } from './setup/auth'

const oppiaineet = [
  ['2016', 'MAY', '112726', 'HL', '7', 'N'],
  ['2016', 'MAY', '100179', 'HL', '6', 'N'],
  ['2016', 'MAY', '100680', 'HL', '6', 'N'],
  ['2016', 'MAY', '100088', 'SL', '6', 'N'],
  ['2016', 'MAY', '166711', 'SL', '5', 'N'],
  ['2016', 'MAY', '100452', 'SL', '5', 'N'],
  ['2016', 'MAY', '100680', 'EE', 'B', 'N'],
  ['2016', 'MAY', '100577', 'TK', 'A', 'N']
]

const ibisRivi = (iboTunniste: string) =>
  [
    '2016',
    'MAY',
    '000001',
    iboTunniste,
    '04/07/2001',
    'FEMALE',
    'DIPLOMA',
    'IB-Diploma',
    'Dani',
    '',
    '',
    '06/07/2016',
    '37',
    'D',
    '+2',
    ...oppiaineet.flat()
  ]
    .map((kenttä) => `"${kenttä}"`)
    .join(',')

// Kiintoaineiston IB Diploma -oppijan rivi, rivi tuntemattomalla tunnisteella ja rivi, jota ei voi jäsentää.
const tiedosto = [ibisRivi('dip123'), ibisRivi('xyz000'), 'rikkinäinen rivi']
  .map((rivi) => `${rivi}\n`)
  .join('')

test.describe('IBIS-tiedoston tuonti', () => {
  test.describe('OPH-pääkäyttäjä', () => {
    test.use({ storageState: virkailija('pää') })

    test.beforeEach(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test('esikatselee IBIS-tiedoston ja tuo sen', async ({
      page,
      tiedostonTuontiPage
    }) => {
      await page.goto('/koski/tiedonsiirrot')
      await tiedostonTuontiPage.naviLinkki.click()
      await expect(page).toHaveURL(/\/koski\/tiedonsiirrot\/tiedoston-tuonti$/)
      await expect(tiedostonTuontiPage.otsikko).toHaveText('IBIS-tuonti')
      await expect(tiedostonTuontiPage.esimerkkirivi).toBeHidden()
      await tiedostonTuontiPage.esimerkki.locator('summary').click()
      await expect(tiedostonTuontiPage.esimerkkirivi).toContainText('"abc123"')
      await expect(tiedostonTuontiPage.esikatseleButton).toHaveCount(0)
      await expect(tiedostonTuontiPage.tuoButton).toHaveCount(0)
      await expect(tiedostonTuontiPage.tyhjennäButton).toHaveCount(0)

      await tiedostonTuontiPage.valitseTiedosto(tiedosto)
      await expect(tiedostonTuontiPage.tuoButton).toHaveCount(0)
      await tiedostonTuontiPage.esikatseleButton.click()
      await expect(tiedostonTuontiPage.tuloksenOtsikko).toHaveText(
        'Esikatselun tulos'
      )
      await expect(tiedostonTuontiPage.tulosilmoitus).toHaveText(
        'Tämä on esikatselu: tietoja ei ole tallennettu Koskeen.'
      )
      await expect(tiedostonTuontiPage.esikatseleButton).toHaveCount(0)
      await expect(tiedostonTuontiPage.uudet).toHaveText('0')
      await expect(tiedostonTuontiPage.päivitykset).toHaveText('1')
      await expect(tiedostonTuontiPage.hylätyt).toHaveText('1')
      await expect(tiedostonTuontiPage.jäsentämättömät).toHaveText('1')
      await expect(tiedostonTuontiPage.rivit).toHaveCount(2)
      await expect(tiedostonTuontiPage.tila(0)).toHaveText('Päivitetään')
      await expect(tiedostonTuontiPage.tila(1)).toHaveText('Hylätään')
      await expect(tiedostonTuontiPage.rivit.nth(1)).toContainText(
        'Oppijaa ei voitu tunnistaa'
      )
      await expect(tiedostonTuontiPage.jäsennysvirheet).toHaveCount(1)

      await tiedostonTuontiPage.tuoButton.click()
      await expect(tiedostonTuontiPage.tuloksenOtsikko).toHaveText(
        'Tuonnin tulos'
      )
      await expect(tiedostonTuontiPage.tulosilmoitus).toHaveText(
        'Tuonti on tehty: hyväksytyt rivit on tallennettu Koskeen.'
      )
      await expect(tiedostonTuontiPage.päivitykset).toHaveText('1')
      await expect(tiedostonTuontiPage.tila(0)).toHaveText('Päivitetty')
      await expect(tiedostonTuontiPage.tila(1)).toHaveText('Hylätty')
      await expect(tiedostonTuontiPage.tuoButton).toBeDisabled()
      await expect(tiedostonTuontiPage.esikatseleButton).toHaveCount(0)

      await tiedostonTuontiPage.tyhjennäButton.click()
      await expect(tiedostonTuontiPage.tulos).toHaveCount(0)
      await expect(tiedostonTuontiPage.tuoButton).toHaveCount(0)
      await expect(tiedostonTuontiPage.tyhjennäButton).toHaveCount(0)
    })
  })

  test.describe('Oppilaitoksen tallentaja', () => {
    test.use({ storageState: virkailija('hki-tallentaja') })

    test('ei näe tiedoston tuontia', async ({ page, tiedostonTuontiPage }) => {
      await page.goto('/koski/tiedonsiirrot')
      await expect(
        page.locator('.tiedonsiirrot-navi .tiedonsiirto-link')
      ).toBeVisible()
      await expect(tiedostonTuontiPage.naviLinkki).toHaveCount(0)

      await tiedostonTuontiPage.goto()
      await expect(
        page.getByText('Sallittu vain OPH-pääkäyttäjälle')
      ).toBeVisible()
    })
  })
})
