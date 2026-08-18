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
 * Suurenna selainikkuna koko sivun korkuiseksi.
 *
 * Ikkunan koon muutos voi kasvattaa sivua (kapeampi vieritysalue, korkeuteen
 * sidotut säännöt), joten mitataan uudelleen kunnes korkeus pysyy samana.
 */
const suurennaIkkunaSivunKorkuiseksi = async (
  page: Page,
  { kierroksia = 3 } = {}
): Promise<void> => {
  const leveys = page.viewportSize()?.width ?? 1280
  let edellinen = -1

  for (let i = 0; i < kierroksia; i++) {
    await odotaAsettelunVakiintuminen(page)
    const korkeus = await page.evaluate(
      () => document.documentElement.scrollHeight
    )
    if (korkeus === edellinen) return
    await page.setViewportSize({ width: leveys, height: korkeus })
    edellinen = korkeus
  }

  throw new Error(
    `Sivun korkeus ei vakiintunut ${kierroksia} ikkunan koon muutoksen ` +
      `jälkeen (viimeisin ${edellinen} px). Kuvasta jäisi osa pois.`
  )
}

/**
 * Ota koko sivun kuvakaappaus vasta kun asettelu on vakiintunut.
 * Käytä tätä kaikissa visuaalitesteissä suoran toHaveScreenshotin sijaan.
 *
 * `fullPage`-kaappauksen sijaan ikkuna suurennetaan sivun korkuiseksi.
 * Syy: fullPage piirtää `position: fixed` -elementit sille vierityskohdalle,
 * jossa sivu sattuu kaappaushetkellä olemaan, jolloin #topbar ja .FooterBar
 * päätyvät keskelle kuvaa peittämään sisältöä. Playwright vierittää sivua
 * klikkausten yhteydessä (esim. "Avaa kaikki") eikä vieritä takaisin, joten
 * kohta vaihtelee testeittäin. Sivun korkuisessa ikkunassa kiinnitetyt
 * elementit asettuvat sinne minne kuuluvatkin: topbar ylälaitaan ja footer
 * alalaitaan.
 */
export const otaVakaaKuvakaappaus = async (
  page: Page,
  nimi: string
): Promise<void> => {
  await suurennaIkkunaSivunKorkuiseksi(page)
  // Ikkuna kattaa nyt koko sivun, mutta varmistetaan vieritys alkuun: fixed-
  // elementit asemoituvat ikkunaan, ei dokumenttiin.
  await page.evaluate(() => window.scrollTo(0, 0))
  // expect.timeout (5 s) ei riitä pitkän sivun kaappaukseen.
  await expect(page).toHaveScreenshot(nimi, {
    timeout: 15000
  })
}
