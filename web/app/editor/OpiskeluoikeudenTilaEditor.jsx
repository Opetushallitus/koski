import React from 'baret'
import R from 'ramda'
import * as L from 'partial.lenses'
import Atom from 'bacon.atom'
import {modelData, modelItems, modelLookup} from './EditorModel.js'
import {OpiskeluoikeudenUusiTilaPopup} from './OpiskeluoikeudenUusiTilaPopup.jsx'
import {lensedModel, modelSetValue, modelTitle, pushModel, pushRemoval} from './EditorModel'
import {suoritusKesken} from './Suoritus'
import {parseISODate} from '../date.js'
import {Editor} from './Editor.jsx'
import Text from '../Text.jsx'

export const OpiskeluoikeudenTilaEditor = ({model}) => {
  let wrappedModel = lensedModel(model, L.rewrite(fixPäättymispäivä))
  let jaksotModel = opiskeluoikeusjaksot(wrappedModel)
  let addingNew = Atom(false)
  let items = modelItems(jaksotModel).slice(0).reverse()
  let suorituksiaKesken = wrappedModel.context.edit && R.any(suoritusKesken)(modelItems(wrappedModel, 'suoritukset'))
  let showAddDialog = () => addingNew.modify(x => !x)

  let lisääJakso = (uusiJakso) => {
    if (uusiJakso) {
      pushModel(uusiJakso, wrappedModel.context.changeBus)
    }
    addingNew.set(false)
  }

  let removeItem = () => {
    pushRemoval(items[0], wrappedModel.context.changeBus)
    addingNew.set(false)
  }

  let showLisaaTila = wrappedModel.context.edit && !onLopputilassa(wrappedModel)
  let edellisenTilanAlkupäivä = modelData(items[0], 'alku') && new Date(modelData(items[0], 'alku'))

  return (
      <div>
        <ul className="array">
          {
            items.map((item, i) => {
              return (<li key={i}>
                <div className={'opiskeluoikeusjakso' + (i === getActiveIndex(items) ? ' active' : '')}>
                  <label className="date"><Editor model={item} path="alku" edit={false}/></label>
                  <label className="tila">{modelTitle(item, 'tila')}</label>
                </div>
                {wrappedModel.context.edit && i === 0 && items.length > 1 && <a className="remove-item" onClick={removeItem}>{''}</a>}
              </li>)
            })
          }
          {
            showLisaaTila && <li className="add-item"><a onClick={showAddDialog}><Text name="Lisää opiskeluoikeuden tila"/></a></li>
          }
        </ul>
        {
          addingNew.map(adding => adding && <OpiskeluoikeudenUusiTilaPopup tilaListModel={jaksotModel} suorituksiaKesken={suorituksiaKesken} edellisenTilanAlkupäivä={edellisenTilanAlkupäivä} resultCallback={(uusiJakso) => lisääJakso(uusiJakso)} />)
        }
      </div>
  )
}

export const onLopputila = (tila) => {
  let koodi = modelData(tila).koodiarvo
  return koodi === 'eronnut' || koodi === 'valmistunut' || koodi === 'erotettu' || koodi === 'peruutettu'
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

const viimeinenJakso = (opiskeluoikeus) => modelItems(opiskeluoikeusjaksot(opiskeluoikeus)).last()

const opiskeluoikeusjaksot = (opiskeluoikeus) => {
  return modelLookup(opiskeluoikeus, 'tila.opiskeluoikeusjaksot')
}

let fixPäättymispäivä = (opiskeluoikeus) => {
  let päättymispäivä = onLopputilassa(opiskeluoikeus)
    ? modelLookup(viimeinenJakso(opiskeluoikeus), 'alku').value
    : null

  return modelSetValue(opiskeluoikeus, päättymispäivä, 'päättymispäivä')
}