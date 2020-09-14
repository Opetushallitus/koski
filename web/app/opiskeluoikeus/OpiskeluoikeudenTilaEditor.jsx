import React from 'baret'
import * as R from 'ramda'
import * as L from 'partial.lenses'
import Atom from 'bacon.atom'
import {
  lensedModel,
  modelData,
  modelItems,
  modelLookup,
  modelSetValue,
  modelTitle,
  pushModel,
  pushRemoval,
  recursivelyEmpty
} from '../editor/EditorModel'
import {OpiskeluoikeudenUusiTilaPopup} from './OpiskeluoikeudenUusiTilaPopup'
import {arvioituTaiVahvistettu, osasuoritukset} from '../suoritus/Suoritus'
import {eiTiedossaOppiaine} from '../suoritus/TilaJaVahvistusEditor'
import {parseISODate} from '../date/date.js'
import {Editor} from '../editor/Editor'
import Text from '../i18n/Text'
import {isLukionOppiaineidenOppimaarienSuoritus2019} from '../lukio/lukio.js'

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
    const suoritukset = modelItems(model, 'suoritukset')
    const suorituksiaKesken = suoritukset.some(s => !arvioituTaiVahvistettu(s))
    const suoritettuAineopintoTaiAikuistenPerusopetuksenOppimäärä = suoritukset.some(s => arvioituTaiVahvistettu(s) && (isAineopinto(s) || isAikuistenPerusopetuksenOppimäärä(s)))
    const suorituksissaValmistumiskelpoinenLukionOppiaineidenOppimaarienSuoritus2019 = suoritukset.some( s => isLukionOppiaineidenOppimaarienSuoritus2019(s) && osasuoritukset(s).some(arvioituTaiVahvistettu) )
    const disabloiValmistunut = suoritukset.some(eiTiedossaOppiaine) ||
      (suorituksiaKesken && !suoritettuAineopintoTaiAikuistenPerusopetuksenOppimäärä && !suorituksissaValmistumiskelpoinenLukionOppiaineidenOppimaarienSuoritus2019)

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
              <OpiskeluoikeudenUusiTilaPopup tilaListModel={jaksotModel} disabloiValmistunut={disabloiValmistunut}
                                             edellisenTilanAlkupäivä={edellisenTilanAlkupäivä}
                                             resultCallback={(uusiJakso) => lisääJakso(uusiJakso)}/>
          })
        }
      </div>
    )
  }
}

OpiskeluoikeudenTilaEditor.isEmpty = m => recursivelyEmpty(m, 'opiskeluoikeusjaksot')

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

const aineOpinnot = ['lukionoppiaineenoppimaara', 'nuortenperusopetuksenoppiaineenoppimaara', 'perusopetuksenoppiaineenoppimaara']
const isAineopinto = suoritus => aineOpinnot.includes(modelData(suoritus, 'tyyppi.koodiarvo'))
const isAikuistenPerusopetuksenOppimäärä = suoritus => modelData(suoritus, 'tyyppi.koodiarvo') === 'aikuistenperusopetuksenoppimaara'

const opiskeluoikeusjaksot = (opiskeluoikeus) => {
  return modelLookup(opiskeluoikeus, 'tila.opiskeluoikeusjaksot')
}

let fixPäättymispäivä = (opiskeluoikeus) => {
  let päättymispäivä = onLopputilassa(opiskeluoikeus)
                       ? modelLookup(viimeinenJakso(opiskeluoikeus), 'alku').value
                       : null

  return modelSetValue(opiskeluoikeus, päättymispäivä, 'päättymispäivä')
}
