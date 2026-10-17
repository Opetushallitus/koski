import { expect, test } from './base'

/*
 * Valpas käyttää Kosken paikallista kansalaisen kirjautumissivua (korhopankki,
 * web/app/Korhopankki.jsx ja web/app/korhopankki/HetuLogin.jsx) kansalaisnäkymänsä
 * kirjautumiseen, kun oikeaa CAS-kirjautumista ei ole käytössä: paikallisessa
 * kehityksessä ja Valppaan selaintesteissä.
 *
 * Tämä testi kuvaa sen, mihin Valpas sivulla luottaa.
 */
test.describe('Valppaan käyttämä kansalaisen paikallinen kirjautuminen', () => {
  test('ohjaa redirect-parametrin osoitteeseen Valppaan hyväksymällä istunnolla', async ({
    page,
    baseURL,
    kansalainenLoginPage
  }) => {
    const paluuosoite = new URL(
      '/koski/valpas/api/kansalainen/user?date=2021-09-05',
      baseURL
    ).href

    await page.goto(
      `/koski/login/oppija/local?redirect=${encodeURIComponent(paluuosoite)}`
    )
    await kansalainenLoginPage.hetuInput.pressSequentially('220109-784L')
    await kansalainenLoginPage.loginButton.click()

    await expect(page).toHaveURL(paluuosoite)

    const user = await page.request.get('/koski/valpas/api/kansalainen/user')
    expect(await user.json()).toMatchObject({
      name: 'Kaisa Koululainen',
      kansalainen: true
    })
  })
})
