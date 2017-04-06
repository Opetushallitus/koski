import React from 'baret'
import R from 'ramda'
import * as L from 'partial.lenses'
import Atom from 'bacon.atom'
import {modelData, modelItems, modelLookup} from './EditorModel.js'
import {ArrayEditor} from './ArrayEditor.jsx'
import {OpiskeluoikeusjaksoEditor} from './OpiskeluoikeusjaksoEditor.jsx'
import {OpiskeluoikeudenUusiTilaPopup} from './OpiskeluoikeudenUusiTilaPopup.jsx'
import {modelSetValue, lensedModel} from './EditorModel'

export const OpiskeluoikeudenTilaEditor = ({model}) => {
  let wrappedModel = lensedModel(model, L.rewrite(fixPäättymispäivä))
  
  let jaksotModel = opiskeluoikeusjaksot(wrappedModel)
  let addingNew = Atom(false)
  let items = modelItems(jaksotModel).slice(0).reverse()
  let suorituksiaKesken = wrappedModel.context.edit && R.any(s => s.tila && s.tila.koodiarvo == 'KESKEN')(modelData(wrappedModel, 'suoritukset') || [])
  let showAddDialog = () => addingNew.modify(x => !x)

  let lisääJakso = (uusiJakso) => {
    if (uusiJakso) {
      wrappedModel.context.changeBus.push([uusiJakso.context, uusiJakso])
    }
    addingNew.set(false)
  }

  let removeItem = () => {
    wrappedModel.context.changeBus.push([items[0].context, undefined])
    addingNew.set(false)
  }

  let showLisaaTila = !onLopputilassa(wrappedModel)
  let edellisenTilanAlkupäivä = modelData(items[0], 'alku') && new Date(modelData(items[0], 'alku'))

  return (
    wrappedModel.context.edit ?
      <div>
        <ul className="array">
          {
            items.map((item, i) => {
              return (<li key={i}>
                <OpiskeluoikeusjaksoEditor model={item}/>
                {i === 0 && <a className="remove-item" onClick={removeItem}></a>}
              </li>)
            })
          }
          {
            showLisaaTila && <li className="add-item"><a onClick={showAddDialog}>Lisää opiskeluoikeuden tila</a></li>
          }
        </ul>
        {
          addingNew.map(adding => adding && <OpiskeluoikeudenUusiTilaPopup tilaListModel={jaksotModel} suorituksiaKesken={suorituksiaKesken} edellisenTilanAlkupäivä={edellisenTilanAlkupäivä} resultCallback={(uusiJakso) => lisääJakso(uusiJakso)} />)
        }
      </div> :
      <div><ArrayEditor reverse={true} model={jaksotModel}/></div>
  )
}

export const onLopputila = (tila) => {
  let koodi = modelData(tila).koodiarvo
  return koodi === 'eronnut' || koodi === 'valmistunut' || koodi === 'katsotaaneronneeksi'
}

export const onLopputilassa = (opiskeluoikeus) => {
  let jakso = viimeinenJakso(opiskeluoikeus)
  if (!jakso) return false
  return onLopputila(modelLookup(jakso, 'tila'))
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