import { expect, test } from './base'

test.describe('Schema viewer', () => {
  test.setTimeout(60000)

  test('OksaUri-linkit näkyvät klikattavina ja sisältävät #-fragmentin', async ({
    page
  }) => {
    await page.goto(
      '/koski/json-schema-viewer#viewer-page?v=1-0-18-10' //Vapaan sivistystyön opiskeluoikeus / tyyppi
    )

    await page.locator('svg#jsv-tree').waitFor({
      state: 'visible',
      timeout: 30000
    })

    await page.locator('a[href="#info-panel"]').click()

    const infoDefinition = page.locator('#info-definition')
    await expect(infoDefinition).toBeVisible({ timeout: 10000 })

    const oksaLink = infoDefinition.locator('a[href*="wiki.eduuni.fi"]')
    await expect(oksaLink).toBeVisible({ timeout: 10000 })
    const oksaHref = await oksaLink.getAttribute('href')
    expect(oksaHref).toContain('#tmpOKSAID')
  })
})
