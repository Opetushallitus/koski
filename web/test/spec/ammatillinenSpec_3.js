describe('Ammatillinen koulutus 3', function () {
  before(Authentication().login())

  var addOppija = AddOppijaPage()
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  describe('Ammatillinen perustutkinto', function () {
    before(
      Authentication().login(),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('280618-402H')
    )
    describe('Suoritus valmis, kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)
      describe('Tietojen näyttäminen', function () {
        it('näyttää ammatillisenopiskeluoikeudentyypin tiedot', function () {
          expect(extractAsText(S('.ammatillinenkoulutus'))).to.equal(
            'Ammatillinen koulutus\n' +
            'Stadin ammatti- ja aikuisopisto\n' +
            'Ammatillinen tutkinto 2012 — 2016 , Valmistunut'
          )
        })
        it('näyttää opiskeluoikeuden otsikkotiedot', function () {
          expect(
            opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
          ).to.deep.equal([
            'Stadin ammatti- ja aikuisopisto, Luonto- ja ympäristöalan perustutkinto (2012—2016, valmistunut)'
          ])
        })
        it('näyttää opiskeluoikeuden tiedot', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
            'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 31.5.2016\n' +
            'Tila 31.5.2016 Valmistunut (työnantajan kokonaan rahoittama)\n' +
            '1.9.2012 Läsnä (työnantajan kokonaan rahoittama)'
          )
        })

        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equalIgnoreNewlines(
            'Koulutus Luonto- ja ympäristöalan perustutkinto 361902 62/011/2014\n' +
            'Suoritustapa Ammatillinen perustutkinto\n' +
            'Tutkintonimike Ympäristönhoitaja\nOsaamisala Ympäristöalan osaamisala\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Suorituskieli suomi\n' +
            'Järjestämismuodot 1.9.2013 — , Koulutuksen järjestäminen oppilaitosmuotoisena\n' +
            'Työssäoppimisjaksot 1.1.2014 — 15.3.2014 Jyväskylä , Suomi\n' +
            'Työssäoppimispaikka Sortti-asema\n' +
            'Työtehtävät Toimi harjoittelijana Sortti-asemalla\n' +
            'Laajuus 5 osp\n' +
            'Ryhmä YMP14SN\n' +
            'Painotettu keskiarvo 4,00\n' +
            'Suoritus valmis Vahvistus : 31.5.2016 Helsinki Reijo Reksi , rehtori'
          )
        })

        it('näyttää tutkinnon osat', function () {
          return wait.until(() => {
            let text = extractAsText(
              S('.ammatillisentutkinnonsuoritus > .osasuoritukset')
            )
            if (!(text.length > 0)) {
              return false
            }
            expect(text).to.equalIgnoreNewlines(
              'Sulje kaikki\n' +
              'Ammatilliset tutkinnon osat Laajuus (osp) Arvosana\n' +
              'Kestävällä tavalla toimiminen 40 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 1.1.2015\n' +
              'Ympäristön hoitaminen 35 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Näyttö\n' +
              'Kuvaus Muksulan päiväkodin ympäristövaikutusten arvioiminen ja ympäristön kunnostustöiden tekeminen sekä mittauksien tekeminen ja näytteiden ottaminen\n' +
              'Suorituspaikka Muksulan päiväkoti, Kaarinan kunta\n' +
              'Suoritusaika 1.2.2016 — 1.2.2016\n' +
              'Työssäoppimisen yhteydessä ei\n' +
              'Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Arvioijat Jaana Arstila ( näyttötutkintomestari ) Pekka Saurmann ( näyttötutkintomestari ) Juhani Mykkänen\n' +
              'Arviointikohteet Arviointikohde Arvosana\n' +
              'Työprosessin hallinta 3\n' +
              'Työmenetelmien, -välineiden ja materiaalin hallinta 2\n' +
              'Työn perustana olevan tiedon hallinta 2\n' +
              'Elinikäisen oppimisen avaintaidot 3\n' +
              'Arvioinnista päättäneet Opettaja\n' +
              'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Uusiutuvien energialähteiden hyödyntäminen 15 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Ulkoilureittien rakentaminen ja hoitaminen 15 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Kulttuuriympäristöjen kunnostaminen ja hoitaminen 15 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Näyttö\n' +
              'Kuvaus Sastamalan kunnan kulttuuriympäristöohjelmaan liittyvän Wanhan myllyn lähiympäristön kasvillisuuden kartoittamisen sekä ennallistamisen suunnittelu ja toteutus\n' +
              'Suorituspaikka Sastamalan kunta\n' +
              'Suoritusaika 1.3.2016 — 1.3.2016\n' +
              'Työssäoppimisen yhteydessä ei\n' +
              'Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Arvioijat Jaana Arstila ( näyttötutkintomestari ) Pekka Saurmann ( näyttötutkintomestari ) Juhani Mykkänen\n' +
              'Arviointikohteet Arviointikohde Arvosana\n' +
              'Työprosessin hallinta 3\n' +
              'Työmenetelmien, -välineiden ja materiaalin hallinta 2\n' +
              'Työn perustana olevan tiedon hallinta 2\n' +
              'Elinikäisen oppimisen avaintaidot 3\n' +
              'Arvioinnista päättäneet Opettaja\n' +
              'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Vesistöjen kunnostaminen ja hoitaminen 15 Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Lisätiedot\n' +
              'Muutos arviointiasteikossa\n' +
              'Tutkinnon osa on koulutuksen järjestäjän päätöksellä arvioitu asteikolla hyväksytty/hylätty.\n' +
              'Näyttö\n' +
              'Kuvaus Uimarin järven tilan arviointi ja kunnostus\n' +
              'Suorituspaikka Vesipojat Oy\n' +
              'Suoritusaika 1.4.2016 — 1.4.2016\n' +
              'Työssäoppimisen yhteydessä ei\n' +
              'Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Arvioijat Jaana Arstila ( näyttötutkintomestari ) Pekka Saurmann ( näyttötutkintomestari ) Juhani Mykkänen\n' +
              'Arviointikohteet Arviointikohde Arvosana\n' +
              'Työprosessin hallinta 3\n' +
              'Työmenetelmien, -välineiden ja materiaalin hallinta 2\n' +
              'Työn perustana olevan tiedon hallinta 2\n' +
              'Elinikäisen oppimisen avaintaidot 3\n' +
              'Arvioinnista päättäneet Opettaja\n' +
              'Arviointikeskusteluun osallistuneet Opettaja Itsenäinen ammatinharjoittaja\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Kokonaisuus Arvosana\n' +
              'Hoitotarpeen määrittäminen Hyväksytty\n' +
              'Kuvaus Hoitotarpeen määrittäminen\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.3.2013\n' +
              'Arvioijat Jaana Arstila Pekka Saurmann Juhani Mykkänen\n' +
              'Yhteensä 135 / 135 osp\n' +
              'Yhteiset tutkinnon osat Laajuus (osp) Arvosana\n' +
              'Viestintä- ja vuorovaikutusosaaminen 11 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Osa-alue Laajuus (osp) Arvosana\n' +
              'Äidinkieli, Suomen kieli ja kirjallisuus 5 3\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Äidinkieli, Suomen kieli ja kirjallisuus 3 3\n' +
              'Pakollinen ei\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Toinen kotimainen kieli, ruotsi, ruotsi 1 3\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Vieraat kielet, englanti 2 3\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Matemaattis-luonnontieteellinen osaaminen 12 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Lisätiedot\n' +
              'Osaamisen arvioinnin mukauttaminen\n' +
              'Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Osa-alue Laajuus (osp) Arvosana\n' +
              'Matematiikka 3 3\n' +
              'Kuvaus Matematiikan opinnot\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Matematiikka 3 3\n' +
              'Kuvaus Matematiikan opinnot\n' +
              'Pakollinen ei\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Fysiikka ja kemia 2 3\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Fysiikka ja kemia 3 3\n' +
              'Pakollinen ei\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Tieto- ja viestintätekniikka sekä sen hyödyntäminen 1 3\n' +
              'Pakollinen kyllä\n' +
              'Alkamispäivä 1.1.2014\n' +
              'Tunnustettu\n' +
              'Tutkinnon osa Asennushitsaus\n' +
              'Selite Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta\n' +
              'Rahoituksen piirissä ei\n' +
              'Lisätiedot\n' +
              'Osaamisen arvioinnin mukauttaminen\n' +
              'Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 1.1.2015\n' +
              'Yhteiskunnassa ja työelämässä tarvittava osaaminen 8 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Osa-alue Laajuus (osp) Arvosana\n' +
              'Yhteiskuntatieto 8 3\n' +
              'Kuvaus Yhteiskuntaopin opinnot\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sosiaalinen ja kulttuurinen osaaminen 7 3\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Sulje kaikki\n' +
              'Osa-alue Laajuus (osp) Arvosana\n' +
              'Sosiaalitaito 7 3\n' +
              'Kuvaus Vuorotaitovaikutuksen kurssi\n' +
              'Pakollinen kyllä\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yhteensä 38 / 35 osp\n' +
              'Vapaasti valittavat tutkinnon osat Laajuus (osp) Arvosana\n' +
              'Sosiaalinen ja kulttuurinen osaaminen 5 3\n' +
              'Kuvaus Sosiaalinen ja kulttuurinen osaaminen\n' +
              'Pakollinen ei\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yhteensä 5 / 10 osp\n' +
              'Tutkintoa yksilöllisesti laajentavat tutkinnon osat Laajuus (osp) Arvosana\n' +
              'Matkailuenglanti 5 3\n' +
              'Kuvaus Matkailuenglanti\n' +
              'Pakollinen ei\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana 3\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yhteensä 5 osp'
            )
            return true
          })
        })
      })
    })

    describe('Suoritus kesken, vanhan perusteen suoritus tunnustettu', function () {
      before(
        Authentication().login(),
        resetFixtures,
        page.openPage,
        page.oppijaHaku.searchAndSelect('140176-449X'),
        opinnot.expandAll
      )
      it('näyttää opiskeluoikeuden tiedot', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 1.9.2016 — 1.5.2020 (arvioitu)\n' +
          'Tila 1.9.2016 Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })

      it('näyttää suorituksen tiedot', function () {
        return wait.until(() => {
          let text = extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
          let expected =
            'Koulutus Autoalan perustutkinto 351301 39/011/2014\n' +
            'Suoritustapa Näyttötutkinto\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Alkamispäivä 1.9.2016\n' +
            'Suorituskieli suomi\n' +
            'Suoritus kesken'
          if (text !== expected) {
            return false
          }
          expect(text).to.equal(expected)
          return true
        })()
      })

      it('näyttää tutkinnon osat', function () {
        return wait.until(() => {
          let text = extractAsText(S('.osasuoritukset'))
          let expected =
            'Sulje kaikki\n' +
            'Tutkinnon osa Laajuus (osp) Arvosana\n' +
            'Moottorin ja voimansiirron huolto ja korjaus 15 Hyväksytty\n' +
            'Pakollinen ei\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Vahvistus 31.5.2013 Reijo Reksi , rehtori\n' +
            'Tunnustettu\n' +
            'Tutkinnon osa Tunniste 11-22-33\n' +
            'Nimi Moottorin korjaus\n' +
            'Kuvaus Opiskelijan on - tunnettava jakopyörästön merkitys moottorin toiminnalle - osattava kytkeä moottorin testauslaite ja tulkita mittaustuloksen suhdetta valmistajan antamiin ohjearvoihin - osattava käyttää moottorikorjauksessa tarvittavia perustyökaluja - osattava suorittaa jakopään hammashihnan vaihto annettujen ohjeiden mukaisesti - tunnettava venttiilikoneiston merkitys moottorin toiminnan osana osatakseen mm. ottaa se huomioon jakopään huoltoja tehdessään - noudatettava sovittuja työaikoja\n' +
            'Vahvistus 28.5.2002 Reijo Reksi\n' +
            'Näyttö\n' +
            'Kuvaus Moottorin korjaus\n' +
            'Suorituspaikka Autokorjaamo Oy, Riihimäki\n' +
            'Suoritusaika 20.4.2002 — 20.4.2002\n' +
            'Työssäoppimisen yhteydessä ei\n' +
            'Selite Tutkinnon osa on tunnustettu aiemmin suoritetusta autoalan perustutkinnon osasta (1.8.2000 nro 11/011/2000)\nRahoituksen piirissä ei\n' +
            'Arviointi Arvosana Hyväksytty\n' +
            'Arviointipäivä 20.3.2013\n' +
            'Arvioijat Jaana Arstila Pekka Saurmann Juhani Mykkänen\n' +
            'Yhteensä 15 osp'
          if (!(text.length > 0)) {
            return false
          }
          expect(text).to.equalIgnoreNewlines(expected)
          return true
        })
      })
    })

    describe('Opiskeluoikeuden lisätiedot', function () {
      before(
        Authentication().login(),
        resetFixtures,
        page.openPage,
        page.oppijaHaku.searchAndSelect('211097-402L'),
        opinnot.expandAll
      )

      it('näytetään', function () {
        expect(
          extractAsText(S('.opiskeluoikeuden-tiedot > .lisätiedot'))
        ).to.equal(
          'Lisätiedot\n' +
          'Majoitus 1.9.2012 — 1.9.2013\n' +
          'Sisäoppilaitosmainen majoitus 1.9.2012 — 1.9.2013\n' +
          'Vaativan erityisen tuen yhteydessä järjestettävä majoitus 1.9.2012 — 1.9.2013\n' +
          'Ulkomaanjaksot 1.9.2012 — 1.9.2013 Maa Ruotsi Kuvaus Harjoittelua ulkomailla\n' +
          'Hojks Opetusryhmä Yleinen opetusryhmä\nVaikeasti vammaisille järjestetty opetus 1.9.2012 — 1.9.2013\n' +
          'Vammainen ja avustaja 1.9.2012 — 1.9.2013\nOsa-aikaisuusjaksot 1.9.2012 — Osa-aikaisuus 80 %\n8.5.2019 — Osa-aikaisuus 60 %\n' +
          'Opiskeluvalmiuksia tukevat opinnot 1.10.2013 — 31.10.2013 Kuvaus Opiskeluvalmiuksia tukevia opintoja\n' +
          'Henkilöstökoulutus kyllä\nVankilaopetuksessa 2.9.2013 —'
        )
      })
    })
  })

  describe('Osittainen ammatillinen tutkinto', function () {
    before(
      Authentication().login(),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('230297-6448')
    )
    describe('Kaikki tiedot näkyvissä', function () {
      before(opinnot.expandAll)

      it('näyttää opiskeluoikeuden otsikkotiedot', function () {
        expect(
          opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()
        ).to.deep.equal([
          'Stadin ammatti- ja aikuisopisto, Luonto- ja ympäristöalan perustutkinto, osittainen (2012—2016, valmistunut)'
        ])
        expect(extractAsText(S('.suoritus-tabs .selected'))).to.equal(
          'Luonto- ja ympäristöalan perustutkinto, osittainen'
        )
      })

      it('näyttää opiskeluoikeuden tiedot', function () {
        expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
          'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 4.6.2016\n' +
          'Tila 4.6.2016 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
          '1.9.2012 Läsnä (valtionosuusrahoitteinen koulutus)'
        )
      })

      it('näyttää suorituksen tiedot', function () {
        expect(
          extractAsText(
            S('.suoritus > .properties, .suoritus > .tila-vahvistus')
          )
        ).to.equal(
          'Koulutus Luonto- ja ympäristöalan perustutkinto 361902 62/011/2014\n' +
          'Suoritustapa Ammatillinen perustutkinto\n' +
          'Tutkintonimike Ympäristönhoitaja\n' +
          'Toinen tutkintonimike kyllä\n' +
          'Osaamisala Ympäristöalan osaamisala\n' +
          'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
          'Suorituskieli suomi\n' +
          'Järjestämismuodot 1.9.2012 — , Koulutuksen järjestäminen oppilaitosmuotoisena\n' +
          'Todistuksella näkyvät lisätiedot Suorittaa toista osaamisalaa\n' +
          'Painotettu keskiarvo 4,00\n' +
          'Suoritus valmis Vahvistus : 4.6.2016 Reijo Reksi , rehtori'
        )
      })

      it('näyttää tutkinnon osat', function () {
        expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
          'Sulje kaikki\n' +
          'Ammatilliset tutkinnon osat Laajuus (osp) Arvosana\n' +
          'Ympäristön hoitaminen 35 3\n' +
          'Pakollinen kyllä\n' +
          'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
          'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
          'Arviointi Arvosana 3\n' +
          'Arviointipäivä 20.10.2014\n' +
          'Yhteensä 35 / 135 osp'
        )
      })
    })
  })

  describe('Näyttötutkinnot', function () {
    before(
      Authentication().login(),
      resetFixtures,
      page.openPage,
      page.oppijaHaku.searchAndSelect('250989-419V'),
      OpinnotPage().valitseSuoritus(
        undefined,
        'Näyttötutkintoon valmistava koulutus'
      )
    )
    describe('Näyttötutkintoon valmistava koulutus', function () {
      describe('Kaikki tiedot näkyvissä', function () {
        before(opinnot.expandAll)
        it('näyttää opiskeluoikeuden tiedot', function () {
          expect(
            opinnot.opiskeluoikeudet.valitunVälilehdenAlaotsikot()
          ).to.deep.equal([
            'Näyttötutkintoon valmistava koulutus 2012—2016, Valmistunut'
          ])
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
            'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 31.8.2016\n' +
            'Tila 31.8.2016 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
            '1.9.2012 Läsnä (valtionosuusrahoitteinen koulutus)'
          )
        })

        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Koulutus Näyttötutkintoon valmistava koulutus\n' +
            'Tutkinto Autoalan työnjohdon erikoisammattitutkinto 457305 40/011/2001\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Alkamispäivä 1.9.2012\n' +
            'Päättymispäivä 31.5.2015\n' +
            'Suorituskieli suomi\n' +
            'Suoritus valmis Vahvistus : 31.5.2015 Helsinki Reijo Reksi , rehtori'
          )
        })

        it('näyttää tutkinnon osat', function () {
          expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
            'Sulje kaikki\n' +
            'Koulutuksen osa\n' +
            'Johtaminen ja henkilöstön kehittäminen\n' +
            'Kuvaus Johtamisen ja henkilöstön kehittämisen valmistava koulutus\n' +
            'Auton lisävarustetyöt\n' +
            'Kuvaus valojärjestelmät\n' +
            'Auton lisävarustetyöt\n' +
            'Kuvaus lämmitysjärjestelmät'
          )
        })
      })

      describe('Tietojen muokkaus', function () {
        describe('Tutkinnon osan lisääminen', function () {
          before(editor.edit)

          describe('Paikallinen koulutuksen osa', function () {
            before(
              editor.edit,
              opinnot
                .tutkinnonOsat()
                .lisääPaikallinenTutkinnonOsa('Hassut temput')
            )

            describe('Lisäyksen jälkeen', function () {
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(3).nimi()).to.equal(
                  'Hassut temput'
                )
              })
            })

            describe('Tallennuksen jälkeen', function () {
              before(editor.saveChanges)
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(3).nimi()).to.equal(
                  'Hassut temput'
                )
              })
            })
          })

          describe('Ammatillinen tutkinnon osa', function () {
            before(
              editor.edit,
              opinnot.tutkinnonOsat().lisääTutkinnonOsa('Projektiosaaminen')
            )

            describe('Lisäyksen jälkeen', function () {
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(4).nimi()).to.equal(
                  'Projektiosaaminen'
                )
              })
            })

            describe('Tallennuksen jälkeen', function () {
              before(editor.saveChanges)
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(4).nimi()).to.equal(
                  'Projektiosaaminen'
                )
              })
            })
          })

          describe('Tutkinnon osa toisesta tutkinnosta', function () {
            before(
              editor.edit,
              opinnot
                .tutkinnonOsat()
                .lisääTutkinnonOsaToisestaTutkinnosta(
                  'Autoalan perustutkinto',
                  'Auton korjaaminen'
                )
            )

            describe('Lisäyksen jälkeen', function () {
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(5).nimi()).to.equal(
                  'Auton korjaaminen'
                )
              })
            })

            describe('Tallennuksen jälkeen', function () {
              before(editor.saveChanges)
              it('lisätty osa näytetään', function () {
                expect(opinnot.tutkinnonOsat().tutkinnonOsa(5).nimi()).to.equal(
                  'Auton korjaaminen'
                )
              })
            })
          })
        })
      })
    })

    describe('Erikoisammattitutkinto', function () {
      before(
        wait.until(page.isOppijaSelected('Erja')),
        OpinnotPage().valitseSuoritus(
          undefined,
          'Autoalan työnjohdon erikoisammattitutkinto'
        )
      )
      describe('Kaikki tiedot näkyvissä', function () {
        before(opinnot.expandAll)
        it('näyttää opiskeluoikeuden tiedot', function () {
          expect(extractAsText(S('.opiskeluoikeuden-tiedot'))).to.equal(
            'Opiskeluoikeuden voimassaoloaika : 1.9.2012 — 31.8.2016\n' +
            'Tila 31.8.2016 Valmistunut (valtionosuusrahoitteinen koulutus)\n' +
            '1.9.2012 Läsnä (valtionosuusrahoitteinen koulutus)'
          )
        })

        it('näyttää suorituksen tiedot', function () {
          expect(
            extractAsText(
              S('.suoritus > .properties, .suoritus > .tila-vahvistus')
            )
          ).to.equal(
            'Koulutus Autoalan työnjohdon erikoisammattitutkinto 457305 40/011/2001\n' +
            'Suoritustapa Näyttötutkinto\n' +
            'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
            'Suorituskieli suomi\n' +
            'Järjestämismuodot 1.8.2014 — , Koulutuksen järjestäminen oppilaitosmuotoisena\n' +
            '31.5.2015 — , Koulutuksen järjestäminen oppisopimuskoulutuksena\n' +
            'Yritys Autokorjaamo Oy Y-tunnus 1234567-8\n' +
            '31.3.2016 — , Koulutuksen järjestäminen oppilaitosmuotoisena\n' +
            'Suoritus valmis Vahvistus : 31.5.2016 Helsinki Reijo Reksi , rehtori'
          )
        })

        it('näyttää tutkinnon osat', function () {
          return wait.until(() => {
            let text = extractAsText(S('.osasuoritukset'))
            if (!(text.length > 0)) {
              return false
            }
            expect(text).to.equalIgnoreNewlines(
              'Sulje kaikki Tutkinnon osa Arvosana\n' +
              'Johtaminen ja henkilöstön kehittäminen Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Asiakaspalvelu ja korjaamopalvelujen markkinointi Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Työnsuunnittelu ja organisointi Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Taloudellinen toiminta Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yrittäjyys Hyväksytty\n' +
              'Pakollinen kyllä\n' +
              'Oppilaitos / toimipiste Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka\n' +
              'Vahvistus 31.5.2016 Reijo Reksi , rehtori\n' +
              'Arviointi Arvosana Hyväksytty\n' +
              'Arviointipäivä 20.10.2014\n' +
              'Yhteensä 0 osp'
            )
            return true
          })
        })
      })

      describe('Tutkinnon osat', function () {
        before(editor.edit)
        it('Tutkinnon osia ei ryhmitellä', function () {
          expect(opinnot.tutkinnonOsat('1').isGroupHeaderVisible()).to.equal(
            false
          )
        })

        before(
          opinnot.tutkinnonOsat().lisääTutkinnonOsa('Tekniikan asiantuntemus')
        )

        describe('Lisäyksen jälkeen', function () {
          it('lisätty osa näytetään', function () {
            expect(opinnot.tutkinnonOsat().tutkinnonOsa(5).nimi()).to.equal(
              'Tekniikan asiantuntemus'
            )
          })
          describe('kun tallennetaan', function () {
            before(
              editor.property('tila').removeItem(0),
              opinnot.tilaJaVahvistus.merkitseKeskeneräiseksi,
              opinnot
                .tutkinnonOsat()
                .tutkinnonOsa(5)
                .propertyBySelector('.arvosana')
                .setValue('3', 1),
              editor.saveChanges
            )
            it('tallennus onnistuu', function () {
              expect(page.isSavedLabelShown()).to.equal(true)
            })
          })
        })
      })
    })

    describe('Uusi erikoisammattitutkinto', function () {
      before(
        addOppija.addNewOppija('kalle', '250858-5188', {
          oppilaitos: 'Stadin',
          tutkinto: 'Autoalan työnjohdon erikoisammattitutkinto',
          suoritustapa: 'Näyttötutkinto'
        })
      )
      describe('Uuden tutkinnonosan lisääminen', function () {
        before(
          editor.edit,
          opinnot.tutkinnonOsat().lisääTutkinnonOsa('Tekniikan asiantuntemus'),
          opinnot
            .tutkinnonOsat()
            .tutkinnonOsa(0)
            .propertyBySelector('.arvosana')
            .setValue('3', 1),
          editor.saveChanges,
          wait.until(page.isSavedLabelShown)
        )
        it('onnistuu', function () {
          expect(extractAsText(S('.osasuoritukset'))).to.equalIgnoreNewlines(
            'Avaa kaikki\n' +
            'Tutkinnon osa Arvosana\n' +
            'Tekniikan asiantuntemus 3\n' +
            'Yhteensä 0 osp'
          )
        })
      })
    })
  })
})
