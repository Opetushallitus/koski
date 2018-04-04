import React from 'baret'
import R from 'ramda'
import * as L from 'partial.lenses'
import Atom from 'bacon.atom'
import {
  modelData,
  modelItems,
  modelLookup,
  recursivelyEmpty,
  lensedModel,
  modelSetValue,
  modelTitle,
  pushModel,
  pushRemoval
} from '../editor/EditorModel'
import {OpiskeluoikeudenUusiTilaPopup} from './OpiskeluoikeudenUusiTilaPopup'
import {arvioituTaiVahvistettu} from '../suoritus/Suoritus'
import {parseISODate} from '../date/date.js'
import {Editor} from '../editor/Editor'
import Text from '../i18n/Text'

const showOpiskeluoikeudenTilaDialog = Atom(false)
export const OpiskeluoikeudenTilaEditor = ({model, alkuChangeBus}) => {
  let wrappedModel = fixOpiskeluoikeudenPäättymispäivä(model)
  let jaksotModel = opiskeluoikeusjaksot(wrappedModel)
  let items = modelItems(jaksotModel).slice(0).reverse()
  let suorituksiaKesken = wrappedModel.context.edit && R.any(s => !arvioituTaiVahvistettu(s))(modelItems(wrappedModel, 'suoritukset'))
  let showAddDialog = () => showOpiskeluoikeudenTilaDialog.modify(x => !x)

  let lisääJakso = (uusiJakso) => {
    if (uusiJakso) {
      pushModel(uusiJakso, wrappedModel.context.changeBus)
    }
    showOpiskeluoikeudenTilaDialog.set(false)
  }

  let removeItem = () => {
    pushRemoval(items[0], wrappedModel.context.changeBus)
    showOpiskeluoikeudenTilaDialog.set(false)
  }

  let showLisaaTila = wrappedModel.context.edit && !onLopputilassa(wrappedModel)
  let edellisenTilanAlkupäivä = modelData(items[0], 'alku') && new Date(modelData(items[0], 'alku'))

  return (
      <div>
        <ul className="array">
          {
            items.map((item, i) => (
              <li key={i}>
                <div className={'opiskeluoikeusjakso' + (i === getActiveIndex(items) ? ' active' : '')}>
                  <label className="date">
                    {i === items.length - 1
                      ? <Editor model={item} path="alku" changeBus={alkuChangeBus}/>
                      : <Editor model={item} path="alku" edit={false}/>
                    }
                  </label>
                  <label className="tila">
                    {modelTitle(item, 'tila')}
                    {
                      rahoitusMuuttunut(items, i)&& <span className="rahoitus">{formatRahoitus(rahoitus(items, i))}</span>
                    }
                  </label>
                </div>
                {wrappedModel.context.edit && i === 0 && items.length > 1 && <a className="remove-item" onClick={removeItem}/>}
              </li>)
            )
          }
          {
            showLisaaTila && <li className="add-item"><a onClick={showAddDialog}><Text name="Lisää opiskeluoikeuden tila"/></a></li>
          }
        </ul>
        {
          showOpiskeluoikeudenTilaDialog.map(showDialog => showDialog && <OpiskeluoikeudenUusiTilaPopup tilaListModel={jaksotModel} suorituksiaKesken={suorituksiaKesken} edellisenTilanAlkupäivä={edellisenTilanAlkupäivä} resultCallback={(uusiJakso) => lisääJakso(uusiJakso)} />)
        }
      </div>
  )
}

OpiskeluoikeudenTilaEditor.isEmpty = m => recursivelyEmpty(m, 'opiskeluoikeusjaksot')

const rahoitusMuuttunut = (items, index) => {
  return rahoitus(items, index) != rahoitus(items, index + 1)
}

let formatRahoitus = rahoitus => rahoitus && ` (${rahoitus.toLowerCase()})`
let rahoitus = (items, index) => items[index] && modelTitle(items[index], 'opintojenRahoitus')


export const fixOpiskeluoikeudenPäättymispäivä = model =>
  lensedModel(model, L.rewrite(fixPäättymispäivä))

export const onLopputila = (tila) => {
  let koodi = modelData(tila).koodiarvo
  return koodi === 'eronnut' || koodi === 'valmistunut' || koodi === 'peruutettu'
}

export const onLopputilassa = (opiskeluoikeus) => {
  let jakso = viimeinenJakso(opiskeluoikeus)
  if (!jakso) return false
  return onLopputila(modelLookup(jakso, 'tila'))
}

const getActiveIndex = (jaksot) => {
  let today = new Date()
  return jaksot.findIndex(j => parseISODate(modelData(j, 'alku')) <= today)
}

const viimeinenJakso = (opiskeluoikeus) => R.last(modelItems(opiskeluoikeusjaksot(opiskeluoikeus)))

const opiskeluoikeusjaksot = (opiskeluoikeus) => {
  return modelLookup(opiskeluoikeus, 'tila.opiskeluoikeusjaksot')
}

let fixPäättymispäivä = (opiskeluoikeus) => {
  let päättymispäivä = onLopputilassa(opiskeluoikeus)
    ? modelLookup(viimeinenJakso(opiskeluoikeus), 'alku').value
    : null

  return modelSetValue(opiskeluoikeus, päättymispäivä, 'päättymispäivä')
}
