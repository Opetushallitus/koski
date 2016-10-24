import React from 'react'
import Http from './http'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'

const tiedonsiirtovirheetP = (queryString) => Http.get('/koski/api/tiedonsiirrot/virheet' + queryString).toProperty()

export const tiedonsiirtovirheetContentP = (queryString) => tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', tiedonsiirtovirheetP(queryString).map(({henkilöt, oppilaitos}) =>
  ({
    content: (<div className="tiedonsiirto-virheet">
                <p>Alla olevien opiskelijoiden tiedot ovat virhetilassa { oppilaitos ? <span> oppilaitoksessa {oppilaitos.nimi.fi}</span> : null }.</p>
                <p>Opiskelija poistuu virhelistalta kun virheen aiheuttanut tieto on korjattu lähdejärjestelmässä ja opiskelijan
                  tiedot on siirretty uudelleen onnistuneesti KOSKI-palveluun.</p>
                <Tiedonsiirtotaulukko rivit={henkilöt} showError={true}/>
              </div>),
    title: 'Tiedonsiirtovirheet'
  }))
)