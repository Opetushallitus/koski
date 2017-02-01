function OpinnotPage() {

  function oppija() { return findSingle('.oppija') }
  function opiskeluoikeus() { return findSingle('.opiskeluoikeus')}

  var api = {
    getTutkinto: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      var nth = S('.opiskeluoikeus .suoritus .property.koulutusmoduuli .koulutusmoduuli .tunniste')[index]
      return S(nth).text()
    },
    getOppilaitos: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      return S(S('.opiskeluoikeus > h3 > .oppilaitos')[index]).text().slice(0, -1)
    },
    valitseSuoritus: function(nimi) {
      return function() {
        triggerEvent(findSingle('.suoritus-tabs li:contains(' + nimi + ') a'), 'click')
      }
    },
    avaaOpintosuoritusote: function (index) {
      return function() {
        triggerEvent(findSingle('.opiskeluoikeuksientiedot li:nth-child('+index+') a.opintosuoritusote'), 'click')
        return wait.until(OpintosuoritusotePage().isVisible)()
      }
    },
    avaaTodistus: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      return function() {
        triggerEvent(S(S('a.todistus')[index]), 'click')
        return wait.until(TodistusPage().isVisible)()
      }
    },
    valitseOpiskeluoikeudenTyyppi: function(tyyppi) {
      return function() {
        triggerEvent(findSingle('.opiskeluoikeustyypit .' + tyyppi + ' a'), 'click')
        return wait.forAjax()
      }
    },
    suoritusEditor: function() {
      return Editor(function() { return findSingle('.suoritus') })
    },
    anythingEditable: function() {
      return Editor(function() { return findSingle('.content-area') } ).isEditable()
    },
    expandAll: function() {
      var checkAndExpand = function() {
        if (expanders().is(':visible')) {
          triggerEvent(expanders(), 'click')
          return wait.forMilliseconds(10)().then(checkAndExpand)
        }
      }
      return checkAndExpand()
      function expanders() {
        return S('.foldable.collapsed>.toggle-expand')
      }
    }
  }

  return api
}

function Editor(elem) {
  return {
    edit: function() {
      triggerEvent(findSingle('.toggle-edit', elem()), 'click')
    },
    isEditable: function() {
      return elem().find('.toggle-edit').is(':visible')
    },
    property: function(key) {
      return Property(function() {return findSingle('.property.'+key, elem())})
    }
  }
}

function Property(elem) {
  return {
    addValue: function() {
      triggerEvent(findSingle('.add-value', elem()), 'click')
    },
    waitUntilLoaded: function() {
      return wait.until(function(){
        return elem().is(':visible') && !elem().find('.loading').is(':visible')
      })()
    },
    setValue: function(value) {
      return function() {
        return Page(elem).setInputValue("select", value)()
      }
    },
    getValue: function() {
      return findSingle('.value', elem()).text()
    }
  }
}