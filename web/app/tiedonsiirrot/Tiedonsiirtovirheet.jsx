import React from 'react'
import Pager from '../util/Pager'
import * as L from 'partial.lenses'
import {tiedonsiirrotContentP} from './Tiedonsiirrot'
import {Tiedonsiirtotaulukko} from './Tiedonsiirtotaulukko'
import Text from '../i18n/Text'
import {OppilaitosTitle} from './Tiedonsiirtoloki'

export const tiedonsiirtovirheetContentP = (queryString) => {
  let pager = Pager('/koski/api/tiedonsiirrot/virheet' + queryString, L.prop('henkilöt') )

  return tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', pager.rowsP.map(({henkilöt, oppilaitos}) =>
    ({
      content: (<div className="tiedonsiirto-virheet">
        <p><Text name="Alla olevien opiskelijoiden tiedot ovat virhetilassa"/><OppilaitosTitle oppilaitos={oppilaitos}/>{'.'}</p>
        <p><Text name="Opiskelija poistuu virhelistalta"/></p>
        <Tiedonsiirtotaulukko rivit={henkilöt} showError={true} pager={pager}/>
      </div>),
      title: 'Tiedonsiirtovirheet'
    }))
  )
}
