function PulssiPage() {

  var api = {
    openPage: function() {
      return openPage('/koski/pulssi', api.isVisible)()
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
        return elem.find('.metric-tiny, .metric-value').toArray().reduce((acc, e) => acc + parseFloat(S(e).text()), 0)
      }
    }
  }

  return api
}