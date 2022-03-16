import { format, formatISO, getYear, parseISO } from "date-fns"
import { ISODate } from "../state/common"
import { getSearchQueryMap } from "../state/searchQuery"
import { nonNull } from "./arrays"
import { runningLocally } from "./environment"

export const DATE_FORMAT = "d.M.yyyy"

export const formatDate = (date: ISODate): string =>
  format(parseISO(date), DATE_FORMAT)

export const formatNullableDate = (date?: ISODate): string =>
  date ? formatDate(date) : "–"

export const formatDateRange = (fromDate?: ISODate, toDate?: ISODate) =>
  [
    fromDate ? formatDate(fromDate) : null,
    "–",
    toDate ? formatDate(toDate) : null,
  ]
    .filter(nonNull)
    .join("")

export const currentYear = () => getYear(dateToday())

export const parseYear = (date: ISODate): number => getYear(parseISO(date))

export const today = (): ISODate =>
  formatISO(dateToday(), { representation: "date" })

export const dateToday = () => {
  if (runningLocally()) {
    const date = getSearchQueryMap().date
    if (date) {
      return new Date(parseISO(date))
    }
  }
  return new Date()
}
