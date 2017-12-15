import {addContext, modelData, modelItems, modelLookup, modelSet, modelSetValue, pushModel} from '../editor/EditorModel'
import React from 'baret'
import R from 'ramda'
import Atom from 'bacon.atom'
import {PropertyEditor} from '../editor/PropertyEditor'
import {MerkitseSuoritusValmiiksiPopup} from './MerkitseSuoritusValmiiksiPopup'
import {JääLuokalleTaiSiirretäänEditor} from './JaaLuokalleTaiSiirretaanEditor'
import {
  arviointiPuuttuu, arvioituTaiVahvistettu, onKeskeneräisiäOsasuorituksia, suoritusKesken,
  suoritusValmis,
  tilaText
} from './Suoritus'
import Text from '../i18n/Text'
import {isPerusopetuksenOppimäärä, isYsiluokka, jääLuokalle} from '../perusopetus/Perusopetus'
import {t} from '../i18n/i18n'

export const TilaJaVahvistusEditor = ({model}) => {
  return (<div className="tila-vahvistus">
      <span className="tiedot">
        <span className="tila">
          <span className={ suoritusValmis(model) ? 'tila valmis' : 'tila'}>{ tilaText(model) }</span>
        </span>
        {
          modelData(model).vahvistus && <PropertyEditor model={model} propertyName="vahvistus" edit="false"/>
        }
        <JääLuokalleTaiSiirretäänEditor model={addContext(model, {edit:false})}/>
      </span>
      <span className="controls">
        <MerkitseValmiiksiButton model={model}/>
        <MerkitseKeskeneräiseksiButton model={model}/>
      </span>
    </div>
  )
}

const MerkitseKeskeneräiseksiButton = ({model}) => {
  if (!model.context.edit || suoritusKesken(model)) return null
  var opiskeluoikeudenTila = modelData(model.context.opiskeluoikeus, 'tila.opiskeluoikeusjaksot.-1.tila').koodiarvo
  let merkitseKeskeneräiseksi = () => {
    pushModel(modelSetValue(model, undefined, 'vahvistus'))
  }
  let valmistunut = opiskeluoikeudenTila === 'valmistunut'
  return <button className="merkitse-kesken" title={valmistunut ? t('Ei voi merkitä keskeneräiseksi, koska opiskeluoikeuden tila on Valmistunut.') : ''} disabled={valmistunut} onClick={merkitseKeskeneräiseksi}><Text name="Merkitse keskeneräiseksi"/></button>
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
          var ysiLuokkaValmis = modelSet(ysiluokkaKesken, modelLookup(suoritusModel, 'vahvistus'), 'vahvistus')
          pushModel(ysiLuokkaValmis, model.context.changeBus)
        }
      }
    } else {
      addingAtom.set(false)
    }
  }
  let keskeneräisiä = onKeskeneräisiäOsasuorituksia(model) || arviointiPuuttuu(model)
  let buttonText = arvioituTaiVahvistettu(model) ? t('Muokkaa vahvistusta') : t('Merkitse valmiiksi')
  return (<span>
    <button className="merkitse-valmiiksi" title={keskeneräisiä ? t('Ei voi merkitä valmiiksi, koska suorituksessa on keskeneräisiä tai arvioimattomia osasuorituksia.') : ''} disabled={keskeneräisiä} onClick={() => addingAtom.modify(x => !x)}>{buttonText}</button>
    {
      addingAtom.map(adding => adding && <MerkitseSuoritusValmiiksiPopup suoritus={model} resultCallback={merkitseValmiiksiCallback}/>)
    }
  </span>)
}