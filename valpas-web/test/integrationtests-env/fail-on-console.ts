import { WebDriver, logging } from "selenium-webdriver"

const unexpectedLogLevels = [logging.Level.SEVERE, logging.Level.WARNING]
const expectNetworkErrorsFroms = ["/valpas/api/user"]

export async function expectCleanConsoleLogs(driver: WebDriver) {
  const logs = await driver.manage().logs().get("browser")
  const errors = logs
    .filter(
      (entry) =>
        unexpectedLogLevels.includes(entry.level) &&
        isUnexpectedError(entry.message)
    )
    .map((entry) => entry.message)

  expect(errors, "Expected no errors or warnings on console log").toEqual([])
}

function isUnexpectedError(message: string) {
  return !expectNetworkErrorsFroms.some((path) =>
    message.split(" ")[0]?.includes(path)
  )
}
