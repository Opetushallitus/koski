import React from 'baret'
import Text from '../Text.jsx'
import {KurssitEditor} from './KurssitEditor.jsx'
import {modelItems} from './EditorModel'
import {arvioituTaiVahvistettu} from './Suoritus'

export default ({model}) => {
  let suorituksiaTehty = modelItems(model, 'osasuoritukset').filter(arvioituTaiVahvistettu).length > 0
  return (<div className="kurssit">
    {(model.context.edit || suorituksiaTehty) && <h5><Text name="Kurssit"/></h5>}
    <KurssitEditor model={model}/>
  </div>)
}