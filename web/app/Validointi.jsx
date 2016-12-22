import React from 'react'
import Oboe from 'oboe'
import R from 'ramda'
import Bacon from 'baconjs'

export const validointiContentP = (query) => {

  let oboeBus = Bacon.Bus()
  Oboe('/koski/api/opiskeluoikeus/validate' + query)
    .node('{errors opiskeluoikeusId}', (x) => oboeBus.push(x))
    .done(() => oboeBus.end())
    .fail((e) => oboeBus.error(e))
  var keyCounter = 0
  let validationStatusP = oboeBus.scan([], (grouped, error) => {
    for (var i in grouped) {
      if (R.equals(grouped[i].errors, error.errors)) {
        grouped[i].oids.push(error.henkilöOid)
        return grouped
      }
    }
    grouped.push({
      errors: error.errors,
      key: ++keyCounter,
      oids: [error.henkilöOid]
    })
    return grouped
  }).throttle(1000)

  let validationFinishedP = validationStatusP.filter(false).mapEnd(true).startWith(false)

  return Bacon.combineWith(validationStatusP, validationFinishedP, (validationStatus, finished) => ({
    content: (<div className='content-area validaatio'>
      <div className="main-content">
        <h2>Tiedon validointi</h2>
        { finished ? 'Kaikki opiskeluoikeudet validoitu' : 'Odota, tietoja validoidaan. Tämä saattaa kestää useita minuutteja.'}
        <table>
          <thead>
            <tr><th className="virhetyyppi">Virhetyyppi</th><th className="virheteksti">Virheteksti</th><th className="lukumäärä">Lukumäärä</th></tr>
          </thead>
          <tbody>
          { validationStatus.map(({errors, oids, key}) =>
            <tr key={key}>
              <td className="virhetyyppi">{errors.map((error, i) => <div key={i}>{error.key}</div>)}</td>
              <td className="virheteksti">{errors.map((error, i) => {
                let message = typeof error.message == 'string'
                  ? <div>{error.message}</div>
                  : <pre className="json"><code>{JSON.stringify(error.message, null, 2)}</code></pre>
                return <span key={i}>{message}</span>
              })}</td>
              <td className="lukumäärä">{oids.length}</td>
            </tr>
          )}
          </tbody>
        </table>
      </div>
    </div>),
    title: ''
  }))
}
