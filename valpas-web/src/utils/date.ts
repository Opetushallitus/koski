import { format, formatISO, getYear, parseISO } from "date-fns"
import { ISODate } from "../state/common"
import { nonNull } from "./arrays"

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

export const currentYear = () => getYear(new Date())

export const parseYear = (date: ISODate): number => getYear(parseISO(date))

export const today = (): ISODate =>
  formatISO(new Date(), { representation: "date" })
