import path from 'node:path'
import { expect, Page } from '@playwright/test'
import { replaceScreenshotData } from './screenshotDataReplacements'

const screenshotStylePath = path.join(__dirname, 'fullPageScreenshot.css')

export const takeFullPageScreenshot = async (
  page: Page,
  name: string
): Promise<void> => {
  const { restore } = await replaceScreenshotData(page)

  try {
    await page.evaluate(
      () =>
        new Promise<void>((resolve) =>
          requestAnimationFrame(() => requestAnimationFrame(() => resolve()))
        )
    )

    await expect(page).toHaveScreenshot(name, {
      fullPage: true,
      stylePath: screenshotStylePath,
      timeout: 15_000
    })
  } finally {
    await restore()
  }
}
