import { test, expect } from "@playwright/test"

test("Front page opens", async ({ page }) => {
  await page.goto("/")

  // Expect a title "to contain" a substring.
  await expect(page).toHaveTitle(/OmaDataOAuth2 Sample/)
})
