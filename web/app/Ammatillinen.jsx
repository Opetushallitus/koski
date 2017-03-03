import React from 'react'
import { modelData, modelTitle } from './EditorModel.js'
import { PropertiesEditor } from './editor/PropertiesEditor.jsx'
import { PäivämääräväliEditor, KoulutusmoduuliEditor } from './editor/CommonEditors.jsx'

export const NäytönSuorituspaikkaEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span>{modelTitle(model, 'kuvaus')}</span>
  }
})
NäytönSuorituspaikkaEditor.readOnly = true

export const NäytönArvioitsijaEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span>{modelTitle(model, 'nimi')} { modelData(model, 'ntm') ? ' (näyttötutkintomestari)' : ''}</span>
  }
})
NäytönArvioitsijaEditor.readOnly = true
NäytönArvioitsijaEditor.canShowInline = () => true

const OppisopimusEditor = React.createClass({
  render() {
    let {model} = this.props
    return (<div className="oppisopimuksellinenjarjestamismuoto">
      <div>{ modelTitle(model, 'tunniste')}</div>
      <PropertiesEditor
        model = {model}
        propertyFilter={p => !['tunniste'].includes(p.key)}
      />
    </div>)
  }
})

export const TyössäoppimisjaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (
      <div className="tyossaoppimisjakso">
        <PäivämääräväliEditor model={model}/> { modelTitle(model, 'paikkakunta')}, { modelTitle(model, 'maa')}
        <PropertiesEditor
          model = {model}
          propertyFilter={p => !['alku', 'loppu', 'paikkakunta', 'maa'].includes(p.key)}
        />
      </div>
    )
  }
})
TyössäoppimisjaksoEditor.readOnly = true

export const editorMapping = {
  'ammatillisentutkinnonosa': KoulutusmoduuliEditor,
  'naytonsuorituspaikka': NäytönSuorituspaikkaEditor,
  'naytonarvioitsija': NäytönArvioitsijaEditor,
  'naytonsuoritusaika': PäivämääräväliEditor,
  'tyossaoppimisjakso': TyössäoppimisjaksoEditor,
  'oppisopimuksellinenjarjestamismuoto': OppisopimusEditor
}