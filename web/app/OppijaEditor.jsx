import React from 'react'
import R from 'ramda'
import { modelData, modelLookup, modelTitle, modelItems } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'

const OppijaEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return (<ul className="oppilaitokset">
      {
        modelLookup(model, 'opiskeluoikeudet').value.map((oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) => {
          var oppijaOid = modelData(model, 'henkilö.oid')
          let oppijaContext = R.merge(context, { oppijaOid: oppijaOid })
          let oppilaitos = modelLookup(oppilaitoksenOpiskeluoikeudet, 'oppilaitos')
          let opiskeluoikeudet = modelItems(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet')
          return (<li className="oppilaitos" key={modelData(oppilaitos).oid}>
            <span className="oppilaitos">{modelTitle(oppilaitos)}</span>
            <OppilaitoksenOpintosuoritusoteLink oppilaitos={oppilaitos} tyyppi={modelData(opiskeluoikeudet[0], 'tyyppi').koodiarvo} oppijaOid={oppijaOid} />
            {
              opiskeluoikeudet.map( (opiskeluoikeus, opiskeluoikeusIndex) =>
                <OpiskeluoikeusEditor key={ opiskeluoikeusIndex } model={ opiskeluoikeus } context={GenericEditor.childContext(oppijaContext, 'opiskeluoikeudet', oppilaitosIndex, 'opiskeluoikeudet', opiskeluoikeusIndex)} />
              )
            }
          </li>)
          }
        )}
    </ul>)
  }
})

const OpiskeluoikeusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let opiskeluoikeusContext = R.merge(context, {editable: model.editable, opiskeluoikeusId: modelData(model, 'id')})
    return (<div className="opiskeluoikeus">
      <div className="kuvaus">
        Opiskeluoikeus&nbsp;
        { modelData(model, 'alkamispäivä')
          ? (<span>
                <span className="alku pvm">{modelTitle(model, 'alkamispäivä')}</span>-
                <span className="loppu pvm">{modelTitle(model, 'päättymispäivä')}</span>,&nbsp;
            </span>)
          : null
        }
          <span className="tila">{modelTitle(model, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()}</span>
        <GenericEditor.FoldableEditor
          expandedView={() => <GenericEditor.PropertiesEditor properties={ model.value.properties.filter(property => property.key != 'suoritukset') } context={opiskeluoikeusContext}/>}
          context={GenericEditor.childContext(opiskeluoikeusContext, 'tiedot')}
        />
      </div>
      {
        modelItems(model, 'suoritukset').map((suoritusModel, i) =>
          <SuoritusEditor model={suoritusModel} context={GenericEditor.childContext(opiskeluoikeusContext, 'suoritukset', i)} key={i}/>
        )
      }
      <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={model} context={context}/>
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
      <TodistusLink suoritus={model} context={context}/>
      <GenericEditor.FoldableEditor
        expandedView={() => <GenericEditor.PropertiesEditor properties={model.value.properties} context={R.merge(context, {editable: model.editable})}/>}
        context={context}
      />
    </div>)
  }
})

const TodistusLink = React.createClass({
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

const OppilaitoksenOpintosuoritusoteLink = React.createClass({
  render() {
    let {oppilaitos, tyyppi, oppijaOid} = this.props

    if (tyyppi == 'korkeakoulutus') { // vain korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(oppilaitos).oid
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})

const OpiskeluoikeudenOpintosuoritusoteLink = React.createClass({
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
      <span className="arvosana">{modelTitle(model, 'arviointi.-1.arvosana')}</span>
      {modelData(model, 'korotus') ? <span className="korotus">(korotus)</span> : null}
    </div>)
  }
})
OppiaineEditor.canShowInline = () => false

const LaajuusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <GenericEditor.ObjectEditor model={model} context={context}/>
      : <span>{modelTitle(model, 'arvo')} {modelTitle(model, 'yksikkö')}</span>
  }
})

const VahvistusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <GenericEditor.ObjectEditor model={model} context={context} />
      : (<span className="vahvistus inline">
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

const OpiskeluoikeusjaksoEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <GenericEditor.ObjectEditor {...this.props}/>
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
      <GenericEditor.FoldableEditor
        defaultExpanded={context.edit}
        collapsedView={() => <span className="tutkinnonosan-tiedot">
          <label className="nimi">{modelTitle(model, 'koulutusmoduuli')}</label>
          <span className="arvosana">{modelTitle(model, 'arviointi.-1.arvosana')}</span>
          </span>}
        expandedView={() => <span>
          <label className="nimi">{modelTitle(model, 'koulutusmoduuli')}</label>
          <GenericEditor.PropertiesEditor properties={model.value.properties} context={context}/>
          </span>}
        context={context}
      />
    </div>)
  }
})
TutkinnonosaEditor.canShowInline = () => false

export const editorMapping = {
  'oppijaeditorview': OppijaEditor,
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
  'laajuusosaamispisteissa' : LaajuusEditor,
  'laajuuskursseissa' : LaajuusEditor,
  'laajuusopintopisteissa' : LaajuusEditor,
  'laajuusvuosiviikkotunneissa' : LaajuusEditor
}
