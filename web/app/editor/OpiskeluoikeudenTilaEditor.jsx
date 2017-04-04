import React from 'baret'
import R from 'ramda'
import Atom from 'bacon.atom'
import {modelData, modelItems, modelLookup} from './EditorModel.js'
import {resetOptionalModel} from './OptionalEditor.jsx'
import {ArrayEditor} from './ArrayEditor.jsx'
import {OpiskeluoikeusjaksoEditor} from './OpiskeluoikeusjaksoEditor.jsx'
import {OpiskeluoikeudenUusiTilaPopup} from './OpiskeluoikeudenUusiTilaPopup.jsx'
import {modelSetValue} from './EditorModel'

export const OpiskeluoikeudenTilaEditor = ({model}) => {
  let jaksotModel = opiskeluoikeusjaksot(model)
  let addingNew = Atom(false)
  let items = modelItems(jaksotModel).slice(0).reverse()
  let suorituksiaKesken = model.context.edit && R.any(s => s.tila && s.tila.koodiarvo == 'KESKEN')(modelData(model, 'suoritukset') || [])
  let showAddDialog = () => addingNew.modify(x => !x)

  let lisääJakso = (uusiJakso) => {
    if (uusiJakso) {
      let tilaModel = modelLookup(uusiJakso, 'tila')
      if (onLopputila(tilaModel)) {
        let paattymispaivaModel = modelLookup(model, 'päättymispäivä')
        let uudenJaksonAlku = modelLookup(uusiJakso, 'alku')
        model.context.changeBus.push([paattymispaivaModel.context, modelSetValue(paattymispaivaModel, uudenJaksonAlku.value)])
      }
      model.context.changeBus.push([uusiJakso.context, uusiJakso])
    }
    addingNew.set(false)
  }

  let removeItem = () => {
    if (onLopputila(modelLookup(items[0], 'tila'))) {
      let paattymispaivaModel = modelLookup(model, 'päättymispäivä')
      resetOptionalModel(paattymispaivaModel)
    }
    model.context.changeBus.push([items[0].context, undefined])
    addingNew.set(false)
  }

  let showLisaaTila = !onLopputilassa(model)
  let edellisenTilanAlkupäivä = modelData(items[0], 'alku') && new Date(modelData(items[0], 'alku'))

  return (
    model.context.edit ?
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
  let jaksot = modelItems(opiskeluoikeusjaksot(opiskeluoikeus))
  let viimeinenJakso = jaksot.last()
  if (!viimeinenJakso) return false
  return onLopputila(modelLookup(viimeinenJakso, 'tila'))
}

export const opiskeluoikeusjaksot = (opiskeluoikeus) => {
  return modelLookup(opiskeluoikeus, 'tila.opiskeluoikeusjaksot')
}