import React from 'react'
import Http from './http'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'

const tiedonsiirrotP = (queryString) => Http.get('/koski/api/tiedonsiirrot' + queryString).toProperty()

export const tiedonsiirtolokiContentP = (queryString) => tiedonsiirrotContentP('/koski/tiedonsiirrot', tiedonsiirrotP(queryString).map((rivit) =>
  (<div>
    Viimeisimmät KOSKI-palveluun siirtyneet opiskelijatiedot
    <Tiedonsiirtotaulukko rivit={rivit} showError={false}/>
  </div>)
))