import React from 'react'
import R from 'ramda'
import { modelData, modelLookup, modelTitle, modelItems } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'
import Versiohistoria from './Versiohistoria.jsx'
import Link from './Link.jsx'
import { currentLocation } from './location.js'
import { yearFromFinnishDateString } from './date'
import { PerusopetuksenOppiaineetEditor, ErityisenTuenPäätösEditor } from './Perusopetus.jsx'
import { TutkinnonOsatEditor, NäytönSuorituspaikkaEditor, NäytönArvioitsijaEditor, TyössäoppimisjaksoEditor } from './Ammatillinen.jsx'

const OppijaEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let oppijaOid = modelData(model, 'henkilö.oid')
    let oppijaContext = R.merge(context, { oppijaOid: oppijaOid })

    let selectedTyyppi = currentLocation().params.opiskeluoikeudenTyyppi

    var opiskeluoikeusTyypit = modelLookup(model, 'opiskeluoikeudet').value


    let selectedIndex = selectedTyyppi
      ? opiskeluoikeusTyypit.findIndex((opiskeluoikeudenTyyppi) => selectedTyyppi == modelData(opiskeluoikeudenTyyppi, 'tyyppi.koodiarvo'))
      : 0


    return (
      <div>
        <ul className="opiskeluoikeustyypit">
          {
            opiskeluoikeusTyypit.map((opiskeluoikeudenTyyppi, tyyppiIndex) => {
              let selected = tyyppiIndex == selectedIndex
              let koodiarvo = modelData(opiskeluoikeudenTyyppi, 'tyyppi.koodiarvo')
              let className = selected ? koodiarvo + ' selected' : koodiarvo
              let content = (<div>
                <div className="opiskeluoikeustyyppi">{ modelTitle(opiskeluoikeudenTyyppi, 'tyyppi') }</div>
                <ul className="oppilaitokset">
                  {
                    modelLookup(opiskeluoikeudenTyyppi, 'opiskeluoikeudet').value.map((oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) =>
                      <li key={oppilaitosIndex}>
                        <span className="oppilaitos">{modelTitle(oppilaitoksenOpiskeluoikeudet, 'oppilaitos')}</span>
                        <ul className="opiskeluoikeudet">
                          {
                            modelLookup(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet').value.map((opiskeluoikeus, opiskeluoikeusIndex) =>
                              modelLookup(opiskeluoikeus, 'suoritukset').value.filter(näytettäväPäätasonSuoritus).map((suoritus, suoritusIndex) =>
                                <li className="opiskeluoikeus" key={opiskeluoikeusIndex + '-' + suoritusIndex}>
                                  <span className="koulutus inline-text">{ modelTitle(suoritus, 'tyyppi') }</span>
                                  { modelData(opiskeluoikeus, 'alkamispäivä')
                                    ? <span className="inline-text">
                                        <span className="alku pvm">{yearFromFinnishDateString(modelTitle(opiskeluoikeus, 'alkamispäivä'))}</span>-
                                        <span className="loppu pvm">{yearFromFinnishDateString(modelTitle(opiskeluoikeus, 'päättymispäivä'))},</span>
                                      </span>
                                    : null
                                  }
                                  <span className="tila">{ modelTitle(opiskeluoikeus, 'tila.opiskeluoikeusjaksot.-1.tila') }</span>
                                </li>
                              )
                            )
                          }
                        </ul>
                      </li>
                    )
                  }
                </ul>
              </div>)
              return (
                <li className={className} key={tyyppiIndex}>
                  { selected ? content : <Link href={'?opiskeluoikeudenTyyppi=' + koodiarvo}>{content}</Link> }
                </li>)
            })}
        </ul>
        <ul className="opiskeluoikeuksientiedot">
          {
            modelLookup(model, 'opiskeluoikeudet.' + selectedIndex + '.opiskeluoikeudet').value.flatMap((oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) => {
              return modelLookup(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet').value.map((opiskeluoikeus, opiskeluoikeusIndex) =>
                <li key={ oppilaitosIndex + '-' + opiskeluoikeusIndex }>
                  <OpiskeluoikeusEditor
                    model={ opiskeluoikeus }
                    context={GenericEditor.childContext(this, oppijaContext, 'opiskeluoikeudet', selectedIndex, 'opiskeluoikeudet', oppilaitosIndex, 'opiskeluoikeudet', opiskeluoikeusIndex)}
                  />
                </li>
              )
            })
          }
        </ul>
      </div>)
  }
})

const OpiskeluoikeusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let id = modelData(model, 'id')
    let opiskeluoikeusContext = R.merge(context, {editable: model.editable, opiskeluoikeusId: id})
    let suoritusQueryParam = context.path + '.suoritus'
    let suoritusIndex = currentLocation().params[suoritusQueryParam] || 0
    let suoritukset = modelItems(model, 'suoritukset')
    let excludedProperties = ['suoritukset', 'alkamispäivä', 'arvioituPäättymispäivä', 'päättymispäivä', 'oppilaitos', 'lisätiedot']
    let päättymispäiväProperty = (modelData(model, 'arvioituPäättymispäivä') && !modelData(model, 'päättymispäivä')) ? 'arvioituPäättymispäivä' : 'päättymispäivä'

    return (<GenericEditor.TogglableEditor context={opiskeluoikeusContext} renderChild={ (editableContext, editLink) => (<div className="opiskeluoikeus">
      <h3>
        <span className="oppilaitos inline-text">{modelTitle(model, 'oppilaitos')},</span>
        <span className="koulutus inline-text">{modelTitle(modelLookup(model, 'suoritukset').value.find(näytettäväPäätasonSuoritus), 'koulutusmoduuli')}</span>
         { modelData(model, 'alkamispäivä')
            ? <span className="inline-text">(
                  <span className="alku pvm">{yearFromFinnishDateString(modelTitle(model, 'alkamispäivä'))}</span>-
                  <span className="loppu pvm">{yearFromFinnishDateString(modelTitle(model, 'päättymispäivä'))},</span>
              </span>
            : null
          }
        <span className="tila">{modelTitle(model, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()})</span>
        <Versiohistoria opiskeluOikeusId={id} oppijaOid={context.oppijaOid}/>
      </h3>
      <div className="opiskeluoikeus-content">
        <div className="opiskeluoikeuden-tiedot">
          {editLink}
          <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={model} context={context}/>
          <div className="alku-loppu">
            <GenericEditor.PropertyEditor context={editableContext} model={model} propertyName="alkamispäivä" /> — <GenericEditor.PropertyEditor context={editableContext} model={model} propertyName={päättymispäiväProperty} />
          </div>
          <GenericEditor.PropertiesEditor
            properties={ model.value.properties.filter(p => !excludedProperties.includes(p.key)) }
            context={editableContext}
            getValueEditor={ (prop, ctx, getDefault) => prop.key == 'tila'
              ? <GenericEditor.ArrayEditor reverse={true} model={modelLookup(prop.model, 'opiskeluoikeusjaksot')} context={GenericEditor.childContext(this, ctx, 'opiskeluoikeusjaksot')}/>
              : getDefault() }
           />
          <ExpandablePropertiesEditor context={editableContext} model={model} propertyName="lisätiedot" />
        </div>


        <div className="suoritukset">
          {
            suoritukset.length >= 2 ? (
              <div>
                <h4>Suoritukset</h4>
                <ul className="suoritus-tabs">
                  {
                    suoritukset.map((suoritusModel, i) => {
                      let selected = i == suoritusIndex
                      let title = modelTitle(suoritusModel, 'koulutusmoduuli')
                      return (<li className={selected ? 'selected': null} key={i}>
                        { selected ? title : <Link href={currentLocation().addQueryParams({[suoritusQueryParam]: i}).toString()}> {title} </Link>}
                      </li>)
                    })
                  }
                </ul>
              </div>
            ) : <hr/>
          }
          {
            suoritukset.map((suoritusModel, i) =>
              i == suoritusIndex
                ? <PäätasonSuoritusEditor model={suoritusModel} context={GenericEditor.childContext(this, opiskeluoikeusContext, 'suoritukset', i)} key={i}/> : null
            )
          }
        </div>
      </div>
    </div>)

    } />)
  }
})

const ExpandablePropertiesEditor = React.createClass({
  render() {
    let {model, context, propertyName} = this.props
    let {open} = this.state
    return modelData(model, propertyName) ?
      <div className={'expandable-container ' + propertyName}>
        <a className={open ? 'open expandable' : 'expandable'} onClick={this.toggleOpen}>{model.value.properties.find(p => p.key === propertyName).title}</a>
        { open ?
          <div className="value">
            <GenericEditor.PropertiesEditor properties={modelLookup(model, propertyName).value.properties} context={GenericEditor.childContext(this, context, propertyName)}/>
          </div> : null
        }
      </div> : null
  },
  toggleOpen() {
    this.setState({open: !this.state.open})
  },
  getInitialState() {
    return {open: false}
  }
})

const PäätasonSuoritusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let className = 'suoritus ' + model.value.classes.join(' ')
    let excludedProperties = ['osasuoritukset', 'käyttäytymisenArvio', 'tila', 'vahvistus']

    return (<GenericEditor.TogglableEditor
      context={context}
      renderChild={ (ctx, editLink) => <div className={className}>
        {editLink}
        <TodistusLink suoritus={model} context={ctx}/>
        <GenericEditor.PropertiesEditor properties={model.value.properties.filter(p => !excludedProperties.includes(p.key))} context={R.merge(ctx, {editable: model.editable})}/>
        <div className="tila-vahvistus">
          <span className="tila">
            Suoritus: <span className={ 'VALMIS' == model.value.data.tila.koodiarvo ? 'valmis' : ''}>{ model.value.data.tila.koodiarvo }</span>
          </span>
          <GenericEditor.PropertyEditor context={ctx} model={model} propertyName="vahvistus" />
        </div>
        <div className="osasuoritukset">
          {
            ['perusopetuksenvuosiluokansuoritus', 'perusopetuksenoppimaaransuoritus', 'perusopetuksenlisaopetuksensuoritus', 'perusopetukseenvalmistavanopetuksensuoritus'].includes(model.value.classes[0])
              ? <PerusopetuksenOppiaineetEditor context={ctx} model={model}/>
              : (model.value.classes.includes('ammatillinenpaatasonsuoritus'))
                ? <TutkinnonOsatEditor context={ctx} model={model} propertyName="osasuoritukset"/>
                : <GenericEditor.PropertyEditor context={ctx} model={model} propertyName="osasuoritukset"/>
          }
        </div>
      </div>
      }
    />)
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

const OpiskeluoikeudenOpintosuoritusoteLink = React.createClass({
  render() {
    let {opiskeluoikeus, context: { oppijaOid }} = this.props
    var opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo
    if (opiskeluoikeusTyyppi == 'lukiokoulutus' || opiskeluoikeusTyyppi == 'ibtutkinto') { // lukio/ib näytetään opiskeluoikeuskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?opiskeluoikeus=' + modelData(opiskeluoikeus, 'id')
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else if (opiskeluoikeusTyyppi == 'korkeakoulutus') { // korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(opiskeluoikeus, 'oppilaitos').oid
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})

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


export const LaajuusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    var yksikköData = modelData(model, 'yksikkö')
    let yksikkö = yksikköData && (yksikköData.lyhytNimi || yksikköData.nimi).fi
    return context.edit
      ? <GenericEditor.ObjectEditor model={model} context={context}/>
      : (modelData(model, 'arvo'))
        ? <span>{modelTitle(model, 'arvo')} {yksikkö}</span>
        : <span>-</span>
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

const KoulutusmoduuliEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <GenericEditor.ObjectEditor {...this.props}/>
      : <span className="koulutusmoduuli">
          <span className="tunniste">{modelTitle(model, 'tunniste')}</span>
          <span className="diaarinumero">{modelTitle(model, 'perusteenDiaarinumero')}</span>
          <GenericEditor.PropertiesEditor properties={model.value.properties.filter(p => !['tunniste', 'perusteenDiaarinumero', 'pakollinen'].includes(p.key))} context={context}/>
        </span>
  }
})

export const PäivämääräväliEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return context.edit
      ? <GenericEditor.ObjectEditor {...this.props}/>
      : <span>
          <GenericEditor.Editor context={context} model={modelLookup(model, 'alku')}/> — <GenericEditor.Editor context={context} model={modelLookup(model, 'loppu')}/>
        </span>
  }
})
PäivämääräväliEditor.canShowInline = () => true

const näytettäväPäätasonSuoritus = s => !['perusopetuksenvuosiluokka', 'korkeakoulunopintojakso'].includes(s.value.data.tyyppi.koodiarvo)

export const editorMapping = {
  'oppijaeditorview': OppijaEditor,
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
  'laajuusvuosiviikkotunneissa' : LaajuusEditor,
  'koulutus' : KoulutusmoduuliEditor,
  'preibkoulutusmoduuli': KoulutusmoduuliEditor,
  'ammatillisentutkinnonosa': KoulutusmoduuliEditor,
  'naytonsuorituspaikka': NäytönSuorituspaikkaEditor,
  'naytonarvioitsija': NäytönArvioitsijaEditor,
  'naytonsuoritusaika': PäivämääräväliEditor,
  'paatosjakso': PäivämääräväliEditor,
  'tyossaoppimisjakso': TyössäoppimisjaksoEditor,
  'erityisentuenpaatos': ErityisenTuenPäätösEditor
}
