import type { BrowserContext } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

const langCookie = async (context: BrowserContext) =>
  (await context.cookies()).find((c) => c.name === 'lang')

/**
 * Virkailijan asiointikieli haettiin aiemmin vain CAS-tiketin validoinnin yhteydessä. lang on istuntoeväste ja
 * koskiUser pysyvä, joten selaimen sulkeminen hukkasi kielen mutta säilytti istunnon: istunto jatkui ilman uutta
 * tikettiä eikä kieltä haettu enää koskaan uudelleen.
 */
test.describe('Virkailijan kielivalinta', () => {
  test.use({ storageState: virkailija('ruotsinkielinen') })

  test('kieli säilyy, kun istuntoeväste katoaa mutta istunto jatkuu', async ({
    page,
    context
  }) => {
    await page.goto('/koski/virkailija')
    await expect(page.locator('html')).toHaveAttribute('lang', 'sv')
    await expect(page.locator('.oppijataulukko-header')).toContainText(
      'Studerande'
    )

    // Vastaa selaimen sulkemista: istuntoeväste katoaa, pysyvä koskiUser jää voimaan
    await context.clearCookies({ name: 'lang' })
    expect(await langCookie(context)).toBeUndefined()

    await page.reload()

    await expect(page.locator('html')).toHaveAttribute('lang', 'sv')
    await expect(page.locator('.oppijataulukko-header')).toContainText(
      'Studerande'
    )
    expect((await langCookie(context))?.value).toEqual('sv')
  })

  test('täydennetty lang-eväste on istuntoeväste', async ({
    page,
    context
  }) => {
    await context.clearCookies({ name: 'lang' })
    await page.goto('/koski/virkailija')

    const lang = await langCookie(context)
    expect(lang?.value).toEqual('sv')
    // Istuntoevästeisyys on tietoinen valinta: pitkä voimassaoloaika jättäisi väärän kielen voimaan
    // selaimen uudelleenkäynnistysten yli sen sijaan, että se korjaantuisi itsestään.
    expect(lang?.expires).toEqual(-1)
  })
})
