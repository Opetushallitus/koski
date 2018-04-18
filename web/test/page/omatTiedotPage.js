function OmatTiedotPage() {

  var api = {
    openPage: function() {
      return openPage('/koski/omattiedot', api.isVisible)()
    },
    isVisible: function() {
      return isElementVisible(S('.omattiedot')) && !isLoading()
    },
    nimi: function() {
      return S('.user-info .name').text()
    },
    oppija: function() {
      return S('.main-content.oppija h1').text().replace('JSON', '')
    },
    virhe: function() {
      return S('.ei-suorituksia').text()
    },
    ingressi: function() {
      return S('header .header__caption p').text()
    },
    palvelussaNäkyvätTiedotButton: function() {
      return S('header span:contains(Mitkä tiedot palvelussa näkyvät?)')
    },
    palvelussaNäkyvätTiedotText: function() {
      var el = findFirstNotThrowing('header .tiedot-palvelussa')
      return el ? extractAsText(el) : ''
    },
    palvelussaNäkyvätTiedotCloseButton: function() {
      return S('header .popup__close-button')
    },
    virheraportointiButton: function() {
      return S('header a span:contains(Onko suorituksissasi virhe?)')
    },
    virheraportointiForm: VirheraportointiForm(),
    headerNimi: function() {
      var el = findFirstNotThrowing('header .header__name')
      return el ? extractAsText(el) : ''
    }
  }
  return api
}

function VirheraportointiForm() {
  var pageApi = Page(findSingle('#lander-page .omattiedot'))
  var elem = findSingle('.virheraportointi')

  var api = {
    self: function() {
      return elem
    },
    contentsAsText: function() {
      return extractAsText(elem)
    },
    acceptDisclaimer: function() {
      function disclaimerToggle() {
        return S('.virheraportointi span:contains(Asiani koskee tietoa, joka näkyy, tai kuuluisi yllämainitun perusteella näkyä Koski-palvelussa.)')
      }

      return click(disclaimerToggle)()
    },
    selectOppilaitos: function(oid) {
      function option() {
        return S('.oppilaitos-options input[value="' + oid + '"]')
      }

      return click(option)
    },
    oppilaitosNames: function() {
      return toArray(elem().find('.oppilaitos-options label')).map(function(i) { return i.innerHTML })
    },
    oppilaitosOids: function() {
      return toArray(elem().find('.oppilaitos-options input[type="radio"]')).map(function(i) { return i.value })
    },
    oppilaitosOptionsText: function() {
      return extractAsText(elem().find('.oppilaitos-options'))
    },
    oppilaitosPicker: function() {
      return findSingle('.oppilaitos-options .oppilaitos-picker')
    },
    selectMuuOppilaitos: function(nimi) {
      return OrganisaatioHaku(this.oppilaitosPicker()).select(nimi)
    },
    yhteystiedot: function() {
      return extractAsText(S('.oppilaitos-options .yhteystieto .yhteystieto__contact-info'))
    },
    sähköpostiButton: function() {
      return S('.oppilaitos-options .yhteystieto .yhteystieto__linkki a button:contains(Avaa sähköpostissa)')
    },
    sähköpostiButtonMailtoContents: function() {
      return S('.oppilaitos-options .yhteystieto .yhteystieto__linkki a').attr('href')
    },
    yhteystiedotTekstinä: function() {
      return extractAsText(S('.oppilaitos-options .copyable-text'))
    },
    isVisible: function() {
      return isElementVisible(elem)
    }
  }

  return api
}
