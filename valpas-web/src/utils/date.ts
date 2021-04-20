import { format, getYear, parseISO } from "date-fns"
import { ISODate } from "../state/common"

export const DATE_FORMAT = "d.M.yyyy"

export const formatDate = (date: ISODate): string =>
  format(parseISO(date), DATE_FORMAT)

export const formatNullableDate = (date?: ISODate): string =>
  date ? formatDate(date) : "–"

export const currentYear = () => getYear(new Date())

export const parseYear = (date: ISODate): number => getYear(parseISO(date))
