function OpinnotPage() {

  function oppija() { return S('.oppija') }
  function opiskeluoikeus() { return S('.opiskeluoikeus')}

  var api = {
    getTutkinto: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      var nth = S('.opiskeluoikeus > .suoritus > .kuvaus')[index]
      return S(nth).text()
    },
    getOppilaitos: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      return S(S('.opiskeluoikeus > h3 > .oppilaitos')[index]).text()
    },
    avaaOpintosuoritusote: function (index) {
      return function() {
        triggerEvent(S('.opiskeluoikeuksientiedot li:nth-child('+index+') a.opintosuoritusote'), 'click')
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
        triggerEvent(S('.opiskeluoikeustyypit .' + tyyppi + ' a'), 'click')
        return wait.forAjax()
      }
    },
    suoritus: function(name) {
      return Editor(function() { return S('.suoritus:contains("' + name + '")') })
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
    expand: function() {
      triggerEvent(elem().find('>.foldable.collapsed>.toggle-expand'), 'click')
    },
    edit: function() {
      triggerEvent(elem().find('.toggle-edit'), 'click')
    },
    isEditable: function() {
      return elem().find('.toggle-edit').is(':visible')
    },
    property: function(key) {
      return Property(function() {return elem().find('.property.'+key)})
    }
  }
}

function Property(elem) {
  return {
    addValue: function() {
      triggerEvent(elem().find('.add-value'), 'click')
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
      return elem().find('.value').text()
    }
  }
}