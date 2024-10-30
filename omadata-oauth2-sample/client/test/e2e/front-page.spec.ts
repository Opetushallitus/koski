import { test, expect } from "@playwright/test"

test("Front page opens", async ({ page }) => {
  await page.goto("/")

  // Expect a title "to contain" a substring.
  await expect(page).toHaveTitle(/OmaDataOAuth2 Sample/)
})

test("Front page contains content from KOSKI", async ({ page }) => {
  await page.goto("/")
  await expect(page.getByRole("banner")).toContainText("SUCCESS")
  await expect(page.getByRole("banner")).toContainText("henkilö")
  await expect(page.getByRole("banner")).toContainText("opiskeluoikeudet")
})
