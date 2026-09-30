import { expect, test } from './base'
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

    test('sulkee ja avaa info-paneelin painikkeesta', async ({ page }) => {
      const panel = page.locator('#info-panel')
      await expect(panel).toBeVisible()
      const toggle = page.getByTitle('Toggle Info', { exact: true })
      await toggle.click()
      await expect(panel).toBeHidden()
      await toggle.click()
      await expect(panel).toBeVisible()
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
  })
})
