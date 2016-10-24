import React from 'react'
import Http from './http'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'

const tiedonsiirrotP = (queryString) => Http.get('/koski/api/tiedonsiirrot' + queryString).toProperty()

export const tiedonsiirtolokiContentP = (queryString) => tiedonsiirrotContentP('/koski/tiedonsiirrot', tiedonsiirrotP(queryString).map(({henkilöt, oppilaitos}) => ({
    content: (<div>
      Viimeisimmät KOSKI-palveluun siirtyneet opiskelijatiedot { oppilaitos ?
      <span> oppilaitoksessa {oppilaitos.nimi.fi}</span> : null }
      <Tiedonsiirtotaulukko rivit={henkilöt} showError={false}/>
    </div>),
      title: 'Tiedonsiirrot'
  })
))