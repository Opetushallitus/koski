# E2E-testien kirjoittaminen Playwright-kirjastolla

## Virkailijan ja kansalaisen istunnot

Kosken Playwright-toteutukseen on rakennettu autentikaatiotoiminnallisuus, joka toimii "lazy load"-tapaisesti. Kun testeissä halutaan kirjautua jollain tunnuksella, luodaan tunnukselle istunto. Kyseinen istunto tallennetaan lokaalisti talteen, jolloin sitä voidaan uudelleenkäyttää useammassa testissä. Tämä myös nopeuttaa testiajoja.

Käytössä olevat virkailijakäyttäjät ovat nähtävissä tiedostossa `test/e2e/setup/users.json` tai TypeScript:n Intellisensen automaattisesti ehdottamissa `virkailija()`-funktion ensimmäisessä parametrissa.

### Virkailija
```typescript
test.use({ storageState: virkailija("kalle")})
test("Hello world", async ({page}) => {
    await page.goto("/")
    await expect(page).toHaveURL(/\//)
})
```
### Kansalainen

```typescript
test.use({ storageState: kansalainen("HETI")})
test("Hello world", async () => {
    await page.goto("/")
    await expect(page).toHaveURL(/\//)
})
```

## Testidatan nollaus

### Ennen kaikkia testejä

```typescript
test.beforeAll(async ({ browser }, testInfo) => {
  const virkailijaSessionPath = await getVirkailijaSession(
    testInfo,
    'kalle',
    'kalle'
  )
  const ctx = await browser.newContext({
    storageState: virkailijaSessionPath
  })
  const page = await ctx.newPage()
  await new KoskiFixtures(page).reset()
})
```
### Jokaisen testin aikana
```typescript
// HUOM. Virkailija-istunnon tulee olla voimassa ennen tätä
test.beforeEach(async ({ fixtures }, testInfo) => {
  await fixtures.reset()
})
```
