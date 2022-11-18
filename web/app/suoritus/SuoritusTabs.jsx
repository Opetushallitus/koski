import React from 'baret'
import { pathOr, reverse } from 'ramda'
import {
  modelData,
  modelTitle,
  modelLookup,
  pushModel
} from '../editor/EditorModel'
import Link from '../components/Link'
import { currentLocation, navigateTo } from '../util/location.js'
import { suorituksenTyyppi, suoritusTitle, suoritusValmis } from './Suoritus'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'
import {
  isPerusopetuksenOppimäärä,
  luokkaAste
} from '../perusopetus/Perusopetus'
import UusiSuoritusLink from '../uusisuoritus/UusiSuoritusLink'
import { isPaikallinen } from './Koulutusmoduuli'
import { buildClassNames } from '../components/classnames'

export const SuoritusTabs = ({ model, suoritukset }) => {
  const uusiSuoritusCallback = (suoritus) => {
    pushModel(suoritus, model.context.changeBus)
    const suoritukset2 = [suoritus].concat(suoritukset)
    assignTabNames(suoritukset2) // to get the correct tab name for the new suoritus
    navigateTo(urlForTab(suoritukset2, 0))
  }
  const tabTitle = (suoritusModel) => {
    switch (suorituksenTyyppi(suoritusModel)) {
      case 'perusopetuksenoppimaara':
        return <Text name="Päättötodistus" />
      case 'diatutkintovaihe':
        return <Text name="Deutsche Internationale Abitur" />
      default:
        return suoritusTitle(suoritusModel)
    }
  }

  const opiskeluoikeudenSuoritukset = modelData(model, 'suoritukset')
  const isNuortenPerusopetuksenOppiaineenOppimaara =
    Array.isArray(opiskeluoikeudenSuoritukset) &&
    opiskeluoikeudenSuoritukset.length > 0 &&
    opiskeluoikeudenSuoritukset.every(
      (suoritus) =>
        suoritus.tyyppi.koodiarvo === 'nuortenperusopetuksenoppiaineenoppimaara'
    )

  return (
    <div className="suoritus-tabs">
      <ul role="tablist" aria-label="Suoritukset">
        {suoritukset.map((suoritusModel, i) => {
          const selected = i === suoritusTabIndex(suoritukset)
          const titleEditor = tabTitle(suoritusModel)
          const classNames = buildClassNames([
            'tab',
            selected && 'selected',
            isPaikallinen(modelLookup(suoritusModel, 'koulutusmoduuli')) &&
              'paikallinen'
          ])
          const luokkaAsteLookup = modelData(suoritusModel, 'luokkaAste')
          const suoritustapa = modelLookup(suoritusModel, 'suoritustapa')
          const isErityinenTutkinto =
            pathOr('', ['value', 'value'], suoritustapa) ===
            'perusopetuksensuoritustapa_erityinentutkinto'
          const hasLuokkaAste = luokkaAsteLookup !== undefined
          const onYsiLuokkaTaiTyhja =
            !hasLuokkaAste ||
            (hasLuokkaAste && luokkaAsteLookup.koodiarvo === '9')
          return (
            <li
              className={classNames}
              key={i}
              role="tab"
              aria-label={`${selected ? 'Valittu ' : ''}${
                typeof titleEditor === 'string'
                  ? titleEditor
                  : `Suoritus ${i + 1}`
              }`}
            >
              {selected ? (
                titleEditor
              ) : (
                <Link href={urlForTab(suoritukset, i)} exitHook={false}>
                  {' '}
                  {titleEditor}{' '}
                </Link>
              )}
              {isNuortenPerusopetuksenOppiaineenOppimaara &&
                isErityinenTutkinto && (
                  <>
                    <br />
                    <small>
                      {onYsiLuokkaTaiTyhja
                        ? t('oppiaineenOppimaara')
                        : t(luokkaAsteLookup.nimi)}
                    </small>
                  </>
                )}
            </li>
          )
        })}
      </ul>
      <UusiSuoritusLink
        opiskeluoikeus={model}
        callback={uusiSuoritusCallback}
      />
    </div>
  )
}

export const assignTabNames = (suoritukset) => {
  suoritukset = reverse(suoritukset) // they are in reverse chronological-ish order
  const tabNamesInUse = {}
  for (const i in suoritukset) {
    const suoritus = suoritukset[i]
    if (suoritus.tabName) {
      tabNamesInUse[suoritus.tabName] = true
    }
  }
  for (const j in suoritukset) {
    const suoritus = suoritukset[j]
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

const resolveTabName = (suoritus) => {
  switch (suorituksenTyyppi(suoritus)) {
    case 'lukionaineopinnot':
      return modelTitle(suoritus, 'tyyppi')
    default:
      return modelTitle(suoritus, 'koulutusmoduuli.tunniste')
  }
}

export const urlForTab = (suoritukset, i) => {
  const tabName = suoritukset[i].tabName
  return currentLocation()
    .addQueryParams({ [suoritusQueryParam(suoritukset[0].context)]: tabName })
    .toString()
}

const suoritusQueryParam = (context) =>
  (modelData(context.opiskeluoikeus, 'oid') || context.opiskeluoikeusIndex) +
  '.suoritus'

export const suoritusTabIndex = (suoritukset) => {
  if (!suoritukset.length) return 0
  const paramName = suoritusQueryParam(suoritukset[0].context)
  let selectedTabName = currentLocation().params[paramName]
  let index = suoritukset.map((s) => s.tabName).indexOf(selectedTabName)
  if (index < 0) {
    index = suoritukset.findIndex(
      (s) =>
        luokkaAste(s) || (isPerusopetuksenOppimäärä(s) && suoritusValmis(s))
    )
    if (index < 0) index = 0
    selectedTabName = suoritukset[index].tabName
    const newLocation = currentLocation()
      .addQueryParams({ [paramName]: selectedTabName })
      .toString()
    history.replaceState(null, null, newLocation)
  }
  return index
}
