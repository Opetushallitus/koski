import { test, expect } from "@playwright/test"

test("OAuth2 data access from KOSKI using Java sample works", async ({
  page,
}) => {
  await page.goto("http://localhost:7052/oauth2/authorization/koski")

  // Korhopankki login
  await page.waitForURL("**/koski/login/oppija/**")
  await page.getByTestId("hetu").fill("280618-402H")
  await expect(
    page.getByRole("button", { name: "Kirjaudu sisään" }),
  ).toBeEnabled()
  await page.getByRole("button", { name: "Kirjaudu sisään" }).click()

  // Auth page
  await page.waitForURL("**/koski/omadata-oauth2/authorize**")
  await expect(page.getByText("Henkilötunnus")).toBeVisible()
  await expect(page.getByText("Kaikki opiskeluoikeustiedot\n")).toBeVisible()
  await expect(page.getByLabel("Suostumuksen voimassaoloaika")).toBeVisible()

  // Approve authorization
  await page.getByRole("button", { name: "Hyväksy" }).click()

  // Check that oppija data containing ssn is displayed
  await page.waitForURL("**/api/openid-api-test/form-post-response-cb**")
  await expect(page.locator("html")).toContainText("280618-402H")
})
