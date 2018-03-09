describe('Tiedonsiirrot', function() {
  var tiedonsiirrot = TiedonsiirrotPage()
  var authentication = Authentication()


  before(
    authentication.login('stadin-palvelu'),
    resetFixtures,
    insertOppija('<oppija></oppija>'),
    insertOppija('{"henkilö": {}}'),
    insertExample('tiedonsiirto - epäonnistunut.json'),
    insertExample('tiedonsiirto - onnistunut.json'),
    insertExample('tiedonsiirto - epäonnistunut 2.json'),
    insertExample('tiedonsiirto - vain syntymäaika.json'),
    syncTiedonsiirrot,
    tiedonsiirrot.openPage
  )

  describe("Tiedonsiirtoloki", function() {
    function sortByName(a, b) {
      return a[1].localeCompare(b[1])
    }

    it('Näytetään', function() {
      expect(tiedonsiirrot.tiedot().sort(sortByName)).to.deep.equal([
        ['280618-402H', 'Aarne Ammattilainen', 'Aalto-yliopisto', 'virhe', 'tiedot'],
        ['24.2.1977', 'Heikki Hetuton', 'Stadin ammattiopisto', '', ''],
        ['270303-281N', 'Tiina Tiedonsiirto', 'Stadin ammattiopisto', '', ''],
        ['', '', '', 'virhe', 'tiedot']
      ].sort(sortByName))
    })
  })

  describe("Virhelistaus", function() {
    before(tiedonsiirrot.openVirhesivu)

    it('Näytetään', function() {
      expect(tiedonsiirrot.tiedot()).to.deep.equal([
        ['280618-402H', 'Aarne Ammattilainen', 'Aalto-yliopisto', 'Ei oikeuksia organisatioon 1.2.246.562.10.56753942459virhe', 'tiedot'],
        ['', '', '', 'Viesti ei ole skeeman mukainen (notAnyOf henkilö)virhe', 'tiedot']
      ])
    })
  })

  describe("Yhteenveto", function() {
    before(tiedonsiirrot.openYhteenveto)

    it('Näytetään', function() {
      expect(tiedonsiirrot.tiedot().map(function(row) { return row[0]})).to.deep.equal(['Aalto-yliopisto', 'HELSINGIN KAUPUNKI', 'Stadin ammattiopisto'])
    })
  })


  function insertExample(name) {
    return function() {
      return getJson('/koski/api/documentation/examples/' + name).then(function(data) {
        return putJson('/koski/api/oppija', data).catch(function(){})
      })
    }
  }

  function insertOppija(dataString) {
    return function() {
      return sendAjax('/koski/api/oppija', 'application/json', dataString, 'PUT').catch(function(){})
    }
  }
})