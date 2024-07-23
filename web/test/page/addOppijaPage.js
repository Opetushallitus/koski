function AddOppijaPage() {
  function form() {
    return S('form.uusi-oppija, .UusiOpiskeluoikeusDialog')
  }
  function button() {
    return form().find('button')
  }
  function modalButton() {
    return form().find('button.vahvista, .RaisedButton')
  }
  function selectedOppilaitos() {
    return form().find('.oppilaitos .selected')
  }
  function selectedTutkinto() {
    return form().find('.tutkinto .selected')
  }
  function selectValue(field, value) {
    return Select(`uusiOpiskeluoikeus.modal.${field}`, form).select(value)
  }

  var pageApi = Page(form)
  var api = {
    addNewOppija: function (username, hetu, oppijaData) {
      return function () {
        return prepareForNewOppija(username, hetu)()
          .then(api.enterValidDataAmmatillinen(oppijaData))
          .then(api.submitAndExpectSuccess(hetu, (oppijaData || {}).tutkinto))
      }
    },
    isVisible: function () {
      return isElementVisible(form()) && isNotLoading()
    },
    isEnabled: function () {
      return button().is(':visible') && !button().is(':disabled')
    },
    isModalButtonEnabled: function () {
      return modalButton().is(':visible') && !modalButton().is(':disabled')
    },
    tutkintoIsEnabled: function () {
      return (
        S('.tutkinto input').is(':visible') &&
        !S('.tutkinto input').is(':disabled')
      )
    },
    rahoitusIsVisible: function () {
      return isElementVisible(
        S('[data-testid="uusiOpiskeluoikeus.modal.opintojenRahoitus"')
      )
    },
    enterValidDataInternationalSchool: function (params) {
      params = _.merge(
        {
          oppilaitos: 'International School of Helsinki',
          grade: 'Grade explorer',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('International school'))
          .then(
            api.selectFromDropdown(
              '.international-school-grade .dropdown',
              params.grade
            )
          )
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
          .then(wait.forAjax)
      }
    },
    enterValidDataEsh: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Helsingin eurooppalainen koulu',
          luokkaaste: 'N1',
          alkamispäivä: '1.1.2022'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
          .then(wait.forAjax)
      }
    },
    enterValidDataEbTutkinto: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Helsingin eurooppalainen koulu',
          alkamispäivä: '1.1.2022'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('EB-tutkinto'))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(wait.forAjax)
      }
    },
    enterValidDataPerusopetus: function (params) {
      params = _.merge(
        { oppilaitos: 'Jyväskylän normaalikoulu', alkamispäivä: '1.1.2018' },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(wait.forAjax)
      }
    },
    enterValidDataEsiopetus: function (params) {
      return api.enterData(
        _.merge(
          {
            oppilaitos: 'Jyväskylän normaalikoulu',
            opiskeluoikeudenTyyppi: 'Esiopetus'
          },
          {},
          params
        )
      )
    },
    enterValidDataPäiväkodinEsiopetus: function (params) {
      return api.enterData(
        _.merge(
          { oppilaitos: 'PK Vironniemi', opiskeluoikeudenTyyppi: 'Esiopetus' },
          {},
          params
        )
      )
    },
    enterHenkilötiedot: function (params) {
      params = _.merge(
        { etunimet: 'Tero', kutsumanimi: 'Tero', sukunimi: 'Tyhjä' },
        {},
        params
      )
      return function () {
        return pageApi
          .setInputValue('.etunimet input', params.etunimet)()
          .then(
            pageApi.setInputValue(
              '.kutsumanimi input[disabled], .kutsumanimi .dropdown',
              params.kutsumanimi
            )
          )
          .then(pageApi.setInputValue('.sukunimi input', params.sukunimi))
      }
    },
    enterValidDataVSTKOPS: function (params) {
      params = _.merge({
        oppilaitos: 'Varsinais-Suomen kansanopisto',
        oppimäärä: 'Oppivelvollisille suunnattu vapaan sivistystyön koulutus',
        suorituskieli: 'suomi',
        alkamispäivä: '1.8.2021'
      })
      return function () {
        return api
          .enterData(params)()
          .then(
            api.selectOpiskeluoikeudenTyyppi('Vapaan sivistystyön koulutus')
          )
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectMaksuttomuus(0))
      }
    },
    enterValidDataVSTLukutaito: function (params) {
      params = _.merge({
        oppilaitos: 'Varsinais-Suomen kansanopisto',
        oppimäärä: 'Vapaan sivistystyön lukutaitokoulutus',
        suorituskieli: 'suomi',
        alkamispäivä: '1.8.2021'
      })
      return function () {
        return api
          .enterData(params)()
          .then(
            api.selectOpiskeluoikeudenTyyppi('Vapaan sivistystyön koulutus')
          )
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectMaksuttomuus(0))
      }
    },
    enterValidDataVSTVapaatavoitteinen: function (params) {
      params = _.merge({
        oppilaitos: 'Varsinais-Suomen kansanopisto',
        oppimäärä: 'Vapaan sivistystyön vapaatavoitteinen koulutus',
        suorituskieli: 'suomi',
        tila: 'Hyväksytysti suoritettu',
        alkamispäivä:
          new Date().getDate() +
          '.' +
          (1 + new Date().getMonth()) +
          '.' +
          new Date().getFullYear(),
        opintokokonaisuus: '1138 Kuvallisen ilmaisun perusteet ja välineet'
      })
      return function () {
        return api
          .enterData(params)()
          .then(
            api.selectOpiskeluoikeudenTyyppi('Vapaan sivistystyön koulutus')
          )
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectOpiskeluoikeudenTila(params.tila))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintokokonaisuus(params.opintokokonaisuus))
      }
    },
    enterValidDataVSTJOTPA: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Varsinais-Suomen kansanopisto',
          oppimäärä: 'Jatkuvaan oppimiseen suunnattu',
          suorituskieli: 'suomi',
          tila: 'Läsnä',
          alkamispäivä: '1.1.2023',
          opintokokonaisuus: '1138 Kuvallisen ilmaisun perusteet ja välineet'
        },
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(
            api.selectOpiskeluoikeudenTyyppi('Vapaan sivistystyön koulutus')
          )
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectOpiskeluoikeudenTila(params.tila))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintokokonaisuus(params.opintokokonaisuus))
      }
    },
    enterValidDataAmmatillinen: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Stadin',
          tutkinto: 'Autoalan perust',
          suoritustapa: 'Ammatillinen perustutkinto',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'))
          .then(api.selectTutkinto(params.tutkinto))
          .then(api.selectSuoritustapa(params.suoritustapa))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
      }
    },
    enterValidDataMuuAmmatillinen: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Stadin',
          oppimäärä: 'Muun ammatillisen koulutuksen suoritus',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'))
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(function () {
            if (params.koulutusmoduuli) {
              return api.selectKoulutusmoduuli(params.koulutusmoduuli)()
            }
          })
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
      }
    },
    enterValidDataLukio: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Ressun',
          oppimäärä: 'Lukion oppimäärä',
          peruste: '60/011/2015',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('Lukiokoulutus'))
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectPeruste(params.peruste))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
      }
    },
    enterValidDataLuva: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Ressun',
          peruste: '56/011/2015',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(
            api.selectOpiskeluoikeudenTyyppi(
              'Lukioon valmistava koulutus (LUVA)'
            )
          )
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
      }
    },
    enterValidDataIB: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Ressun',
          oppimäärä: 'IB-tutkinto',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('IB-tutkinto'))
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
      }
    },
    enterValidDataPreIB: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Ressun',
          oppimäärä: 'Pre-IB',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('IB-tutkinto'))
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
      }
    },
    enterValidDataDIA: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Helsingin',
          oppimäärä: 'DIA-tutkinto',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('DIA-tutkinto'))
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
      }
    },
    enterValidDataDIAValmistavaVaihe: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Helsingin',
          oppimäärä: 'Valmistava DIA-vaihe',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectOpiskeluoikeudenTyyppi('DIA-tutkinto'))
          .then(api.selectOppimäärä(params.oppimäärä))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
      }
    },
    enterValidDataTUVA: function (params) {
      params = _.merge({
        oppilaitos: 'Ressun',
        järjestämislupa: 'Perusopetuksen järjestämislupa (TUVA)',
        suorituskieli: 'suomi',
        alkamispäivä: '1.8.2021',
        opintojenRahoitus: 'Muuta kautta rahoitettu'
      })
      return function () {
        return api
          .enterData(params)()
          .then(
            api.selectOpiskeluoikeudenTyyppi(
              'Tutkintokoulutukseen valmentava koulutus'
            )
          )
          .then(api.selectJärjestämislupa(params.järjestämislupa))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
          .then(api.selectMaksuttomuus(0))
      }
    },
    enterValidDataTUVAAmmatillinen: function (params) {
      params = _.merge({
        oppilaitos: 'Ressun',
        järjestämislupa: 'Ammatillisen koulutuksen järjestämislupa (TUVA)',
        suorituskieli: 'suomi',
        alkamispäivä: '1.8.2021',
        tila: 'Loma',
        opintojenRahoitus: 'Muuta kautta rahoitettu'
      })
      return function () {
        return api
          .enterData(params)()
          .then(
            api.selectOpiskeluoikeudenTyyppi(
              'Tutkintokoulutukseen valmentava koulutus'
            )
          )
          .then(api.selectJärjestämislupa(params.järjestämislupa))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpiskeluoikeudenTila(params.tila))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
          .then(api.selectMaksuttomuus(0))
      }
    },
    enterPaikallinenKoulutusmoduuliData: function (params) {
      params = _.merge(
        {
          nimi: 'Varaston täyttäminen',
          koodi: 'vrs-t-2019-k',
          kuvaus:
            'Opiskelija osaa tehdä tilauksen vakiotoimittajilta sekä menettelytavat palavien ja vaarallisten aineiden varastoinnissa'
        },
        {},
        params
      )

      return function () {
        return pageApi
          .setInputValue('.koulutusmoduuli .nimi input', params.nimi)()
          .then(
            pageApi.setInputValue(
              '.koulutusmoduuli .koodiarvo input',
              params.koodi
            )
          )
          .then(
            pageApi.setInputValue(
              '.koulutusmoduuli .kuvaus textarea',
              params.kuvaus
            )
          )
      }
    },
    enterAmmatilliseenTehtäväänvalmistava: function (
      ammatilliseentehtäväänvalmistavakoulutus
    ) {
      return selectFromDropdown(
        '.ammatilliseentehtäväänvalmistavakoulutus .dropdown',
        ammatilliseentehtäväänvalmistavakoulutus
      )
    },
    enterData: function (params) {
      return function () {
        return api
          .enterHenkilötiedot(params)()
          .then(api.selectOppilaitos(params.oppilaitos))
          .then(function () {
            if (params.opiskeluoikeudenTyyppi) {
              return api.selectOpiskeluoikeudenTyyppi(
                params.opiskeluoikeudenTyyppi
              )()
            }
          })
          .then(function () {
            if (params.suorituskieli) {
              return api.selectSuorituskieli(params.suorituskieli)()
            }
          })
          .then(function () {
            if (params.opintojenRahoitus) {
              return api.selectOpintojenRahoitus(params.opintojenRahoitus)
            }
          })
      }
    },
    enterTutkinto: function (name) {
      return function () {
        return pageApi.setInputValue('.tutkinto input', name)()
      }
    },
    enterOppilaitos: function (name) {
      return UusiOpiskeluoikeusOppilaitosSelect(form).enter(name)
    },
    selectOppilaitos: function (name) {
      return UusiOpiskeluoikeusOppilaitosSelect(form).select(name)
    },
    oppilaitokset: UusiOpiskeluoikeusOppilaitosSelect(form).oppilaitokset,
    toimipisteet: UusiOpiskeluoikeusOppilaitosSelect(form).toimipisteet,
    oppilaitos: UusiOpiskeluoikeusOppilaitosSelect(form).oppilaitos,
    selectTutkinto: function (name) {
      if (!name) {
        return wait.forAjax
      }
      return function () {
        return wait
          .until(pageApi.getInput('.tutkinto input').isVisible)()
          .then(pageApi.setInputValue('.tutkinto input', name))
          .then(
            wait.until(function () {
              return isElementVisible(selectedTutkinto())
            })
          )
          .then(click(selectedTutkinto))
      }
    },
    selectSuoritustapa: function (suoritustapa) {
      if (suoritustapa) {
        return selectFromDropdown('.suoritustapa .dropdown', suoritustapa)
      } else return function () {}
    },
    selectAloituspäivä: function (date) {
      return pageApi.setInputValue(
        '[data-testid="uusiOpiskeluoikeus.modal.date.edit.input"]',
        date
      )
    },
    selectOpiskeluoikeudenTila: function (tila) {
      if (tila) {
        return selectFromDropdown('.opiskeluoikeudentila .dropdown', tila)
      } else return function () {}
    },
    selectMaksuttomuus: function (index) {
      return function () {
        return click(
          S(
            '[data-testid="maksuttomuus-radio-buttons"] .radio-option-container'
          )[index]
        )()
      }
    },
    henkilötiedot: function () {
      return [
        '.etunimet input',
        '.kutsumanimi input[disabled], .kutsumanimi .dropdown',
        '.sukunimi input'
      ].map(function (selector) {
        return pageApi.getInputValue(selector)
      })
    },
    hetu: function () {
      return extractAsText(S('.hetu .value'))
    },
    submit: function () {
      if (!api.isEnabled()) {
        throw new Error('Button not enabled')
      }
      return click(button)()
    },
    submitModal: function () {
      if (!api.isModalButtonEnabled()) {
        throw new Error('Button not enabled')
      }
      return click(modalButton)()
    },
    submitAndExpectSuccess: function (oppija, tutkinto) {
      tutkinto = tutkinto || 'Autoalan perustutkinto'
      var timeoutMs = 15000
      return function () {
        return wait
          .until(api.isEnabled)()
          .then(api.submit)
          .then(
            wait.until(function () {
              return (
                KoskiPage().getSelectedOppija().indexOf(oppija) >= 0 &&
                OpinnotPage().suoritusOnValittu(0, tutkinto)
              )
            }, timeoutMs)
          )
      }
    },
    submitAndExpectSuccessModal: function (oppija, tutkinto) {
      tutkinto = tutkinto || 'Autoalan perustutkinto'
      return function () {
        var timeoutMs = 15000
        return wait
          .until(api.isModalButtonEnabled)()
          .then(api.submitModal)
          .then(
            wait.until(function () {
              return (
                KoskiPage().getSelectedOppija().indexOf(oppija) >= 0 &&
                OpinnotPage().suoritusOnValittu(0, tutkinto)
              )
            }, timeoutMs)
          )
      }
    },
    isErrorShown: function (field) {
      return function () {
        return isElementVisible(form().find('.error-messages .' + field))
      }
    },
    opiskeluoikeudenTyypit: function () {
      return pageApi.getInputOptions('.opiskeluoikeudentyyppi .dropdown')
    },
    opiskeluoikeudenTyyppi: function () {
      return pageApi.getInputValue('.opiskeluoikeudentyyppi input')
    },
    selectOpiskeluoikeudenTyyppi: function (tyyppi) {
      return selectValue('opiskeluoikeus', tyyppi)
    },
    selectOpintokokonaisuus: function (kokonaisuus) {
      return selectFromDropdown('.opintokokonaisuus .dropdown', kokonaisuus)
    },
    oppimäärät: function () {
      return pageApi.getInputOptions('.oppimaara .dropdown')
    },
    oppiaineet: function () {
      return pageApi.getInputOptions('.oppiaine .dropdown')
    },
    selectOppimäärä: function (oppimäärä) {
      return selectFromDropdown('.oppimaara .dropdown', oppimäärä)
    },
    selectOppiaine: function (oppiaine) {
      return selectFromDropdown('.oppiaine .dropdown', oppiaine)
    },
    selectSuorituskieli: function (kieli) {
      return selectValue('suorituskieli', kieli)
    },
    selectJärjestämislupa: function (järjestämislupa) {
      return selectFromDropdown('.järjestämislupa .dropdown', järjestämislupa)
    },
    opintojenRahoitukset: function () {
      return pageApi.getInputOptions('.opintojenrahoitus .dropdown')
    },
    selectOpintojenRahoitus: function (rahoitus) {
      return rahoitus
        ? selectFromDropdown('.opintojenrahoitus .dropdown', rahoitus)
        : function () {}
    },
    opiskeluoikeudenTilat: function () {
      const s = Select('uusiOpiskeluoikeus.modal.tila', form)
      s.openSync()
      return s.optionTexts()
    },
    tutkinnot: function () {
      return extractAsText(
        S(
          "[data-testid='tutkinto-autocomplete'] [data-testid='autocomplete-results']"
        )
      )
    },
    tutkinnotIsVisible: function () {
      return isElementVisible(
        S(
          "[data-testid='tutkinto-autocomplete'] [data-testid='autocomplete-results']"
        )
      )
    },
    selectPeruste: function (peruste) {
      return selectFromDropdown("[data-testid='peruste-dropdown']", peruste)
    },
    perusteet: function () {
      return extractAsText(S("[data-testid='peruste-dropdown'] .options"))
    },
    selectKieli: function (kieli) {
      return selectFromDropdown('.kieli .dropdown', kieli)
    },
    selectKoulutusmoduuli: function (koulutusmoduuli) {
      return selectFromDropdown('.koulutusmoduuli .dropdown', koulutusmoduuli)
    },
    goBack: click(findSingle('h1 a')),
    selectFromDropdown,
    selectVarhaiskasvatusOrganisaationUlkopuolelta: function (checked) {
      return pageApi.setInputValue(
        '[data-testid="uusiOpiskeluoikeus.modal.hankintakoulutus.esiopetus"]',
        checked
      )
    },
    selectJärjestämismuoto: function (järjestämismuoto) {
      return selectValue(
        'hankintakoulutus.varhaiskasvatuksenJärjestämismuoto',
        järjestämismuoto
      )
    }
  }
  function selectFromDropdown(selector, value, exact) {
    return function () {
      return wait
        .until(pageApi.getInput(selector).isVisible)()
        .then(wait.forAjax)
        .then(pageApi.setInputValue(selector, value))
    }
  }
  return api
}

// Uuden opiskeluoikeuden organisaatiovalitsin

function Select(testId, base) {
  const baseElem = () => base()[0]
  const input = () => Page(findByTestId(testId, baseElem())).getInput('input')
  const findByTestId = (id, baseE) => {
    const elem = $(baseE.querySelector(`[data-testid="${id}"]`))
    if (!elem) {
      throw new Error(`Test id "${id}" does not exist on dom`)
    }
    return elem
  }
  const sleep = (time = 100) => new Promise((r) => setTimeout(r, time))
  const assertVisibility = () => {
    if (!input().isVisible()) {
      throw new Error(`${testId} is not visible`)
    }
  }

  const api = {
    select: function (value) {
      return eventually(async () => {
        assertVisibility()
        await api.open()
        let clicked = false
        api.options().map((_, o) => {
          const $o = $(o)
          if ($o.text() === value) {
            $o.click()
            clicked = true
          }
        })
        if (!clicked) {
          throw new Error(`Value '${value}' does not exist in ${testId}`)
        }
        await sleep()
      })
    },
    search: function (value) {
      return eventually(async () => {
        assertVisibility()
        try {
          return await api
            .enter(value)()
            .then(async () => {
              api.options().click()
              await sleep()
            })
        } catch (er) {
          console.error('UusiOpiskeluoikeusOppilaitosSelect.select failed:', er)
          throw er
        }
      })
    },
    enter: function (value) {
      return eventually(async () => {
        assertVisibility()
        try {
          await sleep()
          const result = input().setValue(value)
          await sleep(500)
          await wait.forAjax()
          return result
        } catch (er) {
          console.error('UusiOpiskeluoikeusOppilaitosSelect.enter failed:', er)
          throw er
        }
      })
    },
    open: async function () {
      return eventually(async () => {
        assertVisibility()
        await sleep()
        const target = findByTestId(`${testId}.input`, baseElem())
        target.click()
      })()
    },
    openSync: function () {
      assertVisibility()
      const target = findByTestId(`${testId}.input`, baseElem())
      target.click()
    },
    options: function () {
      assertVisibility()
      return findByTestId(`${testId}.options`, baseElem()).find(
        '.Select__optionLabel:not(.Select__optionGroup)'
      )
    },
    optionTexts: function () {
      assertVisibility()
      return api
        .options()
        .map((_, o) => $(o).text())
        .get()
    }
  }
  return api
}

function UusiOpiskeluoikeusOppilaitosSelect(base) {
  const select = Select('uusiOpiskeluoikeus.modal.oppilaitos', base)

  const api = {
    select: function (value) {
      return select.search(value)
    },
    enter: function (value) {
      return select.enter(value)
    },
    open: function () {
      return select.open()
    },
    oppilaitokset: function () {
      return ['TODO: oppilaitokset()']
    },
    toimipisteet: function () {
      return select.optionTexts()
    },
    oppilaitos: function () {
      return 'TODO: oppilaitos()'
    }
  }
  return api
}
