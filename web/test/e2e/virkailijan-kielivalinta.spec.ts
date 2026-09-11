import type { BrowserContext } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

const langCookie = async (context: BrowserContext) =>
  (await context.cookies()).find((c) => c.name === 'lang')

/**
 * Virkailijan kieli on asiointikieli oppijanumerorekisteristä ja se ratkaistaan palvelimella
 * jokaisella sivunlatauksella. Aiemmin kieli haettiin vain CAS-tiketin validoinnissa ja talletettiin
 * istuntoevästeeseen: selaimen sulkeminen hukkasi lang-evästeen mutta säilytti pysyvän koskiUser-
 * evästeen, jolloin istunto jatkui ilman uutta tikettiä ja käyttöliittymä jäi suomeksi. Nyt kieltä
 * ei talleteta selaimeen lainkaan, joten sitä ei voi hukata eikä selaimeen jäänyt arvo voi jäädä
 * ohittamaan asiointikieltä.
 */
test.describe('Virkailijan kielivalinta', () => {
  test.use({ storageState: virkailija('ruotsinkielinen') })

  test('kieli tulee asiointikielestä eikä sitä talleteta evästeeseen', async ({
    page,
    context
  }) => {
    await context.clearCookies({ name: 'lang' })
    await page.goto('/koski/virkailija')

    await expect(page.locator('html')).toHaveAttribute('lang', 'sv')
    await expect(page.locator('.oppijataulukko-header')).toContainText(
      'Studerande'
    )
    expect(await langCookie(context)).toBeUndefined()

    // Vastaa selaimen sulkemista: aiemmin kieli katosi tässä, nyt ei ole mitään mitä hukata.
    await page.reload()

    await expect(page.locator('html')).toHaveAttribute('lang', 'sv')
    await expect(page.locator('.oppijataulukko-header')).toContainText(
      'Studerande'
    )
    expect(await langCookie(context)).toBeUndefined()
  })

  test('selaimeen jäänyt lang-eväste ei ohita asiointikieltä', async ({
    page,
    context
  }) => {
    await context.clearCookies({ name: 'lang' })
    await context.addCookies([
      { name: 'lang', value: 'fi', domain: 'localhost', path: '/' }
    ])
    await page.goto('/koski/virkailija')

    await expect(page.locator('html')).toHaveAttribute('lang', 'sv')
    await expect(page.locator('.oppijataulukko-header')).toContainText(
      'Studerande'
    )
  })
})
