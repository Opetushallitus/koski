import React from 'react'
import Bacon from 'baconjs'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko, esimerkkidata } from './Tiedonsiirtotaulukko.jsx'

export const tiedonsiirtolokiContentP = tiedonsiirrotContentP('/koski/tiedonsiirrot', Bacon.constant().map(() =>
  <div>
    Viimeisimmät KOSKI-palveluun siirtyneet opiskelijatiedot
    <Tiedonsiirtotaulukko rivit={esimerkkidata}/>
  </div>
))