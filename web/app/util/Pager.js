import Http from './http'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import { appendQueryParams } from './location'

const defaultPageSize = 100
const pagerCache = {} // URL -> pages

export default (baseUrl, rowsLens = L.identity, pageSize = defaultPageSize) => {
  const nextPageBus = Bacon.Bus()
  const cachedPages = pagerCache[baseUrl] || []
  pagerCache[baseUrl] = cachedPages

  const pageNumberP = Bacon.update(
    cachedPages.length,
    nextPageBus,
    (prev) => prev + 1
  )

  const pageDataE = pageNumberP
    .skip(cachedPages.length ? 1 : 0)
    .flatMap((pageNumber) =>
      Http.get(
        appendQueryParams(baseUrl, {
          pageNumber,
          pageSize
        })
      )
    )

  const fetchingP = nextPageBus.awaiting(pageDataE)
  fetchingP.onValue()
  const pageResultE = pageDataE.map('.result')
  let mayHaveMore = cachedPages.length
    ? cachedPages[cachedPages.length - 1].mayHaveMore
    : false
  const concatPages = (previousData, newData) => {
    const previousRows = !previousData ? [] : L.get(rowsLens, previousData)
    return L.modify(
      rowsLens,
      (newRows) => previousRows.concat(newRows),
      newData
    )
  }
  const initialRows = cachedPages.length
    ? cachedPages.map((page) => page.result).reduce(concatPages)
    : null
  const rowsP = Bacon.update(initialRows, pageResultE, concatPages).filter(
    Bacon._.id
  )

  pageDataE.onValue((page) => {
    mayHaveMore = page.mayHaveMore
    cachedPages.push(page)
  })

  rowsP.onValue()

  return {
    rowsP,
    mayHaveMore: () => mayHaveMore,
    next: () =>
      fetchingP
        .take(1)
        .filter((fetching) => !fetching)
        .onValue(() => nextPageBus.push())
  }
}
