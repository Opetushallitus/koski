function OrganisaatioHaku(elem) {
  var api = {
    select: function (value) {

      if (value) {
        return this.enter(value)
          .then(function () {
            return triggerEvent(S('.organisaatio-popup a:contains(' + value + ')').get(0), 'click')
          })
          .then(wait.forAjax)
      } else {
        if (!window.callPhantom) { // workaround for focus glitch, when running in browser
          triggerEvent('.organisaatio-selection', 'click')
        }
        triggerEvent(S('.organisaatio-popup .kaikki'), 'click')
        return wait.forAjax()
      }
    },
    enter: function (value) {
      triggerEvent('.organisaatio-selection', 'click')
      if (!window.callPhantom) { // workaround for focus glitch, when running in browser
        triggerEvent('.organisaatio-selection', 'click')
      }

      return Page(elem).setInputValue(".organisaatio-popup input", value || "")().then(wait.forAjax)

    },
    oppilaitokset: function() {
      return textsOf(elem.find('.organisaatiot li'))
    }
  }
  return api
}

