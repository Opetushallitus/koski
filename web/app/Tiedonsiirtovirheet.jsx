import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'

const tiedonsiirrotP = Bacon.once().flatMap(() => Http.get('/koski/api/tiedonsiirrot/virheet')).toProperty()

export const tiedonsiirtovirheetContentP = tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', tiedonsiirrotP.map((rivit) =>
  (<div>
  <p>Alla olevien opiskelijoiden tiedot ovat virhetilassa.</p>
  <p>Opiskelija poistuu virhelistalta kun virheen aiheuttanut tieto on korjattu lähdejärjestelmässä ja opiskelijan
    tiedot on siirretty uudelleen onnistuneesti KOSKI-palveluun.</p>
  <Tiedonsiirtotaulukko rivit={rivit} showError={true}/>
  </div>))
)