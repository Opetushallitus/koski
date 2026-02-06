describe('Omat tiedot - lukio', function () {
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()
  var etusivu = LandingPage()
  var korhopankki = KorhoPankki()
  before(authentication.login(), resetFixtures)

  describe('Mobiilinäkymä', function () {
    before(authentication.logout, etusivu.openMobilePage)

    describe('Lukiosta valmistunut', function () {
      before(
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('020655-2479'),
        wait.until(omattiedot.isVisible)
      )

      describe('Kun opiskeluoikeus avataan', function () {
        before(
          opinnot.valitseOmatTiedotOpiskeluoikeus(
            'Lukion oppimäärä (2012—2016, valmistunut)'
          )
        )

        it('Näytetään taulukko oppiaineista', function () {
          expect(extractAsText(S('table.omattiedot-suoritukset'))).to.equal(
            'Oppiaine Arvosana\n' +
            '+\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus\n' +
            '(8 kurssia) 9\n' +
            '+\n' +
            'A1-kieli, englanti\n' +
            '(9 kurssia) 9\n' +
            '+\n' +
            'B1-kieli, ruotsi\n' +
            '(5 kurssia) 7\n' +
            '+\n' +
            'B3-kieli, latina\n' +
            '(2 kurssia) 9\n' +
            '+\n' +
            'Äidinkielenomainen kieli A-oppimäärä, suomi\n' +
            '(2 kurssia) 8\n' +
            '+\n' +
            'Matematiikka, pitkä oppimäärä\n' +
            '(14 kurssia) 9\n' +
            '+\n' +
            'Biologia\n' +
            '(7,5 kurssia) 9\n' +
            '+\n' +
            'Maantieto\n' +
            '(2 kurssia) 8\n' +
            '+\n' +
            'Fysiikka\n' +
            '(13 kurssia) 8\n' +
            '+\n' +
            'Kemia\n' +
            '(8 kurssia) 8\n' +
            '+\n' +
            'Uskonto/Elämänkatsomustieto\n' +
            '(3 kurssia) 8\n' +
            '+\n' +
            'Filosofia\n' +
            '(1 kurssia) 8\n' +
            '+\n' +
            'Psykologia\n' +
            '(1 kurssia) 9\n' +
            '+\n' +
            'Historia\n' +
            '(4 kurssia) 7\n' +
            '+\n' +
            'Yhteiskuntaoppi\n' +
            '(2 kurssia) 8\n' +
            '+\n' +
            'Liikunta\n' +
            '(3 kurssia) 9\n' +
            '+\n' +
            'Musiikki\n' +
            '(1 kurssia) 8\n' +
            '+\n' +
            'Kuvataide\n' +
            '(2 kurssia) 9\n' +
            '+\n' +
            'Terveystieto\n' +
            '(1 kurssia) 9\n' +
            '+\n' +
            'Tanssi ja liike *\n' +
            '(1 kurssia) 10\n' +
            '+\n' +
            'Teemaopinnot\n' +
            '(1 kurssia) S\n' +
            '+\n' +
            'Oman äidinkielen opinnot\n' +
            '(1 kurssia) S'
          )
        })

        describe('Kun oppiainetta klikataan', function () {
          before(
            opinnot.valitseOmatTiedotOpiskeluoikeudenLukionSuoritus(
              'Lukion oppimäärä (2012—2016, valmistunut)',
              'Äidinkieli ja kirjallisuus'
            )
          )

          it('Näytetään oppiaineen kurssit', function () {
            expect(
              extractAsText(
                S(
                  'table.omattiedot-suoritukset tr.oppiaine-kurssit table.kurssilista-mobile'
                )
              )
            ).to.equal(
              'Kurssi Arvosana Lisätiedot\n' +
              'ÄI1 8 Avaa\n' +
              'ÄI2 8 Avaa\n' +
              'ÄI3 8 Avaa\n' +
              'ÄI4 8 Avaa\n' +
              'ÄI5 9 Avaa\n' +
              'ÄI6 9 Avaa\n' +
              'ÄI8 9 Avaa\n' +
              'ÄI9 9 Avaa'
            )
          })
        })
      })
    })

    describe('Lukiosta valmistunut 2019', function () {
      before(
        authentication.logout,
        etusivu.openMobilePage,
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('250605A518Y'),
        wait.until(omattiedot.isVisible)
      )

      describe('Kun opiskeluoikeus avataan', function () {
        before(
          opinnot.valitseOmatTiedotOpiskeluoikeus(
            'Lukion oppimäärä (2019—2021, valmistunut)'
          )
        )

        it('Näytetään taulukko oppiaineista', function () {
          expect(extractAsText(S('table.omattiedot-suoritukset'))).to.equal(
            'Oppiaine Arvosana\n' +
            '+\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus\n' +
            '(6 opintopistettä) 9\n' +
            '+\n' +
            'Matematiikka, lyhyt oppimäärä\n' +
            '(8 opintopistettä) 9\n' +
            '+\n' +
            'Opinto-ohjaus\n' +
            '(4 opintopistettä) H\n' +
            '+\n' +
            'Uskonto/Elämänkatsomustieto\n' +
            '(1,5 opintopistettä) 4\n' +
            '+\n' +
            'Äidinkielenomainen kieli A-oppimäärä, ruotsi\n' +
            '(1 opintopistettä) 9\n' +
            '+\n' +
            'A-kieli, espanja\n' +
            '(2 opintopistettä) 9\n' +
            '+\n' +
            'Fysiikka\n' +
            '(87 opintopistettä) 10\n' +
            'Kemia 4\n' +
            '+\n' +
            'Tanssi ja liike, valinnainen *\n' +
            '(52 opintopistettä) 8\n' +
            '+\n' +
            'Lukiodiplomit\n' +
            '(4 opintopistettä) -\n' +
            '+\n' +
            'Muut suoritukset\n' +
            '(7 opintopistettä) -\n' +
            '+\n' +
            'Teemaopinnot\n' +
            '(1 opintopistettä) -'
          )
        })

        describe('Kun oppiainetta klikataan', function () {
          before(
            opinnot.valitseOmatTiedotOpiskeluoikeudenLukionSuoritus(
              'Lukion oppimäärä (2019—2021, valmistunut)',
              'Äidinkieli ja kirjallisuus'
            )
          )

          it('Näytetään oppiaineen kurssit', function () {
            expect(
              extractAsText(
                S(
                  'table.omattiedot-suoritukset tr.oppiaine-kurssit table.kurssilista-mobile'
                )
              )
            ).to.equal(
              'Kurssi Arvosana Lisätiedot\n' +
              'ÄI1 8 Avaa\n' +
              'ÄI2 8 Avaa\n' +
              'ÄI3 8 Avaa'
            )
          })
        })
      })
    })
  })

  describe('Työpöytänäkymä', function () {
    describe('Lukiosta valmistunut', function () {
      before(authentication.logout, etusivu.openPage)
      before(
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('020655-2479'),
        wait.until(omattiedot.isVisible)
      )

      it('Opiskeluoikeus näytetään listassa', function () {
        expect(
          opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()
        ).to.contain('Lukion oppimäärä (2012—2016, valmistunut)')
      })

      describe('Kun opiskeluoikeus avataan', function () {
        before(
          opinnot.valitseOmatTiedotOpiskeluoikeus(
            'Lukion oppimäärä (2012—2016, valmistunut)'
          )
        )
        it('Näytetään taulukko oppiaineista sekä kurssisuorituksista', function () {
          expect(extractAsText(S('table.omattiedot-suoritukset'))).to.equal(
            'Oppiaine Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus\n' +
            '(8 kurssia) 9\n' +
            'ÄI1\n' +
            '8 ÄI2\n' +
            '8 ÄI3\n' +
            '8 ÄI4\n' +
            '8 ÄI5\n' +
            '9 ÄI6\n' +
            '9 ÄI8\n' +
            '9 ÄI9\n' +
            '9\n' +
            'A1-kieli, englanti\n' +
            '(9 kurssia) 9\n' +
            'ENA1\n' +
            '10 ENA2\n' +
            '10 ENA3\n' +
            '9 ENA4\n' +
            '9 ENA5\n' +
            '9 ENA6\n' +
            '8 ENA7\n' +
            '8 ENA8\n' +
            '9 ENA 10 *\n' +
            'S\n' +
            'B1-kieli, ruotsi\n' +
            '(5 kurssia) 7\n' +
            'RUB11\n' +
            '9 RUB12\n' +
            '8 RUB13\n' +
            '7 RUB14\n' +
            '7 RUB15\n' +
            '6\n' +
            'B3-kieli, latina\n' +
            '(2 kurssia) 9\n' +
            'LAB31\n' +
            '9 LAB32\n' +
            '8\n' +
            'Äidinkielenomainen kieli A-oppimäärä, suomi\n' +
            '(2 kurssia) 8\n' +
            'ÄI1\n' +
            '9 ÄI2\n' +
            '8\n' +
            'Matematiikka, pitkä oppimäärä\n' +
            '(14 kurssia) 9\n' +
            'MAA1 *\n' +
            '9 MAA2\n' +
            '10 MAA3\n' +
            '8 MAA4\n' +
            '10 MAA5\n' +
            '7 MAA6\n' +
            '9 MAA7\n' +
            '8 MAA8\n' +
            '7 MAA9\n' +
            '9 MAA10\n' +
            '8 MAA11\n' +
            '8 MAA12\n' +
            '10 MAA13\n' +
            'H MAA14 *\n' +
            '9 MAA16 *\n' +
            '9\n' +
            'Biologia\n' +
            '(7,5 kurssia) 9\n' +
            'BI1\n' +
            '8 BI2\n' +
            '9 BI3\n' +
            '8 BI4\n' +
            '9 BI5\n' +
            '10 BI6 *\n' +
            'S BI7 *\n' +
            'S BI8 *\n' +
            'S\n' +
            'Maantieto\n' +
            '(2 kurssia) 8\n' +
            'GE1\n' +
            '9 GE2\n' +
            '7\n' +
            'Fysiikka\n' +
            '(13 kurssia) 8\n' +
            'FY1\n' +
            '8 FY2\n' +
            '9 FY3\n' +
            '9 FY4\n' +
            '7 FY5\n' +
            '8 FY6\n' +
            '7 FY7\n' +
            '8 FY8 *\n' +
            '7 FY9 *\n' +
            '7 FY10 *\n' +
            'S FY11 *\n' +
            'S FY12 *\n' +
            'S FY13 *\n' +
            'S\n' +
            'Kemia\n' +
            '(8 kurssia) 8\n' +
            'KE1\n' +
            '8 KE2\n' +
            '9 KE3\n' +
            '9 KE4\n' +
            '5 KE5\n' +
            '7 KE6 *\n' +
            '5 KE7 *\n' +
            'S KE8 *\n' +
            'S\n' +
            'Uskonto/Elämänkatsomustieto\n' +
            '(3 kurssia) 8\n' +
            'UE1\n' +
            '8 UE2\n' +
            '7 UE3\n' +
            '8\n' +
            'Filosofia\n' +
            '(1 kurssia) 8\n' +
            'FI1\n' +
            '8\n' +
            'Psykologia\n' +
            '(1 kurssia) 9\n' +
            'PS1\n' +
            '9\n' +
            'Historia\n' +
            '(4 kurssia) 7\n' +
            'HI1\n' +
            '7 HI2\n' +
            '8 HI3\n' +
            '7 HI4\n' +
            '6\n' +
            'Yhteiskuntaoppi\n' +
            '(2 kurssia) 8\n' +
            'YH1\n' +
            '8 YH2\n' +
            '8\n' +
            'Liikunta\n' +
            '(3 kurssia) 9\n' +
            'LI1\n' +
            '8 LI2\n' +
            '9 LI12 *\n' +
            'S\n' +
            'Musiikki\n' +
            '(1 kurssia) 8\n' +
            'MU1\n' +
            '8\n' +
            'Kuvataide\n' +
            '(2 kurssia) 9\n' +
            'KU1\n' +
            '8 KU2 *\n' +
            '9\n' +
            'Terveystieto\n' +
            '(1 kurssia) 9\n' +
            'TE1\n' +
            '8\n' +
            'Tanssi ja liike\n' +
            '*\n' +
            '(1 kurssia) 10\n' +
            'ITT1 *\n' +
            '10\n' +
            'Teemaopinnot\n' +
            '(1 kurssia) S\n' +
            'MTA *\n' +
            'S\n' +
            'Oman äidinkielen opinnot\n' +
            '(1 kurssia) S\n' +
            'OA1 *\n' +
            'S'
          )
        })
      })
    })

    describe('Lukiosta valmistunut 2019', function () {
      before(authentication.logout, etusivu.openPage)
      before(
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('250605A518Y'),
        wait.until(omattiedot.isVisible)
      )

      it('Opiskeluoikeus näytetään listassa', function () {
        expect(
          opinnot.opiskeluoikeudet.omatTiedotOpiskeluoikeuksienOtsikot()
        ).to.contain('Lukion oppimäärä (2019—2021, valmistunut)')
      })

      describe('Kun opiskeluoikeus avataan', function () {
        before(
          opinnot.valitseOmatTiedotOpiskeluoikeus(
            'Lukion oppimäärä (2019—2021, valmistunut)'
          )
        )
        it('Näytetään taulukko oppiaineista sekä kurssisuorituksista', function () {
          expect(extractAsText(S('table.omattiedot-suoritukset'))).to.equal(
            'Oppiaine Arvosana\n' +
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus\n' +
            '(6 opintopistettä) 9\n' +
            'ÄI1\n' +
            '8 ÄI2\n' +
            '8 ÄI3\n' +
            '8\n' +
            'Matematiikka, lyhyt oppimäärä\n' +
            '(8 opintopistettä) 9\n' +
            'MAY1\n' +
            '8 MAB2\n' +
            '8 MAB3\n' +
            '8 MAB4\n' +
            '9\n' +
            'Opinto-ohjaus\n' +
            '(4 opintopistettä) H\n' +
            'OP1\n' +
            'H OP2\n' +
            'S\n' +
            'Uskonto/Elämänkatsomustieto\n' +
            '(1,5 opintopistettä) 4\n' +
            'UE1\n' +
            '4\n' +
            'Äidinkielenomainen kieli A-oppimäärä, ruotsi\n' +
            '(1 opintopistettä) 9\n' +
            'RUA4\n' +
            '7\n' +
            'A-kieli, espanja\n' +
            '(2 opintopistettä) 9\n' +
            'VKA1\n' +
            '7 VKA8\n' +
            '7\n' +
            'Fysiikka\n' +
            '(87 opintopistettä) 10\n' +
            'FY1\n' +
            '10 FY2\n' +
            '10 FY3\n' +
            '10 FY123 *\n' +
            '10 FY124 *\n' +
            'S\n' +
            'Kemia 4\n' +
            'Tanssi ja liike, valinnainen\n' +
            '*\n' +
            '(52 opintopistettä) 8\n' +
            'ITT234 *\n' +
            '10 ITT235 *\n' +
            '10\n' +
            'Lukiodiplomit\n' +
            '(4 opintopistettä) -\n' +
            'MELD5\n' +
            '7 KÄLD3\n' +
            '9\n' +
            'Muut suoritukset\n' +
            '(7 opintopistettä) -\n' +
            'KE3\n' +
            '10 HAI765 *\n' +
            'S VKA1\n' +
            '10 ENA1\n' +
            '10\n' +
            'Teemaopinnot\n' +
            '(1 opintopistettä) -\n' +
            'KAN200 *\n' +
            'S'
          )
        })
      })
    })

    describe('Lukion aineopinnot 2019, erityinen tutkinto', function () {
      before(authentication.logout, etusivu.openPage)
      before(
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('010705A6119'),
        wait.until(omattiedot.isVisible)
      )

      describe('Kun opiskeluoikeus avataan', function () {
        before(
          opinnot.valitseOmatTiedotOpiskeluoikeus(
            'Lukion aineopinnot (2019—2021, valmistunut)'
          )
        )

        it('näyttää erityisenä tutkintona suoritetun oppiaineen laajuuden oikein', function () {
          expect(
            extractAsText(
              S('table.omattiedot-suoritukset tr.oppiaine-header:contains("Biologia")')
            )
          ).to.contain('(6 opintopistettä)')
        })

        it('näyttää ** merkinnän erityisenä tutkintona suoritetulla oppiaineella', function () {
          expect(
            extractAsText(
              S('table.omattiedot-suoritukset tr.oppiaine-header:contains("Biologia")')
            )
          ).to.contain('**')
        })

        it('näyttää footnote-kuvauksen sivun alalaidassa', function () {
          expect(extractAsText(S('.selitteet'))).to.contain(
            '** = erityisenä tutkintona suoritettu oppiaine'
          )
        })

        it('näyttää yhteensä-laajuuden oikein', function () {
          expect(extractAsText(S('.kurssit-yhteensä'))).to.contain(
            'Arvioitujen osasuoritusten laajuus yhteensä: 167,5 Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 164,0'
          )
        })
      })
    })
  })
})
