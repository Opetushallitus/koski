import { Page } from '@playwright/test'
import { expect, test } from './base'

const viewer = (hash: string, schema = 'migri-oppija-schema.json') =>
  `/koski/json-schema-viewer/?schema=${schema}#viewer-page?${hash}`

// Pakollisen kentän nimen perässä on tähti
const nodeText = (page: Page, name: string) =>
  page.locator('svg#jsv-tree g.node text.node-text', {
    hasText: new RegExp(`^${name}\\*?$`)
  })

test.describe('JSON Schema Viewer deep links', () => {
  test('v= nimipolulla avaa ja valitsee solmun', async ({ page }) => {
    await page.goto(viewer('v=opiskeluoikeudet.lisätiedot.koulutusvienti'))

    const node = nodeText(page, 'koulutusvienti')
    await expect(node).toBeVisible()
    await expect(node.locator('..')).toHaveClass(/focus/)
  })

  test('v= vanhalla indeksipolulla toimii edelleen', async ({ page }) => {
    await page.goto(viewer('v=1-0'))

    await expect(page.locator('svg#jsv-tree g.node.focus text')).toHaveText(
      /^Migri opiskeluoikeus/
    )
  })

  test('nimipolku voi nimetä abstraktin välisolmun', async ({ page }) => {
    await page.goto(
      viewer(
        'v=opiskeluoikeudet.Migri opiskeluoikeus.lisätiedot.koulutusvienti'
      )
    )

    await expect(nodeText(page, 'koulutusvienti').locator('..')).toHaveClass(
      /focus/
    )
  })

  test('open= avaa useita polkuja', async ({ page }) => {
    await page.goto(
      viewer('open=opiskeluoikeudet.tila.opiskeluoikeusjaksot,henkilö')
    )

    await expect(nodeText(page, 'alku')).toBeVisible()
    await expect(nodeText(page, 'hetu')).toBeVisible()
    await expect(nodeText(page, 'koulutusvienti')).toHaveCount(0)
  })

  test('mark= korostaa solmut ja avaa niiden vanhemmat', async ({ page }) => {
    await page.goto(
      viewer(
        'mark=opiskeluoikeudet.lisätiedot.koulutusvienti,opiskeluoikeudet.tila.opiskeluoikeusjaksot'
      )
    )

    const marked = page.locator('svg#jsv-tree g.node.marked')
    await expect(marked).toHaveCount(2)
    await expect(nodeText(page, 'koulutusvienti')).toBeVisible()
    await expect(nodeText(page, 'alku')).toHaveCount(0)
    await expect(
      page.locator('#legend-items text:text-is("Highlighted")')
    ).toBeAttached()
  })

  test('v= yksin avaa info-paneelin', async ({ page }) => {
    await page.goto(viewer('v=opiskeluoikeudet.lisätiedot.koulutusvienti'))

    await expect(page.locator('#info-panel')).toHaveClass(/ui-panel-open/)
  })

  test('mark= pitää info-paneelin kiinni myös v=:n kanssa', async ({
    page
  }) => {
    await page.goto(
      viewer(
        'mark=opiskeluoikeudet.lisätiedot.koulutusvienti,henkilö.hetu&v=opiskeluoikeudet.lisätiedot.koulutusvienti'
      )
    )

    await expect(nodeText(page, 'koulutusvienti')).toBeVisible()
    await expect(page.locator('#info-panel')).not.toHaveClass(/ui-panel-open/)
  })

  test('oneOf-haaran voi valita nimen alkuosalla', async ({ page }) => {
    await page.goto(
      viewer('mark=opiskeluoikeudet.Aikuisten.oid', 'koski-oppija-schema.json')
    )

    const marked = page.locator('svg#jsv-tree g.node.marked')
    await expect(marked).toHaveCount(1)
    await expect(marked.locator('text')).toHaveText(/^oid/)
    await expect(
      nodeText(page, 'Aikuisten perusopetuksen opiskeluoikeus\\{ \\}')
    ).toBeVisible()
  })

  test('nimeämätön oneOf-haara merkitsee osuman kaikista haaroista', async ({
    page
  }) => {
    await page.goto(
      viewer('mark=opiskeluoikeudet.oid', 'ytl-oppija-schema.json')
    )

    await expect(page.locator('#loading')).toBeHidden()
    const branchCount = await page.evaluate(() => {
      const n = (window as any).JSV.resolveNodePath('opiskeluoikeudet')
      let kids = n.children || n._children
      while (kids.length === 1 && !kids[0].isReal) {
        kids = kids[0].children || kids[0]._children
      }
      return kids.length
    })
    expect(branchCount).toBeGreaterThan(1)
    await expect(page.locator('svg#jsv-tree g.node.marked')).toHaveCount(
      branchCount
    )
  })

  test('hashin muutos päivittää näkymän ilman uudelleenlatausta', async ({
    page
  }) => {
    await page.goto(viewer('mark=opiskeluoikeudet.lisätiedot.koulutusvienti'))
    await expect(nodeText(page, 'koulutusvienti')).toBeVisible()

    await page.evaluate(() => {
      window.location.hash = '#viewer-page?mark=henkilö.hetu'
    })

    const marked = page.locator('svg#jsv-tree g.node.marked')
    await expect(marked).toHaveCount(1)
    await expect(marked.locator('text')).toHaveText(/^hetu/)
    await expect(nodeText(page, 'koulutusvienti')).toHaveCount(0)
  })

  test('info-paneelin välilehti ei lataa koko sivua uudelleen sisäänsä', async ({
    page
  }) => {
    await page.goto(viewer('v=opiskeluoikeudet.lisätiedot.koulutusvienti'))

    await expect(page.locator('#info-panel .jsv-term').first()).toHaveText(
      /koulutusvienti/i
    )
    await expect(page.locator('#loading')).toHaveCount(1)
    await expect(page.locator('[data-role=page]')).toHaveCount(2)
  })

  test('open= keskittää näkymän avattuun solmuun myös hashin vaihtuessa', async ({
    page
  }) => {
    await page.goto(viewer('open=henkilö'))
    await expect(nodeText(page, 'hetu')).toBeVisible()

    await page.evaluate(() => {
      window.location.hash =
        '#viewer-page?open=opiskeluoikeudet.suoritukset.osasuoritukset.osasuoritukset.tunnustettu'
    })

    // Solmut animoituvat juuren kohdalta paikoilleen, joten odotetaan liikkeen päättymistä.
    // (toBeInViewport ei toimi luotettavasti SVG:n sisäisille elementeille.)
    const viewport = page.viewportSize()!
    let previous = ''
    await expect
      .poll(async () => {
        const box = await nodeText(page, 'selite').boundingBox()
        const current = JSON.stringify(box)
        const settled = current === previous
        previous = current
        if (!box || !settled) return 'moving'
        const inside =
          box.x >= 0 &&
          box.y >= 0 &&
          box.x + box.width <= viewport.width &&
          box.y + box.height <= viewport.height
        return inside ? 'inside viewport' : 'outside viewport'
      })
      .toBe('inside viewport')
  })

  test('tuntematon polku ohitetaan hiljaisesti', async ({ page }) => {
    await page.goto(
      viewer(
        'mark=opiskeluoikeudet.eiolemassa,opiskeluoikeudet.lisätiedot.koulutusvienti'
      )
    )

    await expect(page.locator('svg#jsv-tree g.node.marked')).toHaveCount(1)
    await expect(nodeText(page, 'koulutusvienti')).toBeVisible()
  })

  test('jakolinkki käyttää open=-parametria ja nimipolkua', async ({
    page
  }) => {
    await page.goto(viewer('v=opiskeluoikeudet.lisätiedot.koulutusvienti'))

    await expect(page.locator('#sharelink')).toHaveValue(
      /#viewer-page\?open=opiskeluoikeudet\.lis%C3%A4tiedot\.koulutusvienti$/
    )
  })
})
