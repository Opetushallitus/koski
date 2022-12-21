import fecha from 'fecha'

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
  fecha.parse(dateStr, 'YYYY-MM-DDThh:mmZZ')

export const formatFinnishDateTime = (date: Date) =>
  format(date, 'D.M.YYYY H:mm')

export const ISO2FinnishDateTime = (dateStr: string) =>
  mapDate(parseISODateTime(dateStr), formatFinnishDateTime)

export const parseISODate = (dateStr: string) =>
  fecha.parse(dateStr, 'YYYY-MM-DD')

export const formatFinnishDate = (date: Date) => format(date, 'D.M.YYYY')

export const yearFromIsoDateString = (dateStr: string) =>
  mapDate(parseISODate(dateStr), (d) => d.getFullYear().toString()) || ''

export const ISO2FinnishDate = (dateStr?: string) =>
  dateStr && mapDate(parseISODate(dateStr), formatFinnishDate)

export const formatRange =
  (formatter: (s: string) => string | undefined) =>
  (from?: string, to?: string) => {
    const range = [from ? formatter(from) : '', to ? formatter(to) : '']
    return range[0] === range[1] ? range[0] : range.join(' – ').trim()
  }

export const formatDateRange = formatRange(ISO2FinnishDate)

export const formatYearRange = formatRange(yearFromIsoDateString)

const format = (date: Date | number, mask: string) => {
  try {
    return fecha.format(date, mask)
  } catch (e) {
    console.error('invalid date', date)
  }
}

const isDate = (date: Date | boolean | undefined): date is Date =>
  date !== undefined && typeof date !== 'boolean'

const mapDate = <T>(
  date: Date | boolean | undefined,
  fn: (d: Date) => T
): T | undefined => (isDate(date) ? fn(date) : undefined)

export const today = (): Date => new Date()

export const todayISODate = (): string => formatISODate(today())!
