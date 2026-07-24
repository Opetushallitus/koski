import React from 'react'
import { Editor } from '../editor/Editor'
import {
  diaLaajuudenOletusprototyyppi,
  modelData,
  modelEmpty,
  modelLookup,
  modelSetValue,
  modelValid,
  onDiaLaajuusUnioni,
  oneOfPrototypes,
  pushModel,
  resetOptionalModel,
  wrapOptional
} from '../editor/EditorModel'
import { hasValue } from '../types/EditorModels'
import {
  EnumEditor,
  fetchAlternativesBasedOnPrototypes
} from '../editor/EnumEditor'
import { parseBool } from '../util/util'
import { numberToString } from '../util/format'
import { t } from '../i18n/i18n'
import { hyphenate } from '../util/hyphenate'

export class LaajuusEditor extends React.Component {
  render() {
    const { model, compact, showReadonlyScope = true, disabled } = this.props
    const wrappedModel = wrapOptional(model)
    return (
      <span>
        <span className="property laajuus arvo" data-testid="laajuus-editor">
          <span className={modelValid(wrappedModel) ? 'value' : 'value error'}>
            {onDiaLaajuusUnioni(model) ? (
              <DiaLaajuudenArvoEditor model={model} disabled={disabled} />
            ) : (
              <Editor model={wrappedModel} path="arvo" disabled={disabled} />
            )}
          </span>
        </span>
        <LaajuudenYksikköEditor {...{ model, compact, showReadonlyScope }} />
      </span>
    )
  }
}

// Työntää koko laajuusmallin oikealla yksiköllä laajuuden polkuun (yksikkö valitaan
// renderöinnissä alkamispäivän mukaan; changeBus ei näe opiskeluoikeuskontekstia).
const DiaLaajuudenArvoEditor = ({ model, disabled }) => {
  const data = modelData(model, 'arvo')
  if (!model.context.edit || disabled) {
    return <span className="inline number">{numberToString(data)}</span>
  }
  const onChange = (event) => {
    const raw = event.target.value
    if (!raw) {
      resetOptionalModel(model)
      return
    }
    const base = hasValue(model)
      ? wrapOptional(model)
      : diaLaajuudenOletusprototyyppi(model)
    pushModel(modelSetValue(base, { data: parseNumber(raw) }, 'arvo'))
  }
  const error = !modelValid(model)
  return (
    <input
      type="text"
      defaultValue={numberToString(data)}
      onChange={onChange}
      className={'editor-input inline number' + (error ? ' error' : '')}
      data-testid="number-editor"
    />
  )
}

const parseNumber = (s) => {
  s = s.replace(',', '.')
  if (isNaN(s)) return s
  return parseFloat(s)
}
LaajuusEditor.isEmpty = (m) => modelEmpty(m, 'arvo')
LaajuusEditor.createEmpty = (m) => modelSetValue(m, undefined, 'arvo')
LaajuusEditor.readOnly = false
LaajuusEditor.handlesOptional = () => true

LaajuusEditor.validateModel = (model) => {
  const arvo = modelData(model, 'arvo')

  if ((arvo && isNaN(arvo)) || arvo <= 0) {
    return [{ key: 'invalid.laajuus' }]
  }
  if (!model.optional && !arvo) {
    return [{ key: 'missing' }]
  }
  return []
}

const LaajuudenYksikköEditor = ({ model, compact, showReadonlyScope }) => {
  const arvoData = modelData(model, 'arvo')
  const yksikköModel = modelLookup(model, 'yksikkö')
  const yksikköData = modelData(yksikköModel)
  const yksikkö =
    arvoData === undefined
      ? ''
      : yksikköData && t(yksikköData.lyhytNimi || yksikköData.nimi)
  const alternatives = EnumEditor.knownAlternatives(yksikköModel)

  return model.context.edit ? (
    !yksikköModel ||
    !alternatives ||
    (alternatives.length === 1 && parseBool(compact)) ? null : (
      <span className="property laajuudenyksikko yksikko inline">
        <Editor
          model={yksikköModel}
          edit={alternatives.length !== 1 || !!model.oneOfPrototypes}
          fetchAlternatives={(m) =>
            model.oneOfPrototypes
              ? yksikköAlternativesBasedOnPrototypes(model)
              : EnumEditor.fetchAlternatives(m)
          }
        />
      </span>
    )
  ) : showReadonlyScope ? (
    <span
      className={'property laajuudenyksikko yksikko ' + yksikkö.toLowerCase()}
    >
      {'\u00a0'}
      {hyphenate(yksikkö)}
    </span>
  ) : null
}

const yksikköAlternativesBasedOnPrototypes = (model) =>
  fetchAlternativesBasedOnPrototypes(oneOfPrototypes(model), 'yksikkö')
    .startWith([])
    .map((alts) => alts.map((m) => modelLookup(m, 'yksikkö').value))
