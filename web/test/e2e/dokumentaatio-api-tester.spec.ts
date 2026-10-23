import { Locator, Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

const clickAndGetHetuSearchRequestBody = async (page: Page, button: Locator) => {
  const request = page.waitForRequest(
    (r) => r.method() === 'POST' && r.url().endsWith('/koski/api/henkilo/hetu')
  )
  await button.click()
  return (await request).postDataJSON() as unknown
}

test.describe('Documentation API tester', () => {
  test.use({ storageState: virkailija('pää') })

  test('sends the post data from the editor', async ({ page }) => {
    await page.goto('/koski/dokumentaatio/rajapinnat/opintohallintojarjestelmat')

    const operation = page.locator('.api-operation', {
      has: page.getByRole('heading', {
        name: /^POST\s*\/koski\/api\/henkilo\/hetu$/
      })
    })
    await operation.getByRole('heading').click()

    const exampleRequestBody = { hetu: '010101-123N' }
    const requestBodyJson = operation.getByRole('textbox')
    await expect(requestBodyJson).toHaveValue(
      JSON.stringify(exampleRequestBody, null, 2)
    )

    const tryButton = operation.getByRole('button', { name: 'Kokeile' })
    const result = operation.locator('.result')

    expect(await clickAndGetHetuSearchRequestBody(page, tryButton)).toEqual(
      exampleRequestBody
    )
    await expect(result).toContainText('200')
    await expect(result).toContainText('Esimerkki')

    const editedRequestBody = { hetu: '081165-793C' }
    await requestBodyJson.fill(JSON.stringify(editedRequestBody))

    expect(await clickAndGetHetuSearchRequestBody(page, tryButton)).toEqual(
      editedRequestBody
    )
    await expect(result).toContainText('Çelik-Eerola')
  })
})
