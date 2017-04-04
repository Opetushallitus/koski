function OrganisaatioHaku(elem) {
  var api = {
    select: function (value) {
      return function() {
        if (value) {
          return api.enter(value)()
            .then(function () {
              return triggerEvent(elem().find('.organisaatio-popup a:contains(' + value + ')').get(0), 'click')
            })
            .then(wait.forAjax)
        } else {
          api.open()
          triggerEvent(elem().find('.organisaatio-popup .kaikki'), 'click')
          return wait.forAjax()
        }
      }
    },
    enter: function (value) {
      return function() {
        api.open()
        return Page(elem()).setInputValue(".organisaatio-popup input", value || "")().then(wait.forAjax)
      }
    },
    open: function() {
      triggerEvent(elem().find('.organisaatio-selection'), 'click')
      if (!elem().find('.organisaatio-popup').is(':visible')) { // workaround for focus glitch, when running in browser
        triggerEvent(elem().find('.organisaatio-selection'), 'click')
      }
    },
    oppilaitokset: function() {
      return textsOf(elem().find('.organisaatiot li'))
    },
    oppilaitos: function() {
      return elem().find('.organisaatio-selection').text()
    }
  }
  return api
}

