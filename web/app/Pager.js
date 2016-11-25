import Http from './http'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import { appendQueryParams } from './location'

export default (baseUrl, rowsLens = L.identity) => {
  let nextPageBus = Bacon.Bus()
  let pageSize = 100
  let pageNumberP = Bacon.update(0, nextPageBus, (prev) => prev + 1)

  let pageDataE = pageNumberP.flatMap((pageNumber) => Http.get(appendQueryParams(baseUrl, {'pageNumber' : pageNumber, 'pageSize' : pageSize})))
  let fetchingP = nextPageBus.awaiting(pageDataE)
  fetchingP.onValue()
  let pageResultE = pageDataE.map('.result')
  var mayHaveMore = false

  let rowsP = Bacon.update(null,
    pageResultE, (previousData, newData) => {
      let previousRows = L.get(L.compose(rowsLens, L.defaults([])), previousData)
      return L.modify(rowsLens, (newRows) => previousRows.concat(newRows), newData)
    }
  ).skip(1)

  pageDataE.onValue((d) => mayHaveMore = d.mayHaveMore)

  rowsP.onValue()

  return {
    rowsP,
    mayHaveMore: () => mayHaveMore,
    next: () => fetchingP.take(1).filter((fetching) => !fetching).onValue(() => nextPageBus.push())
  }
}
