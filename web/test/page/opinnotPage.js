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
    valitseSuoritus: function(opiskeluoikeusIndex, nimi) {
      return function() {
        var tab = findSingle('.opiskeluoikeuksientiedot > li:nth-child('+opiskeluoikeusIndex+')').find('.suoritus-tabs li:contains(' + nimi + ')')
        if (!tab.hasClass('selected')) {
          triggerEvent(findSingle('a', tab), 'click')
        }
      }
    },
    suoritusOnValittu: function(opiskeluoikeusIndex, nimi) {
      var tab = findSingle('.opiskeluoikeuksientiedot > li:nth-child('+opiskeluoikeusIndex+')').find('.suoritus-tabs li:contains(' + nimi + ')')
      return tab.hasClass('selected')
    },
    suoritusTabs: function(opiskeluoikeusIndex) {
      return textsOf(findSingle('.opiskeluoikeuksientiedot > li:nth-child('+opiskeluoikeusIndex+')').find('.suoritus-tabs > li:not(.add-suoritus)'))
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
      return wait.until(api.lisääSuoritusDialog().isVisible)()
    },
    lisääSuoritusDialog: function() {
      return LisääSuoritusDialog()
    },
    tilaJaVahvistus: TilaJaVahvistus,
    versiohistoria: Versiohistoria,
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

function Versiohistoria() {
  function elem() { return findSingle('.versiohistoria') }
  function versiot() {
    return elem().find('td.versionumero')
  }

  var api = {
    avaa: function () {
      if (!S('.versiohistoria > table').is(':visible')) {
        triggerEvent(findSingle('> a', elem()), 'click')
      }
      return wait.until(function(){
        return elem().find('tr.selected').is(':visible')
      })().then(wait.forAjax)
    },
    sulje: function () {
      if (S('.versiohistoria > table').is(':visible')) {
        triggerEvent(findSingle('> a', elem()), 'click')
      }
    },
    listaa: function() {
      return textsOf(versiot())
    },
    valitse: function(versio) {
      return function() {
        triggerEvent(findSingle('td.versionumero:contains('+ versio +')', elem()).next('td.aikaleima').find('a'), 'click')
      }
    }
  }
  return api
}

function TilaJaVahvistus() {
  function elem() { return findSingle('.tila-vahvistus') }
  function merkitseValmiiksiButton() { return elem().find('button.merkitse-valmiiksi') }
  function merkitseKeskeneräiseksiButton() { return elem().find('button.merkitse-kesken') }
  var api = {
    merkitseValmiiksiEnabled: function() {
      return merkitseValmiiksiButton().is(':visible') && !merkitseValmiiksiButton().is(':disabled')
    },
    merkitseValmiiksi: function( ) {
      triggerEvent(merkitseValmiiksiButton(), 'click')
      return wait.forAjax()
    },
    merkitseKeskeneräiseksi: function() {
      triggerEvent(merkitseKeskeneräiseksiButton(), 'click')
    },
    text: function( ){
      return extractAsText(findSingle('.tiedot', elem()))
    },
    merkitseValmiiksiDialog: MerkitseValmiiksiDialog()
  }
  return api
}

function MerkitseValmiiksiDialog() {
  function elem() { return findSingle('.merkitse-valmiiksi-modal')}
  function buttonElem() { return findSingle('button', elem())}
  return {
    merkitseValmiiksi: function( ) {
      if (buttonElem().is(':disabled')) throw new Error('disabled button')
      triggerEvent(buttonElem(), 'click')
      return wait.forAjax()
    },
    organisaatio: OrganisaatioHaku(function() { return findSingle('.myöntäjäOrganisaatio', elem()) } ),
    editor: Editor(elem)
  }
}

function LisääSuoritusDialog() {
  function elem() { return findSingle('.lisaa-suoritus-modal')}
  function buttonElem() { return findSingle('button', elem())}
  var api = _.merge({
    isVisible: function() {
      return isVisibleBy(elem)
    },
    isEnabled: function() {
      return !buttonElem().is(':disabled')
    },
    lisääSuoritus: function() {
      if (!api.isEnabled()) throw new Error('button not enabled')
      function count() { return OpinnotPage().suoritusTabs(1).length }
      var prevCount = count()
      triggerEvent(buttonElem(), 'click')
      return wait.until(function() { return count() == prevCount + 1 })()
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
    canSave: function() {
      return S('.toggle-edit.editing').is(':visible')
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
    },
    elem: elem
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
    },
    organisaatioValitsin: function() {
      return OrganisaatioHaku(elem)
    },
    getOptions: function() {
      return Page(elem).getInputOptions('.dropdown')
    }
  }, Editor(elem))
}