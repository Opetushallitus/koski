import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'

const tiedonsiirtovirheetP = Bacon.once().flatMap(() => Http.get('/koski/api/tiedonsiirrot/virheet')).toProperty()
tiedonsiirtovirheetP.onValue(() => {})

export const tiedonsiirtovirheetContentP = tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', tiedonsiirtovirheetP.map((rivit) =>
  (<div className="tiedonsiirto-virheet">
  <p>Alla olevien opiskelijoiden tiedot ovat virhetilassa.</p>
  <p>Opiskelija poistuu virhelistalta kun virheen aiheuttanut tieto on korjattu lähdejärjestelmässä ja opiskelijan
    tiedot on siirretty uudelleen onnistuneesti KOSKI-palveluun.</p>
  <Tiedonsiirtotaulukko rivit={rivit} showError={true}/>
  </div>))
)