import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import * as L from 'partial.lenses'
import {modelData, modelLookup} from './EditorModel.js'
import {completeWithFieldAlternatives} from './PerusopetuksenOppiaineetEditor.jsx'
import {lensedModel, modelSetValue, oneOfPrototypes} from './EditorModel'
import {EnumEditor} from './EnumEditor.jsx'

export const SelectAlternativeByEnumValueEditor = ({ model, path }) => {
  return (<span>
        {
          completeWithFieldAlternatives(oneOfPrototypes(model), path).map( protos => {
            let enumValues = protos.map(proto => modelLookup(proto, path).value)
            let tunnisteModel = lensedModel(model, L.lens(
              (m) => modelLookup(m, path),
              (enumModel, m) => {
                let foundProto = R.reverse(protos).find(proto => R.equals(modelData(enumModel), modelData(proto, path)))
                return modelSetValue(m, foundProto.value)
              }
            ))

            return <EnumEditor model={ tunnisteModel } fetchAlternatives = { () => Bacon.constant(enumValues) }/>
          })
        }
  </span>)
}
