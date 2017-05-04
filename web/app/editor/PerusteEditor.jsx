import React from 'baret'
import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import {modelData} from './EditorModel.js'
import {pushModelValue} from './EditorModel'
import {wrapOptional} from './OptionalEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {PerusteDropdown} from './PerusteDropdown.jsx'

export const PerusteEditor = ({model}) => {
  if (!model.context.edit) return <StringEditor model={model}/>
  model = wrapOptional({model})
  let perusteAtom = Atom(modelData(model))
  perusteAtom.onValue(diaarinumero => pushModelValue(model, { data: diaarinumero }))
  return <span>
    <PerusteDropdown {...{perusteAtom, suoritusP: Bacon.constant(modelData(model.context.suoritus))}}/>
  </span>
}
PerusteEditor.handlesOptional=true