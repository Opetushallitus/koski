import {modelData, pushModel, addContext} from './EditorModel'
import React from 'baret'
import Atom from 'bacon.atom'
import {PropertyEditor} from './PropertyEditor.jsx'
import {MerkitseSuoritusValmiiksiPopup} from './MerkitseSuoritusValmiiksiPopup.jsx'
import {JääLuokalleTaiSiirretäänEditor} from './JaaLuokalleTaiSiirretaanEditor.jsx'
import {onKeskeneräisiäOsasuorituksia} from './Suoritus'

export const TilaJaVahvistusEditor = ({model}) => {
  let tila = modelData(model).tila.koodiarvo
  return (<div className="tila-vahvistus">
      <span className="tiedot">
        <span className="tila">
          Suoritus: <span className={ tila === 'VALMIS' ? 'valmis' : ''}>{ modelData(model).tila.koodiarvo }</span> { /* TODO: i18n */ }
        </span>
        {
          modelData(model).vahvistus && <PropertyEditor model={model} propertyName="vahvistus" edit="false"/>
        }
        <JääLuokalleTaiSiirretäänEditor model={addContext(model, {edit:false})}/>
      </span>
      <span className="controls">
        <MerkitseValmiiksiButton model={model}/>
      </span>
    </div>
  )
}

const MerkitseValmiiksiButton = ({model}) => {
  let tila = modelData(model).tila.koodiarvo
  if (!model.context.edit || tila != 'KESKEN') return null
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