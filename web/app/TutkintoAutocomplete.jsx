import React from 'baret'
import Bacon from 'baconjs'
import Autocomplete from './Autocomplete.jsx'
import Http from './http'
import Text from './Text.jsx'

export default ({tutkintoAtom, oppilaitosP}) => {
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
            />
          </label>
        )
      )
    }
  </div> )
}
