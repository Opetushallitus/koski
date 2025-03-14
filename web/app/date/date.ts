import fecha from 'fecha'
import { pipe } from 'fp-ts/lib/function'

const toUndefined =
  <T extends any[]>(f: (...as: T) => Date | null | boolean) =>
  (...as: T): Date | undefined => {
    const date = f(...as)
    return isDate(date) ? date : undefined
  }

const finnishDateRE = /([0-3]?\d)\.([0-2]?\d)\.(\d\d\d\d)/
export const formatISODate = (date: Date) => format(date, 'YYYY-MM-DD')
export const parseFinnishDate = (dateStr: string) => {
  const match = dateStr.match(finnishDateRE)
  if (match) {
    const year = parseInt(match[3], 10)
    const month = parseInt(match[2], 10) - 1
    const day = parseInt(match[1], 10)
    const date = new Date(year, month, day)
    if (date && date.getDate() === day && date.getMonth() === month) {
      return date
    }
  }
}
export const parseISODateTime = (dateStr: string) =>
  fecha.parse(dateStr, 'YYYY-MM-DDTHH:mmZZ') ||
  fecha.parse(dateStr, 'YYYY-MM-DDTHH:mm:ss.SSS')

export const formatFinnishDateTime = (date: Date) =>
  format(date, 'D.M.YYYY H:mm')

export const ISO2FinnishDateTime = (dateStr: string) =>
  mapDate(parseISODateTime(dateStr), formatFinnishDateTime)

export const parseISODate = (dateStr: string): Date | null =>
  fecha.parse(dateStr, 'YYYY-MM-DD')

export const parseISODateNullable = toUndefined(parseISODate)

export const formatFinnishDate = (date: Date) => format(date, 'D.M.YYYY')

export const yearFromIsoDateString = (dateStr: string) =>
  mapDate(parseISODate(dateStr), (d) => d.getFullYear().toString()) || ''

export const ISO2FinnishDate = (dateStr?: string) =>
  dateStr && mapDate(parseISODate(dateStr), formatFinnishDate)

export const formatRange =
  (formatter: (s: string) => string | undefined) =>
  (from?: string, to?: string) => {
    const range = [from ? formatter(from) : '', to ? formatter(to) : '']
    return range.join(' – ').trim()
  }

export const formatDateRange = formatRange(ISO2FinnishDate)

export const formatYearRange = formatRange(yearFromIsoDateString)

const format = (date: Date | number, mask: string) => {
  try {
    return fecha.format(ensureDate(date), mask)
  } catch (e) {
    console.error('invalid date', date)
  }
}

const isDate = (date: Date | boolean | null | undefined): date is Date =>
  date instanceof Date

const mapDate = <T>(
  date: Date | boolean | null | undefined,
  fn: (d: Date) => T
): T | undefined => (isDate(date) ? fn(date) : undefined)

export const today = (): Date => new Date()

export const todayISODate = (): string => formatISODate(today()) || ''

export const addDays =
  (days: number) =>
  (date: Date | null | boolean): Date | undefined =>
    mapDate(date, (d) => {
      d.setDate(d.getDate() + days)
      return d
    })

export const addDaysISO =
  (days: number) =>
  (dateStr?: string): string | undefined =>
    (dateStr &&
      pipe(
        dateStr,
        parseISODate,
        addDays(days),
        (date) => date && formatISODate(date)
      )) ||
    undefined

    export const ensureDate = (d: Date | number): Date => d instanceof Date ? d : new Date(d)