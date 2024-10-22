import { test, expect } from "@playwright/test"

test.use({
  ignoreHTTPSErrors: true,
})

test("Luovutuspalvelu healthcheck page opens", async ({ page }) => {
  await page.goto(
    "https://localhost:7022/koski-luovutuspalvelu/healthcheck/proxy",
  )
  await expect(page.getByText("ok")).toBeVisible()
})
