import { test, expect } from "@playwright/test"

test("Healthcheck test", async ({ page }) => {
  await page.goto("http://localhost:7051/api/healthcheck")
  await page.waitForURL("**/api/healthcheck**")
  await expect(page.getByText("Ok")).toBeVisible()
})

test("OAuth2 data access from KOSKI using openid-client works", async ({
  page,
}) => {
  await page.goto("http://localhost:7051/api/openid-api-test")

  // Korhopankki login
  await page.waitForURL("**/koski/login/oppija/**")
  await page.getByTestId("hetu").fill("280618-402H")
  await expect(
    page.getByRole("button", { name: "Kirjaudu sisään" }),
  ).toBeEnabled()
  await page.getByRole("button", { name: "Kirjaudu sisään" }).click()

  // Auth page
  await page.waitForURL("**/koski/omadata-oauth2/authorize**")
  await expect(page.getByText("Nimi")).toBeVisible()
  await expect(page.getByText("Henkilötunnus")).toBeVisible()
  await expect(page.getByText("Suoritetut tutkinnot")).toBeVisible()
  await expect(page.getByText("Suostumuksesi pää")).toBeVisible()
  await expect(page.getByLabel("Suostumuksen voimassaoloaika")).toBeVisible()

  // Approve authorization
  await page.getByRole("button", { name: "Hyväksy" }).click()

  // Check that oppija data containing ssn is displayed
  await page.waitForURL("**/api/openid-api-test/form-post-response-cb**")
  await expect(page.locator("html")).toContainText("280618-402H")
})

test("Standard error from server is displayed when user declines", async ({
  page,
}) => {
  await page.goto("http://localhost:7051/api/openid-api-test")

  // Korhopankki login
  await page.waitForURL("**/koski/login/oppija/**")
  await page.getByTestId("hetu").fill("280618-402H")
  await expect(
    page.getByRole("button", { name: "Kirjaudu sisään" }),
  ).toBeEnabled()
  await page.getByRole("button", { name: "Kirjaudu sisään" }).click()

  // Auth page
  await page.waitForURL("**/koski/omadata-oauth2/authorize**")

  // Decline authorization
  await expect(
    page.getByRole("button", { name: "Peruuta ja palaa" }),
  ).toBeVisible()
  await page.getByRole("button", { name: "Peruuta ja palaa" }).click()

  // Check that error data is displayed
  await page.waitForURL("**/api/openid-api-test/form-post-response-cb**")
  await expect(page.locator("html")).toContainText('"error":"access_denied"')
})

test("Error page displayed in browser when using invalid redirect_uri", async ({
  page,
}) => {
  await page.goto(
    "http://localhost:7051/api/openid-api-test/invalid-redirect-uri",
  )
  await expect(page.getByTestId("error")).toBeVisible()
  await expect(page.locator("#error")).toContainText("invalid_client_data")
  await expect(page.getByLabel("Tapahtui virhe:")).toContainText(
    "omadataoauth2-error-",
  )
  await expect(page.getByLabel("Tapahtui virhe:")).toContainText(
    "invalid_client_data",
  )
  await page.getByText("✕").click()
  await expect(page.getByTestId("error")).toBeHidden()
})
