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
                'ÄI9 9 Avaa\n' +
                '(Keskiarvo 8,5)'
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
              'Matematiikka, pitkä oppimäärä\n' +
              '(6 opintopistettä) 9\n' +
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
              '9 Keskiarvo 8,5\n' +
              '(8,5)\n' +
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
              'S Keskiarvo 9,0\n' +
              '(9,0)\n' +
              'B1-kieli, ruotsi\n' +
              '(5 kurssia) 7\n' +
              'RUB11\n' +
              '9 RUB12\n' +
              '8 RUB13\n' +
              '7 RUB14\n' +
              '7 RUB15\n' +
              '6 Keskiarvo 7,4\n' +
              '(7,4)\n' +
              'B3-kieli, latina\n' +
              '(2 kurssia) 9\n' +
              'LAB31\n' +
              '9 LAB32\n' +
              '8 Keskiarvo 8,5\n' +
              '(8,5)\n' +
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
              '9 Keskiarvo 8,6\n' +
              '(8,6)\n' +
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
              'S Keskiarvo 8,8\n' +
              '(8,8)\n' +
              'Maantieto\n' +
              '(2 kurssia) 8\n' +
              'GE1\n' +
              '9 GE2\n' +
              '7 Keskiarvo 8,0\n' +
              '(8,0)\n' +
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
              'S Keskiarvo 7,8\n' +
              '(7,8)\n' +
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
              'S Keskiarvo 7,2\n' +
              '(7,2)\n' +
              'Uskonto/Elämänkatsomustieto\n' +
              '(3 kurssia) 8\n' +
              'UE1\n' +
              '8 UE2\n' +
              '7 UE3\n' +
              '8 Keskiarvo 7,7\n' +
              '(7,7)\n' +
              'Filosofia\n' +
              '(1 kurssia) 8\n' +
              'FI1\n' +
              '8 Keskiarvo 8,0\n' +
              '(8,0)\n' +
              'Psykologia\n' +
              '(1 kurssia) 9\n' +
              'PS1\n' +
              '9 Keskiarvo 9,0\n' +
              '(9,0)\n' +
              'Historia\n' +
              '(4 kurssia) 7\n' +
              'HI1\n' +
              '7 HI2\n' +
              '8 HI3\n' +
              '7 HI4\n' +
              '6 Keskiarvo 7,0\n' +
              '(7,0)\n' +
              'Yhteiskuntaoppi\n' +
              '(2 kurssia) 8\n' +
              'YH1\n' +
              '8 YH2\n' +
              '8 Keskiarvo 8,0\n' +
              '(8,0)\n' +
              'Liikunta\n' +
              '(3 kurssia) 9\n' +
              'LI1\n' +
              '8 LI2\n' +
              '9 LI12 *\n' +
              'S Keskiarvo 8,5\n' +
              '(8,5)\n' +
              'Musiikki\n' +
              '(1 kurssia) 8\n' +
              'MU1\n' +
              '8 Keskiarvo 8,0\n' +
              '(8,0)\n' +
              'Kuvataide\n' +
              '(2 kurssia) 9\n' +
              'KU1\n' +
              '8 KU2 *\n' +
              '9 Keskiarvo 8,5\n' +
              '(8,5)\n' +
              'Terveystieto\n' +
              '(1 kurssia) 9\n' +
              'TE1\n' +
              '8 Keskiarvo 8,0\n' +
              '(8,0)\n' +
              'Tanssi ja liike\n' +
              '*\n' +
              '(1 kurssia) 10\n' +
              'ITT1 *\n' +
              '10 Keskiarvo 10,0\n' +
              '(10,0)\n' +
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
              'Matematiikka, pitkä oppimäärä\n' +
              '(6 opintopistettä) 9\n' +
              'MAB2\n' +
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

    describe('IB-Predicted', function () {
      before(authentication.logout, etusivu.openPage)
      before(
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('071096-317K'),
        wait.until(omattiedot.isVisible)
      )

      describe('Kun opiskeluoikeus avataan', function () {
        before(
          opinnot.valitseOmatTiedotOpiskeluoikeus(
            'IB-tutkinto (International Baccalaureate) (2012—2016, valmistunut)'
          )
        )

        it('Näytetään suoritustaulukko aineryhmittäin', function () {
          expect(extractAsText(S('div.aineryhmat'))).to.equal(
            'Studies in language and literature\n' +
              'Oppiaine Arvosana\n' +
              'Language A: literature, suomi\n' +
              '(9 kurssia) 4 *\n' +
              'FIN_S1\n' +
              '4 FIN_S2\n' +
              '4 FIN_S3\n' +
              'S FIN_S4\n' +
              '5 FIN_S5\n' +
              '6 FIN_S6\n' +
              '5 FIN_S7\n' +
              '5 FIN_S8\n' +
              'S FIN_S9\n' +
              '5\n' +
              'Language A: language and literature, englanti\n' +
              '(6 kurssia) 7 *\n' +
              'ENG_B_H1\n' +
              '6 ENG_B_H2\n' +
              '7 ENG_B_H4\n' +
              'S ENG_B_H5\n' +
              '6 ENG_B_H6\n' +
              '6 ENG_B_H8\n' +
              '5\n' +
              'Individuals and societies\n' +
              'Oppiaine Arvosana\n' +
              'History\n' +
              '(6 kurssia) 6 *\n' +
              'HIS_H3\n' +
              '6 HIS_H4\n' +
              '6 HIS_H5\n' +
              '7 HIS_H6\n' +
              '6 HIS_H7\n' +
              '1 HIS_H9\n' +
              'S\n' +
              'Psychology\n' +
              '(9 kurssia) 7 *\n' +
              'PSY_S1\n' +
              '6 PSY_S2\n' +
              '6 PSY_S3\n' +
              '6 PSY_S4\n' +
              '5 PSY_S5\n' +
              'S PSY_S6\n' +
              '6 PSY_S7\n' +
              '5 PSY_S8\n' +
              '2 PSY_S9\n' +
              'S\n' +
              'Experimental sciences\n' +
              'Oppiaine Arvosana\n' +
              'Biology\n' +
              '(9 kurssia) 5 *\n' +
              'BIO_H1\n' +
              '5 BIO_H2\n' +
              '4 BIO_H3\n' +
              'S BIO_H4\n' +
              '5 BIO_H5\n' +
              '5 BIO_H6\n' +
              '2 BIO_H7\n' +
              '3 BIO_H8\n' +
              '4 BIO_H9\n' +
              '1\n' +
              'Mathematics\n' +
              'Oppiaine Arvosana\n' +
              'Mathematical studies\n' +
              '(6 kurssia) 5 *\n' +
              'MATST_S1\n' +
              '5 MATST_S2\n' +
              '7 MATST_S3\n' +
              '6 MATST_S4\n' +
              '6 MATST_S5\n' +
              '4 MATST_S6\n' +
              'S\n' +
              '* = ennustettu arvosana'
          )
        })

        describe('Kun valitaan Pre-IB -välilehti', function () {
          before(
            opinnot.valitseSuoritus(
              'IB-tutkinto (International Baccalaureate) (2012—2016, valmistunut)',
              'Pre-IB',
              true
            )
          )

          it('Näytetään Pre-IB -oppiaineet sekä kurssit', function () {
            expect(extractAsText(S('table.omattiedot-suoritukset'))).to.equal(
              'Oppiaine Arvosana\n' +
                'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus\n' +
                '(3 kurssia) 8\n' +
                'ÄI1\n' +
                '8 ÄI2\n' +
                '8 ÄI3\n' +
                '8 Keskiarvo 8,0\n' +
                '(8,0)\n' +
                'A1-kieli, englanti\n' +
                '(3 kurssia) 10\n' +
                'ENA1\n' +
                '10 ENA2\n' +
                '10 ENA5\n' +
                '10 Keskiarvo 10,0\n' +
                '(10,0)\n' +
                'B1-kieli, ruotsi\n' +
                '(2 kurssia) 7\n' +
                'RUB11\n' +
                '8 RUB12\n' +
                '7 Keskiarvo 7,5\n' +
                '(7,5)\n' +
                'B2-kieli, ranska\n' +
                '(1 kurssia) 9\n' +
                'RAN3 *\n' +
                '9 Keskiarvo 9,0\n' +
                '(9,0)\n' +
                'B3-kieli, espanja\n' +
                '(1 kurssia) 6\n' +
                'ES1 *\n' +
                'S\n' +
                'Matematiikka, pitkä oppimäärä\n' +
                '(4 kurssia) 7\n' +
                'MAA11\n' +
                '7 MAA12\n' +
                '7 MAA13\n' +
                '7 MAA2\n' +
                '7 Keskiarvo 7,0\n' +
                '(7,0)\n' +
                'Biologia\n' +
                '(2 kurssia) 8\n' +
                'BI1\n' +
                '8 BI10 *\n' +
                'S Keskiarvo 8,0\n' +
                '(8,0)\n' +
                'Maantieto\n' +
                '(1 kurssia) 10\n' +
                'GE2\n' +
                '10 Keskiarvo 10,0\n' +
                '(10,0)\n' +
                'Fysiikka\n' +
                '(1 kurssia) 7\n' +
                'FY1\n' +
                '7 Keskiarvo 7,0\n' +
                '(7,0)\n' +
                'Kemia\n' +
                '(1 kurssia) 8\n' +
                'KE1\n' +
                '8 Keskiarvo 8,0\n' +
                '(8,0)\n' +
                'Uskonto/Elämänkatsomustieto\n' +
                '(1 kurssia) 10\n' +
                'UK4\n' +
                '10 Keskiarvo 10,0\n' +
                '(10,0)\n' +
                'Filosofia\n' +
                '(1 kurssia) 7\n' +
                'FI1\n' +
                'S\n' +
                'Psykologia\n' +
                '(1 kurssia) 8\n' +
                'PS1\n' +
                '8 Keskiarvo 8,0\n' +
                '(8,0)\n' +
                'Historia\n' +
                '(3 kurssia) 8\n' +
                'HI3\n' +
                '9 HI4\n' +
                '8 HI10 *\n' +
                'S Keskiarvo 8,5\n' +
                '(8,5)\n' +
                'Yhteiskuntaoppi\n' +
                '(1 kurssia) 8\n' +
                'YH1\n' +
                '8 Keskiarvo 8,0\n' +
                '(8,0)\n' +
                'Liikunta\n' +
                '(1 kurssia) 8\n' +
                'LI1\n' +
                '8 Keskiarvo 8,0\n' +
                '(8,0)\n' +
                'Musiikki\n' +
                '(1 kurssia) 8\n' +
                'MU1\n' +
                '8 Keskiarvo 8,0\n' +
                '(8,0)\n' +
                'Kuvataide\n' +
                '(1 kurssia) 9\n' +
                'KU1\n' +
                '9 Keskiarvo 9,0\n' +
                '(9,0)\n' +
                'Terveystieto\n' +
                '(1 kurssia) 7\n' +
                'TE1\n' +
                '7 Keskiarvo 7,0\n' +
                '(7,0)\n' +
                'Opinto-ohjaus\n' +
                '(1 kurssia) 7\n' +
                'OP1\n' +
                'S\n' +
                'Teemaopinnot\n' +
                '(1 kurssia) S\n' +
                'MTA *\n' +
                'S'
            )
          })
        })
      })
    })

    describe('IB-Pre-IB 2019', function () {
      before(authentication.logout, etusivu.openPage)
      before(
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('180300A8736'),
        wait.until(omattiedot.isVisible)
      )

      describe('Kun opiskeluoikeus avataan', function () {
        before(
          opinnot.valitseOmatTiedotOpiskeluoikeus(
            'Pre-IB 2019 (2012—2016, valmistunut)'
          )
        )

        it('Näytetään Pre-IB -oppiaineet sekä kurssit', function () {
          expect(extractAsText(S('table.omattiedot-suoritukset'))).to.equal(
            'Oppiaine Arvosana\n' +
              'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus, valinnainen\n' +
              '(4 opintopistettä) 9\n' +
              'ÄI1\n' +
              '8 ÄI2\n' +
              '8\n' +
              'Matematiikka, pitkä oppimäärä\n' +
              '(4 opintopistettä) 10\n' +
              'MAB2\n' +
              '10 MAB3\n' +
              '10\n' +
              'Uskonto/Elämänkatsomustieto\n' +
              '(2 opintopistettä) 9\n' +
              'UK1\n' +
              '9\n' +
              'Liikunta\n' +
              '(3 opintopistettä) 8\n' +
              'LI2\n' +
              '8 LITT1 *\n' +
              'S\n' +
              'Fysiikka 8\n' +
              'Kemia\n' +
              '(2 opintopistettä) 7\n' +
              'KE1\n' +
              '6\n' +
              'A-kieli, englanti\n' +
              '(4 opintopistettä) 9\n' +
              'ENA1\n' +
              '10 ENA2\n' +
              '9\n' +
              'A-kieli, espanja\n' +
              '(4 opintopistettä) 6\n' +
              'VKA1\n' +
              '6 VKA2\n' +
              '7\n' +
              'Tanssi ja liike, valinnainen\n' +
              '*\n' +
              '(2 opintopistettä) 6\n' +
              'ITT234 *\n' +
              '6 ITT235 *\n' +
              '7\n' +
              'Muut suoritukset\n' +
              '(6 opintopistettä) -\n' +
              'ÄI1\n' +
              '7 VKAAB31\n' +
              '6 RUB11\n' +
              '6\n' +
              'Lukiodiplomit\n' +
              '(2 opintopistettä) -\n' +
              'KULD2\n' +
              '9\n' +
              'Teemaopinnot\n' +
              '(1 opintopistettä) -\n' +
              'HAI765 *\n' +
              'S'
          )
        })
      })
    })

    describe('IB-Final', function () {
      before(authentication.logout, etusivu.openPage)
      before(
        etusivu.login(),
        wait.until(korhopankki.isReady),
        korhopankki.login('040701-432D'),
        wait.until(omattiedot.isVisible)
      )

      describe('Kun opiskeluoikeus avataan', function () {
        before(
          opinnot.valitseOmatTiedotOpiskeluoikeus(
            'IB-tutkinto (International Baccalaureate) (2012—2016, valmistunut)'
          )
        )

        it('Näytetään suoritustaulukko aineryhmittäin', function () {
          expect(extractAsText(S('div.aineryhmat'))).to.equal(
            'Studies in language and literature\n' +
              'Oppiaine Arvosana\n' +
              'Language A: literature, suomi\n' +
              '(9 kurssia) 4\n' +
              'FIN_S1\n' +
              '4 FIN_S2\n' +
              '4 FIN_S3\n' +
              'S FIN_S4\n' +
              '5 FIN_S5\n' +
              '6 FIN_S6\n' +
              '5 FIN_S7\n' +
              '5 FIN_S8\n' +
              'S FIN_S9\n' +
              '5\n' +
              'Language A: language and literature, englanti\n' +
              '(6 kurssia) 7\n' +
              'ENG_B_H1\n' +
              '6 ENG_B_H2\n' +
              '7 ENG_B_H4\n' +
              'S ENG_B_H5\n' +
              '6 ENG_B_H6\n' +
              '6 ENG_B_H8\n' +
              '5\n' +
              'Individuals and societies\n' +
              'Oppiaine Arvosana\n' +
              'History\n' +
              '(6 kurssia) 6\n' +
              'HIS_H3\n' +
              '6 HIS_H4\n' +
              '6 HIS_H5\n' +
              '7 HIS_H6\n' +
              '6 HIS_H7\n' +
              '1 HIS_H9\n' +
              'S\n' +
              'Psychology\n' +
              '(9 kurssia) 7\n' +
              'PSY_S1\n' +
              '6 PSY_S2\n' +
              '6 PSY_S3\n' +
              '6 PSY_S4\n' +
              '5 PSY_S5\n' +
              'S PSY_S6\n' +
              '6 PSY_S7\n' +
              '5 PSY_S8\n' +
              '2 PSY_S9\n' +
              'S\n' +
              'Experimental sciences\n' +
              'Oppiaine Arvosana\n' +
              'Biology\n' +
              '(9 kurssia) 5\n' +
              'BIO_H1\n' +
              '5 BIO_H2\n' +
              '4 BIO_H3\n' +
              'S BIO_H4\n' +
              '5 BIO_H5\n' +
              '5 BIO_H6\n' +
              '2 BIO_H7\n' +
              '3 BIO_H8\n' +
              '4 BIO_H9\n' +
              '1\n' +
              'Mathematics\n' +
              'Oppiaine Arvosana\n' +
              'Mathematical studies\n' +
              '(6 kurssia) 5\n' +
              'MATST_S1\n' +
              '5 MATST_S2\n' +
              '7 MATST_S3\n' +
              '6 MATST_S4\n' +
              '6 MATST_S5\n' +
              '4 MATST_S6\n' +
              'S'
          )
        })
      })
    })
  })
})
