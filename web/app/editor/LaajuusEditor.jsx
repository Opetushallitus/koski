import React from 'react'
import {Editor} from './Editor.jsx'
import {modelData, modelEmpty, modelSetValue, modelValid} from './EditorModel'
import {wrapOptional} from './OptionalEditor.jsx'

export const LaajuusEditor = React.createClass({
  render() {
    let { model } = this.props
    let wrappedModel = wrapOptional({model: model, isEmpty: m => modelEmpty(m, 'arvo'), createEmpty: m => modelSetValue(m, undefined, 'arvo')})
    let yksikköData = modelData(wrappedModel, 'yksikkö')
    let arvoData = modelData(wrappedModel, 'arvo')

    let yksikkö = arvoData === undefined ? '' : yksikköData && (yksikköData.lyhytNimi || yksikköData.nimi).fi

    // TODO, Yksikön editointi
    return (
      <span className="property laajuus">
        <span className={modelValid(wrappedModel) ? 'value' : 'value error'}>
          <Editor model={wrappedModel} path="arvo"/>
        </span> {!wrappedModel.context.edit && <span className={'yksikko ' + yksikkö.toLowerCase()}>{yksikkö}</span>}
      </span>
    )
  }
})
LaajuusEditor.readOnly = false
LaajuusEditor.handlesOptional = true

LaajuusEditor.validateModel = (model) => {
  let arvo = modelData(model, 'arvo')

  if (arvo && isNaN(arvo) || arvo <= 0) {
    return ['invalid value']
  }
  if (!model.optional && !arvo) {
    return ['required value']
  }
  return []
}