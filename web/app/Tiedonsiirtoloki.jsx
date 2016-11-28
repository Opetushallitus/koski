import React from 'react'
import Pager from './Pager'
import * as L from 'partial.lenses'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'

export const tiedonsiirtolokiContentP = (queryString) => {
  let pager = Pager('/koski/api/tiedonsiirrot' + queryString, L.prop('henkilöt') )
  return tiedonsiirrotContentP('/koski/tiedonsiirrot', pager.rowsP.map(({henkilöt, oppilaitos}) => ({
      content: (<div>
          Viimeisimmät KOSKI-palveluun siirtyneet opiskelijatiedot { oppilaitos ?
          <span> oppilaitoksessa {oppilaitos.nimi.fi}</span> : null }
          <Tiedonsiirtotaulukko rivit={henkilöt} showError={false} pager={pager}/>
        </div>),
      title: 'Tiedonsiirrot' + (oppilaitos ? ' - ' + oppilaitos.nimi.fi : '')
    })
  ))
}