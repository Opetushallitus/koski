import React from 'baret'
import Pager from '../util/Pager'
import * as L from 'partial.lenses'
import { ReloadButton, tiedonsiirrotContentP } from './Tiedonsiirrot'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko'
import Text from '../i18n/Text'
import { OppilaitosTitle } from './Tiedonsiirtoloki'
import Atom from 'bacon.atom'
import Http from '../util/http'
import { userP } from '../util/user'
import Bacon from 'baconjs'

export const tiedonsiirtovirheetContentP = (queryString) => {
  const pager = Pager(
    '/koski/api/tiedonsiirrot/virheet' + queryString,
    L.prop('henkilöt'),
    1000
  )

  const selected = Atom([])
  const deleting = Atom(false)

  const removeSelected = () => {
    deleting.set(true)
    Http.post('/koski/api/tiedonsiirrot/delete', { ids: selected.get() })
      .doError(() => {
        selected.set([])
        deleting.set(false)
      })
      .onValue(() => window.location.reload(true))
  }

  const contentP = deleting
    .not()
    .flatMap((notDeleting) =>
      notDeleting
        ? Bacon.combineWith(
            pager.rowsP,
            userP,
            ({ henkilöt, oppilaitos }, user) => ({
              content: (
                <div className="tiedonsiirto-virheet">
                  <ReloadButton />
                  <button
                    className="koski-button remove-selected"
                    disabled={selected.map((s) => !s.length)}
                    style={
                      user.hasAnyInvalidateAccess ? {} : { display: 'none' }
                    }
                    onClick={removeSelected}
                  >
                    <Text name="Poista valitut" />
                  </button>
                  <span>
                    <Text name="Alla olevien opiskelijoiden tiedot ovat virhetilassa" />
                    <OppilaitosTitle oppilaitos={oppilaitos} />
                    {'.'}
                  </span>
                  <p>
                    <Text name="Opiskelija poistuu virhelistalta" />
                  </p>
                  <Tiedonsiirtotaulukko
                    rivit={henkilöt}
                    showError={true}
                    pager={pager}
                    selected={selected}
                    showSelected={user.hasAnyInvalidateAccess}
                  />
                </div>
              ),
              title: 'Tiedonsiirtovirheet'
            })
          )
        : Bacon.constant({
            content: (
              <div className="ajax-loading-placeholder">
                <Text name="Poistetaan" />
              </div>
            ),
            title: 'Tiedonsiirtovirheet'
          })
    )
    .toProperty()

  return tiedonsiirrotContentP('/koski/tiedonsiirrot/virheet', contentP)
}
