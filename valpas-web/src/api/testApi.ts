import { ISODate } from "../state/common"
import { queryPath } from "../state/paths"
import { apiGet } from "./apiFetch"

export type FixtureState = {
  fixture: string
  tarkastelupäivä: ISODate
}

export const resetMockData = () =>
  apiGet<FixtureState>("valpas/test/reset-mock-data")

export const resetMockDataToDate =
  (tarkastelupäivä: string, force: boolean) => () =>
    apiGet<FixtureState>(
      queryPath(
        "valpas/test/reset-mock-data/" + (tarkastelupäivä || "2021-09-01"),
        {
          force: force ? "true" : null,
        }
      )
    )

export const clearMockData = () =>
  apiGet<FixtureState>("valpas/test/clear-mock-data")

export const getMockStatus = () =>
  apiGet<FixtureState>("valpas/test/current-mock-status")

export const loadRaportointikanta = () =>
  apiGet<{}>("valpas/test/load-raportointikanta")
