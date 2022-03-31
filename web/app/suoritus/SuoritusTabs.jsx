import React from 'baret'
import * as R from 'ramda'
import {modelData, modelTitle} from '../editor/EditorModel.js'
import Link from '../components/Link'
import {currentLocation} from '../util/location.js'
import {navigateTo} from '../util/location'
import {modelLookup, pushModel} from '../editor/EditorModel'
import {suorituksenTyyppi, suoritusTitle, suoritusValmis} from './Suoritus'
import Text from '../i18n/Text'
import {isPerusopetuksenOppimäärä, luokkaAste} from '../perusopetus/Perusopetus'
import UusiSuoritusLink from '../uusisuoritus/UusiSuoritusLink'
import {isPaikallinen} from './Koulutusmoduuli'
import {buildClassNames} from '../components/classnames'

export const SuoritusTabs = ({ model, suoritukset }) => {
  let uusiSuoritusCallback = (suoritus) => {
    pushModel(suoritus, model.context.changeBus)
    let suoritukset2 = [suoritus].concat(suoritukset)
    assignTabNames(suoritukset2) // to get the correct tab name for the new suoritus
    navigateTo(urlForTab(suoritukset2, 0))
  }
  let tabTitle = (suoritusModel) => {
    switch (suorituksenTyyppi(suoritusModel)) {
      case 'perusopetuksenoppimaara': return <Text name="Päättötodistus"/>
      case 'diatutkintovaihe': return <Text name="Deutsche Internationale Abitur"/>
      default: return suoritusTitle(suoritusModel)
    }
  }

  return (<div className="suoritus-tabs"><ul>
      {
        suoritukset.map((suoritusModel, i) => {
          let selected = i === suoritusTabIndex(suoritukset)
          let titleEditor = tabTitle(suoritusModel)
          const classNames = buildClassNames([
            'tab',
            selected && 'selected',
            isPaikallinen(modelLookup(suoritusModel, 'koulutusmoduuli')) && 'paikallinen'
          ])
          return (<li className={classNames} key={i}>
            { selected ? titleEditor : <Link href={ urlForTab(suoritukset, i) } exitHook={false}> {titleEditor} </Link> }
          </li>)
        })
      }
      </ul>
      <UusiSuoritusLink opiskeluoikeus={model} callback={uusiSuoritusCallback}/>
    </div>
  )}

SuoritusTabs.displayName = 'SuoritusTabs'

export const assignTabNames = (suoritukset) => {
  suoritukset = R.reverse(suoritukset) // they are in reverse chronological-ish order
  let tabNamesInUse = {}
  for (var i in suoritukset) {
    let suoritus = suoritukset[i]
    if (suoritus.tabName) {
      tabNamesInUse[suoritus.tabName] = true
    }
  }
  for (var i in suoritukset) {
    let suoritus = suoritukset[i]
    if (!suoritus.tabName) {
      let tabName = resolveTabName(suoritus)
      while (tabNamesInUse[tabName]) {
        tabName += '-2'
      }
      tabNamesInUse[tabName] = true
      suoritus.tabName = tabName
    }
  }
}

const resolveTabName = suoritus => {
  switch (suorituksenTyyppi(suoritus)) {
    case 'lukionaineopinnot': return modelTitle(suoritus, 'tyyppi')
    default: return modelTitle(suoritus, 'koulutusmoduuli.tunniste')
  }
}

export const urlForTab = (suoritukset, i) => {
  let tabName = suoritukset[i].tabName
  return currentLocation().addQueryParams({[suoritusQueryParam(suoritukset[0].context)]: tabName}).toString()
}

const suoritusQueryParam = context => (modelData(context.opiskeluoikeus, 'oid') || context.opiskeluoikeusIndex) + '.suoritus'

export const suoritusTabIndex = (suoritukset) => {
  if (!suoritukset.length) return 0
  let paramName = suoritusQueryParam(suoritukset[0].context)
  let selectedTabName = currentLocation().params[paramName]
  let index = suoritukset.map(s => s.tabName).indexOf(selectedTabName)
  if (index < 0) {
    index = suoritukset.findIndex(s => luokkaAste(s) || (isPerusopetuksenOppimäärä(s) && suoritusValmis(s) ))
    if (index < 0) index = 0
    selectedTabName = suoritukset[index].tabName
    let newLocation = currentLocation().addQueryParams({ [paramName]: selectedTabName }).toString()
    history.replaceState(null, null, newLocation)
  }
  return index
}
