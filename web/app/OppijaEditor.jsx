import React from 'react'
import R from 'ramda'
import { modelData, modelLookup, modelTitle, modelEmpty, modelItems } from './EditorModel.js'
import { opiskeluOikeusChange } from './Oppija.jsx'
import { formatISODate, parseFinnishDate } from './date.js'
import Http from './http'
import Bacon from 'baconjs'

export const OppijaEditor = React.createClass({
  render() {
    let {model} = this.props
    return model ? (
      <ul className="oppilaitokset">
        {
          modelLookup(model, 'opiskeluoikeudet').value.map((oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) => {
              let context = { oppijaOid: modelData(model, 'henkilö.oid'), root: true, prototypes: model.prototypes }
              let oppilaitos = modelLookup(oppilaitoksenOpiskeluoikeudet, 'oppilaitos')
              let opiskeluoikeudet = modelItems(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet')
              return (<li className="oppilaitos" key={modelData(oppilaitos).oid}>
                <span className="oppilaitos">{modelTitle(oppilaitos)}</span>
                <OppilaitoksenOpintosuoritusote oppilaitos={oppilaitos} tyyppi={modelData(opiskeluoikeudet[0], 'tyyppi').koodiarvo} context={context} />
                {
                  opiskeluoikeudet.map( (opiskeluoikeus, opiskeluoikeusIndex) =>
                    <OpiskeluoikeusEditor key={ opiskeluoikeusIndex } model={ opiskeluoikeus } context={addPath(context, 'opiskeluoikeudet', oppilaitosIndex, 'opiskeluoikeudet', opiskeluoikeusIndex)} />
                  )
                }
              </li>)
            }
          )}
      </ul>
    ) : null
  }
})

const OpiskeluoikeusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let subContext = R.merge(context, {editable: model.editable, opiskeluoikeusId: modelData(model, 'id')})
    return (<div className="opiskeluoikeus">
      <div className="kuvaus">
        Opiskeluoikeus&nbsp;
          <span className="alku pvm">{modelTitle(model, 'alkamispäivä')}</span>-
          <span className="loppu pvm">{modelTitle(model, 'päättymispäivä')}</span>,&nbsp;
          <span className="tila">{modelTitle(model, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()}</span>
        <FoldableEditor expandedView={() => <PropertiesEditor properties={ model.value.properties.filter(property => property.key != 'suoritukset') } context={subContext}/>}/>
      </div>
      {
        modelItems(model, 'suoritukset').map((suoritusModel, i) =>
          <SuoritusEditor model={suoritusModel} context={addPath(subContext, 'suoritukset', i)} key={i}/>
        )
      }
      <OpiskeluoikeudenOpintosuoritusote opiskeluoikeus={model} context={context}/>
    </div>)
  }
})

const SuoritusEditor = React.createClass({
  render() {
    let {model, context} = this.props

    let title = modelTitle(model, 'koulutusmoduuli')
    let className = 'suoritus ' + model.value.class
    return (<div className={className}>
      <span className="kuvaus">{title}</span>
      <Todistus suoritus={model} context={context}/>
      <FoldableEditor expandedView={() => <PropertiesEditor properties={model.value.properties} context={R.merge(context, {editable: model.editable})}/>}/>
    </div>)
  }
})

const Todistus = React.createClass({
  render() {
    let {suoritus, context: { oppijaOid }} = this.props
    let suoritustyyppi = modelData(suoritus, 'tyyppi').koodiarvo
    let koulutusmoduuliKoodistoUri = modelData(suoritus, 'koulutusmoduuli').tunniste.koodistoUri
    let koulutusmoduuliKoodiarvo = modelData(suoritus, 'koulutusmoduuli').tunniste.koodiarvo
    let suoritusTila = modelData(suoritus, 'tila').koodiarvo
    let href = '/koski/todistus/' + oppijaOid + '?suoritustyyppi=' + suoritustyyppi + '&koulutusmoduuli=' + koulutusmoduuliKoodistoUri + '/' + koulutusmoduuliKoodiarvo
    return suoritusTila == 'VALMIS' && suoritustyyppi != 'korkeakoulututkinto'
      ? <a className="todistus" href={href}>näytä todistus</a>
      : null
  }
})

const OppilaitoksenOpintosuoritusote = React.createClass({
  render() {
    let {oppilaitos, tyyppi, context: { oppijaOid }} = this.props

    if (tyyppi == 'korkeakoulutus') { // vain korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(oppilaitos).oid
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})

const OpiskeluoikeudenOpintosuoritusote = React.createClass({
  render() {
    let {opiskeluoikeus, context: { oppijaOid }} = this.props
    if (modelData(opiskeluoikeus, 'tyyppi').koodiarvo == 'lukiokoulutus') { // vain lukiokoulutukselle näytetään opiskeluoikeuskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?opiskeluoikeus=' + modelData(opiskeluoikeus, 'id')
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})

const OppiaineEditor = React.createClass({
  render() {
    let {model} = this.props
    return (<div className="oppiaineensuoritus">
      <label className="oppiaine">{modelTitle(model, 'koulutusmoduuli')}</label>
      <span className="arvosana">{modelTitle(model, 'arviointi.-1')}</span>
      {modelData(model, 'korotus') ? <span className="korotus">(korotus)</span> : null}
    </div>)
  }
})

const LaajuusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <ObjectEditor model={model} context={context}/>
      : <span>{modelTitle(model, 'arvo')} {modelTitle(model, 'yksikkö')}</span>
  }
})

const VahvistusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <ObjectEditor model={model} context={context} />
      : (<span className="vahvistus simple">
          <span className="date">{modelTitle(model, 'päivä')}</span>&nbsp;
          <span className="allekirjoitus">{modelTitle(model, 'paikkakunta')}</span>&nbsp;
          {
            (modelItems(model, 'myöntäjäHenkilöt') || []).map( (henkilö,i) =>
              <span key={i} className="nimi">{modelData(henkilö, 'nimi')}</span>
            )
          }
        </span>)
  }
})

const OsaamisenTunnustaminenEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <ObjectEditor model={model} context={context}/>
      : (<span className="object osaamisentunnustaminen">
          <label>{modelTitle(model, 'selite')}</label>
          { getModelEditor(modelLookup(model, 'osaaminen'), addPath(context, 'osaaminen'))}
        </span>)
  }
})

const OpiskeluoikeusjaksoEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <ObjectEditor model={model} context={context}/>
      : (<div className="opiskeluoikeusjakso">
        <label className="date">{modelTitle(model, 'alku')}</label>
        <label className="tila">{modelTitle(model, 'tila')}</label>
      </div>)
  }
})

const TutkinnonosaEditor = React.createClass({
  render() {
    let {model, context} = this.props

    return (<div className="suoritus tutkinnonosa">
      <FoldableEditor defaultExpanded={context.edit}
                      collapsedView={() => <span className="tutkinnonosan-tiedot">
                        <label className="nimi">{modelTitle(model, 'koulutusmoduuli')}</label>
                        <span className="arvosana">{modelTitle(model, 'arviointi.-1')}</span>
                        </span>}
                      expandedView={() => <span>
                        <label className="nimi">{modelTitle(model, 'koulutusmoduuli')}</label>
                        <PropertiesEditor properties={model.value.properties} context={context}/>
                        </span>}
      />
    </div>)
  }
})

const ObjectEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let className = model.value
      ? 'object ' + model.value.class
      : 'object empty'
    let representative = model.value.properties.find(property => property.representative)
    let representativeEditor = () => getModelEditor(representative.model, addPath(context, representative.key))
    let objectEditor = () => <div className={className}><PropertiesEditor properties={model.value.properties} context={context} /></div>
    return modelTitle(model)
      ? context.edit
        ? objectEditor()
        : <span className="simple title">{modelTitle(model)}</span>
      : representative
        ? model.value.properties.length == 1
          ? representativeEditor()
          : <div className="object-wrapper">{representativeEditor()}<FoldableEditor expandedView={objectEditor} defaultExpandeded={context.edit} /></div>
        : objectEditor()
  }
})

const FoldableEditor = React.createClass({
  render() {
    let {collapsedView, expandedView, defaultExpanded} = this.props
    let expanded = this.state? this.state.expanded : defaultExpanded
    let toggleExpanded = () => { this.setState({expanded: !expanded})}
    let className = expanded ? 'foldable expanded' : 'foldable collapsed'
    return (<div className={className}>
      <a className="toggle-expand" onClick={toggleExpanded}>{ expanded ? '-' : '+' }</a>
      { expanded ? expandedView() : (collapsedView ? collapsedView() : null) }
    </div>)
  }
})

const PropertiesEditor = React.createClass({
  render() {
    let {properties, context} = this.props
    let edit = context.edit || (this.state && this.state.edit)
    let toggleEdit = () => this.setState({edit: !edit})
    return (<ul className="properties">
      {
        context.editable && !context.edit ? <a className="toggle-edit" onClick={toggleEdit}>{edit ? 'valmis' : 'muokkaa'}</a> : null
      }
      {
        properties.filter(property => (edit || !modelEmpty(property.model)) && !property.hidden).map(property => {
          let propertyClassName = 'property ' + property.key
          return (<li className={propertyClassName} key={property.key}>
            <label>{property.title}</label>
            <span className="value">{ getModelEditor(property.model, addPath(R.merge(context, {edit: edit}),property.key)) }</span>
          </li>)
        })
      }
    </ul>)
  }
})

let addPath = (context, ...pathElems) => {
  let path = ((context.path && [context.path]) || []).concat(pathElems).join('.')
  return R.merge(context, { path, root: false })
}

const ArrayEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let items = modelItems(model)
    let simple = !items[0] || items[0].simple || (!context.edit && modelTitle(items[0]))
    let className = simple ? 'array simple' : 'array'
    let adding = this.state && this.state.adding || []
    let add = () => this.setState({adding: adding.concat(model.prototype)})
    return (
      <ul className={className}>
        {
          items.concat(adding).map((item, i) =>
            <li key={i}>{getModelEditor(item, addPath(context, i) )}</li>
          )
        }
        {
          context.edit && model.prototype !== undefined ? <li className="add-item"><a onClick={add}>lisää uusi</a></li> : null
        }
      </ul>
    )
  }
})


const OptionalEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let adding = this.state && this.state.adding
    let add = () => this.setState({adding: true})
    return adding
      ? getModelEditor(model.prototype, context, true)
      : <a className="add-value" onClick={add}>lisää</a>
  }
})

const StringEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let {valueBus} = this.state

    let onChange = (event) => {
      valueBus.push([context, {data: event.target.value}])
    }

    return context.edit
      ? <input type="text" defaultValue={modelData(model)} onChange={ onChange }></input>
      : <span className="simple string">{modelData(model)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.throttle(1000).onValue((v) => {opiskeluOikeusChange.push(v)})
  }

})

const BooleanEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let onChange = event => {
      opiskeluOikeusChange.push([context, {data: event.target.checked}])
    }

    return context.edit
      ? <input type="checkbox" defaultChecked={modelData(model)} onChange={ onChange }></input>
      : <span className="simple string">{modelTitle(model)}</span>
  }
})

const DateEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let {invalidDate, valueBus} = this.state

    let onChange = (event) => {
      var date = parseFinnishDate(event.target.value)
      if (date) {
        valueBus.push([context, {data: formatISODate(date)}])
      }
      this.setState({invalidDate: date ? false : true})
    }

    return context.edit
      ? <input type="text" defaultValue={modelTitle(model)} onChange={ onChange } className={invalidDate ? 'error' : ''}></input>
      : <span className="simple date">{modelTitle(model)}</span>
  },

  getInitialState() {
    return {valueBus: Bacon.Bus()}
  },

  componentDidMount() {
    this.state.valueBus.throttle(1000).onValue((v) => {opiskeluOikeusChange.push(v)})
  }

})

const EnumEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let alternatives = model.alternatives || (this.state.alternatives) || []
    let className = alternatives.length ? '' : 'loading'
    let onChange = (event) => {
      let selected = alternatives.find(alternative => alternative.value == event.target.value)
      opiskeluOikeusChange.push([context, selected])
    }
    return context.edit
      ? (<select className={className} defaultValue={model.value && model.value.value} onChange={ onChange }>
          {
            alternatives.map( alternative =>
              <option value={ alternative.value } key={ alternative.value }>{alternative.title}</option>
            )
          }
        </select>)
      : <span className="simple enum">{modelTitle(model)}</span>
  },

  update(props) {
    let {model, context} = props
    if (context.edit && model.alternativesPath && !this.state.alternativesP) {
      this.state.alternativesP = Alternatives[model.alternativesPath]
      if (!this.state.alternativesP) {
        this.state.alternativesP = Http.get(model.alternativesPath).toProperty()
        Alternatives[model.alternativesPath] = this.state.alternativesP
      }
      this.state.alternativesP.onValue(alternatives => this.setState({alternatives}))
    }
  },

  componentWillMount() {
    this.update(this.props)
  },

  componentWillReceiveProps(props) {
    this.update(props)
  },

  getInitialState() {
    return {}
  }
})

const Alternatives = {}

const NullEditor = React.createClass({
  render() {
    return null
  }
})

const editorTypes = {
  'perusopetuksenoppiaineensuoritus': OppiaineEditor,
  'perusopetukseenvalmistavanopetuksenoppiaineensuoritus': OppiaineEditor,
  'perusopetuksenlisaopetuksenoppiaineensuoritus': OppiaineEditor,
  'ammatillisentutkinnonosansuoritus': TutkinnonosaEditor,
  'lukionoppiaineensuoritus': TutkinnonosaEditor,
  'ylioppilastutkinnonkokeensuoritus': TutkinnonosaEditor,
  'lukionkurssinsuoritus': OppiaineEditor,
  'ammatillinenopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'lukionopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'perusopetuksenopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'henkilovahvistus': VahvistusEditor,
  'organisaatiovahvistus': VahvistusEditor,
  'osaamisentunnustaminen': OsaamisenTunnustaminenEditor,
  'laajuusosaamispisteissa' : LaajuusEditor,
  'laajuuskursseissa' : LaajuusEditor,
  'laajuusopintopisteissa' : LaajuusEditor,
  'laajuusvuosiviikkotunneissa' : LaajuusEditor,
  'object': ObjectEditor,
  'array': ArrayEditor,
  'string': StringEditor,
  'number': StringEditor,
  'date': DateEditor,
  'boolean': BooleanEditor,
  'enum': EnumEditor
}

const getModelEditor = (model, context) => {
  const getEditorFunction = () => {
    if (!model) return NullEditor
    if (model.type == 'prototype' && context.editable) {
      let prototypeModel = context.prototypes[model.key]
      model = model.optional
        ? R.merge(prototypeModel, { value: null, optional: true, prototype: model.prototype}) // Remove value from prototypal value of optional model, to show it as empty
        : prototypeModel
    }
    if (modelEmpty(model) && model.optional && model.prototype !== undefined) {
      return OptionalEditor
    }
    let editor = (model.value && editorTypes[model.value.class]) || editorTypes[model.type]
    if (!editor) {
      if (!model.type) {
        console.log('Typeless model', model)
      }
      console.log('Missing editor ' + model.type)
      return NullEditor
    }
    return editor
  }
  var Editor = getEditorFunction()
  return <Editor model={model} context={context} />
}

