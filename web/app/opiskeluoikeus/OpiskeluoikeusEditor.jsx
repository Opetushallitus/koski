import React from 'baret'
import Bacon from 'baconjs'
import {
  addContext,
  modelData,
  modelItems,
  modelLookup,
  modelSetValues,
  modelTitle,
  pushModel
} from '../editor/EditorModel.js'
import {TogglableEditor} from '../editor/TogglableEditor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {OpiskeluoikeudenTilaEditor} from './OpiskeluoikeudenTilaEditor'
import Versiohistoria from './Versiohistoria'
import {ExpandablePropertiesEditor} from '../editor/ExpandablePropertiesEditor'
import {Editor} from '../editor/Editor'
import {navigateTo} from '../util/location'
import {suorituksenTyyppi, suoritusTitle} from '../suoritus/Suoritus'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {assignTabNames, suoritusTabIndex, SuoritusTabs, urlForTab} from '../suoritus/SuoritusTabs'
import {Korkeakoulusuoritukset} from '../virta/Korkeakoulusuoritukset'
import {OpiskeluoikeudenTila} from '../omattiedot/fragments/OpiskeluoikeudenTila'

export const excludedProperties = ['suoritukset', 'alkamispäivä', 'arvioituPäättymispäivä', 'päättymispäivä', 'oppilaitos', 'lisätiedot', 'synteettinen']

export const OpiskeluoikeusEditor = ({model}) => {
  return (<TogglableEditor model={addContext(model, {opiskeluoikeus: model})} renderChild={ (mdl, editLink) => {
    const context = mdl.context

    const alkuChangeBus = Bacon.Bus()
    alkuChangeBus.onValue(v => {
      const value = v[0].value
      pushModel(modelSetValues(model, {'alkamispäivä' : value, 'tila.opiskeluoikeusjaksot.0.alku': value}))
    })

    const hasOppilaitos = !!modelData(mdl, 'oppilaitos')
    const hasAlkamispäivä = !!modelData(mdl, 'alkamispäivä')
    const isSyntheticOpiskeluoikeus = !!modelData(model, 'synteettinen')

    return (
      <div className="opiskeluoikeus">
        <h3>
          <span className="otsikkotiedot">
            { hasOppilaitos && <span className="oppilaitos">{modelTitle(mdl, 'oppilaitos')}</span> }
            { hasOppilaitos && <span>{', '}</span> }
            <span className="koulutus" style={hasOppilaitos ? { textTransform: 'lowercase' } : undefined}>{näytettäväPäätasonSuoritusTitle(mdl)}</span>
            {hasAlkamispäivä && <OpiskeluoikeudenTila opiskeluoikeus={mdl}/>}
          </span>
          <Versiohistoria opiskeluoikeusOid={modelData(mdl, 'oid')} oppijaOid={context.oppijaOid}/>
          <OpiskeluoikeudenId opiskeluoikeus={mdl}/>
        </h3>
        <div className={mdl.context.edit ? 'opiskeluoikeus-content editing' : 'opiskeluoikeus-content'}>
          {!isSyntheticOpiskeluoikeus &&
            <OpiskeluoikeudenTiedot
              opiskeluoikeus={mdl}
              editLink={editLink}
              alkuChangeBus={alkuChangeBus}
            />
          }
          <Suoritukset opiskeluoikeus={mdl}/>
        </div>
      </div>)
    }
  } />)
}

const OpiskeluoikeudenTiedot = ({opiskeluoikeus, editLink, alkuChangeBus}) => (
  <div className="opiskeluoikeuden-tiedot">
    {editLink}
    <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={opiskeluoikeus}/>
    {
      modelData(opiskeluoikeus, 'alkamispäivä') && <OpiskeluoikeudenVoimassaoloaika opiskeluoikeus={opiskeluoikeus}/>
    }
    <PropertiesEditor
      model={opiskeluoikeus}
      propertyFilter={ p => !excludedProperties.includes(p.key) }
      getValueEditor={ (prop, getDefault) => {
        switch (prop.key) {
          case 'tila': return <OpiskeluoikeudenTilaEditor model={opiskeluoikeus} alkuChangeBus={alkuChangeBus}/>
          default: return getDefault()
        }
      }}
    />
    {
      modelLookup(opiskeluoikeus, 'lisätiedot') &&
      <ExpandablePropertiesEditor
        model={opiskeluoikeus}
        propertyName="lisätiedot"
        propertyFilter={prop => opiskeluoikeus.context.edit || modelData(prop.model) !== false}
      />
    }
  </div>
)

const OpiskeluoikeudenId = ({opiskeluoikeus}) => {
  let selectAllText = (e) => {
    let el = e.target
    var range = document.createRange()
    range.selectNodeContents(el)
    var sel = window.getSelection()
    sel.removeAllRanges()
    sel.addRange(range)
  }
  const opiskeluoikeusOid = modelData(opiskeluoikeus, 'oid')
  return opiskeluoikeusOid ? <span className="id"><Text name="Opiskeluoikeuden oid"/>{': '}<span className="value" onClick={selectAllText}>{opiskeluoikeusOid}</span></span> : null
}

export const OpiskeluoikeudenVoimassaoloaika = ({opiskeluoikeus}) => {
  let päättymispäiväProperty = (modelData(opiskeluoikeus, 'arvioituPäättymispäivä') && !modelData(opiskeluoikeus, 'päättymispäivä')) ? 'arvioituPäättymispäivä' : 'päättymispäivä'
  return (<div className="alku-loppu opiskeluoikeuden-voimassaoloaika">
    <Text name="Opiskeluoikeuden voimassaoloaika"/>{': '}
    <span className="alkamispäivä"><Editor model={addContext(opiskeluoikeus, {edit: false})} path="alkamispäivä"/></span>
    {' — '}
    <span className="päättymispäivä"><Editor model={addContext(opiskeluoikeus, {edit: false})} path={päättymispäiväProperty} /></span>
    {' '}
    {päättymispäiväProperty == 'arvioituPäättymispäivä' && <Text name="(arvioitu)"/>}
  </div>)
}

const Suoritukset = ({opiskeluoikeus}) => {
  const opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo

  return (
    <div className="suoritukset">
      {opiskeluoikeusTyyppi === 'korkeakoulutus'
        ? <Korkeakoulusuoritukset opiskeluoikeus={opiskeluoikeus}/>
        : <TabulatedSuoritukset model={opiskeluoikeus}/>
       }
    </div>
  )
}

const TabulatedSuoritukset = ({model}) => {
  const suoritukset = modelItems(model, 'suoritukset')
  assignTabNames(suoritukset)

  const index = suoritusTabIndex(suoritukset)
  if (index < 0 || index >= suoritukset.length) {
    navigateTo(urlForTab(suoritukset, index))
    return null
  }
  const valittuSuoritus = suoritukset[index]

  return (
    <div className="suoritukset">
      <h4><Text name="Suoritukset"/></h4>
      <SuoritusTabs model={model} suoritukset={suoritukset}/>
      <Editor key={valittuSuoritus.tabName} model={valittuSuoritus} alwaysUpdate="true" />
    </div>
  )
}

export class OpiskeluoikeudenOpintosuoritusoteLink extends React.Component {
  render() {
    let {opiskeluoikeus} = this.props
    let oppijaOid = opiskeluoikeus.context.oppijaOid
    var opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo
    if (opiskeluoikeusTyyppi === 'lukiokoulutus' || opiskeluoikeusTyyppi === 'ibtutkinto') { // lukio/ib näytetään opiskeluoikeuskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?opiskeluoikeus=' + modelData(opiskeluoikeus, 'oid')
      return <a className="opintosuoritusote" href={href}><Text name="näytä opintosuoritusote"/></a>
    } else if (opiskeluoikeusTyyppi === 'korkeakoulutus') { // korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(opiskeluoikeus, 'oppilaitos').oid
      return <a className="opintosuoritusote" href={href}><Text name="näytä opintosuoritusote"/></a>
    } else {
      return null
    }
  }
}


const opintojaksoTaiOppimäärä = suoritus =>
  isPerusopetuksenOppimäärä(suoritus) || isOpintojakso(suoritus)

const isPerusopetuksenOppimäärä = suoritus =>
  ['perusopetuksenoppiaineenoppimaara', 'nuortenperusopetuksenoppiaineenoppimaara'].includes(suorituksenTyyppi(suoritus))

const isOpintojakso = suoritus => suorituksenTyyppi(suoritus) === 'korkeakoulunopintojakso'
const isPerusopetuksenVuosiluokka = suoritus => suorituksenTyyppi(suoritus) === 'perusopetuksenvuosiluokka'

const oppimääräTaiOpintojaksoOtsikko = suoritukset => {
  const otsikkoAvain = suoritukset.every(isPerusopetuksenOppimäärä) ? 'oppiainetta' : 'opintojaksoa'
  return `${suoritukset.filter(opintojaksoTaiOppimäärä).length} ${t(otsikkoAvain)}`
}

// Duplicates the logic from src/main/scala/fi/oph/koski/luovutuspalvelu/SuomiFiService.scala#suorituksenNimi
export const näytettäväPäätasonSuoritusTitle = opiskeluoikeus => {
  const suoritukset = modelItems(opiskeluoikeus, 'suoritukset')
  const sisältääOppiaineenOppimääränTaiOpintoJakson = suoritukset.some(opintojaksoTaiOppimäärä) && suoritukset.length > 1
  return sisältääOppiaineenOppimääränTaiOpintoJakson
    ? oppimääräTaiOpintojaksoOtsikko(suoritukset)
    : suoritukset.every(isPerusopetuksenVuosiluokka)
      ? t('Perusopetus')
      : suoritusTitle(suoritukset[0])
}
