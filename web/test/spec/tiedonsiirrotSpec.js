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
  before(
    resetFixtures,
    authentication.login('tiedonsiirtäjä'),
    insertExample('tiedonsiirto - epäonnistunut.json'),
    insertExample('tiedonsiirto - onnistunut.json'),
    insertExample('tiedonsiirto - epäonnistunut 2.json'),
    tiedonsiirrot.openPage
  )
  it('Näytetään', function() {

  })
})