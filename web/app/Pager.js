import Http from './http'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import { appendQueryParams } from './location'

let pageSize = 100
let pagerCache = {} // URL -> pages

export default (baseUrl, rowsLens = L.identity) => {
  let nextPageBus = Bacon.Bus()
  let cachedPages = pagerCache[baseUrl] || []
  pagerCache[baseUrl] = cachedPages

  let pageNumberP = Bacon.update(cachedPages.length, nextPageBus, (prev) => prev + 1)

  let pageDataE = pageNumberP.flatMap((pageNumber) => Http.get(appendQueryParams(baseUrl, {'pageNumber' : pageNumber, 'pageSize' : pageSize}))).skip(cachedPages.length ? 1 : 0)

  let fetchingP = nextPageBus.awaiting(pageDataE)
  fetchingP.onValue()
  let pageResultE = pageDataE.map('.result')
  var mayHaveMore = cachedPages.length ? cachedPages[cachedPages.length - 1].mayHaveMore : false
  let initialRows = cachedPages.length ? cachedPages.flatMap((page) => L.get(rowsLens, page).result) : null
  let rowsP = Bacon.update(initialRows,
    pageResultE, (previousData, newData) => {
      let previousRows = previousData == null ? [] : L.get(rowsLens, previousData)
      return L.modify(rowsLens, (newRows) => previousRows.concat(newRows), newData)
    }
  ).filter(Bacon._.id)

  // TODO: error handling

  pageDataE.onValue((page) => {
    mayHaveMore = page.mayHaveMore
    cachedPages.push(page)
  })

  rowsP.onValue()

  return {
    rowsP,
    mayHaveMore: () => mayHaveMore,
    next: () => fetchingP.take(1).filter((fetching) => !fetching).onValue(() => nextPageBus.push())
  }
}
