import React from 'react'
import Pager from '../util/Pager'
import * as L from 'partial.lenses'
import { tiedonsiirrotContentP, ReloadButton } from './Tiedonsiirrot'
import { Tiedonsiirtotaulukko } from './Tiedonsiirtotaulukko'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'

export const tiedonsiirtolokiContentP = (queryString) => {
  const pager = Pager(
    '/koski/api/tiedonsiirrot' + queryString,
    L.prop('henkilöt')
  )
  return tiedonsiirrotContentP(
    '/koski/tiedonsiirrot',
    pager.rowsP.map(({ henkilöt, oppilaitos }) => ({
      content: (
        <div>
          <Text name="Viimeisimmät KOSKI-palveluun siirtyneet opiskelijatiedot" />
          <OppilaitosTitle oppilaitos={oppilaitos} />
          <ReloadButton />
          <Tiedonsiirtotaulukko
            rivit={henkilöt}
            showError={false}
            pager={pager}
          />
        </div>
      ),
      title: 'Tiedonsiirrot'
    }))
  )
}

export const OppilaitosTitle = ({ oppilaitos }) =>
  oppilaitos ? (
    <span>
      {' '}
      <Text name="oppilaitoksessa" />
      {` ${t(oppilaitos.nimi)}`}
    </span>
  ) : null
