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
        var tab = findSingle('.suoritus-tabs li:contains(' + nimi + ')')
        if (!tab.hasClass('selected')) {
          triggerEvent(findSingle('a', tab), 'click')
        }
      }
    },
    suoritusTabs: function() {
      return textsOf(S('.suoritus-tabs > li:not(.add-suoritus)'))
    },
    onTallennettavissa: function() {
      return S('.toggle-edit.editing').is(':visible')
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
    avaaLisaysDialogi: function() {
      if (!S('.lisaa-opiskeluoikeusjakso-modal .modal-content').is(':visible')) {
        triggerEvent(S('.opiskeluoikeuden-tiedot .add-item a'), 'click')
        return wait.forAjax()
      }
    },
    valitseOpiskeluoikeudenTyyppi: function(tyyppi) {
      return function() {
        triggerEvent(findSingle('.opiskeluoikeustyypit .' + tyyppi + ' a'), 'click')
        return wait.forAjax()
      }
    },
    opiskeluoikeusEditor: function() {
      return Editor(function() { return findSingle('.opiskeluoikeus-content') })
    },
    lisääSuoritusVisible: function() {
      return S(".add-suoritus").is(":visible")
    },
    lisääSuoritus: function() {
      triggerEvent(S(".add-suoritus a"), 'click')
      return wait.forAjax()
    },
    lisääSuoritusDialog: function() {
      return LisääSuoritusDialog()
    },
    anythingEditable: function() {
      return Editor(function() { return findSingle('.content-area') } ).isEditable()
    },
    expandAll: function() {
      var checkAndExpand = function() {
        if (expanders().is(':visible')) {
          triggerEvent(expanders(), 'click')
          return wait.forMilliseconds(10)().then(wait.forAjax).then(checkAndExpand)
        }
      }
      return checkAndExpand()
      function expanders() {
        return S('.foldable.collapsed>.toggle-expand:not(.disabled), tbody:not(.expanded) > tr > td > .toggle-expand:not(.disabled), a.expandable:not(.open)')
      }
    },
    collapseAll: function() {
      var checkAndCollapse = function() {
        if (collapsers().is(':visible')) {
          triggerEvent(collapsers(), 'click')
          return wait.forMilliseconds(10)().then(wait.forAjax).then(checkAndCollapse)
        }
      }
      return checkAndCollapse()
      function collapsers() {
        return S('.foldable:not(.collapsed)>.toggle-expand:not(.disabled), tbody.expanded .toggle-expand:not(.disabled), a.expandable.open')
      }
    }
  }

  return api
}

function LisääSuoritusDialog() {
  function elem() { return findSingle('.lisaa-suoritus-modal')}
  function buttonElem() { return findSingle('button', elem())}
  var api = _.merge({
    isEnabled: function() {
      return !buttonElem().is(':disabled')
    },
    lisääSuoritus: function() {
      triggerEvent(buttonElem(), 'click')
      return wait.forAjax()
    },
    toimipiste: OrganisaatioHaku(elem)
  }, {}, Editor(elem))
  return api
}

function Päivämääräväli(elem) {
  var api = {
    setAlku: function(value) {
      return function() {
        return Page(elem).setInputValue('.calendar-input:nth-child(1) input', value)()
      }
    },
    getAlku: function() {
      return elem().find('span.inline.date:nth-child(1)').text()
    },
    setLoppu: function(value) {
      return function() {
        return Page(elem).setInputValue('.calendar-input:nth-child(2) input', value)()
      }
    },
    isValid: function() {
      return !elem().find('.date-range').hasClass('error')
    }
  }
  return api
}

function OpiskeluoikeusDialog() {
  return {
    tila: function() {
      return Property(function() {return S('.lisaa-opiskeluoikeusjakso-modal')})
    },
    alkuPaiva: function() {
      return Property(function() {return S('.property.alku')})
    },
    tallenna: function() {
      triggerEvent(findSingle('button.opiskeluoikeuden-tila'), 'click')
      return wait.forAjax()
    },
    isEnabled: function() {
      return !findSingle('button.opiskeluoikeuden-tila').is(':disabled')
    },
    radioEnabled: function(value) {
      return !findSingle('input[value="' + value + '"]').is(':disabled')
    }
  }
}

function Editor(elem) {
  return {
    edit: function() {
      var editLink = findSingle('.toggle-edit', elem())
      if (!editLink.hasClass('editing'))
        triggerEvent(editLink, 'click')
      return KoskiPage().verifyNoError()
    },
    doneEditing: function() {
      triggerEvent(findSingle('.toggle-edit.editing', elem()), 'click')
      return KoskiPage().verifyNoError()
    },
    isEditable: function() {
      return elem().find('.toggle-edit').is(':visible')
    },
    property: function(key) {
      return Property(function() {return findSingle('.property.'+key+':eq(0)', elem())})
    },
    propertyBySelector: function(selector) {
      return Property(function() {return findSingle(selector, elem())})
    },
    subEditor: function(selector) {
      return Editor(function() { return findSingle(selector, elem()) })
    }
  }
}

function Property(elem) {
  return _.merge({
    addValue: function() {
      triggerEvent(findSingle('.add-value', elem()), 'click')
      return KoskiPage().verifyNoError()
    },
    isRemoveValueVisible: function() {
      return elem().find('.remove-value').is(':visible')
    },
    addItem: function() {
      var link = findSingle('.add-item a', elem())
      triggerEvent(link, 'click')
      return KoskiPage().verifyNoError()
    },
    removeValue: function() {
      triggerEvent(findSingle('.remove-value', elem()), 'click')
      return KoskiPage().verifyNoError()
    },
    removeItem: function(index) {
      return function() {
        triggerEvent(findSingle('li:eq(' + index + ') .remove-item', elem()), 'click')
        return KoskiPage().verifyNoError()
      }
    },
    waitUntilLoaded: function() {
      return wait.until(function(){
        return elem().is(':visible') && !elem().find('.loading').is(':visible')
      })()
    },
    selectValue: function(value) {
      return function() {
        return Page(elem).setInputValue('.dropdown', value)().then(wait.forAjax)
      }
    },
    setValue: function(value) {
      return function() {
        return Page(elem).setInputValue('.dropdown, .editor-input', value)()
      }
    },
    toPäivämääräväli: function() {
      return Päivämääräväli(elem)
    },
    click: function(selector) {
      return function() {
        triggerEvent(findSingle(selector, elem()), 'click')
        return KoskiPage().verifyNoError()
      }
    },
    getValue: function() {
      return elem().find('input').val() || elem().find('.value').text()
    },
    getText: function() {
      return extractAsText(elem())
    },
    itemEditor: function(index) {
      return this.propertyBySelector('.array > li:nth-child(' + (index + 1) +')')
    },
    getItems: function() {
      return toArray(elem().find('.value .array > li:not(.add-item)')).map(function(elem) { return Property(function() { return S(elem) })})
    },
    isVisible: function() {
      return isVisibleBy(function() { return findSingle('.value', elem())})
    },
    isValid: function() {
      return !elem().find('.error').is(':visible')
    }
  }, Editor(elem))
}