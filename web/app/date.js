const finnishDateRE = /([0-3]?\d).([0-2]?\d).(\d\d\d\d)/
export const formatISODate = (date) => date.toISOString().substring(0, 10)
export const parseFinnishDate = (dateStr) => {
  let match = dateStr.match(finnishDateRE)
  if (match) {
    let year = parseInt(match[3], 10)
    let month = parseInt(match[2], 10) - 1
    let day = parseInt(match[1], 10)
    var date = new Date(Date.UTC(year, month, day))
    if (date && date.getDate() === day && date.getMonth() === month) {
      return date
    }
  }
}