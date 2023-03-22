import {
  findSingle,
  isElementVisible,
  openPage,
  S
} from '../util/testHelpers.js'

export function RaporttiPage() {
  const api = {
    openPage: function (predicate) {
      return function () {
        return openPage('/koski/pulssi/raportti', predicate)()
      }
    },
    isVisible: function () {
      return isElementVisible(S('#raportti'))
    },
    metric: function (name) {
      return Metric(findSingle('li.' + name))
    }
  }

  function Metric(elem) {
    return {
      value: function () {
        return parseFloat(elem().find('.value').text())
      }
    }
  }

  return api
}
