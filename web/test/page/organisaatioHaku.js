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
        this.open()
        triggerEvent(S('.organisaatio-popup .kaikki'), 'click')
        return wait.forAjax()
      }
    },
    enter: function (value) {
      this.open()
      return Page(elem).setInputValue(".organisaatio-popup input", value || "")().then(wait.forAjax)
    },
    open: function() {
      triggerEvent('.organisaatio-selection', 'click')
      if (!S('.organisaatio-popup').is(':visible')) { // workaround for focus glitch, when running in browser
        triggerEvent('.organisaatio-selection', 'click')
      }
    },
    oppilaitokset: function() {
      return textsOf(elem.find('.organisaatiot li'))
    },
    oppilaitos: function() {
      return elem.find('.organisaatio-selection').text()
    }
  }
  return api
}

