import { expect, test } from "@playwright/test"

const koskiBackendHost = process.env.KOSKI_BACKEND_PORT
  ? `172.17.0.1:${process.env.KOSKI_BACKEND_PORT}`
  : "localhost:7021"

const login = async (page) => {
  // Korhopankki login
  await page.waitForURL("**/koski/login/oppija/**")
  // Testataan erillisellä hetulla jotta saadaan yksittäinen käyttölupa näkyviin
  await page.getByTestId("hetu").fill("010101-123N")
  await expect(
    page.getByRole("button", { name: "Kirjaudu sisään" }),
  ).toBeEnabled()
  await page.getByRole("button", { name: "Kirjaudu sisään" }).click()
}

test("OAuth consent revocation UI works", async ({ page }) => {
  await page.goto("http://localhost:7051/api/openid-api-test")

  await login(page)

  // Auth page
  await page.waitForURL("**/koski/omadata-oauth2/authorize**")

  // Approve authorization
  await page.getByRole("button", { name: "Hyväksy" }).click()
  await page.goto(`http://${koskiBackendHost}/koski/omadata/kayttooikeudet`)
  await login(page)
  await expect(page.locator("ul.kayttolupa-list > li")).toHaveCount(1)
  await expect(page.locator("ul.kayttolupa-list > li")).toContainText(
    "omadataoauth2sample",
  )
  // Revoke authorization (consent)
  await page.locator("ul.kayttolupa-list > li").getByRole("button").click()
  await page.getByTestId("dialog-vahvista").click()

  await expect(
    page.locator("ul.kayttolupa-list > li.no-permission"),
  ).toHaveCount(1)
  await page.reload()
  await expect(
    page.locator("ul.kayttolupa-list > li.no-permission"),
  ).toHaveCount(1)
})
