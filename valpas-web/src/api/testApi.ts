import { ISODate } from "../state/types"
import { apiGet } from "./apiFetch"

export type FixtureState = {
  fixture: string
  rajapäivät: Rajapäivät
}

export type Rajapäivät = {
  tarkasteluPäivä: ISODate
}

export const resetMockData = () =>
  apiGet<FixtureState>("valpas/test/reset-mock-data")

export const resetMockDataToDate = (tarkasteluPäivä: string) => () =>
  apiGet<FixtureState>("valpas/test/reset-mock-data/" + tarkasteluPäivä)

export const clearMockData = () =>
  apiGet<FixtureState>("valpas/test/clear-mock-data")

export const getMockStatus = () =>
  apiGet<FixtureState>("valpas/test/current-mock-status")
