import React from 'baret'
import Bacon from 'baconjs'
import Autocomplete from './Autocomplete.jsx'
import Http from './http'
import Text from './Text.jsx'
import {t} from './i18n'

export default ({tutkintoAtom, oppilaitosP, ...rest}) => {
  return (<div className="tutkinto-autocomplete">
    {
      Bacon.combineWith(oppilaitosP, tutkintoAtom, (oppilaitos, tutkinto) =>
        oppilaitos && (
          <label className='tutkinto'>
            <Text name="Tutkinto"/>
            <Autocomplete
              resultAtom={tutkintoAtom}
              fetchItems={(value) => (value.length >= 3)
                ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value)
                : Bacon.constant([])}
              disabled={!oppilaitos}
              selected={tutkinto}
              displayValue={item => t(item.nimi) + ' ' + item.diaarinumero}
              {...rest}
            />
          </label>
        )
      )
    }
  </div> )
}
