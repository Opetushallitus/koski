import { ISODate } from "../state/common"
import { apiGet } from "./apiFetch"

export type FixtureState = {
  fixture: string
  tarkastelupäivä: ISODate
}

export const resetMockData = () =>
  apiGet<FixtureState>("valpas/test/reset-mock-data")

export const resetMockDataToDate = (tarkastelupäivä: string) => () =>
  apiGet<FixtureState>(
    "valpas/test/reset-mock-data/" + (tarkastelupäivä || "2021-09-01")
  )

export const clearMockData = () =>
  apiGet<FixtureState>("valpas/test/clear-mock-data")

export const getMockStatus = () =>
  apiGet<FixtureState>("valpas/test/current-mock-status")
