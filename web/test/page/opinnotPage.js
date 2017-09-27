function OpinnotPage() {
  var api = {
    getTutkinto: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      var nth = S('.opiskeluoikeus .suoritus .property.koulutusmoduuli .koulutusmoduuli .tunniste')[index]
      return S(nth).text()
    },
    getOppilaitos: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      return S(S('.opiskeluoikeus > h3 .otsikkotiedot .oppilaitos')[index]).text().slice(0, -1)
    },
    getSuorituskieli: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      var nth = S('.opiskeluoikeus .suoritus .property.suorituskieli .value')[index]
      return S(nth).text()
    },
    valitseSuoritus: function(opiskeluoikeusIndex, nimi) {
      return function() {
        var tab = findSingle('.opiskeluoikeuksientiedot > li:nth-child('+opiskeluoikeusIndex+')')().find('.suoritus-tabs > ul > li:contains(' + nimi + ')')
        if (!tab.hasClass('selected')) {
          return click(findSingle('a', tab.eq(0)))()
        }
      }
    },
    suoritusOnValittu: function(opiskeluoikeusIndex, nimi) {
      var tab = findSingle('.opiskeluoikeuksientiedot > li:nth-child('+opiskeluoikeusIndex+')')().find('.suoritus-tabs > ul > li:contains(' + nimi + ')')
      return tab.hasClass('selected')
    },
    suoritusTabs: function(opiskeluoikeusIndex) {
      return textsOf(subElement(findSingle('.opiskeluoikeuksientiedot > li:nth-child('+opiskeluoikeusIndex+')'), ('.suoritus-tabs > ul > li')))
    },
    suoritusTabIndex: function(opiskeluoikeusIndex){
      var tabs = toArray(subElement(findSingle('.opiskeluoikeuksientiedot > li:nth-child('+opiskeluoikeusIndex+')'), '.suoritus-tabs > ul > li'))
      for (var i in tabs) {
        if (S(tabs[i]).hasClass('selected')) return parseInt(i)
      }
      return -1
    },
    onTallennettavissa: function() {
      return S('#edit-bar button:not(:disabled)').is(':visible')
    },
    isEditing: function() {
      return api.isDirty() || S('.oppija-content.edit').is(':visible')
    },
    isDirty: function() {
      return S('.oppija-content.dirty').is(':visible')
    },
    avaaOpintosuoritusote: function (index) {
      return seq(
        click(findSingle('.opiskeluoikeuksientiedot li:nth-child('+index+') a.opintosuoritusote')),
        wait.until(OpintosuoritusotePage().isVisible)
      )
    },
    avaaTodistus: function(index) {
      index = typeof index !== 'undefined' ? index : 0
      return seq(
        click('a.todistus:eq(' + index + ')'),
        wait.until(TodistusPage().isVisible)
      )
    },
    avaaLisaysDialogi: function() {
      if (!S('.lisaa-opiskeluoikeusjakso-modal .modal-content').is(':visible')) {
        return click(S('.opiskeluoikeuden-tiedot .add-item a'))()
      }
    },
    opiskeluoikeudet: Opiskeluoikeudet(),
    opiskeluoikeusEditor: function(index) {
      index = index || 0
      var elem = findSingle('.opiskeluoikeus-content:eq(' + index + ')')
      return _.merge(
        Editor(elem),
        {
          päättymispäivä: function() { return elem().find('.päättymispäivä').text() }
        }
      )
    },
    lisääSuoritusVisible: function() {
      return S(".add-suoritus a").is(":visible")
    },
    lisääSuoritusDialog: LisääSuoritusDialog,
    lisääSuoritus: click(findSingle(".add-suoritus a")),
    tilaJaVahvistus: TilaJaVahvistus(),
    versiohistoria: Versiohistoria(),
    oppiaineet: Oppiaineet(),
    tutkinnonOsat: TutkinnonOsat,
    anythingEditable: function() {
      return Editor(findSingle('.content-area') ).isEditable()
    },
    expandAll: function() {
      var checkAndExpand = function() {
        if (expanders().is(':visible')) {
          return seq(click(expanders),
            wait.forMilliseconds(10),
            wait.forAjax,
            checkAndExpand)()
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
          return seq(click(collapsers),
            wait.forMilliseconds(10),
            wait.forAjax,
            checkAndCollapse)()
        }
      }
      return checkAndCollapse()
      function collapsers() {
        return S('.foldable:not(.collapsed)>.toggle-expand:not(.disabled), tbody.expanded .toggle-expand:not(.disabled), a.expandable.open')
      }
    },
    backToList: click(findSingle('.back-link'))
  }

  return api
}

function Oppiaineet() {
  var api = {
    isVisible: function() { return S('.oppiaineet h5').is(':visible') },

    merkitseOppiaineetValmiiksi: function () {
      var editor = OpinnotPage().opiskeluoikeusEditor()
      var count = 20
      var promises = []
      for (var i = 0; i < count; i++) {
        var oppiaine = api.oppiaine(i)
        var arvosana = oppiaine.propertyBySelector('.arvosana')
        if (arvosana.isVisible()) {
          promises.push(arvosana.selectValue('5')())
        }
      }
      return Q.all(promises)
    },

    uusiOppiaine: function(selector) {
      selector = selector || ""
      return OpinnotPage().opiskeluoikeusEditor().propertyBySelector(selector + ' .uusi-oppiaine')
    },

    oppiaine: Oppiaine
  }
  return api

  function Oppiaine(indexOrClass) {
    var oppiaineElem = typeof indexOrClass == 'number'
      ? findSingle('.oppiaineet .oppiaine-rivi:eq(' + indexOrClass + ')')
      : findSingle('.oppiaineet .oppiaine-rivi.' + indexOrClass)
    var editorApi = Editor(oppiaineElem)
    var oppiaineApi = _.merge({
      text: function() { return extractAsText(oppiaineElem) },
      avaaLisääKurssiDialog: click(findSingle('.uusi-kurssi a', oppiaineElem)),
      lisääKurssiDialog: LisääKurssiDialog(),
      kurssi: function(identifier) {
        return Kurssi(subElement(oppiaineElem, ".kurssi:contains(" + identifier +")"))
      },
      errorText: function() { return extractAsText(subElement(oppiaineElem, '> .error')) },
      arvosana: editorApi.propertyBySelector('tr td.arvosana')
    }, editorApi)
    return oppiaineApi

    function LisääKurssiDialog() {
      var modalElem = findSingle('.uusi-kurssi-modal', oppiaineElem)
      function kurssiDropdown() { return api.propertyBySelector('.kurssi') }
      var api = _.merge({
        valitseKurssi: function(kurssi) {
          return kurssiDropdown().setValue(kurssi)
        },
        lisääKurssi: click(subElement(modalElem, 'button')),
        kurssit: function() {
          return kurssiDropdown().getOptions()
        },
        sulje: click('.uusi-kurssi-modal .peruuta')
      }, Editor(modalElem))
      return api
    }
  }
}

function Kurssi(elem) {
  var detailsElem = subElement(elem, '.details')
  var api = {
    detailsText: function() {
      return toElement(detailsElem).is(":visible") ? extractAsText(detailsElem) : ""
    },
    arvosana: Editor(elem).propertyBySelector('.arvosana'),
    toggleDetails: click(subElement(elem, '.tunniste')),
    showDetails: function() {
      if (api.detailsText() == '')
        return api.toggleDetails()
      return wait.forAjax()
    },
    details: function() {
      return Editor(detailsElem)
    },
    poistaKurssi: click(subElement(elem, '.remove-value'))
  }
  return api
}
Kurssi.findAll = function() {
  return toArray(S(".kurssi")).map(Kurssi)
}


function TutkinnonOsat(groupId) {
  function withSuffix(s) { return groupId ? s + '.' + groupId : s }
  var uusiTutkinnonOsaElement = findSingle(withSuffix('.uusi-tutkinnon-osa'))

  return {
    tyhjä: function() {
      return S(withSuffix('.tutkinnon-osa')).length === 0
    },
    isGroupHeaderVisible: function() {
      return S(withSuffix('.group-header')).is(':visible')
    },
    tutkinnonOsa: function(tutkinnonOsaIndex) {
      var tutkinnonOsaElement = findSingle(withSuffix('.tutkinnon-osa') + ':eq(' + tutkinnonOsaIndex + ')')

      var api = _.merge({
        tila: function() {
          return findSingle('.tila', tutkinnonOsaElement)().attr('title')
        },
        nimi: function() {
          return findSingle('.nimi', tutkinnonOsaElement)().text()
        },
        toggleExpand: click(findSingle('.suoritus .toggle-expand', tutkinnonOsaElement)),
        lisääOsaamisenTunnustaminen: click(findSingle('.tunnustettu .add-value', tutkinnonOsaElement)),
        poistaOsaamisenTunnustaminen: click(findSingle('.tunnustettu .remove-value', tutkinnonOsaElement)),
        poistaTutkinnonOsa: function() {
          var removeElement = findSingle('.remove-value', tutkinnonOsaElement)
          if (isElementVisible(removeElement)) {
            return click(removeElement)()
          }
        },
        näyttö: function() {
          return api.property('näyttö')
        },
        lueNäyttöModal: function() {
          function extractDropdownArray(elem) {
            return elem.find('ul.array > li').map(function() {return Page(this).getInput('.dropdown').value()}).get().slice(0, -1)
          }
          return {
            arvosana: Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .arvosana .value .dropdown').value(),
            arviointipäivä: Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .päivä .value input').value(),
            kuvaus: Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .kuvaus .value textarea').value(),
            arvioinnistaPäättäneet: extractDropdownArray(S('.näyttö .modal-content .arvioinnistaPäättäneet .value', tutkinnonOsaElement)),
            arviointikeskusteluunOsallistuneet: extractDropdownArray(S('.näyttö .modal-content .arviointikeskusteluunOsallistuneet .value', tutkinnonOsaElement)),
            suorituspaikka: [
              Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .suorituspaikka .value .dropdown').value(),
              Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .suorituspaikka .value input:not(.select)').value()
            ],
            työssäoppimisenYhteydessä: Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .työssäoppimisenYhteydessä .value input').value()
          }
        },
        avaaNäyttöModal: function() {
          var valueExists = !!tutkinnonOsaElement().find('.näyttö .edit-value').length
          return Q()
            .then(click(findSingle('.näyttö .'+(valueExists?'edit':'add')+'-value', tutkinnonOsaElement)))
            .then(wait.untilVisible(findSingle('.lisää-näyttö-modal .modal-content')))
        },
        asetaNäytönTiedot: function(tiedot) {
          return function () {
            return wait.forAjax().then(function() {
              // Normalize state
              var addVButton = tutkinnonOsaElement().find('.näyttö .modal-content .arviointi .add-value')
              if (addVButton.length) {
                click(addVButton)()
              }
              else {
                tutkinnonOsaElement().find('.näyttö .modal-content .arvioinnistaPäättäneet .value li .remove-item:visible').each(function(i, e) {
                  click(e)()
                })
                tutkinnonOsaElement().find('.näyttö .modal-content .arviointikeskusteluunOsallistuneet .value li .remove-item:visible').each(function(i, e) {
                  click(e)()
                })
              }
            }).then(wait.forAjax).then(function () {
              Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .arvosana .value .dropdown').setValue(tiedot.arvosana, exact = true)
              Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .päivä .value input').setValue(tiedot.arviointipäivä)
              Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .kuvaus .value textarea').setValue(tiedot.kuvaus)
              tiedot.arvioinnistaPäättäneet.map(function (v, i) {
                Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .arvioinnistaPäättäneet .value li:'+(i===0?'first':'last')+'-child .dropdown').setValue(v, exact = true)
              })
              tiedot.arviointikeskusteluunOsallistuneet.map(function (v, i) {
                Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .arviointikeskusteluunOsallistuneet .value li:'+(i===0?'first':'last')+'-child .dropdown').setValue(v, exact = true)
              })
              Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .suorituspaikka .value .dropdown').setValue(tiedot.suorituspaikka[0], exact = true)
              Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .suorituspaikka .value input:not(.select)').setValue(tiedot.suorituspaikka[1])
              Page(tutkinnonOsaElement).getInput('.näyttö .modal-content .työssäoppimisenYhteydessä .value input').setValue(tiedot.työssäoppimisenYhteydessä)

              if (findSingle('.näyttö .modal-content button', tutkinnonOsaElement)().prop('disabled')) {
                throw new Error('Invalid model')
              }
            })
          }
        },
        painaOkNäyttöModal: click(findSingle('.näyttö .modal-content button', tutkinnonOsaElement)),
        poistaNäyttö: click(findSingle('.näyttö .remove-value', tutkinnonOsaElement))
      }, {}, Editor(tutkinnonOsaElement))
      return api
    },
    lisääTutkinnonOsa: function(hakusana) {
      return function() {
        return Page(uusiTutkinnonOsaElement).setInputValue(".dropdown, .autocomplete", hakusana)()
          .then(wait.forAjax)
      }
    },
    lisääPaikallinenTutkinnonOsa: function(nimi) {
      return function() {
        var modalElement = subElement(uusiTutkinnonOsaElement, '.lisaa-paikallinen-tutkinnon-osa-modal')
        return click(subElement(uusiTutkinnonOsaElement, ('.paikallinen-tutkinnon-osa a')))()
          .then(Page(modalElement).setInputValue('input', nimi))
          .then(click(subElement(modalElement, 'button:not(:disabled)')))
      }
    },
    lisääTutkinnonOsaToisestaTutkinnosta: function(tutkinto, nimi) {
      return function() {
        var modalElement = subElement(uusiTutkinnonOsaElement, '.osa-toisesta-tutkinnosta .modal')
        return click(subElement(uusiTutkinnonOsaElement, ('.osa-toisesta-tutkinnosta a')))()
          .then(Page(modalElement).setInputValue('.tutkinto .autocomplete', tutkinto))
          .then(Page(modalElement).setInputValue('.tutkinnon-osat .dropdown', nimi))
          .then(click(subElement(modalElement, 'button:not(:disabled)')))
      }
    },
    tutkinnonosavaihtoehdot: function() {
      return Page(uusiTutkinnonOsaElement).getInputOptions(".dropdown")
    },
    laajuudenOtsikko: function() {
      return S('.suoritus-taulukko:eq(0) td.laajuus:eq(0)').text()
    },
    isSuoritusTaulukkoVisible: function() {
      return S('.suoritus-taulukko:eq(0)').is(':visible')
    },
    valitseSuoritustapaTeksti: function() {
      return S('.osasuoritukset').text()
    }
  }
}

function Opiskeluoikeudet() {
  return {
    opiskeluoikeuksienMäärä: function() {
      return S('.opiskeluoikeuksientiedot .opiskeluoikeus').length
    },

    opiskeluoikeuksienOtsikot: function() {
      return textsOf(S('.opiskeluoikeuksientiedot .opiskeluoikeus h3 .otsikkotiedot'))
    },

    valitseOpiskeluoikeudenTyyppi: function(tyyppi) {
      return function() {
        return click(findSingle('.opiskeluoikeustyypit .' + tyyppi + ' a'))()
      }
    },
    lisääOpiskeluoikeus: click(findSingle('.add-opiskeluoikeus a')),
    lisääOpiskeluoikeusEnabled: function() {
      return S('.add-opiskeluoikeus').is(':visible')
    }
  }
}

function Versiohistoria() {
  function elem() { return S('.versiohistoria') }
  function versiot() {
    return elem().find('.versionumero')
  }

  var api = {
    avaa: function () {
      if (!S('.versiohistoria > .modal').is(':visible')) {
        click(findSingle('> a', elem()))()
      }
      return wait.untilVisible(subElement(elem, 'li.selected'))().then(wait.forAjax)
    },
    sulje: function () {
      if (S('.versiohistoria > .modal').is(':visible')) {
        return click(findSingle('> a', elem()))()
      }
    },
    listaa: function() {
      return textsOf(versiot())
    },
    valittuVersio: function() {
      return elem().find('.selected').find('.versionumero').text()
    },
    valitse: function(versio) {
      return function() {
        click(findSingle('.versionumero:contains('+ versio +')', elem())().parent())()
        return wait.until(function() { return api.valittuVersio() == versio })()
      }
    }
  }
  return api
}

function TilaJaVahvistus() {
  var elem = findSingle('.tila-vahvistus')
  function merkitseValmiiksiButton() { return elem().find('button.merkitse-valmiiksi') }
  function merkitseKeskeneräiseksiButton() { return elem().find('button.merkitse-kesken') }
  function merkitseKeskeytyneeksiButton() { return elem().find('button.merkitse-keskeytyneeksi') }
  
  var api = {
    merkitseValmiiksiEnabled: function() {
      return merkitseValmiiksiButton().is(':visible') && !merkitseValmiiksiButton().is(':disabled')
    },
    merkitseValmiiksi: click(merkitseValmiiksiButton),
    merkitseKeskeneräiseksi: click(merkitseKeskeneräiseksiButton),
    merkitseKeskeytyneeksi: click(merkitseKeskeytyneeksiButton),
    text: function( ){
      return extractAsText(findSingle('.tiedot', elem()))
    },
    tila: function( ) {
      return extractAsText(findSingle('.tila .tila', elem()))
    },
    merkitseValmiiksiDialog: MerkitseValmiiksiDialog()
  }
  return api
}

function MerkitseValmiiksiDialog() {
  var elem = findSingle('.merkitse-valmiiksi-modal')
  var buttonElem = findSingle('button', elem)
  var api = {
    merkitseValmiiksi: function( ) {
      if (buttonElem().is(':disabled')) throw new Error('disabled button')
      return click(buttonElem())()
    },
    peruuta: click(findSingle('.peruuta', elem)),
    organisaatio: OrganisaatioHaku(findSingle('.myöntäjäOrganisaatio', elem) ),
    editor: Editor(elem),
    myöntäjät: Editor(elem).property('myöntäjäHenkilöt'),
    lisääMyöntäjä: function(nimi, titteli) {
      return function() {
        return api.myöntäjät.itemEditor(0).setValue('Lisää henkilö')()
          .then(api.myöntäjät.itemEditor(0).propertyBySelector('.nimi').setValue(nimi))
          .then(api.myöntäjät.itemEditor(0).propertyBySelector('.titteli').setValue(titteli))
      }
    }
  }
  return api
}

function LisääSuoritusDialog() {
  var elem = findSingle('.lisaa-suoritus-modal')
  var buttonElem = findSingle('button', elem)
  var api = _.merge({
    open: function() {
      if (!api.isVisible()) {
        return seq(click(S(".add-suoritus a")), wait.until(api.isVisible))()
      }
    },
    isVisible: function() {
      return isElementVisible(elem)
    },
    isEnabled: function() {
      return !buttonElem().is(':disabled')
    },
    lisääSuoritus: function() {
      if (!api.isEnabled()) throw new Error('button not enabled')
      function count() { return OpinnotPage().suoritusTabs(1).length }
      var prevCount = count()
      return Q()
        .then(click(buttonElem))
        .then(wait.until(function() { return count() == prevCount + 1 }))
    },
    toimipiste: OrganisaatioHaku(elem)
  }, {}, Editor(elem))
  return api
}

function Päivämääräväli(elem) {
  var api = {
    setAlku: function(value) {
      return function() {
        return Page(elem).setInputValue('.alku input', value)()
      }
    },
    getAlku: function() {
      return elem().find('.alku span.inline.date').text()
    },
    setLoppu: function(value) {
      return function() {
        return Page(elem).setInputValue('.loppu input', value)()
      }
    },
    isValid: function() {
      return !elem().find('.date-range').hasClass('error')
    }
  }
  return api
}

function OpiskeluoikeusDialog() {
  var elem = findSingle('.lisaa-opiskeluoikeusjakso-modal')
  var button = findSingle('button', elem)
  return {
    tila: function() {
      return Property(function() {return S('.lisaa-opiskeluoikeusjakso-modal')})
    },
    alkuPaiva: function() {
      return Property(findSingle('.property.alku', elem))
    },
    tallenna: click(button),
    peruuta: click(findSingle('.peruuta', elem)),
    isEnabled: function() {
      return !button().is(':disabled')
    },
    radioEnabled: function(value) {
      return !findSingle('input[value="' + value + '"]', elem())().is(':disabled')
    }
  }
}

function Editor(elem) {
  var editButton = findSingle('.toggle-edit')
  var enabledSaveButton = findSingle('#edit-bar button:not(:disabled)')
  var api = {
    isVisible: function() {
      return isElementVisible(elem)
    },
    edit: function() {
      return wait.until(api.isVisible)().then(function() {
        if (isElementVisible(editButton)) {
          return click(editButton)()
        }
      }).then(KoskiPage().verifyNoError)
    },
    canSave: function() {
      return isElementVisible(enabledSaveButton)
    },
    getEditBarMessage: function() {
      return findSingle('#edit-bar .state-indicator')().text()
    },
    saveChanges: seq(click(enabledSaveButton), KoskiPage().verifyNoError),
    saveChangesAndExpectError: seq(click(enabledSaveButton), wait.until(KoskiPage().isErrorShown)),
    cancelChanges: seq(click(findSingle('#edit-bar .cancel')), KoskiPage().verifyNoError),
    isEditable: function() {
      return elem().find('.toggle-edit').is(':visible')
    },
    property: function(key) {
      return Property(findSingle('.property.'+key+':eq(0)', elem))
    },
    propertyBySelector: function(selector) {
      return Property(findSingle(selector, elem))
    },
    subEditor: function(selector) {
      return Editor(findSingle(selector, elem))
    },
    isEditBarVisible: function() {
      return S("#edit-bar").hasClass("visible")
    },
    elem: elem
  }
  return api
}

function Property(elem) {
  if (typeof elem != 'function') throw new Error('elem has to be function')
  return _.merge({
    addValue: seq(click(findSingle('.add-value', elem)), KoskiPage().verifyNoError),
    isRemoveValueVisible: function() {
      return elem().find('.remove-value').is(':visible')
    },
    addItem: seq(click(findSingle('.add-item a', elem)), KoskiPage().verifyNoError),
    removeValue: seq(click(findSingle('.remove-value', elem)), KoskiPage().verifyNoError),
    removeFromDropdown: function(value) {
      var dropdownElem = findSingle('.dropdown', elem)
      return seq(
        click(findSingle('.select', dropdownElem)),
        wait.until(Page(dropdownElem).getInput('li:contains('+ value +')').isVisible),
        triggerEvent(findSingle('a.remove-value', dropdownElem), 'mousedown'),
        wait.forAjax
      )
    },
    removeItem: function(index) {
      return seq(click(findSingle('li:eq(' + index + ') .remove-item', elem)), KoskiPage().verifyNoError)
    },
    waitUntilLoaded: wait.until(function(){
      return elem().is(':visible') && !elem().find('.loading').is(':visible')
    }),
    selectValue: function(value) {
      return function() {
        return Page(elem).setInputValue('.dropdown', value.toString())().then(wait.forAjax)
      }
    },
    setValue: function(value) {
      return function() {
        return Page(elem).setInputValue('.dropdown, .editor-input', value)()
      }
    },
    getLanguage: function() {
      return elem().find('.localized-string').attr('class').split(/\s+/).filter(function(c) { return ['fi', 'sv', 'en'].includes(c) }).join(' ')
    },
    toPäivämääräväli: function() {
      return Päivämääräväli(elem)
    },
    click: function(selector) {
      return seq(click(findSingle(selector, elem)), KoskiPage().verifyNoError)
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
      return isElementVisible(findSingle('.value', elem))
        || isElementVisible(findSingle('.dropdown', elem))
        || isElementVisible(findSingle('input', elem))
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