import { expect, Page } from '@playwright/test'

/**
 * Apurit visuaalisille regressiotesteille.
 *
 * Osa näkymistä renderöityy loppuun vasta hetken kuluttua: mitattuna VST:n
 * JOTPA-näkymä on välillä 1495 px korkea ennen kuin se asettuu 1776 px:iin, ja
 * kansanopisto 2174 px ennen 3259 px:ää. Jos kuva otetaan kesken renderöinnin,
 * baselineksi jää puolivalmis näkymä.
 *
 * Playwrightin `toHaveScreenshot` odottaa vertailuajossa kahden peräkkäisen
 * kaappauksen täsmäävän, mutta `--update-snapshots` tallentaa ENSIMMÄISEN
 * kaappauksen ilman tuota odotusta. Siksi odotetaan asettuminen itse, jolloin
 * sekä nauhoitus että vertailu näkevät saman, valmiin näkymän.
 */

/**
 * Odota, että sivun asettelu on vakiintunut: fontit ladattu ja sivun korkeus
 * pysynyt samana usealla peräkkäisellä mittauksella.
 *
 * Odottaa vain niin kauan kuin tarpeen – vakaalla sivulla tämä palaa noin
 * kolmessa mittausvälissä.
 */
export const odotaAsettelunVakiintuminen = async (
  page: Page,
  { vakaitaMittauksia = 3, valiMs = 100, aikakatkaisuMs = 10000 } = {}
): Promise<void> => {
  // Fonttien lataus muuttaa tekstin mittoja, joten odotetaan se ensin.
  await page.evaluate(() => document.fonts.ready)

  const alku = Date.now()
  let edellinen = -1
  let samojaPerakkain = 0

  while (Date.now() - alku < aikakatkaisuMs) {
    const korkeus = await page.evaluate(
      () => document.documentElement.scrollHeight
    )
    if (korkeus === edellinen) {
      samojaPerakkain += 1
      if (samojaPerakkain >= vakaitaMittauksia) return
    } else {
      samojaPerakkain = 0
      edellinen = korkeus
    }
    await page.waitForTimeout(valiMs)
  }

  throw new Error(
    `Sivun asettelu ei vakiintunut ${aikakatkaisuMs} ms:ssa ` +
      `(viimeisin korkeus ${edellinen} px). Näkymässä on todennäköisesti ` +
      `jatkuvasti muuttuvaa sisältöä – harkitse toHaveScreenshotin mask-optiota.`
  )
}

/**
 * Ota koko sivun kuvakaappaus vasta kun asettelu on vakiintunut.
 * Käytä tätä kaikissa visuaalitesteissä suoran toHaveScreenshotin sijaan.
 */
export const otaVakaaKuvakaappaus = async (
  page: Page,
  nimi: string
): Promise<void> => {
  await odotaAsettelunVakiintuminen(page)
  // expect.timeout (5 s) ei riitä pitkän sivun fullPage-kaappaukseen.
  await expect(page).toHaveScreenshot(nimi, {
    fullPage: true,
    timeout: 15000
  })
}
