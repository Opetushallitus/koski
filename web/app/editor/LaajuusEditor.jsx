import React from 'react'
import {Editor} from './Editor.jsx'
import {modelData, modelEmpty, modelSetValue, modelValid, modelLookup} from './EditorModel'
import {EnumEditor} from './EnumEditor.jsx'
import {wrapOptional} from './OptionalEditor.jsx'
import {parseBool} from '../util'
import {t} from '../i18n'
export const LaajuusEditor = React.createClass({
  render() {
    let { model, compact, showReadonlyScope = true } = this.props
    let wrappedModel = wrapOptional({model: model})
    return (
      <span className="property laajuus">
        <span className={modelValid(wrappedModel) ? 'value' : 'value error'}>
          <Editor model={wrappedModel} path="arvo"/>
        </span>
        <LaajuudenYksikköEditor { ... {model, compact, showReadonlyScope}}/>
      </span>
    )
  }
})
LaajuusEditor.isEmpty = (m) => modelEmpty(m, 'arvo')
LaajuusEditor.createEmpty = m => modelSetValue(m, undefined, 'arvo')
LaajuusEditor.readOnly = false
LaajuusEditor.handlesOptional = () => true

LaajuusEditor.validateModel = (model) => {
  let arvo = modelData(model, 'arvo')

  if (arvo && isNaN(arvo) || arvo <= 0) {
    return [{key: 'invalid.laajuus'}]
  }
  if (!model.optional && !arvo) {
    return [{key: 'missing'}]
  }
  return []
}

const LaajuudenYksikköEditor = ({model, compact, showReadonlyScope}) => {
  let arvoData = modelData(model, 'arvo')
  let yksikköModel = modelLookup(model, 'yksikkö')
  let yksikköData = modelData(yksikköModel)
  let yksikkö = arvoData === undefined ? '' : yksikköData && t((yksikköData.lyhytNimi || yksikköData.nimi))
  let alternatives = EnumEditor.knownAlternatives(yksikköModel)

  return model.context.edit
    ? !yksikköModel || !alternatives || (alternatives.length == 1 && parseBool(compact))
      ? null
      : <span className="yksikko"><Editor model={yksikköModel} edit={alternatives.length != 1}/></span>
    : showReadonlyScope
      ? <span className={'yksikko ' + yksikkö.toLowerCase()}> {yksikkö}</span>
      : null
}