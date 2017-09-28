import React from 'baret'
import Text from '../Text.jsx'
import {KurssitEditor} from './KurssitEditor.jsx'

export default ({model}) => {
  return (<div className="kurssit">
    <h5><Text name="Kurssit"/></h5>
    <KurssitEditor model={model}/>
  </div>)
}