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
  function selectedTutkinto(index) {
    const ts = form().find(
      `[data-testid="uusiOpiskeluoikeus.modal.tutkinto"] .Select__option`
    )
    const i = index !== undefined ? (index < 0 ? ts.length + index : index) : 0
    return S(ts[i])
  }
  function selectValue(field, value) {
    return Select(`uusiOpiskeluoikeus.modal.${field}`, form).select(value)
  }
  function selectOptions(field) {
    const select = Select(`uusiOpiskeluoikeus.modal.${field}`, form)
    select.openSync()
    return select.optionTexts()
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
      const selector = '[data-testid="uusiOpiskeluoikeus.modal.tutkinto.input"]'
      return S(selector).is(':visible') && !S(selector).is(':disabled')
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
          opiskeluoikeudenTyyppi: 'International school',
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
          .then(api.selectGrade(params.grade))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
          .then(() => {
            if (params.maksuttomuus !== undefined) {
              return api.selectMaksuttomuus(params.maksuttomuus)()
            }
          })
          .then(wait.forAjax)
      }
    },
    enterValidDataEsh: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Helsingin eurooppalainen koulu',
          opiskeluoikeudenTyyppi: 'European School of Helsinki',
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
        {
          oppilaitos: 'Jyväskylän normaalikoulu',
          opiskeluoikeudenTyyppi: 'Perusopetus',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(() => {
            if (params.maksuttomuus !== undefined) {
              return api.selectMaksuttomuus(params.maksuttomuus)()
            }
          })
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
          opiskeluoikeudenTyyppi: 'Ammatillinen koulutus',
          suoritustyyppi: 'Ammatillinen tutkinto',
          suoritustapa: 'Ammatillinen perustutkinto',
          tutkinto: 'Autoalan perust',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus',
          alkamispäivä: '1.1.2018',
          suorituskieli: 'suomi',
          maksuttomuus: 0
        },
        {},
        params
      )
      return function () {
        return api
          .enterData({ ...params, suorituskieli: undefined })()
          .then(api.selectSuoritustyyppi(params.suoritustyyppi))
          .then(api.selectSuorituskieli(params.suorituskieli))
          .then(
            () =>
              params.suoritustapa &&
              api.selectSuoritustapa(params.suoritustapa)()
          )
          .then(() => params.tutkinto && api.selectTutkinto(params.tutkinto)())
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
          .then(
            () =>
              params.maksuttomuus !== undefined &&
              api.selectMaksuttomuus(params.maksuttomuus)()
          )
      }
    },
    enterValidDataMuuAmmatillinen: function (params) {
      params = _.merge(
        {
          oppilaitos: 'Stadin',
          opiskeluoikeudenTyyppi: 'Ammatillinen koulutus',
          oppimäärä: 'Muun ammatillisen koulutuksen suoritus',
          opintojenRahoitus: 'Valtionosuusrahoitteinen koulutus'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
          .then(api.selectSuoritustyyppi(params.oppimäärä))
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
          peruste: '60/011/2015 Lukion opetussuunnitelman perusteet 2015',
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
          opiskeluoikeudenTyyppi: 'DIA-tutkinto',
          oppimäärä: 'DIA-tutkintovaihe',
          alkamispäivä: '1.1.2018'
        },
        {},
        params
      )
      return function () {
        return api
          .enterData(params)()
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
        opiskeluoikeudenTyyppi: 'Tutkintokoulutukseen valmentava koulutus',
        järjestämislupa: 'Perusopetuksen järjestämislupa (TUVA)',
        suorituskieli: 'suomi',
        alkamispäivä: '1.8.2021',
        opintojenRahoitus: 'Muuta kautta rahoitettu'
      })
      return function () {
        return api
          .enterData(params)()
          .then(api.selectJärjestämislupa(params.järjestämislupa))
          .then(api.selectAloituspäivä(params.alkamispäivä))
          .then(api.selectOpintojenRahoitus(params.opintojenRahoitus))
          .then(api.selectMaksuttomuus(0))
      }
    },
    enterValidDataTUVAAmmatillinen: function (params) {
      params = _.merge({
        oppilaitos: 'Ressun',
        opiskeluoikeudenTyyppi: 'Tutkintokoulutukseen valmentava koulutus',
        järjestämislupa: 'Ammatillisen koulutuksen järjestämislupa (TUVA)',
        suorituskieli: 'suomi',
        alkamispäivä: '1.8.2021',
        tila: 'Loma',
        opintojenRahoitus: 'Muuta kautta rahoitettu'
      })
      return function () {
        return api
          .enterData(params)()
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
          koulutusmoduuli: 'Paikallinen koulutus',
          nimi: 'Varaston täyttäminen',
          koodi: 'vrs-t-2019-k',
          kuvaus:
            'Opiskelija osaa tehdä tilauksen vakiotoimittajilta sekä menettelytavat palavien ja vaarallisten aineiden varastoinnissa'
        },
        {},
        params
      )

      return function () {
        return (
          params.koulutusmoduuli
            ? api.selectKoulutusmoduuli(params.koulutusmoduuli)()
            : Promise.resolve()
        )
          .then(
            pageApi.setInputValue(
              '[data-testid="uusiOpiskeluoikeus.modal.paikallinenKoulutus.nimi.input"]',
              params.nimi
            )
          )
          .then(
            pageApi.setInputValue(
              '[data-testid="uusiOpiskeluoikeus.modal.paikallinenKoulutus.koodiarvo.input"]',
              params.koodi
            )
          )
          .then(
            pageApi.setInputValue(
              '[data-testid="uusiOpiskeluoikeus.modal.paikallinenKoulutus.kuvaus.input"]',
              params.kuvaus
            )
          )
      }
    },
    enterAmmatilliseenTehtäväänvalmistava: function (
      ammatilliseentehtäväänvalmistavakoulutus
    ) {
      return selectValue(
        'ammatilliseentehtavaanvalmistavakoulutus',
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
        return pageApi.setInputValue(
          '[data-testid="uusiOpiskeluoikeus.modal.tutkinto.input"]',
          name
        )()
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
    selectTutkinto: function (name, index) {
      if (!name) {
        return wait.forAjax
      }
      const tutkintoSelector =
        '[data-testid="uusiOpiskeluoikeus.modal.tutkinto.input"]'
      return function () {
        return wait
          .until(pageApi.getInput(tutkintoSelector).isVisible)()
          .then(pageApi.setInputValue(tutkintoSelector, name))
          .then(
            wait.until(function () {
              return isElementVisible(selectedTutkinto(index))
            })
          )
          .then(click(() => selectedTutkinto(index)))
      }
    },
    selectSuoritustyyppi: function (suoritustyyppi) {
      return suoritustyyppi
        ? selectValue('suoritustyyppi', suoritustyyppi)
        : function () {}
    },
    selectSuoritustapa: function (suoritustapa) {
      return suoritustapa
        ? selectValue('suoritustapa', suoritustapa)
        : function () {}
    },
    selectAloituspäivä: function (date) {
      return pageApi.setInputValue(
        '[data-testid="uusiOpiskeluoikeus.modal.aloituspäivä.edit.input"]',
        date
      )
    },
    selectOpiskeluoikeudenTila: function (tila) {
      return tila ? selectValue('tila', tila) : function () {}
    },
    selectMaksuttomuus: function (index) {
      return eventually(async () => {
        const selector =
          '[data-testid="uusiOpiskeluoikeus.modal.maksuton"] .RadioButtons__option'
        return click(S(selector)[index])()
      })
    },
    selectGrade: function (grade) {
      return selectValue('grade', grade)
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
    submitModal: async function () {
      await eventually(() => {
        if (!api.isModalButtonEnabled()) {
          throw new Error('Button not enabled')
        }
      })()
      await click(modalButton)()
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
      return selectValue('opintokokonaisuus', kokonaisuus)
    },
    oppimäärät: function () {
      return selectOptions('oppimäärä')
    },
    oppiaineet: function () {
      return selectOptions('oppiaine')
    },
    selectOppimäärä: function (oppimäärä) {
      return selectValue('oppimäärä', oppimäärä)
    },
    selectOppiaine: function (oppiaine) {
      return selectValue('oppiaine', oppiaine)
    },
    selectSuorituskieli: function (kieli) {
      return selectValue('suorituskieli', kieli)
    },
    selectJärjestämislupa: function (järjestämislupa) {
      return selectValue('järjestämislupa', järjestämislupa)
    },
    opintojenRahoitukset: function () {
      return selectOptions('opintojenRahoitus')
    },
    selectOpintojenRahoitus: function (rahoitus) {
      return rahoitus
        ? selectValue('opintojenRahoitus', rahoitus)
        : function () {}
    },
    opiskeluoikeudenTilat: function () {
      return selectOptions('tila')
    },
    tutkinnot: function () {
      return selectOptions('tutkinto')
    },
    tutkinnotIsVisible: function () {
      return isElementVisible(
        S('[data-testid="uusiOpiskeluoikeus.modal.tutkinto"]')
      )
    },
    selectPeruste: function (peruste) {
      return selectValue('peruste', peruste)
    },
    perusteet: function () {
      return selectOptions('peruste')
    },
    selectKieliaineenKieli: function (kieli) {
      return selectValue('kieliaineenKieli', kieli)
    },
    selectKoulutusmoduuli: function (koulutusmoduuli) {
      return selectValue('koulutusmoduuli', koulutusmoduuli)
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
    if (!baseE) {
      throw new Error('Base element does not exist on dom')
    }
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
          if (value === undefined) {
            return
          }
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
    },
    value: function () {
      return findByTestId(`${testId}.input`, baseElem()).val()
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
      return select.optionTexts()
    },
    toimipisteet: function () {
      return select.optionTexts()
    },
    oppilaitos: function () {
      return select.value()
    }
  }
  return api
}
