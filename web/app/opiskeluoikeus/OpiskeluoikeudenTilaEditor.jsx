import React from 'baret'
import * as R from 'ramda'
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

export class OpiskeluoikeudenTilaEditor extends React.Component {
  constructor(props) {
    super(props)
    this.showOpiskeluoikeudenTilaDialog = Atom(false)
  }

  render() {
    const {model, alkuChangeBus} = this.props
    let wrappedModel = fixOpiskeluoikeudenPäättymispäivä(model)
    let jaksotModel = opiskeluoikeusjaksot(wrappedModel)
    let items = modelItems(jaksotModel).slice(0).reverse()
    let suorituksiaKesken = wrappedModel.context.edit && R.any(s => !arvioituTaiVahvistettu(s))(modelItems(wrappedModel, 'suoritukset'))
    let suoritettuAineopinto = wrappedModel.context.edit && hasAtLeastOneCompletedAineopinto(modelLookup(model, 'suoritukset'))
    let eiTiedossaOppiaineita = hasSomeEiTiedossaAineopinto(modelLookup(model, 'suoritukset'))
    let eiSaaAsettaaValmiiksi = eiTiedossaOppiaineita || (suorituksiaKesken && !suoritettuAineopinto)

    let showAddDialog = () => this.showOpiskeluoikeudenTilaDialog.modify(x => !x)

    let lisääJakso = (uusiJakso) => {
      if (uusiJakso) {
        pushModel(uusiJakso, wrappedModel.context.changeBus)
      }
      this.showOpiskeluoikeudenTilaDialog.set(false)
    }

    let removeItem = () => {
      pushRemoval(items[0], wrappedModel.context.changeBus)
      this.showOpiskeluoikeudenTilaDialog.set(false)
    }

    let showLisaaTila = wrappedModel.context.edit && !onLopputilassa(wrappedModel)
    let edellisenTilanAlkupäivä = modelData(items[0], 'alku') && parseISODate(modelData(items[0], 'alku'))

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
                      rahoitusMuuttunut(items, i) &&
                      <span className="rahoitus">{formatRahoitus(rahoitus(items, i))}</span>
                    }
                  </label>
                </div>
                {wrappedModel.context.edit && i === 0 &&
                <a className="remove-item" onClick={removeItem}/>}
              </li>)
            )
          }
          {
            showLisaaTila &&
            <li className="add-item"><a onClick={showAddDialog}><Text name="Lisää opiskeluoikeuden tila"/></a></li>
          }
        </ul>
        {
          this.showOpiskeluoikeudenTilaDialog.map(showDialog => {
            return showDialog &&
              <OpiskeluoikeudenUusiTilaPopup tilaListModel={jaksotModel} suorituksiaKesken={eiSaaAsettaaValmiiksi}
                                             edellisenTilanAlkupäivä={edellisenTilanAlkupäivä}
                                             resultCallback={(uusiJakso) => lisääJakso(uusiJakso)}/>
          })
        }
      </div>
    )
  }
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

const isAineopinto = (opinto) => {
  if (!opinto.oneOfPrototypes) return false

  return opinto.oneOfPrototypes.some(proto =>
    proto.key === 'lukionoppiaineenoppimaaransuoritus' ||
    proto.key === 'aikuistenperusopetuksenoppiaineenoppimaaransuoritus' ||
    proto.key === 'nuortenperusopetuksenoppiaineenoppimaaransuoritus' )
}

const hasAtLeastOneCompletedAineopinto = (suoritukset) => {
  if (!suoritukset || !suoritukset.value) return false

  const aineopinnot = suoritukset.value.filter(isAineopinto)
  return aineopinnot.some(opinto => arvioituTaiVahvistettu(opinto)) // FIXME: Test for "Vahvistus"
}

const hasSomeEiTiedossaAineopinto = (suoritukset) => {
  if (!suoritukset) return false

  return suoritukset.value.some(suoritus =>
    suoritus.value.properties &&
    suoritus.value.properties.some(property =>
      property.model.value && property.model.value.properties &&
      property.model.value.properties.some(p =>
        p.model.value && p.model.value.data && p.model.value.data.koodiarvo === 'XX'
      )
    )
  )
}

const opiskeluoikeusjaksot = (opiskeluoikeus) => {
  return modelLookup(opiskeluoikeus, 'tila.opiskeluoikeusjaksot')
}

let fixPäättymispäivä = (opiskeluoikeus) => {
  let päättymispäivä = onLopputilassa(opiskeluoikeus)
                       ? modelLookup(viimeinenJakso(opiskeluoikeus), 'alku').value
                       : null

  return modelSetValue(opiskeluoikeus, päättymispäivä, 'päättymispäivä')
}
