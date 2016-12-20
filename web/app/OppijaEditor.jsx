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
                <OpiskeluoikeusEditor key={ opiskeluoikeusIndex } model={ opiskeluoikeus } context={GenericEditor.childContext(this, oppijaContext, 'opiskeluoikeudet', oppilaitosIndex, 'opiskeluoikeudet', opiskeluoikeusIndex)} />
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
        <GenericEditor.ExpandableEditor
          editor = {this}
          expandedView={() => <GenericEditor.PropertiesEditor properties={ model.value.properties.filter(property => property.key != 'suoritukset') } context={opiskeluoikeusContext}/>}
          context={GenericEditor.childContext(this, opiskeluoikeusContext, 'tiedot')}
        />
      </div>
      {
        modelItems(model, 'suoritukset').map((suoritusModel, i) =>
          <SuoritusEditor model={suoritusModel} context={GenericEditor.childContext(this, opiskeluoikeusContext, 'suoritukset', i)} key={i}/>
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
      <GenericEditor.ExpandableEditor
        editor = {this}
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
    return suoritusTila == 'VALMIS' && suoritustyyppi != 'korkeakoulututkinto' && suoritustyyppi != 'preiboppimaara' && suoritustyyppi != 'esiopetuksensuoritus'
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
    var opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo
    if (opiskeluoikeusTyyppi == 'lukiokoulutus' || opiskeluoikeusTyyppi == 'ibtutkinto') { // vain lukio/ib näytetään opiskeluoikeuskohtainen suoritusote
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
    var oppiaine = modelTitle(model, 'koulutusmoduuli')
    let arvosana = modelTitle(model, 'arviointi.-1.arvosana')
    let pakollinen = modelData(model, 'koulutusmoduuli.pakollinen')
    if (pakollinen === false) {
      oppiaine = 'Valinnainen ' + oppiaine.toLowerCase() // i18n
    }
    return (<div className="oppiaineensuoritus">
      <label className="oppiaine">{oppiaine}</label>
      <span className="arvosana">{arvosana}</span>
      {modelData(model, 'yksilöllistettyOppimäärä') ? <span className="yksilollistetty"> *</span> : null}
      {modelData(model, 'korotus') ? <span className="korotus">(korotus)</span> : null}
    </div>)
  }
})
OppiaineEditor.canShowInline = () => false

const LukionKurssiEditor = React.createClass({
  render() {
    let {model} = this.props
    var tunniste = modelData(model, 'koulutusmoduuli.tunniste')
    let koodiarvo = tunniste && tunniste.koodiarvo
    let nimi = modelTitle(model, 'koulutusmoduuli')
    let arvosana = modelTitle(model, 'arviointi.-1.arvosana')
    return (<div className="lukionkurssinsuoritus">
      <label className="oppiaine"><span className="koodiarvo">{koodiarvo}</span> <span className="nimi">{nimi}</span></label>
      <span className="arvosana">{arvosana}</span>
    </div>)
  }
})
LukionKurssiEditor.canShowInline = () => false


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
      <GenericEditor.ExpandableEditor
        editor = {this}
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
  'preiboppiaineensuoritus': TutkinnonosaEditor,
  'iboppiaineensuoritus': TutkinnonosaEditor,
  'ammatillisentutkinnonosansuoritus': TutkinnonosaEditor,
  'lukionoppiaineensuoritus': TutkinnonosaEditor,
  'ylioppilastutkinnonkokeensuoritus': TutkinnonosaEditor,
  'lukionkurssinsuoritus': LukionKurssiEditor,
  'ammatillinenopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'lukionopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'perusopetuksenopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'henkilovahvistuspaikkakunnalla': VahvistusEditor,
  'henkilovahvistusilmanpaikkakuntaa': VahvistusEditor,
  'organisaatiovahvistus': VahvistusEditor,
  'laajuusosaamispisteissa' : LaajuusEditor,
  'laajuuskursseissa' : LaajuusEditor,
  'laajuusopintopisteissa' : LaajuusEditor,
  'laajuusvuosiviikkotunneissa' : LaajuusEditor
}
