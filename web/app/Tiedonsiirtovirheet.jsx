import React from 'react'
import Pager from './pager'
import * as L from 'partial.lenses'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'

export const tiedonsiirtovirheetContentP = (queryString) => {
  let pager = Pager('/koski/api/tiedonsiirrot/virheet' + queryString, L.prop('henkilöt') )

  return tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', pager.rowsP.map(({henkilöt, oppilaitos}) =>
    ({
      content: (<div className="tiedonsiirto-virheet">
        <p>Alla olevien opiskelijoiden tiedot ovat virhetilassa { oppilaitos ?
          <span> oppilaitoksessa {oppilaitos.nimi.fi}</span> : null }.</p>
        <p>Opiskelija poistuu virhelistalta kun virheen aiheuttanut tieto on korjattu lähdejärjestelmässä ja opiskelijan
          tiedot on siirretty uudelleen onnistuneesti KOSKI-palveluun.</p>
        <Tiedonsiirtotaulukko rivit={henkilöt} showError={true} pager={pager}/>
      </div>),
      title: 'Tiedonsiirtovirheet'
    }))
  )
}