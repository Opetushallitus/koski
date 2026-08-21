import { expect, Locator, Page } from '@playwright/test'

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
 * Avaa kaikki näkymän laajennettavat osasuoritusrivit.
 *
 * Perusopetuksessa ja Ahvenanmaalla ei ole "Avaa kaikki" -painiketta, joten
 * rivit avataan yksi kerrallaan. ExpandButtonin saavutettavuusnimi vaihtuu
 * avattaessa "Pienennä"-muotoon, joten lokaattori kutistuu joka klikkauksella.
 *
 * Laajennetut rivit ovat visuaalitesteissä olennaisia: kentän nimen ja arvon
 * asettelu näkyy vain avatussa osasuorituksessa.
 */
export const avaaOsasuoritusrivit = async (page: Page): Promise<void> => {
  await odotaAsettelunVakiintuminen(page)
  const laajenna = page.getByRole('button', { name: 'Laajenna Osasuoritus' })
  for (let jäljellä = await laajenna.count(); jäljellä > 0; jäljellä--) {
    await laajenna.first().click()
  }
  await expect(laajenna).toHaveCount(0)
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
 * Wait until the final image has remained unchanged for long enough.
 *
 * Page height alone does not detect changes in a shorter column or changes
 * that only affect color. Compare PNG screenshots taken with the same options
 * used by the actual snapshot assertion.
 */
const waitForImageToStabilize = async (
  page: Page,
  mask: Locator[],
  { stableDurationMs = 500, intervalMs = 100, timeoutMs = 10000 } = {}
): Promise<void> => {
  await page.evaluate(() => document.fonts.ready)

  const startTime = Date.now()
  let stableSince = startTime
  let previousImage: Buffer | undefined

  while (Date.now() - startTime < timeoutMs) {
    const image = await page.screenshot({
      animations: 'disabled',
      caret: 'hide',
      mask,
      scale: 'css'
    })
    const now = Date.now()

    if (previousImage?.equals(image)) {
      if (now - stableSince >= stableDurationMs) return
    } else {
      stableSince = now
      previousImage = image
    }

    await page.waitForTimeout(intervalMs)
  }

  throw new Error(
    `The screenshot did not stabilize within ${timeoutMs} ms. ` +
      `The view probably contains continuously changing content.`
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
  // The study right OID is generated randomly on every fixture run. Mask it
  // instead of changing the DOM. The visibility assertion ensures that a
  // changed test ID cannot silently disable the mask.
  const studyRightOids = page.locator('[data-testid$=".opiskeluoikeus.oid"]')
  await expect(studyRightOids.first()).toBeVisible()
  const mask = [studyRightOids]

  await suurennaIkkunaSivunKorkuiseksi(page)
  // Ikkuna kattaa nyt koko sivun, mutta varmistetaan vieritys alkuun: fixed-
  // elementit asemoituvat ikkunaan, ei dokumenttiin.
  await page.evaluate(() => window.scrollTo(0, 0))
  await waitForImageToStabilize(page, mask)
  // expect.timeout (5 s) ei riitä pitkän sivun kaappaukseen.
  await expect(page).toHaveScreenshot(nimi, {
    mask,
    timeout: 15000
  })
}
