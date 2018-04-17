import React from 'baret'
import Pager from '../util/Pager'
import * as L from 'partial.lenses'
import {ReloadButton, tiedonsiirrotContentP} from './Tiedonsiirrot'
import {Tiedonsiirtotaulukko} from './Tiedonsiirtotaulukko'
import Text from '../i18n/Text'
import {OppilaitosTitle} from './Tiedonsiirtoloki'
import Atom from 'bacon.atom'
import Http from '../util/http'

export const tiedonsiirtovirheetContentP = (queryString) => {
  const createPager = () => Pager('/koski/api/tiedonsiirrot/virheet' + queryString, L.prop('henkilöt'))

  let pagerAtom = Atom(createPager())

  const selected = Atom([])

  const removeSelected = () => {
    Http.post('/koski/api/tiedonsiirrot/delete', {ids: selected.get()}).onValue(() => {
        selected.set([])
        pagerAtom.get().clearCache('/koski/api/tiedonsiirrot/virheet' + queryString)
        pagerAtom.set(createPager())
      }
    )
  }

  let contentP = pagerAtom.log('pagerAtom').flatMap(pager => pager.rowsP.log('rowsP').map(({henkilöt, oppilaitos}) =>
    ({
      content: (
        <div className="tiedonsiirto-virheet">
          <ReloadButton/>
          <button className="remove-selected" disabled={selected.map(s => !s.length)} onClick={removeSelected}>Poista valitut</button>
          <span><Text name="Alla olevien opiskelijoiden tiedot ovat virhetilassa"/><OppilaitosTitle oppilaitos={oppilaitos}/>{'.'}</span>
          <p><Text name="Opiskelija poistuu virhelistalta"/></p>
          <Tiedonsiirtotaulukko rivit={henkilöt} showError={true} pager={pager} selected={selected}/>
        </div>
      ),
      title: 'Tiedonsiirtovirheet'
    })
  )).toProperty()

  return tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', contentP)
}
