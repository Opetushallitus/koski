import Http from './http'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import {appendQueryParams} from './location'

let pageSize = 100
let pagerCache = {} // URL -> pages

export default (baseUrl, rowsLens = L.identity) => {
  let nextPageBus = Bacon.Bus()
  let cachedPages = pagerCache[baseUrl] || []
  pagerCache[baseUrl] = cachedPages

  let pageNumberP = Bacon.update(cachedPages.length, nextPageBus, (prev) => prev + 1)

  let pageDataE = pageNumberP.skip(cachedPages.length ? 1 : 0).flatMap((pageNumber) =>
    Http.get(appendQueryParams(baseUrl, {'pageNumber' : pageNumber, 'pageSize' : pageSize}))
  )

  let fetchingP = nextPageBus.awaiting(pageDataE)
  fetchingP.onValue()
  let pageResultE = pageDataE.map('.result')
  var mayHaveMore = cachedPages.length ? cachedPages[cachedPages.length - 1].mayHaveMore : false
  let concatPages = (previousData, newData) => {
    let previousRows = previousData == null ? [] : L.get(rowsLens, previousData)
    return L.modify(rowsLens, (newRows) => previousRows.concat(newRows), newData)
  }
  let initialRows = cachedPages.length ? cachedPages.map(page => page.result).reduce(concatPages) : null
  let rowsP = Bacon.update(initialRows,
    pageResultE, concatPages
  ).filter(Bacon._.id)

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
