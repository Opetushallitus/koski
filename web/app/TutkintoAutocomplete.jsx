import React from 'baret'
import Bacon from 'baconjs'
import Autocomplete from './Autocomplete.jsx'
import Http from './http'
import {t} from './i18n'

export default ({tutkintoAtom, oppilaitosP, ...rest}) => {
  return (<span><Autocomplete
      key="tutkinto-ac"
      resultAtom={tutkintoAtom}
      fetchItems={(value) => (value.length >= 3)
        ? oppilaitosP.take(1).flatMap(oppilaitos => Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value))
        : Bacon.constant([])}
      disabled={oppilaitosP.not()}
      selected={tutkintoAtom}
      displayValue={item => t(item.nimi) + ' ' + item.diaarinumero}
      {...rest}
    /></span>
  )
}
