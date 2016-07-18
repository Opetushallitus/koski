import React from 'react'
import { modelData, modelLookup } from './EditorModel.js'

export const OppijaEditor = React.createClass({
  render() {
    let {editor} = this.props
    return editor ? (
      <div className="editor">
        <ul className="oppilaitokset">
          {
            modelLookup(editor, 'opiskeluoikeudet').items.map((thing) => {
                let context = { oppijaOid: modelLookup(editor, 'henkilö.oid').data }
                let oppilaitos = modelLookup(thing, 'oppilaitos')
                let opiskeluoikeudet = modelLookup(thing, 'opiskeluoikeudet').items
                return <li className="oppilaitos" key={modelData(oppilaitos).oid}>
                  <span className="oppilaitos">{oppilaitos.title}</span>
                  <OppilaitoksenOpintosuoritusote oppilaitos={oppilaitos} tyyppi={modelData(opiskeluoikeudet[0], 'tyyppi').koodiarvo} context={context} />
                  {
                    opiskeluoikeudet.map( (opiskeluoikeus, index) =>
                      <Opiskeluoikeus key={ index } model={ opiskeluoikeus } context={context} />
                    )
                  }
                </li>
              }
            )}
        </ul>
      </div>
    ) : null
  }
})

const Opiskeluoikeus = React.createClass({
  render() {
    let {model, context} = this.props
    let toggleDetails = () => { this.setState({showDetails: !this.state.showDetails})}

    return <div className="opiskeluoikeus">
      {
        modelLookup(model, 'suoritukset.items').map((suoritusModel, i) => {
          let title = modelLookup(suoritusModel, 'koulutusmoduuli.title')
          return <div className="suoritus" key={i}>
            <span className="tutkinto">{title}</span>
            <OpiskeluoikeudenOpintosuoritusote opiskeluoikeus={model} context={context}/>
            <Todistus suoritus={suoritusModel} context={context}/>
          </div>
        })
      }
      <a onClick={toggleDetails}>{ this.state.showDetails ? '-' : '+' }</a>
      {
        this.state.showDetails
          ? getModelEditor(model, context)
          : null
      }
    </div>
  },
  getInitialState() {
    return {}
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
    return <div className="oppiaineensuoritus">
        <label class="oppiaine">{modelLookup(model, 'koulutusmoduuli').title}</label>
        <span class="arvosana">{modelLookup(model, 'arviointi.-1.arvosana').data.koodiarvo}</span>
      </div>
  }
})

const VahvistusEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span>
      <span class="date">{modelLookup(model, 'päivä').title}</span>&nbsp;
      <span class="allekirjoitus">{modelLookup(model, 'paikkakunta').title}</span>&nbsp;
      {
        modelLookup(model, 'myöntäjäHenkilöt').items.map( henkilö =>
          <span class="nimi">{modelData(henkilö, 'nimi')}</span>
        )
      }
    </span>
  }
})

const OpiskeluoikeusjaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    return <div className="opiskeluoikeusjakso">
      <label class="date">{modelLookup(model, 'alku').title}</label>
      <label class="tila">{modelLookup(model, 'tila').title}</label>
    </div>
  }
})

const ObjectEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let className = 'object ' + model.class
    let flattened = model.properties.find(property => property.flatten)
    return flattened
      ? getModelEditor(flattened.model, context)
      : (
      <div className={className}>
        {
          model.properties.filter(property => !property.model.empty && !property.hidden).map(property => {
            let propertyClassName = 'property ' + property.key
            return <ul className="properties">
              <li className={propertyClassName} key={property.key}>
                <label>{property.title}</label>
                { getModelEditor(property.model, context) }
              </li>
            </ul>
          })
        }
      </div>
    )
  }
})

const ArrayEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return (
      <ul className="array">
        {
          model.items.map((item, i) =>
            <li key={i}>{getModelEditor(item, context)}</li>
          )
        }
      </ul>
    )
  }
})

const StringEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span>{model.data}</span>
  }
})

const DateEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span>{model.title}</span>
  }
})

const EnumEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span>{model.title}</span>
  }
})


const NullEditor = React.createClass({
  render() {
    return null
  }
})

const editorTypes = {
  'perusopetuksenoppiaineensuoritus': OppiaineEditor,
  'perusopetukseenvalmistavanopetuksenoppiaineensuoritus': OppiaineEditor,
  'perusopetuksenopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'henkilövahvistus': VahvistusEditor,
  'object': ObjectEditor,
  'array': ArrayEditor,
  'string': StringEditor,
  'number': StringEditor,
  'date': DateEditor,
  'boolean': StringEditor,
  'enum': EnumEditor
}

const getModelEditor = (model, context) => {
  var Editor = editorTypes[model.class] || editorTypes[model.type]
  if (!Editor) {
    if (!model.type) {
      console.log('Typeless model', model)
    }
    console.log('Missing editor ' + model.type)
    Editor = NullEditor
  }
  return <Editor model={model} context={context}/>
}