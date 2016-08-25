import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'

const tiedonsiirrotP = Bacon.once().flatMap(() => Http.get('/koski/api/tiedonsiirrot')).toProperty()

export const tiedonsiirtolokiContentP = tiedonsiirrotContentP('/koski/tiedonsiirrot', tiedonsiirrotP.map((rivit) =>
  (<div>
    Viimeisimmät KOSKI-palveluun siirtyneet opiskelijatiedot
    <Tiedonsiirtotaulukko rivit={rivit}/>
  </div>)
))