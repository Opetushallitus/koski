import { logging, WebDriver } from "selenium-webdriver"

export const UNAUTHORIZED = "401 (Unauthorized)"
export const FORBIDDEN = "403 (Forbidden)"
export const BAD_REQUEST = "400 (Bad Request)"

const errorLogLevels = [logging.Level.SEVERE, logging.Level.WARNING]

type NetworkErrorCase = [string, string]
const globalAllowedNetworkErrors: NetworkErrorCase[] = [
  // Virkailijanäkymä:
  ["/valpas/api/user", UNAUTHORIZED],
  ["/valpas/api/user", FORBIDDEN],
  // Kansalaisen näkymä:
  ["/valpas/api/kansalainen/user", UNAUTHORIZED],
  ["/valpas/api/kansalainen/user", FORBIDDEN],
  ["/koski/user", UNAUTHORIZED],
  [
    // Chrome 89.0 deprecation message due React
    // Fixed in PR https://github.com/facebook/react/pull/20831
    // Waiting for new React release
    "",
    "SharedArrayBuffer will require cross-origin isolation as of M91, around May 2021.",
  ],
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
