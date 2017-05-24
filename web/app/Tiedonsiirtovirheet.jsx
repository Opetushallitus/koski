import React from 'react'
import Pager from './Pager'
import * as L from 'partial.lenses'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko.jsx'
import {t} from './i18n'
import Text from './Text.jsx'
import { OppilaitosTitle } from './Tiedonsiirtoloki.jsx'

export const tiedonsiirtovirheetContentP = (queryString) => {
  let pager = Pager('/koski/api/tiedonsiirrot/virheet' + queryString, L.prop('henkilöt') )

  return tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', pager.rowsP.map(({henkilöt, oppilaitos}) =>
    ({
      content: (<div className="tiedonsiirto-virheet">
        <p><Text name="Alla olevien opiskelijoiden tiedot ovat virhetilassa"/><OppilaitosTitle oppilaitos={oppilaitos}/>{'.'}</p>
        <p><Text name="Opiskelija poistuu virhelistalta"/></p>
        <Tiedonsiirtotaulukko rivit={henkilöt} showError={true} pager={pager}/>
      </div>),
      title: t('Tiedonsiirtovirheet') + (oppilaitos ? ' - ' + t(oppilaitos.nimi) : '')
    }))
  )
}