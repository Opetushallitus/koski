import { expect, test } from './schema-viewer-base'
import type { Page } from '@playwright/test'

const openNode = async (page: Page, path: string) => {
  const segments = path.split('.')
  const v = segments.map(encodeURIComponent).join('.')
  await page.goto(`/koski/json-schema-viewer/#viewer-page?v=${v}`)
  const expectedName = segments[segments.length - 1]
  await expect(page.locator('#info-title')).toContainText(expectedName, {
    timeout: 30000
  })
  await expect(page.locator('#info-technical')).toBeVisible()
}

const technicalValue = (page: Page, label: string) =>
  page
    .locator('#info-technical .jsv-tech-row')
    .filter({ has: page.getByText(label, { exact: true }) })
    .locator('.jsv-tech-value')

test.describe('Schema viewer', () => {
  test.setTimeout(60000)

  test('Koodisto, Oksa, Allowed ja käännökset (opiskeluoikeuden tyyppi)', async ({
    page
  }) => {
    await openNode(
      page,
      'opiskeluoikeudet.Tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeus.tyyppi'
    )
    const tech = page.locator('#info-technical')
    const koodistoLink = tech.getByRole('link', {
      name: 'opiskeluoikeudentyyppi',
      exact: true
    })
    await expect(koodistoLink).toBeVisible()
    await expect(koodistoLink).toHaveAttribute(
      'href',
      /\/koodisto\/opiskeluoikeudentyyppi\//
    )
    const oksaLink = technicalValue(page, 'Oksa').getByRole('link')
    await expect(oksaLink).toBeVisible()
    await expect(oksaLink).toHaveAttribute(
      'href',
      /wiki\.eduuni\.fi\/.*#tmpOKSAID/
    )
    const allowed = technicalValue(page, 'Allowed')
    await expect(allowed).toHaveText('tuva')
    await expect(allowed.locator('.jsv-chip')).toBeVisible()
    await expect(
      page.locator('#info-localized').getByText('FI', { exact: true })
    ).toBeVisible()
    await expect(
      page.locator('#info-localized').getByText('SV', { exact: true })
    ).toBeVisible()
    await expect(
      page
        .locator('#info-localized .jsv-lang-block')
        .filter({ has: page.getByText('FI', { exact: true }) })
        .locator('.jsv-prose')
    ).toContainText('Opiskeluoikeuden tyyppi')
    await expect(
      page
        .locator('#info-localized .jsv-lang-block')
        .filter({ has: page.getByText('SV', { exact: true }) })
        .locator('.jsv-prose')
    ).toContainText('Typ av studierätt')
  })

  test('@SensitiveData: chip, lock-badge ja taulukon kardinaliteetti', async ({
    page
  }) => {
    await openNode(
      page,
      'opiskeluoikeudet.Ammatillinen.lisätiedot.sisäoppilaitosmainenMajoitus'
    )
    await expect(technicalValue(page, 'Cardinality')).toHaveText('0..*')
    await expect(
      page.locator('#info-technical .jsv-chip', { hasText: '@SensitiveData' })
    ).toBeVisible()
    await expect(page.locator('#info-badges')).toContainText(
      'Erityinen henkilötieto'
    )
  })

  test('@RedundantData: chip, "ei käytössä" -badge ja oletusarvo', async ({
    page
  }) => {
    await openNode(
      page,
      'opiskeluoikeudet.Ammatillinen.lisätiedot.oikeusMaksuttomaanAsuntolapaikkaan'
    )
    await expect(technicalValue(page, 'Default')).toHaveText('null')
    await expect(
      page.locator('#info-technical .jsv-chip', { hasText: '@RedundantData' })
    ).toBeVisible()
    await expect(page.locator('#info-badges')).toContainText(
      'Kenttä ei ole käytössä'
    )
  })

  test('@Deprecated, Koodisto, Read-only ja Computed (vuosiluokan oppiaineen tila)', async ({
    page
  }) => {
    await openNode(
      page,
      [
        'opiskeluoikeudet',
        'Perusopetuksen opiskeluoikeus',
        'suoritukset',
        'Perusopetuksen vuosiluokan suoritus',
        'osasuoritukset',
        'Nuorten perusopetuksen oppiaineen suoritus',
        'tila'
      ].join('.')
    )
    const tech = page.locator('#info-technical')
    const koodistoLink = tech.getByRole('link', {
      name: 'suorituksentila',
      exact: true
    })
    await expect(koodistoLink).toBeVisible()
    await expect(koodistoLink).toHaveAttribute(
      'href',
      /\/koodisto\/suorituksentila\//
    )
    await expect(technicalValue(page, 'Read-only')).toHaveText('Ei käytössä.')
    await expect(technicalValue(page, 'Computed')).toHaveText(
      'Derived value, not set on input'
    )
    await expect(
      tech.locator('.jsv-chip', { hasText: '@Deprecated' })
    ).toBeVisible()
    await expect(page.locator('#info-badges')).toContainText('Vanhentunut')
  })

  test.describe('Viewerin toiminnot testiskeemalla', () => {
    const oppijaSchemaUrl = '/koski/api/documentation/viewer-smoke-schema.json'
    const oppijaSchema = {
      id: oppijaSchemaUrl,
      type: 'object',
      properties: {
        nimi: { type: 'string' },
        ikä: { $ref: 'viewer-smoke-age.json' }
      }
    }
    const validOppija = { nimi: 'Esimerkki', ikä: 17 }

    const ageSchema = { type: 'integer', minimum: 0 }

    test.beforeEach(async ({ page }) => {
      const routes = {
        [`**${oppijaSchemaUrl}`]: oppijaSchema,
        '**/viewer-smoke-age.json': ageSchema
      }
      for (const [url, json] of Object.entries(routes)) {
        await page.route(url, (route) => route.fulfill({ json }))
      }

      await page.goto(
        '/koski/json-schema-viewer/?schema=viewer-smoke-schema.json#viewer-page?v=nimi'
      )
      await expect(page.locator('#loading')).toBeHidden()
    })

    test('jakolinkki päivittyy käsin valitun solmun mukaan ilman alkuhashia', async ({
      page
    }) => {
      await page.goto(
        '/koski/json-schema-viewer/?schema=viewer-smoke-schema.json'
      )
      await expect(page.locator('#loading')).toBeHidden()

      const originalUrl = page.url()
      expect(new URL(originalUrl).hash).toBe('')

      for (const name of ['nimi', 'ikä']) {
        const node = page.locator('#jsv-tree .node-text', {
          hasText: new RegExp(`^${name}\\*?$`)
        })
        await node.click()

        await page.locator('#permalink').click()
        await expect(page.locator('#sharelink')).toBeVisible()
        await expect(page.locator('#sharelink')).toHaveValue(
          `${originalUrl}#viewer-page?open=${encodeURIComponent(name)}`
        )
        await expect(page).toHaveURL(originalUrl)

        await page.keyboard.press('Escape')
        await expect(page).toHaveURL(originalUrl)
      }
    })

    test('sulkee ja avaa info-paneelin painikkeesta', async ({ page }) => {
      const panel = page.locator('#info-panel')
      await expect(panel).toBeVisible()

      const toggle = page.getByTitle('Toggle Info', { exact: true })
      await toggle.click()
      await expect(panel).toBeHidden()

      await toggle.click()
      await expect(panel).toBeVisible()
    })

    test('vaihtaa info-paneelin välilehtiä monistamatta sivuelementtejä', async ({
      page
    }) => {
      for (const [name, id] of [
        ['Example', 'example'],
        ['Schema', 'schema'],
        ['Definition', 'def']
      ]) {
        await page.getByRole('link', { name, exact: true }).click()
        await expect(page.locator(`#info-tab-${id}`)).toBeVisible()
        await expect(page.locator('[data-role=page]')).toHaveCount(2)
      }
    })

    test('hakee solmun ja avaa sen puussa', async ({ page }) => {
      const rootNodeToggle = page.locator('#jsv-tree circle').first()
      // Juuri on aluksi avattu. Sen ympyrän klikkaaminen piilottaa lapsisolmut.
      await rootNodeToggle.click()
      const ageNode = page.locator('#jsv-tree .node-text', {
        hasText: /^ikä/
      })
      await expect(ageNode).toBeHidden()

      await page.getByPlaceholder('Search...').fill('ikä')
      const searchResults = page.locator('#search-result a')
      const result = searchResults.filter({ hasText: /^ikä$/ })
      await expect(result).toBeVisible()
      await expect(searchResults.filter({ hasText: /^nimi$/ })).toBeHidden()

      await result.click()
      await expect(ageNode).toBeVisible()
    })

    test('sulkee ja avaa selitteen', async ({ page }) => {
      const legend = page.locator('#legend-items')
      await expect(legend).toBeVisible()

      const legendToggle = page.getByRole('link', { name: /^Legend\b/ })
      await legendToggle.click()
      await expect(legend).toBeHidden()

      await legendToggle.click()
      await expect(legend).toBeVisible()
    })

    test('zoomaa puuta sisään ja ulos', async ({ page }) => {
      // SVG:n muunnosmatriisin a on vaakasuuntainen skaala, kun puuta ei kierretä.
      const scale = () =>
        page
          .locator('#node-group')
          .evaluate((g) => (g as SVGGElement).getCTM()!.a)
      const initial = await scale()
      await page.getByTitle('Zoom In', { exact: true }).click()
      await expect.poll(scale).toBeGreaterThan(initial)

      const zoomedIn = await scale()
      await page.getByTitle('Zoom Out', { exact: true }).click()
      await expect.poll(scale).toBeLessThan(zoomedIn)
    })

    test('näyttää virheikkunan virheellisestä JSON-tiedostosta', async ({
      page
    }) => {
      await page.getByRole('link', { name: 'Validator', exact: true }).click()

      await page.getByLabel('Upload File:').setInputFiles({
        name: 'virhe.json',
        mimeType: 'application/json',
        buffer: Buffer.from('{')
      })

      const popup = page.locator('#popup-error')
      await expect(popup).toBeVisible()
      await expect(popup).toContainText('The file is not valid JSON.')
    })

    test('validoi JSONin ja erillisestä tiedostosta ladatun viittauksen', async ({
      page
    }) => {
      await page.getByRole('link', { name: 'Validator', exact: true }).click()

      const input = page.getByLabel('JSON to Validate:')
      await input.fill(JSON.stringify(validOppija))
      await page.getByRole('button', { name: 'Validate!' }).click()

      const results = page.locator('#validation-results')
      await expect(results).toHaveText('JSON is valid!')

      await input.fill('{"nimi":"Esimerkki","ikä":-1}')
      await page.getByRole('button', { name: 'Validate!' }).click()
      await expect(results).toContainText('JSON is NOT valid!')
      await expect(results).toContainText('minimum')
    })

    test('lukee ladatun JSON-tiedoston validaattoriin', async ({ page }) => {
      await page.getByRole('link', { name: 'Validator', exact: true }).click()

      const contents = JSON.stringify(validOppija)
      await page.getByLabel('Upload File:').setInputFiles({
        name: 'oppija.json',
        mimeType: 'application/json',
        buffer: Buffer.from(contents)
      })

      await expect(page.getByLabel('JSON to Validate:')).toHaveValue(contents)
    })

    test('lukee pudotetun JSON-tiedoston validaattoriin', async ({ page }) => {
      await page.getByRole('link', { name: 'Validator', exact: true }).click()

      const contents = JSON.stringify(validOppija)
      const dataTransfer = await page.evaluateHandle((text) => {
        const dt = new DataTransfer()
        dt.items.add(new File([text], 'oppija.json'))
        return dt
      }, contents)
      const textarea = page.getByLabel('JSON to Validate:')
      await textarea.dispatchEvent('drop', { dataTransfer })

      await expect(textarea).toHaveValue(contents)
    })
  })
})
