import React from 'baret'
import {
  addContext,
  modelData,
  modelItems,
  modelLookup,
  modelProperties
} from '../editor/EditorModel'
import {
  excludedProperties,
  OpiskeluoikeudenOpintosuoritusoteLink,
  OpiskeluoikeudenVoimassaoloaika
} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {OpiskeluoikeudenTilaEditor} from '../opiskeluoikeus/OpiskeluoikeudenTilaEditor'
import {assignTabNames} from '../suoritus/SuoritusTabs'
import {Korkeakoulusuoritukset} from '../virta/Korkeakoulusuoritukset'
import Text from '../i18n/Text'
import {Editor} from '../editor/Editor'
import {suorituksenTyyppi, suoritusTitle} from '../suoritus/Suoritus'


export const OmatTiedotOpiskeluoikeus = ({model}) => {
  const mdl = addContext(model, {opiskeluoikeus: model})
  const isSyntheticOpiskeluoikeus = !!modelData(mdl, 'synteettinen')

  return (
    <div className="opiskeluoikeus">
      <div className='opiskeluoikeus-content'>
        {!isSyntheticOpiskeluoikeus &&
        <OpiskeluoikeudenTiedot
          opiskeluoikeus={mdl}
        />}
        <Suoritukset opiskeluoikeus={mdl}/>
      </div>
    </div>
  )
}

const OpiskeluoikeudenTiedot = ({opiskeluoikeus}) => {
  const additionalInformation = modelLookup(opiskeluoikeus, 'lisätiedot')
  const additionalInfoPropertyFilter = prop => !!modelData(prop.model)

  return (
    <div className="opiskeluoikeuden-tiedot">
      <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={opiskeluoikeus}/>
      {modelData(opiskeluoikeus, 'alkamispäivä') && <OpiskeluoikeudenVoimassaoloaika opiskeluoikeus={opiskeluoikeus}/>}
      <PropertiesEditor
        model={opiskeluoikeus}
        propertyFilter={ p => !excludedProperties.includes(p.key) }
        getValueEditor={ (prop, getDefault) => {
          switch (prop.key) {
            case 'tila': return <OpiskeluoikeudenTilaEditor model={opiskeluoikeus}/>
            default: return getDefault()
          }
        }}
      />
      {additionalInformation && modelProperties(additionalInformation).filter(additionalInfoPropertyFilter).length > 0 &&
        <OpiskeluoikeudenLisätiedot
          model={opiskeluoikeus}
          propertyFilter={additionalInfoPropertyFilter}
        />}
    </div>
  )
}

class OpiskeluoikeudenLisätiedot extends React.Component {
  constructor(props){
    super(props)
    this.state = {
      expanded: false
    }
    this.toggleExpand = this.toggleExpand.bind(this)
  }

  toggleExpand() {
    this.setState(prevState => ({expanded: !prevState.expanded}))
  }

  render() {
    const {model, propertyFilter} = this.props
    const {expanded} = this.state

    return (
      <div className='expandable-container lisätiedot'>
        <button className={`inline-link-button ${expanded ? 'open' : ''}`} onClick={this.toggleExpand}><Text name='Lisätiedot'/></button>
        {expanded && <div className='value'>
          <PropertiesEditor model={modelLookup(model, 'lisätiedot')} propertyFilter={propertyFilter}/>
        </div>}
      </div>
    )
  }
}

const Suoritukset = ({opiskeluoikeus}) => {
  const opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo

  return (
    <div className="suoritukset">
      {opiskeluoikeusTyyppi === 'korkeakoulutus'
        ? <Korkeakoulusuoritukset opiskeluoikeus={opiskeluoikeus}/>
        : <TabulatedSuoritukset model={opiskeluoikeus}/>}
    </div>
  )
}

class TabulatedSuoritukset extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      selectedTabIndex: 0
    }
    this.onTabChange = this.onTabChange.bind(this)
  }

  onTabChange(newTabIndex) {
    this.setState({selectedTabIndex: newTabIndex})
  }

  render() {
    const {model} = this.props
    const {selectedTabIndex} = this.state

    const suoritukset = modelItems(model, 'suoritukset')
    assignTabNames(suoritukset)

    const valittuSuoritus = suoritukset[selectedTabIndex]

    return (
      <div className="suoritukset">
        <h4><Text name="Suoritukset"/></h4>
        <SuoritusTabs selectedTabIndex={selectedTabIndex} suoritukset={suoritukset} onChange={this.onTabChange} />
        <Editor key={valittuSuoritus.tabName} model={valittuSuoritus} alwaysUpdate="true" />
      </div>
    )
  }
}

const SuoritusTabs = ({selectedTabIndex, suoritukset, onChange}) => {
  const tabTitle = (suoritusModel) => suorituksenTyyppi(suoritusModel) == 'perusopetuksenoppimaara' ? <Text name="Päättötodistus"/> : suoritusTitle(suoritusModel)
  return (
    <div className='suoritus-tabs'>
      <ul>
        {suoritukset.map((suoritus, i) => {
          const selected = selectedTabIndex === i
          const titleEditor = tabTitle(suoritus)
          const onClick = () => onChange(i)
          return (
            <li className={selected ? 'tab selected': 'tab'} key={i}>
              {selected ? titleEditor : <button className='inline-link-button' onClick={onClick}> {titleEditor} </button>}
            </li>
          )
        })}
      </ul>
    </div>
  )
}
