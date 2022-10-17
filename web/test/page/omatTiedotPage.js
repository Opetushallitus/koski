function OmatTiedotPage() {
  var api = {
    openPage: function () {
      return openPage('/koski/omattiedot', api.isVisible)()
    },
    isVisible: function () {
      return isElementVisible(S('.omattiedot')) && !isLoading()
    },
    nimi: function () {
      return S('.user-info .name').text()
    },
    oppija: function () {
      return S('.main-content.oppija h1').text().replace('JSON', '')
    },
    virhe: function () {
      return S('.ei-suorituksia').text()
    },
    varoitukset: function () {
      return extractAsText(S('.varoitus'))
    },
    ingressi: function () {
      return S('header .header__caption p').text()
    },
    virheraportointiButton: function () {
      return S('header button:contains(Onko suorituksissasi virhe?)')
    },
    suoritusjakoButton: function () {
      return S('header button:contains(Jaa suoritustietoja)')
    },
    selectOpiskelija: function () {
      var elem = findSingle('.header__oppijanvalitsin')()
      return findFirstNotThrowing(elem)
    },
    selectOpiskelijaNäkyvissä: function () {
      return isElementVisible(S('.header__oppijanvalitsin'))
    },
    opiskelijanValintaNimet: function () {
      var elem = findSingle('.header__oppijanvalitsin')
      var result = toArray(elem().find('.option')).map(function (i) {
        return i.innerHTML
      })
      return result
    },
    opiskelijanValinta: function (name) {
      return function () {
        var elem = findSingle('.header__oppijanvalitsin')
        var result = toArray(elem().find(`.option:contains("${name}")`))
        return result
      }
    },
    virheraportointiForm: VirheraportointiForm(),
    suoritusjakoForm: SuoritusjakoForm(),
    headerNimi: function () {
      var el = findFirstNotThrowing('header .header__name')
      return el ? extractAsText(el) : ''
    },
    omatTiedotNäkyvissä: function () {
      return !isElementVisible(S('.palaa-omiin-tietoihin'))
    },
    varoitusNäkyvissä: function () {
      return isElementVisible(S('.varoitus'))
    }
  }
  return api
}

function VirheraportointiForm() {
  var pageApi = Page(findSingle('#lander-page .omattiedot'))
  var elem = findSingle('.virheraportointi')

  var api = {
    self: function () {
      return elem
    },
    contentsAsText: function () {
      return extractAsText(elem)
    },
    acceptDisclaimer: function () {
      function disclaimerToggle() {
        return S(
          '.virheraportointi span:contains(Asiani koskee tietoa, joka näkyy, tai kuuluisi yllämainitun perusteella näkyä Koski-palvelussa.)'
        )
      }

      return click(disclaimerToggle)()
    },
    selectOppilaitos: function (oid) {
      function option() {
        return S('.oppilaitos-options input[value="' + oid + '"]')
      }

      return click(option)
    },
    oppilaitosNames: function () {
      return toArray(elem().find('.oppilaitos-options label')).map(function (
        i
      ) {
        return i.innerHTML
      })
    },
    oppilaitosOids: function () {
      return toArray(
        elem().find('.oppilaitos-options input[type="radio"]')
      ).map(function (i) {
        return i.value
      })
    },
    oppilaitosOptionsText: function () {
      return extractAsText(elem().find('.oppilaitos-options'))
    },
    oppilaitosPicker: function () {
      return findSingle('.oppilaitos-options .oppilaitos-picker')
    },
    selectMuuOppilaitos: function (nimi) {
      return OrganisaatioHaku(this.oppilaitosPicker()).select(nimi)
    },
    yhteystiedot: function () {
      return extractAsText(
        S('.oppilaitos-options .yhteystieto .yhteystieto__contact-info')
      )
    },
    sähköpostiButton: function () {
      return S(
        '.oppilaitos-options .yhteystieto .yhteystieto__linkki a:contains(Avaa sähköpostissa)'
      )
    },
    sähköpostiButtonMailtoContents: function () {
      return S('.oppilaitos-options .yhteystieto .yhteystieto__linkki a').attr(
        'href'
      )
    },
    yhteystiedotTekstinä: function () {
      return extractAsText(S('.oppilaitos-options .copyable-text'))
    },
    isVisible: function () {
      return isElementVisible(elem)
    }
  }

  return api
}

function SuoritusjakoForm() {
  var elem = findSingle('.suoritusjako')
  var createSuoritusjakoButton = function () {
    return S('.create-suoritusjako__button > button')
  }
  var openAdditionalSuoritusjakoFormButton = function () {
    return S('.suoritusjako-form > div:last-child > button.toggle-button')
  }

  var api = {
    contentsAsText: function () {
      return extractAsText(elem)
    },
    ingressi: function () {
      return extractAsText(elem().find('.suoritusjako-form__caption'))
    },
    suoritusvaihtoehdotOtsikkoText: function () {
      return extractAsText(elem().find('.create-suoritusjako-header-row h2'))
    },
    suoritusvaihtoehdotText: function () {
      return extractAsText(elem().find('.create-suoritusjako__list'))
    },
    canCreateSuoritusjako: function () {
      return !createSuoritusjakoButton().is(':disabled')
    },
    selectSuoritus: function (
      lähdejärjestelmänId,
      oppilaitosOid,
      suorituksenTyyppi,
      koulutusmoduulinTunniste
    ) {
      function option() {
        return S(
          '.create-suoritusjako__list input[id="' +
            [
              lähdejärjestelmänId,
              oppilaitosOid,
              suorituksenTyyppi,
              koulutusmoduulinTunniste
            ].join('__') +
            '"]'
        )
      }

      return click(option)
    },
    createSuoritusjako: function () {
      return click(createSuoritusjakoButton)
    },
    createAndStoreSuoritusjako: function (name) {
      var lastJako = Suoritusjako(
        '.suoritusjako-form__link-list > li:last-child > .suoritusjako-link'
      )
      return seq(
        click(createSuoritusjakoButton),
        wait.until(lastJako.isVisible),
        function () {
          var secret = lastJako.url().split('/')
          window.secrets[name] = secret[secret.length - 1]
        }
      )
    },
    openAdditionalSuoritusjakoForm: function () {
      return click(openAdditionalSuoritusjakoFormButton)
    },
    suoritusjako: function (selectorOrIndex) {
      return Suoritusjako(selectorOrIndex)
    },
    isVisible: function () {
      return isElementVisible(elem)
    }
  }

  return api
}

function Suoritusjako(selectorOrIndex) {
  var elem =
    typeof selectorOrIndex === 'number'
      ? findSingle(
          '.suoritusjako-form__link-list > li:nth-child(' +
            selectorOrIndex +
            ') > .suoritusjako-link'
        )
      : findSingle(selectorOrIndex)

  var pageApi = Page(elem)

  var api = {
    isVisible: function () {
      return isElementVisible(elem)
    },
    url: function () {
      return elem().find('.suoritusjako-link__url input').val()
    },
    voimassaoloaika: function () {
      return elem().find('.suoritusjako-link__expiration input').val()
    },
    setVoimassaoloaika: function (value) {
      return function () {
        return pageApi.setInputValue(
          '.suoritusjako-link__expiration input',
          value
        )()
      }
    },
    esikatseluLinkHref: function () {
      return elem().find('.suoritusjako-link__preview a').attr('href')
    },
    feedbackText: {
      isVisible: function () {
        return isElementVisible(
          elem().find(
            '.suoritusjako-link__expiration .date-input-feedback .feedback'
          )
        )
      },
      value: function () {
        return extractAsText(
          elem().find(
            '.suoritusjako-link__expiration .date-input-feedback .feedback'
          )
        )
      }
    }
  }

  return api
}
