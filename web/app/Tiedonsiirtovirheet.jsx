import React from 'react'
import Bacon from 'baconjs'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko, esimerkkidata } from './Tiedonsiirtotaulukko.jsx'

export const tiedonsiirtovirheetContentP = tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', Bacon.constant(<div>
  <p>Alla olevien opiskelijoiden tiedot ovat virhetilassa.</p>
  <p>Opiskelija poistuu virhelistalta kun virheen aiheuttanut tieto on korjattu lähdejärjestelmässä ja opiskelijan
    tiedot on siirretty uudelleen onnistuneesti KOSKI-palveluun.</p>
  <Tiedonsiirtotaulukko rivit={esimerkkidata}/>
</div>))