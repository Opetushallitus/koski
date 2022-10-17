import fecha from 'fecha'

const finnishDateRE = /([0-3]?\d)\.([0-2]?\d)\.(\d\d\d\d)/
export const formatISODate = (date) => format(date, 'YYYY-MM-DD')
export const parseFinnishDate = (dateStr) => {
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
export const parseISODateTime = (date) =>
  fecha.parse(date, 'YYYY-MM-DDThh:mmZZ')
export const formatFinnishDateTime = (date) => format(date, 'D.M.YYYY H:mm')
export const ISO2FinnishDateTime = (date) =>
  formatFinnishDateTime(parseISODateTime(date))
export const parseISODate = (date) => fecha.parse(date, 'YYYY-MM-DD')
export const formatFinnishDate = (date) => format(date, 'D.M.YYYY')
export const yearFromIsoDateString = (dateString) => {
  const date = parseISODate(dateString)
  return (date && date.getFullYear()) || ''
}

export const ISO2FinnishDate = (date) =>
  date && formatFinnishDate(parseISODate(date))

const format = (date, f) => {
  try {
    return fecha.format(date, f)
  } catch (e) {
    console.error('invalid date', date)
  }
}
