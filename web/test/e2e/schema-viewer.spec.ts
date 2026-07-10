import { expect, test } from './base'
import { Page } from '@playwright/test'

// The `v=` permalink values are tree-index paths, so they depend on the schema's
// property/union ordering — each test also asserts the node name, so a shifted path
// fails clearly. Nodes below are stable design-example fields.
const openNode = async (page: Page, v: string, expectedName: string) => {
  // Force a full load: the viewer reads `v=` from the hash only on load, and a
  // hash-only change (opening a second node in the same test) does not reload the
  // SPA. about:blank guarantees the next goto navigates fresh to the wanted node.
  await page.goto('about:blank')
  await page.goto(`/koski/json-schema-viewer#viewer-page?v=${v}`)
  await page.locator('svg#jsv-tree').waitFor({ state: 'visible', timeout: 30000 })
  await page.locator('a[href="#info-panel"]').click()
  await expect(page.locator('#info-technical')).toBeVisible({ timeout: 10000 })
  await expect(page.locator('#info-title')).toContainText(expectedName)
}

test.describe('Schema viewer', () => {
  test.setTimeout(60000)

  test('Koodisto, Oksa, Allowed ja käännökset (opiskeluoikeuden tyyppi)', async ({
    page
  }) => {
    await openNode(page, '1-0-18-10', 'tyyppi')
    const tech = page.locator('#info-technical')
    // Koodisto row (linked)
    await expect(
      tech.locator('a[href*="/koodisto/opiskeluoikeudentyyppi/"]')
    ).toBeVisible()
    // Oksa row (linked, with #-fragment)
    const oksaLink = tech.locator('a[href*="wiki.eduuni.fi"]')
    await expect(oksaLink).toBeVisible()
    expect(await oksaLink.getAttribute('href')).toContain('#tmpOKSAID')
    // Allowed koodiarvo chip
    await expect(tech.locator('.jsv-chip', { hasText: 'tuva' })).toBeVisible()
    // fi + sv description blocks
    await expect(
      page.locator('#info-localized .jsv-lang-tag', { hasText: 'FI' })
    ).toBeVisible()
    await expect(
      page.locator('#info-localized .jsv-lang-tag', { hasText: 'SV' })
    ).toBeVisible()
  })

  test('@SensitiveData: chip, lock-badge ja taulukon kardinaliteetti', async ({
    page
  }) => {
    await openNode(page, '1-0-2-11-2', 'sisäoppilaitosmainenMajoitus')
    await expect(page.locator('#info-technical')).toContainText('0..*')
    await expect(
      page.locator('#info-technical .jsv-chip', { hasText: '@SensitiveData' })
    ).toBeVisible()
    await expect(page.locator('#info-badges')).toContainText(
      'Erityinen henkilötieto'
    )
  })

  test('@RedundantData: chip ja "ei käytössä" -badge', async ({ page }) => {
    await openNode(page, '1-0-2-11-0', 'oikeusMaksuttomaanAsuntolapaikkaan')
    await expect(
      page.locator('#info-technical .jsv-chip', { hasText: '@RedundantData' })
    ).toBeVisible()
    await expect(page.locator('#info-badges')).toContainText(
      'Kenttä ei ole käytössä'
    )
  })

  test('@Deprecated, Koodisto ja Read-only (suorituksen tila)', async ({
    page
  }) => {
    await openNode(page, '1-0-16-9-0-2-13-0-0-9', 'tila')
    const tech = page.locator('#info-technical')
    await expect(tech).toContainText('Koodisto')
    await expect(tech).toContainText('Read-only')
    await expect(
      tech.locator('.jsv-chip', { hasText: '@Deprecated' })
    ).toBeVisible()
    await expect(page.locator('#info-badges')).toContainText('Vanhentunut')
  })

  // Default/Computed rows come from the scala-schema change (DefaultValue emitting a
  // `default` field, SyntheticProperty emitting `synthetic`). Enable once scala-schema
  // is released and the Koski dependency bumped past 2.40.0.
  test.skip('Default- ja Computed-rivit (odottaa scala-schema-julkaisua)', async ({
    page
  }) => {
    await openNode(page, '1-0-2-11-0', 'oikeusMaksuttomaanAsuntolapaikkaan')
    await expect(page.locator('#info-technical')).toContainText('Default')
    await openNode(page, '1-0-16-9-0-2-13-0-0-9', 'tila')
    await expect(page.locator('#info-technical')).toContainText('Computed')
  })
})
