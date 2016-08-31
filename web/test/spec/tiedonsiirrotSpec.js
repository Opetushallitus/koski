describe('Tiedonsiirrot', function() {
  var tiedonsiirrot = TiedonsiirrotPage()
  var authentication = Authentication()
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
  before(
    authentication.login('tiedonsiirtäjä'),
    resetFixtures,
    insertOppija('<oppija></oppija>'),
    insertOppija('{"henkilö": {}}'),
    insertExample('tiedonsiirto - epäonnistunut.json'),
    insertExample('tiedonsiirto - onnistunut.json'),
    insertExample('tiedonsiirto - epäonnistunut 2.json'),
    tiedonsiirrot.openPage
  )
  it('Näytetään', function() {
    expect(tiedonsiirrot.tiedot()).to.deep.equal([
      [
        '120496-949B',
        'Aarne Ammattilainen',
        'Aalto-yliopisto',
        'virhe',
        'tiedot'
      ],
      [
        '290896-9674',
        'Tiina Tiedonsiirto',
        'Stadin ammattiopisto',
        '',
        ''
      ],
      [
        '',
        ' ',
        '',
        'virhe',
        'tiedot'
      ],
      [
        '',
        '',
        '',
        'virhe',
        'tiedot'
      ],

    ])
  })
})