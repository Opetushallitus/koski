import React from 'baret'
import Autocomplete from './Autocomplete.jsx'
import Bacon from 'baconjs'
import Http from './http'
import {showInternalError} from './location.js'

const Oppilaitos = ({oppilaitosBus, oppilaitosP}) => (<label className='oppilaitos'>Oppilaitos
  {
    oppilaitosP.map((selected) =>
      <Autocomplete
        resultBus={oppilaitosBus}
        fetchItems={value => (value.length >= 1)
                ?  Http.cachedGet('/koski/api/oppilaitos').map(oppilaitokset => oppilaitokset.filter(oppilaitos => oppilaitos.nimi.fi.toLowerCase().indexOf(value.toLowerCase()) >= 0))
                : Bacon.once([])
              }
        selected={ selected }
      />
    )
  }
</label>)

const Tutkinto = ({tutkintoBus, oppilaitosP, tutkintoP}) =>{
  return (<label className='tutkinto'>Tutkinto
  {
    Bacon.combineTemplate({ selected: tutkintoP, oppilaitos: oppilaitosP}).map(({oppilaitos, selected}) => (
      <Autocomplete
        resultBus={tutkintoBus}
        fetchItems={(value) => (value.length >= 3)
                        ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value).doError(showInternalError)
                        : Bacon.constant([])}
        disabled={!oppilaitos}
        selected={selected}
      />
    ))
  } </label> )
}

export const Opiskeluoikeus = React.createClass({
  render() {
    const {oppilaitosBus, oppilaitosP, tutkintoBus, tutkintoP} = this.state
    return (
      <div>
        <Oppilaitos oppilaitosBus={oppilaitosBus} oppilaitosP={oppilaitosP} />
        <Tutkinto tutkintoBus={tutkintoBus} tutkintoP={tutkintoP} oppilaitosP={oppilaitosP}/>
      </div>
    )
  },

  componentDidMount() {
    const {oppilaitosP, tutkintoP} = this.state
    const opiskeluoikeus = Bacon.combineTemplate({
      oppilaitos: oppilaitosP,
      tutkinto: tutkintoP,
      valid: oppilaitosP.and(tutkintoP).map(v => !!(v))
    })
    this.props.opiskeluoikeusBus.plug(opiskeluoikeus)
  },

  getInitialState() {
    const oppilaitosBus = Bacon.Bus()
    const tutkintoBus = Bacon.Bus()
    const oppilaitosP = oppilaitosBus.toProperty(undefined).skipDuplicates()
    const tutkintoP = tutkintoBus.merge(oppilaitosP.changes().map(undefined)).toProperty(undefined).skipDuplicates()

    return {
      oppilaitosBus: oppilaitosBus,
      tutkintoBus: tutkintoBus,
      oppilaitosP: oppilaitosP,
      tutkintoP: tutkintoP
    }
  }
})