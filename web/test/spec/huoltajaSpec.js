import { Authentication } from '../page/authentication.js'
import { LandingPage, KorhoPankki } from '../page/landingPage.js'
import { OmatTiedotPage } from '../page/omatTiedotPage.js'
import { OpinnotPage } from '../page/opinnotPage.js'
import { expect } from '../util/chai.esm.js'
import {
  wait,
  click,
  extractAsText,
  S,
  isElementVisible,
  findFirst
} from '../util/testHelpers.js'

describe('Huollettavien tiedot', function () {
  let omattiedot = OmatTiedotPage()
  let opinnot = OpinnotPage()
  let authentication = Authentication()
  let etusivu = LandingPage()
  let korhopankki = KorhoPankki()

  describe('Kun huollettavalla on opintoja Koskessa', function () {
    before(
      authentication.logout,
      etusivu.openPage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('030300-5215'),
      wait.until(omattiedot.isVisible)
    )

    it('näytetään omat tiedot', function () {
      verifyOppijaEmpty('Opintoni', 'Faija EiOpintojaKoskessa\ns. 3.3.1900')
    })

    it('on näkyvissä opiskelijan valinnan dropdown', function () {
      expect(omattiedot.selectOpiskelijaNäkyvissä()).to.equal(true)
    })

    describe('Kun painetaan Huollettavien opintotiedot-nappia', function () {
      before(wait.until(omattiedot.omatTiedotNäkyvissä))
      before(click(omattiedot.selectOpiskelija))

      it('näytetään opiskelijan valinta', function () {
        expect(omattiedot.opiskelijanValintaNimet()).to.deep.equal([
          'Essi Eskari',
          'Olli Oiditon (Ei opintoja)',
          'Ynjevi Ylioppilaslukiolainen'
        ])
      })

      describe('Huollettavan jolla ei ole oidia', function () {
        before(click(omattiedot.opiskelijanValinta('Olli')))
        it('valitsemisesta ei tapahdu mitään', function () {
          verifyOppijaEmpty('Opintoni', 'Faija EiOpintojaKoskessa\ns. 3.3.1900')
        })
      })

      describe('Kun valitaan huollettava', function () {
        before(click(omattiedot.opiskelijanValinta('Essi')))
        before(
          wait.until(function () {
            return omattiedot.oppija() === 'Huollettavani opinnot'
          })
        )

        it('näytetään huollettavan tiedot', function () {
          verifyOppija(
            'Huollettavani opinnot',
            'Essi Eskari\ns. 30.9.1996',
            [
              'Jyväskylän normaalikoulu',
              'Päiväkoti Touhula',
              'Päiväkoti Majakka'
            ],
            [
              'Peruskoulun esiopetus (2022—, läsnä)',
              'Peruskoulun esiopetus (2014—2015, valmistunut)',
              'Päiväkodin esiopetus (2014—, läsnä)',
              'Päiväkodin esiopetus (2014—, läsnä)'
            ]
          )
        })
      })

      describe('Kun valitaan yliopistotutkinnon suorittanut huollettava', function () {
        before(click(omattiedot.opiskelijanValinta('Ynjevi')))
        before(
          wait.until(function () {
            return (
              omattiedot.headerNimi() ===
              'Ynjevi Ylioppilaslukiolainen\ns. 8.6.1998'
            )
          })
        )

        it('näytetään huollettavan tiedot', function () {
          verifyOppija(
            'Huollettavani opinnot',
            'Ynjevi Ylioppilaslukiolainen\ns. 8.6.1998',
            ['Jyväskylän normaalikoulu'],
            ['Ylioppilastutkinto', 'Lukion oppimäärä (2012—2016, valmistunut)']
          )
        })

        describe('Ylioppilastutkinnon koesuoritukset', function () {
          before(opinnot.valitseOmatTiedotOpiskeluoikeus('Ylioppilastutkinto'))

          it('näytetään', function () {
            expect(
              extractAsText(S('.ylioppilastutkinnonsuoritus .osasuoritukset'))
            ).to.equal(
              'Tutkintokerta Koe Pisteet Arvosana\n' +
                '2012 kevät Äidinkielen koe, suomi 46 Lubenter approbatur Näytä koesuoritus\n' +
                '2012 kevät Ruotsi, keskipitkä oppimäärä 166 Cum laude approbatur Näytä koesuoritus\n' +
                '2012 kevät Englanti, pitkä oppimäärä 210 Cum laude approbatur Näytä koesuoritus\n' +
                '2012 kevät Maantiede 26 Magna cum laude approbatur Näytä koesuoritus\n' +
                '2012 kevät Matematiikan koe, lyhyt oppimäärä 59 Laudatur Näytä koesuoritus'
            )
          })

          it('koesuoritus linkissä on huollettavan oid', function () {
            expect(findFirst('.koesuoritus a')().attr('href')).to.includes(
              '/koski/koesuoritus/2345K_XX_12345.pdf?huollettava=1.2.246.562.24.'
            )
          })
        })
      })
    })
  })

  describe('Kun huollettavan Koski-tietoja haettaessa tulee ongelmia', function () {
    before(
      authentication.logout,
      etusivu.openPage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('030300-7053'),
      wait.until(omattiedot.isVisible)
    )

    it('näytetään huollettavan tiedot ja varoitus', function () {
      expect(extractAsText(S('.varoitus'))).to.equal(
        'Huollettavan opintoja ei voida tällä hetkellä näyttää.'
      )
    })
  })

  describe('Kun huollettavia ei ole Koskessa', function () {
    before(
      authentication.logout,
      etusivu.openPage,
      etusivu.login(),
      wait.until(korhopankki.isReady),
      korhopankki.login('080698-967F'),
      wait.until(omattiedot.isVisible)
    )
    before(wait.until(omattiedot.omatTiedotNäkyvissä))

    it('ei ole näkyvissä opiskelijan valinnan dropdownia', function () {
      expect(omattiedot.selectOpiskelijaNäkyvissä()).to.equal(false)
    })
  })

  function verifyOppija(
    expectedHeader,
    expectedName,
    expectedOppilaitokset,
    expectedOpiskeluoikeudet
  ) {
    expect(omattiedot.oppija()).to.equal(expectedHeader)
    expect(omattiedot.headerNimi()).to.equal(expectedName)
    expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal(
      expectedOppilaitokset
    )
    expect(
      opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()
    ).to.deep.equal(expectedOpiskeluoikeudet)
  }

  let expectedHuollettavaEmptyText =
    'Huollettavasi tiedoilla ei löydy opintosuorituksia eikä opiskeluoikeuksia.'
  let expectedOmattiedotEmptyText =
    'Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia.'
  function verifyOppijaEmpty(expectedHeader, expectedName) {
    expect(omattiedot.oppija()).to.equal(expectedHeader)
    expect(omattiedot.headerNimi()).to.equal(expectedName)
    expect(isElementVisible(S('.oppija-content .ei-suorituksia'))).to.equal(
      true
    )
    const expectedHeader2 =
      expectedHeader === 'Opintoni'
        ? expectedOmattiedotEmptyText
        : expectedHuollettavaEmptyText
    expect(extractAsText(S('.oppija-content .ei-suorituksia h2'))).to.equal(
      expectedHeader2
    )
    expect(isElementVisible(omattiedot.suoritusjakoButton())).to.equal(false)
    expect(isElementVisible(omattiedot.virheraportointiButton())).to.equal(
      false
    )
  }
})
