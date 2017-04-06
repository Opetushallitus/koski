import React from 'react'
import {Editor} from './Editor.jsx'
import {modelData, modelEmpty, modelSetValue} from './EditorModel'
import {wrapOptional} from './OptionalEditor.jsx'

export const LaajuusEditor = React.createClass({
  render() {
    let { model } = this.props
    let wrappedModel = wrapOptional({model: model, isEmpty: m => modelEmpty(m, 'arvo'), createEmpty: m => modelSetValue(m, undefined, 'arvo')})
    let yksikköData = modelData(wrappedModel, 'yksikkö')
    let yksikkö = yksikköData && (yksikköData.lyhytNimi || yksikköData.nimi).fi

    // TODO, validointi ja yksikön editointi
    return (
      <span className="property laajuus">
        <span className="value">
          <Editor model={wrappedModel} path="arvo"/>
        </span> {!wrappedModel.context.edit && <span className={'yksikko ' + yksikkö.toLowerCase()}>{yksikkö}</span>}
      </span>
    )
  }
})
LaajuusEditor.readOnly = false
LaajuusEditor.handlesOptional = true