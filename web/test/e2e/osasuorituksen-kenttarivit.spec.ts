import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Rakenteellinen tarkistus laajennetun osasuorituksen kenttäriveille.
 *
 * OsasuoritusProperty saa ruudukkonsa raidat ylätason OsasuoritusProperties-
 * elementiltä (grid-template-columns: subgrid). Se toimii vain, jos rivi on
 * ruudukon SUORA lapsi. Jos jokin editori kääriikin rivinsä omaan elementtiinsä,
 * subgrid ei löydä raitoja, laskee arvoon none, ja rivin nimi ja arvo
 * sinkoutuvat mielivaltaisiin kohtiin — käytännössä nimi taulukon oikeaan
 * laitaan ja arvo sivun vasempaan reunaan.
 *
 * Näin kävi VST:n JOTPA-, koto- ja vapaatavoitteisille näkymille: niiden
 * kenttärivit tulevat ArviointiPropertyn kautta, joten ne eivät löytyneet
 * etsimällä OsasuoritusPropertyn importteja. Vika ei näy kaikissa näkymissä
 * eikä välttämättä jää kiinni kuvavertailussa, joten se tarkistetaan tässä
 * suoraan DOM:ista.
 */

type Nakyma = {
  nimi: string
  url: string
  /** Näkymä, jossa kentät renderöityvät: osa kentistä näkyy vain muokattaessa. */
  tila: 'katselu' | 'muokkaus'
  /** Suoritustabin indeksi, jos oletustabi on tyhjä. */
  tabi?: number
}

const näkymät: Nakyma[] = [
  { nimi: 'VST JOTPA', url: '1.2.246.562.24.00000000140', tila: 'katselu' },
  { nimi: 'VST lukutaito', url: '1.2.246.562.24.00000000107', tila: 'katselu' },
  {
    nimi: 'VST lukutaito',
    url: '1.2.246.562.24.00000000107',
    tila: 'muokkaus'
  },
  {
    nimi: 'VST kansanopisto',
    url: '1.2.246.562.24.00000000105',
    tila: 'katselu'
  },
  {
    nimi: 'VST vapaatavoitteinen',
    url: '1.2.246.562.24.00000000108',
    tila: 'muokkaus'
  },
  {
    nimi: 'VST koto 2022',
    url: '1.2.246.562.24.00000000135',
    tila: 'muokkaus'
  },
  {
    nimi: 'Taiteen perusopetus',
    url: '1.2.246.562.24.00000000143?opiskeluoikeudenTyyppi=taiteenperusopetus',
    tila: 'katselu'
  },
  {
    nimi: 'Ammatillinen osittainen',
    url: '1.2.246.562.24.00000000055',
    tila: 'katselu'
  },
  {
    // Oletustabi (9. vuosiluokka) on tyhjä, kentät ovat 8. vuosiluokalla.
    nimi: 'Ahvenanmaan perusopetus',
    url: '1.2.246.562.24.00000000190?opiskeluoikeudenTyyppi=ahvenanmaanperusopetus',
    tila: 'muokkaus',
    tabi: 2
  },
  {
    nimi: 'Perusopetus v2',
    url: '1.2.246.562.24.00000000007?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true',
    tila: 'muokkaus',
    tabi: 2
  }
]

const avaaKaikkiRivit = async (page: Page) => {
  const avaaKaikki = page.getByRole('button', { name: 'Avaa kaikki' })
  if (await avaaKaikki.isVisible().catch(() => false)) await avaaKaikki.click()
  // Sisäkkäiset taulukot paljastuvat vasta kun ylempi rivi on auki, joten
  // avataan kunnes avattavia ei enää ole.
  for (let kierros = 0; kierros < 5; kierros++) {
    const avattavia = await page.evaluate(() => {
      const napit = [
        ...document.querySelectorAll('.OsasuoritusRow .ExpandButton')
      ].filter((n) =>
        (n.getAttribute('aria-label') || '').startsWith('Laajenna')
      )
      napit.forEach((n) => (n as HTMLElement).click())
      return napit.length
    })
    if (avattavia === 0) break
  }
}

const ruudukonUlkopuolisetRivit = (page: Page) =>
  page.evaluate(() =>
    [...document.querySelectorAll('.OsasuoritusProperty')]
      .filter(
        (rivi) =>
          !rivi.parentElement!.classList.contains('OsasuoritusProperties')
      )
      .map(
        (rivi) =>
          `"${rivi.textContent!.slice(0, 40)}" vanhempana ` +
          `<${rivi.parentElement!.tagName.toLowerCase()} class="${rivi.parentElement!.className}">`
      )
  )

test.describe('Laajennetun osasuorituksen kenttärivit', () => {
  test.use({ storageState: virkailija('kalle') })

  for (const { nimi, url, tila, tabi } of näkymät) {
    test(`${nimi} (${tila}): kenttärivit ovat ruudukon suoria lapsia`, async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(url)
      if (tabi !== undefined) {
        await page.getByTestId(`oo.0.suoritusTabs.${tabi}.tab`).click()
      }
      if (tila === 'muokkaus') {
        await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      }
      await expect(page.locator('.OsasuoritusRow').first()).toBeVisible()
      await avaaKaikkiRivit(page)

      await expect(page.locator('.OsasuoritusProperty').first()).toBeVisible()
      expect(await ruudukonUlkopuolisetRivit(page)).toEqual([])
    })
  }
})
