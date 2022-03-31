import React from 'react'
import {Editor} from '../editor/Editor'
import {
  modelData,
  modelEmpty,
  modelLookup,
  modelSetValue,
  modelValid,
  oneOfPrototypes,
  wrapOptional
} from '../editor/EditorModel'
import {EnumEditor, fetchAlternativesBasedOnPrototypes} from '../editor/EnumEditor'
import {parseBool} from '../util/util'
import {t} from '../i18n/i18n'
import {hyphenate} from '../util/hyphenate'

export class LaajuusEditor extends React.Component {
  render() {
    let { model, compact, showReadonlyScope = true } = this.props
    let wrappedModel = wrapOptional(model)
    return (
      <span>
        <span className="property laajuus arvo">
          <span className={modelValid(wrappedModel) ? 'value' : 'value error'}>
            <Editor model={wrappedModel} path="arvo"/>
          </span>
        </span>
        <LaajuudenYksikköEditor { ... {model, compact, showReadonlyScope}}/>
      </span>
    )
  }
}
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
      : <span className="property laajuudenyksikko yksikko inline">
        <Editor model={yksikköModel} edit={alternatives.length !== 1 || !!model.oneOfPrototypes} fetchAlternatives={m => model.oneOfPrototypes ? yksikköAlternativesBasedOnPrototypes(model) : EnumEditor.fetchAlternatives(m)} />
      </span>
    : showReadonlyScope
      ? <span className={'property laajuudenyksikko yksikko ' + yksikkö.toLowerCase()}>{'\u00a0'}{hyphenate(yksikkö)}</span>
      : null
}

LaajuudenYksikköEditor.displayName = 'LaajuudenYksikköEditor'

const yksikköAlternativesBasedOnPrototypes = model => fetchAlternativesBasedOnPrototypes(oneOfPrototypes(model), 'yksikkö').startWith([]).map(alts => alts.map(m => modelLookup(m, 'yksikkö').value))
