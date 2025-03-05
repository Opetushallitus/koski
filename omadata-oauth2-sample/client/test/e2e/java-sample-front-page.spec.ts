import { test, expect } from "@playwright/test"

test("Java sample front page opens", async ({ page }) => {
  await page.goto("http://localhost:7052/")

  await expect(
    page.getByText("You have NOT authorized use of your data."),
  ).toBeVisible()
})
