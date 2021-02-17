import { logging, WebDriver } from "selenium-webdriver"

export const UNAUTHORIZED = "401 (Unauthorized)"
export const FORBIDDEN = "403 (Forbidden)"

const errorLogLevels = [logging.Level.SEVERE, logging.Level.WARNING]

type NetworkErrorCase = [string, string]
const globalAllowedNetworkErrors: NetworkErrorCase[] = [
  ["/valpas/api/user", UNAUTHORIZED],
  ["/valpas/api/user", FORBIDDEN],
]
let testCaseSpecificAllowedNetworkErrors: NetworkErrorCase[] = []

export function resetTestSpecificNetworkErrors() {
  testCaseSpecificAllowedNetworkErrors = []
}

export function allowNetworkError(pathSlice: string, messageSlice: string) {
  testCaseSpecificAllowedNetworkErrors.push([pathSlice, messageSlice])
}

export async function expectCleanConsoleLogs(driver: WebDriver) {
  const logs = await driver.manage().logs().get("browser")
  const errors = logs
    .filter(
      (entry) =>
        errorLogLevels.includes(entry.level) && !isAllowedError(entry.message)
    )
    .map((entry) => entry.message)

  expect(errors, "Expected no errors or warnings on console log").toEqual([])
}

function isAllowedError(message: string) {
  return [
    ...globalAllowedNetworkErrors,
    ...testCaseSpecificAllowedNetworkErrors,
  ].some(
    ([pathSlice, messageSlice]) =>
      message.split(" ")[0]?.includes(pathSlice) &&
      message.includes(messageSlice)
  )
}
