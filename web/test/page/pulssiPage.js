function PulssiPage() {

  var api = {
    openPage: function() {
      return openPage('/koski/pulssi', api.isVisible)().then(wait.forAjax)
    },
    isVisible: function() {
      return isElementVisible(S('#content h1'))
    },
    metric: function(name) {
      return Metric(findSingle('section.' + name))
    }
  }

  function Metric(elem) {
    return {
      value: function() {
        return parseFloat(elem.find('.metric-large, .metric-medium').text())
      },
      sum: function() {
        var sum = 0
        var metrics = elem.find('.metric-tiny, .metric-value').toArray()
        metrics.forEach(function(metric) {
          sum = sum + parseFloat(S(metric).text())
        })
        return sum
      }
    }
  }

  return api
}