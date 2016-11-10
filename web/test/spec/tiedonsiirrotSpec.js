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
    tiedonsiirrot.openPage
  )
  
  describe("Tiedonsiirtoloki", function() {
    it('Näytetään', function() {
      expect(tiedonsiirrot.tiedot()).to.deep.equal([
        ['280618-402H', 'Aarne Ammattilainen', 'Aalto-yliopisto', 'virhe', 'tiedot'],
        ['270303-281N', 'Tiina Tiedonsiirto', 'Stadin ammattiopisto', '', ''],
        ['', ' ', '', 'virhe', 'tiedot'],
        ['', '', '', 'virhe', 'tiedot'],
      ])
    })
  })

  describe("Virhelistaus", function() {
    before(tiedonsiirrot.openVirhesivu())

    it('Näytetään', function() {
      expect(tiedonsiirrot.tiedot()).to.deep.equal([
        ['280618-402H', 'Aarne Ammattilainen', 'Aalto-yliopisto', 'Ei oikeuksia organisatioon 1.2.246.562.10.56753942459virhe', 'tiedot'],
        ['270303-281N', 'Tiina Tiedonsiirto', 'Aalto-yliopisto', 'Ei oikeuksia organisatioon 1.2.246.562.10.56753942459virhe', 'tiedot'],
        ['', ' ', '', 'Viesti ei ole skeeman mukainenvirhe', 'tiedot'],
        ['', '', '', 'Epäkelpo JSON-dokumenttivirhe', 'tiedot'],
      ])
    })
  })

  describe("Yhteenveto", function() {
    before(tiedonsiirrot.openYhteenveto())

    it('Näytetään', function() {
      expect(tiedonsiirrot.tiedot().map(function(row) { return row[0]})).to.deep.equal(['Stadin ammattiopisto'])
    })
  })


  function insertExample(name) {
    return function() {
      return getJson('/koski/documentation/examples/' + name).then(function(data) {
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