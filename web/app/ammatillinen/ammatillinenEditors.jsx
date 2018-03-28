import React from 'baret'
import {modelData, modelLookup, modelTitle} from '../editor/EditorModel.js'
import {Editor} from '../editor/Editor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {PäivämääräväliEditor} from '../date/PaivamaaravaliEditor'
import Text from '../i18n/Text'
import {OrganisaatioEditor} from '../organisaatio/OrganisaatioEditor'
import {SelectAlternativeByEnumValueEditor} from '../editor/SelectAlternativeByEnumValueEditor'
import {AmmatillinenNäyttöEditor} from './AmmatillinenNayttoEditor'
import {TutkinnonOsanSuoritusEditor} from '../suoritus/Suoritustaulukko'
import {InlineJaksoEditor} from '../date/JaksoEditor'
import {wrapOptional} from '../editor/EditorModel'
import {hasModelProperty} from '../editor/EditorModel'

class NäytönSuorituspaikkaEditor extends React.Component {
  render() {
    let {model} = this.props
    return <Editor model={model} path="kuvaus"/>
  }
}
NäytönSuorituspaikkaEditor.readOnly = true

class NäytönArvioitsijaEditor extends React.Component {
  render() {
    let {model} = this.props
    return <span>{modelTitle(model, 'nimi')} { modelData(model, 'ntm') ? <span>{' ('}<Text name='näyttötutkintomestari'/>{')'}</span> : null}</span>
  }
}
NäytönArvioitsijaEditor.readOnly = true
NäytönArvioitsijaEditor.canShowInline = () => true

class OppisopimusEditor extends React.Component {
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
}

class TutkinnonOsanLisätietoEditor extends React.Component {
  render() {
    let {model} = this.props
    return (<div className="ammatillisentutkinnonosanlisatieto">
      <Editor model={ model } path="tunniste"/>
      <div className="kuvaus">
        <Editor model={ model } path="kuvaus"/>
      </div>
    </div>)
  }
}

class JärjestämismuotojaksoEditor extends React.Component {
  render() {
    let {model} = this.props
    let propertyName = hasModelProperty(model, 'järjestämismuoto') ? 'järjestämismuoto' : 'osaamisenHankkimistapa'
    return (
        <div className="jarjestamismuotojakso">
          <PäivämääräväliEditor model={model}/>
          {', '}
          <span className="jarjestamismuoto">
            <SelectAlternativeByEnumValueEditor model={modelLookup(model, propertyName)} path="tunniste" className="järjestämismuoto" />
            <PropertiesEditor
              className="inline"
              model = {modelLookup(model, propertyName)}
              propertyFilter={p => !['tunniste'].includes(p.key)}
            />
            <PropertiesEditor
              className="inline"
              model = {model}
              propertyFilter={p => !['alku', 'loppu', propertyName].includes(p.key)}
            />
          </span>
        </div>
    )
  }
}
JärjestämismuotojaksoEditor.validateModel = PäivämääräväliEditor.validateModel

const TyössäoppimisjaksoEditor = ({model}) => {
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
TyössäoppimisjaksoEditor.readOnly = true
TyössäoppimisjaksoEditor.validateModel = PäivämääräväliEditor.validateModel

const SisältäväOpiskeluoikeusEditor = ({model}) => {
  if (model.context.edit) {
    return (
        <PropertiesEditor model={model} getValueEditor={(p, getDefault) => {
          return p.key === 'oppilaitos' ? <OrganisaatioEditor model={p.model} showAll={true} organisaatioTyypit={['OPPILAITOS', 'OPPISOPIMUSTOIMIPISTE']}/> : getDefault()
        }}/>
    )
  }
  return <span><span className="id"><Editor model={model} path="oid"/></span><span className="oppilaitos">{'('}<Editor model={model} path="oppilaitos"/>{')'}</span></span>
}

const OsaamisalajaksoEditor = ({model}) => {
  let wrappedModel = wrapOptional(model)
  return (
    <span className="osaamisalajakso">
      <span className="property osaamisala"><Editor model={model} path="osaamisala"/></span>
      <PäivämääräväliEditor model={wrappedModel}/>
    </span>
  )
}
OsaamisalajaksoEditor.handlesOptional = () => true
OsaamisalajaksoEditor.validateModel = PäivämääräväliEditor.validateModel

export default {
  'naytonsuorituspaikka': NäytönSuorituspaikkaEditor,
  'naytonarvioitsija': NäytönArvioitsijaEditor,
  'naytonsuoritusaika': PäivämääräväliEditor,
  'tyossaoppimisjakso': TyössäoppimisjaksoEditor,
  'jarjestamismuotojakso': JärjestämismuotojaksoEditor,
  'osaamisenhankkimistapajakso': JärjestämismuotojaksoEditor,
  'oppisopimuksellinenjarjestamismuoto': OppisopimusEditor,
  'oppisopimuksellinenosaamisenhankkimistapa': OppisopimusEditor,
  'ammatillisentutkinnonosanlisatieto': TutkinnonOsanLisätietoEditor,
  'sisaltavaopiskeluoikeus': SisältäväOpiskeluoikeusEditor,
  'naytto': AmmatillinenNäyttöEditor,
  'ammatillisentutkinnonosansuoritus': TutkinnonOsanSuoritusEditor,
  'ulkomaanjakso': InlineJaksoEditor,
  'poissaolojakso': InlineJaksoEditor,
  'osaaikaisuusjakso': InlineJaksoEditor,
  'osaamisalajakso': OsaamisalajaksoEditor,
  'opiskeluvalmiuksiatukevienopintojenjakso': InlineJaksoEditor
}
