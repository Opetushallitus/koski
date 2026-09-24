import { expect, test } from './base'

/*
 * Valpas käyttää Kosken paikallista kansalaisen kirjautumissivua (korhopankki,
 * web/app/Korhopankki.jsx ja web/app/korhopankki/HetuLogin.jsx) kansalaisnäkymänsä
 * kirjautumiseen, kun oikeaa CAS-kirjautumista ei ole käytössä: paikallisessa
 * kehityksessä ja Valpaksen selaintesteissä.
 *
 * Tämä testi kuvaa sen, mihin Valpas sivulla luottaa. Valpaksen selaintestit eivät
 * käytä sivua (ne kirjautuvat suoraan mock-kutsulla), eikä niitä ajeta PR:ssä
 * pelkistä Kosken käyttöliittymämuutoksista. Jos muutat tässä testattua
 * käyttäytymistä, tarkista Valpaksen puoli:
 * - valpas-web/src/state/auth.ts (mockKoskiOppijaLogin) ohjaa sivulle
 * - valpas-web/test/integrationtests/login.test.ts testaa ohjauksen osoitteen
 * - valpas-web/test/integrationtests-env/browser/resetKansalainen.ts tekee saman
 *   mock-kirjautumiskutsun kuin sivu
 */
test.describe('Valpaksen käyttämä kansalaisen paikallinen kirjautuminen', () => {
  test('ohjaa redirect-parametrin osoitteeseen Valpaksen hyväksymällä istunnolla', async ({
    page,
    baseURL
  }) => {
    // Valpas välittää paluuosoitteena oman sivunsa koko osoitteen kyselyparametreineen.
    const paluuosoite = new URL(
      '/koski/valpas/api/kansalainen/user?date=2021-09-05',
      baseURL
    ).href

    // Valpaksen selaintestit kaatuvat konsoliin tuleviin verkkovirheisiin. Sivun
    // tekemä /koski/user-kysely palauttaa kirjautumattomalle 401:n, ja Valpas sallii
    // vain sen.
    const odottamattomatVirheet: string[] = []
    page.on('response', (response) => {
      const sallittu =
        response.status() === 401 &&
        new URL(response.url()).pathname === '/koski/user'
      if (response.status() >= 400 && !sallittu) {
        odottamattomatVirheet.push(`${response.status()} ${response.url()}`)
      }
    })
    page.on('pageerror', (error) => odottamattomatVirheet.push(error.message))

    await page.goto(
      `/koski/login/oppija/local?redirect=${encodeURIComponent(paluuosoite)}`
    )

    // Valpas kirjoittaa henkilötunnuksen #hetu-kenttään ja lähettää lomakkeen
    // Enterillä, ei kirjautumispainiketta klikkaamalla.
    const hetu = page.locator('#hetu')
    await expect(hetu).toBeVisible()
    await hetu.pressSequentially('220109-784L')
    await hetu.press('Enter')

    await expect(page).toHaveURL(paluuosoite)

    const user = await page.request.get('/koski/valpas/api/kansalainen/user')
    expect(user.status()).toBe(200)
    expect(await user.json()).toMatchObject({
      name: 'Kaisa Koululainen',
      kansalainen: true
    })

    expect(odottamattomatVirheet).toEqual([])
  })
})
