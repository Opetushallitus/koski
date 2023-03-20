import {
  findSingle,
  isElementVisible,
  openPage,
  S,
  wait
} from '../util/testHelpers.js'

export function PulssiPage() {
  const api = {
    openPage: function () {
      return openPage('/koski/pulssi', api.isVisible)().then(wait.forAjax)
    },
    isVisible: function () {
      return isElementVisible(S('#content h1'))
    },
    metric: function (name, elemType) {
      return Metric(findSingle((elemType || 'div') + '.' + name))
    }
  }

  function Metric(elem) {
    return {
      value: function () {
        return parseFloat(elem().find('.metric-large, .metric-medium').text())
      },
      sum: function () {
        let sum = 0
        let metrics = elem().find('.metric-tiny, .metric-value').toArray()
        metrics.forEach(function (metric) {
          sum = sum + parseFloat(S(metric).text())
        })
        return sum
      }
    }
  }

  return api
}
