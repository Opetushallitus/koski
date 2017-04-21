import {modelData, pushModel, addContext, modelSetValue, modelLens, modelLookup} from './EditorModel'
import React from 'baret'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import {PropertyEditor} from './PropertyEditor.jsx'
import {MerkitseSuoritusValmiiksiPopup} from './MerkitseSuoritusValmiiksiPopup.jsx'
import {JääLuokalleTaiSiirretäänEditor} from './JaaLuokalleTaiSiirretaanEditor.jsx'
import {onKeskeneräisiäOsasuorituksia, suoritusKesken, suoritusValmis, suorituksenTila, setTila} from './Suoritus'

export const TilaJaVahvistusEditor = ({model}) => {
  return (<div className="tila-vahvistus">
      <span className="tiedot">
        <span className="tila">
          Suoritus: <span className={ suoritusValmis(model) ? 'valmis' : ''}>{ suorituksenTila(model) }</span> { /* TODO: i18n */ }
        </span>
        {
          modelData(model).vahvistus && <PropertyEditor model={model} propertyName="vahvistus" edit="false"/>
        }
        <JääLuokalleTaiSiirretäänEditor model={addContext(model, {edit:false})}/>
      </span>
      <span className="controls">
        <MerkitseValmiiksiButton model={model}/>
        <MerkitseKeskeytyneeksiButton model={model}/>
        <MerkitseKeskeneräiseksiButton model={model}/>
      </span>
    </div>
  )
}

const MerkitseKeskeneräiseksiButton = ({model}) => {
  if (!model.context.edit || suoritusKesken(model)) return null
  var opiskeluoikeudenTila = modelData(model.context.opiskeluoikeus, 'tila.opiskeluoikeusjaksot.-1.tila').koodiarvo
  let merkitseKeskeneräiseksi = () => {
    pushModel(setTila(modelSetValue(model, undefined, 'vahvistus'), 'KESKEN'))
  }
  return <button className="merkitse-kesken" disabled={opiskeluoikeudenTila == 'valmistunut'} onClick={merkitseKeskeneräiseksi}>Merkitse keskeneräiseksi</button>
}

const MerkitseKeskeytyneeksiButton = ({model}) => {
  if (!model.context.edit || !suoritusKesken(model)) return null

  let merkitseKeskeytyneeksiJosKesken = (suoritus) => {
    if (!suoritusKesken(suoritus)) return suoritus
    let osasuorituksetKeskeytetty = L.modify([modelLens('osasuoritukset'), 'value', L.elems], merkitseKeskeytyneeksiJosKesken, suoritus)
    return setTila(osasuorituksetKeskeytetty, 'KESKEYTYNYT')
  }

  let merkitseKeskeytyneeksi = () => {
    pushModel(merkitseKeskeytyneeksiJosKesken(model))
  }
  return <button className="merkitse-keskeytyneeksi" onClick={merkitseKeskeytyneeksi}>Merkitse keskeytyneeksi</button>
}

const MerkitseValmiiksiButton = ({model}) => {
  if (!model.context.edit || !suoritusKesken(model)) return null
  let addingAtom = Atom(false)
  let merkitseValmiiksiCallback = (suoritusModel) => {
    if (suoritusModel) {
      pushModel(suoritusModel, model.context.changeBus)
    } else {
      addingAtom.set(false)
    }
  }
  let keskeneräisiä = onKeskeneräisiäOsasuorituksia(model)
  return (<span>
    <button className="merkitse-valmiiksi" disabled={keskeneräisiä} onClick={() => addingAtom.modify(x => !x)}>Merkitse valmiiksi</button>
    {
      addingAtom.map(adding => adding && <MerkitseSuoritusValmiiksiPopup suoritus={model} resultCallback={merkitseValmiiksiCallback}/>)
    }
  </span>)
}