import { format, getYear, parseISO } from "date-fns"
import { ISODate } from "../state/types"

export const formatDate = (date: ISODate): string =>
  format(parseISO(date), "d.M.yyyy")

export const currentYear = () => getYear(new Date())

export const parseYear = (date: ISODate): number => getYear(parseISO(date))
