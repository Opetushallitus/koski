function OrganisaatioHaku(elem) {
  var api = {
    select: function (value) {
      return function () {
        if (value) {
          if (elem().find('.organisaatio-selection').hasClass('disabled')) {
            // Single organization allowed
            if (
              !elem().find('.organisaatio-selection').text().startsWith(value)
            ) {
              throw new Error('Organisaatio ' + value + ' ei ole valittavissa')
            }
          } else {
            return api
              .enter(value)()
              .then(
                click(
                  findFirst(
                    '.organisaatio-popup a:contains(' + value + ')',
                    elem
                  )
                )
              )
              .then(wait.forAjax)
          }
        } else {
          api.open()
          return click(elem().find('.organisaatio-popup .kaikki'))()
        }
      }
    },
    enter: function (value) {
      return function () {
        api.open()
        return Page(elem())
          .setInputValue('.organisaatio-popup input', value || '')()
          .then(wait.forAjax)
      }
    },
    open: function () {
      click(elem().find('.organisaatio-selection'), 'click')()
      if (!elem().find('.organisaatio-popup').is(':visible')) {
        // workaround for focus glitch, when running in browser
        click(elem().find('.organisaatio-selection'))()
      }
    },
    oppilaitokset: function () {
      return textsOf(elem().find('.organisaatiot li'))
    },
    toimipisteet: function () {
      return textsOf(elem().find('.organisaatiot .aliorganisaatiot li'))
    },
    oppilaitos: function () {
      return elem().find('.organisaatio-selection').text()
    }
  }
  return api
}
