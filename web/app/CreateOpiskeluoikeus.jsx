import React from 'baret'
import Bacon from 'baconjs'
import Autocomplete from './Autocomplete.jsx'
import Http from './http'
import {showInternalError} from './location.js'

const Oppilaitos = ({oppilaitosAtom, oppilaitos}) => (<label className='oppilaitos'>Oppilaitos
  <Autocomplete
    resultAtom={oppilaitosAtom}
    fetchItems={value => (value.length >= 1)
                ?  Http.cachedGet('/koski/api/oppilaitos').map(oppilaitokset => oppilaitokset.filter(o => o.nimi.fi.toLowerCase().indexOf(value.toLowerCase()) >= 0))
                : Bacon.once([])
              }
    selected={ oppilaitos }
  />
</label>)

const Tutkinto = ({tutkintoAtom, oppilaitos, tutkinto}) =>{
  return (<label className='tutkinto'> <Autocomplete
    resultAtom={tutkintoAtom}
    fetchItems={(value) => (value.length >= 3)
                        ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value).doError(showInternalError)
                        : Bacon.constant([])}
    disabled={!oppilaitos}
    selected={tutkinto}
  />
  </label> )
}

export const Opiskeluoikeus = ({opiskeluoikeusAtom}) => {
  const oppilaitosAtom = opiskeluoikeusAtom.view('oppilaitos')
  const tutkintoAtom = opiskeluoikeusAtom.view('tutkinto')
  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))

  return (<div>
    {
      Bacon.combineWith(oppilaitosAtom, tutkintoAtom, (oppilaitos, tutkinto) => <div>
        <Oppilaitos oppilaitosAtom={oppilaitosAtom} oppilaitos={oppilaitos} />
        <Tutkinto tutkintoAtom={tutkintoAtom} tutkinto={tutkinto} oppilaitos={oppilaitos}/>
      </div>)
    }
  </div>)
}