import React from 'react'
import {modelData, modelLookup, modelTitle} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {PäivämääräväliEditor} from './PaivamaaravaliEditor.jsx'
import Text from '../Text.jsx'
import {OrganisaatioEditor} from './OrganisaatioEditor.jsx'

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
    return <span>{modelTitle(model, 'nimi')} { modelData(model, 'ntm') ? <span>{' ('}<Text name='näyttötutkintomestari'/>{')'}</span> : ''}</span>
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

const TutkinnonOsanLisätietoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (<div className="ammatillisentutkinnonosanlisatieto">
      <Editor model={ model } path="tunniste"/>
      <div className="kuvaus">
        <Editor model={ model } path="kuvaus"/>
      </div>
    </div>)
  }
})

export const JärjestämismuotojaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (
        <div className="jarjestamismuotojakso">
          <PäivämääräväliEditor model={model}/>{', '} { modelTitle(model, 'järjestämismuoto.tunniste')}
          <PropertiesEditor
              model = {modelLookup(model, 'järjestämismuoto')}
              propertyFilter={p => !['tunniste'].includes(p.key)}
          />
          <PropertiesEditor
              model = {model}
              propertyFilter={p => !['alku', 'loppu', 'järjestämismuoto'].includes(p.key)}
          />
        </div>
    )
  }
})
JärjestämismuotojaksoEditor.readOnly = true
JärjestämismuotojaksoEditor.validateModel = PäivämääräväliEditor.validateModel

export const TyössäoppimisjaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (
      <div className="tyossaoppimisjakso">
        <PäivämääräväliEditor model={model}/> { modelTitle(model, 'paikkakunta')}{', '}{ modelTitle(model, 'maa')}
        <PropertiesEditor
          model = {model}
          propertyFilter={p => !['alku', 'loppu', 'paikkakunta', 'maa'].includes(p.key)}
        />
      </div>
    )
  }
})
TyössäoppimisjaksoEditor.readOnly = true
TyössäoppimisjaksoEditor.validateModel = PäivämääräväliEditor.validateModel

const SisältäväOpiskeluoikeusEditor = ({model}) => {
  if (model.context.edit) {
    return (
        <PropertiesEditor model={model} getValueEditor={(p, getDefault) => {
          return p.key === 'oppilaitos' ? <OrganisaatioEditor model={p.model} showAll={true}/> : getDefault()
        }}/>
    )
  }
  return <span><span className="id"><Editor model={model} path="id"/></span><span className="oppilaitos">{'('}<Editor model={model} path="oppilaitos"/>{')'}</span></span>
}

export const editorMapping = {
  'naytonsuorituspaikka': NäytönSuorituspaikkaEditor,
  'naytonarvioitsija': NäytönArvioitsijaEditor,
  'naytonsuoritusaika': PäivämääräväliEditor,
  'tyossaoppimisjakso': TyössäoppimisjaksoEditor,
  'jarjestamismuotojakso': JärjestämismuotojaksoEditor,
  'oppisopimuksellinenjarjestamismuoto': OppisopimusEditor,
  'ammatillisentutkinnonosanlisatieto': TutkinnonOsanLisätietoEditor,
  'sisaltavaopiskeluoikeus': SisältäväOpiskeluoikeusEditor
}