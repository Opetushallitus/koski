import React from 'react'
import Bacon from 'baconjs'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'

export const tiedonsiirtovirheetContentP = tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', Bacon.constant(<div>
  <p>Alla olevien opiskelijoiden tiedot ovat virhetilassa.</p>
  <p>Opiskelija poistuu virhelistalta kun virheen aiheuttanut tieto on korjattu lähdejärjestelmässä ja opiskelijan
    tiedot on siirretty uudelleen onnistuneesti KOSKI-palveluun.</p>
</div>))