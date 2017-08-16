import {
  addContext, modelData, modelItems, modelLens, modelLookup, modelSet, modelSetValue,
  pushModel
} from './EditorModel'
import React from 'baret'
import R from 'ramda'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import {PropertyEditor} from './PropertyEditor.jsx'
import {MerkitseSuoritusValmiiksiPopup} from './MerkitseSuoritusValmiiksiPopup.jsx'
import {JääLuokalleTaiSiirretäänEditor} from './JaaLuokalleTaiSiirretaanEditor.jsx'
import {
  arviointiPuuttuu,
  onKeskeneräisiäOsasuorituksia,
  setTila,
  suorituksenTila,
  suoritusKesken,
  suoritusValmis
} from './Suoritus'
import Text from '../Text.jsx'
import {isPerusopetuksenOppimäärä, isYsiluokka, jääLuokalle} from './Perusopetus'
import {t} from '../i18n'

export const TilaJaVahvistusEditor = ({model}) => {
  return (<div className="tila-vahvistus">
      <span className="tiedot">
        <span className="tila">
          <Text name="Suoritus"/>{': '}<span className={ suoritusValmis(model) ? 'tila valmis' : 'tila'}>{ suorituksenTila(model) }</span>
        </span>
        {
          modelData(model).vahvistus && <PropertyEditor model={model} propertyName="vahvistus" edit="false"/>
        }
        <JääLuokalleTaiSiirretäänEditor model={addContext(model, {edit:false})}/>
      </span>
      <span className="controls">
        <MerkitseValmiiksiButton model={model}/>
        <MerkitseKeskeytyneeksiButton model={model}/>
        <MerkitseKeskeneräiseksiButton model={model}/>
      </span>
    </div>
  )
}

const MerkitseKeskeneräiseksiButton = ({model}) => {
  if (!model.context.edit || suoritusKesken(model)) return null
  var opiskeluoikeudenTila = modelData(model.context.opiskeluoikeus, 'tila.opiskeluoikeusjaksot.-1.tila').koodiarvo
  let merkitseKeskeneräiseksi = () => {
    pushModel(setTila(modelSetValue(model, undefined, 'vahvistus'), 'KESKEN'))
  }
  let valmistunut = opiskeluoikeudenTila === 'valmistunut'
  return <button className="merkitse-kesken" title={valmistunut ? t('Ei voi merkitä keskeneräiseksi, koska opiskeluoikeuden tila on "Valmistunut".') : ''} disabled={valmistunut} onClick={merkitseKeskeneräiseksi}><Text name="Merkitse keskeneräiseksi"/></button>
}

const MerkitseKeskeytyneeksiButton = ({model}) => {
  if (!model.context.edit || !suoritusKesken(model)) return null

  let merkitseKeskeytyneeksiJosKesken = (suoritus) => {
    if (!suoritusKesken(suoritus)) return suoritus
    let osasuorituksetKeskeytetty = L.modify([modelLens('osasuoritukset'), 'value', L.elems], merkitseKeskeytyneeksiJosKesken, suoritus)
    return setTila(osasuorituksetKeskeytetty, 'KESKEYTYNYT')
  }

  let merkitseKeskeytyneeksi = () => {
    pushModel(merkitseKeskeytyneeksiJosKesken(model))
  }
  return <button className="merkitse-keskeytyneeksi" onClick={merkitseKeskeytyneeksi}><Text name="Merkitse keskeytyneeksi"/></button>
}

const MerkitseValmiiksiButton = ({model}) => {
  if (!model.context.edit || !suoritusKesken(model) || ( isYsiluokka(model) && !jääLuokalle(model))) return null
  let addingAtom = Atom(false)
  let merkitseValmiiksiCallback = (suoritusModel) => {
    if (suoritusModel) {
      pushModel(suoritusModel, model.context.changeBus)
      if (isPerusopetuksenOppimäärä(model)) {
        let ysiluokkaKesken = modelItems(model.context.opiskeluoikeus, 'suoritukset').find(R.allPass([isYsiluokka, suoritusKesken]))
        if (ysiluokkaKesken) {
          var ysiLuokkaValmis = setTila(ysiluokkaKesken, 'VALMIS')
          ysiLuokkaValmis = modelSet(ysiLuokkaValmis, modelLookup(suoritusModel, 'vahvistus'), 'vahvistus')
          pushModel(ysiLuokkaValmis, model.context.changeBus)
        }
      }
    } else {
      addingAtom.set(false)
    }
  }
  let keskeneräisiä = onKeskeneräisiäOsasuorituksia(model) || arviointiPuuttuu(model)
  return (<span>
    <button className="merkitse-valmiiksi" title={keskeneräisiä ? t('Ei voi merkitä valmiiksi, koska suorituksessa on keskeneräisiä tai arvioimattomia osasuorituksia.') : ''} disabled={keskeneräisiä} onClick={() => addingAtom.modify(x => !x)}><Text name="Merkitse valmiiksi"/></button>
    {
      addingAtom.map(adding => adding && <MerkitseSuoritusValmiiksiPopup suoritus={model} resultCallback={merkitseValmiiksiCallback}/>)
    }
  </span>)
}