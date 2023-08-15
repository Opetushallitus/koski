import {
  addContext,
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  modelSetValue,
  pushModel
} from '../editor/EditorModel'
import React from 'baret'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import { PropertyEditor } from '../editor/PropertyEditor'
import { MerkitseSuoritusValmiiksiPopup } from './MerkitseSuoritusValmiiksiPopup'
import { JääLuokalleTaiSiirretäänEditor } from './JaaLuokalleTaiSiirretaanEditor'
import {
  arviointiPuuttuu,
  arvioituTaiVahvistettu,
  onKeskeneräisiäOsasuorituksia,
  suoritusKesken,
  suoritusValmis,
  tilaText
} from './Suoritus'
import Text from '../i18n/Text'
import {
  isPerusopetuksenOppimäärä,
  isYsiluokka,
  jääLuokalle
} from '../perusopetus/Perusopetus'
import { t } from '../i18n/i18n'
import * as ytr from '../ytr/ytr'
import { ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu } from '../ammatillinen/AmmatillinenOsittainenTutkinto'
import { isLukionOppiaineidenOppimaarienSuoritus2019 } from '../lukio/lukio.js'

const isVirtaSuoritus = (model) =>
  [
    'korkeakoulunopintojakso',
    'korkeakoulututkinto',
    'muukorkeakoulunsuoritus'
  ].includes(modelData(model, 'tyyppi.koodiarvo'))

const isPäättynyt = (model) =>
  modelData(
    model.context.opiskeluoikeus,
    'tila.opiskeluoikeusjaksot.-1.tila.koodiarvo'
  ) === '6'

export const TilaJaVahvistusEditor = ({ model }) => {
  if (ytr.pakollisetKokeetSuoritettuEnnen1990(model)) return null
  if (isLukionOppiaineidenOppimaarienSuoritus2019(model)) return null
  if (isVirtaSuoritus(model) && isPäättynyt(model) && !suoritusValmis(model))
    return null

  return (
    <div
      className={
        suoritusValmis(model) ? 'tila-vahvistus valmis' : 'tila-vahvistus'
      }
    >
      <span className="tiedot">
        <span className="tila">{tilaText(model)}</span>
        {modelData(model).vahvistus && (
          <PropertyEditor model={model} propertyName="vahvistus" edit="false" />
        )}
        <JääLuokalleTaiSiirretäänEditor
          model={addContext(model, { edit: false })}
        />
      </span>
      <span className="controls">
        <MerkitseValmiiksiButton model={model} />
        <MerkitseKeskeneräiseksiButton model={model} />
      </span>
    </div>
  )
}

const MerkitseKeskeneräiseksiButton = ({ model }) => {
  if (!model.context.edit || suoritusKesken(model)) return null
  const opiskeluoikeudenTila = modelData(
    model.context.opiskeluoikeus,
    'tila.opiskeluoikeusjaksot.-1.tila.koodiarvo'
  )
  const merkitseKeskeneräiseksi = () => {
    pushModel(modelSetValue(model, undefined, 'vahvistus'))
  }
  const valmistunut = opiskeluoikeudenTila === 'valmistunut'
  return (
    <button
      className="koski-button merkitse-kesken"
      title={
        valmistunut
          ? t(
              'Ei voi merkitä keskeneräiseksi, koska opiskeluoikeuden tila on Valmistunut.'
            )
          : ''
      }
      disabled={valmistunut}
      onClick={merkitseKeskeneräiseksi}
      data-testid="merkitse-suoritus-kesken"
    >
      <Text name="Merkitse keskeneräiseksi" />
    </button>
  )
}

const MerkitseValmiiksiButton = ({ model }) => {
  if (
    !model.context.edit ||
    !suoritusKesken(model) ||
    (isYsiluokka(model) && !jääLuokalle(model))
  )
    return null
  const addingAtom = Atom(false)
  const merkitseValmiiksiCallback = (suoritusModel) => {
    if (suoritusModel) {
      pushModel(suoritusModel, model.context.changeBus)
      if (isPerusopetuksenOppimäärä(model)) {
        const ysiluokkaKesken = modelItems(
          model.context.opiskeluoikeus,
          'suoritukset'
        ).find(R.allPass([isYsiluokka, suoritusKesken]))
        if (ysiluokkaKesken) {
          const ysiLuokkaValmis = modelSet(
            ysiluokkaKesken,
            modelLookup(suoritusModel, 'vahvistus'),
            'vahvistus'
          )
          pushModel(ysiLuokkaValmis, model.context.changeBus)
        }
      }
    } else {
      addingAtom.set(false)
    }
  }

  const keskeneräisiäSuorituksia =
    onKeskeneräisiäOsasuorituksia(model) || arviointiPuuttuu(model)
  const disabled =
    keskeneräisiäSuorituksia ||
    eiTiedossaOppiaine(model) ||
    ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu(model)
  const buttonText = arvioituTaiVahvistettu(model)
    ? t('Muokkaa vahvistusta')
    : t('Merkitse valmiiksi')
  const title = eiTiedossaOppiaine(model)
    ? t('"Ei tiedossa"-oppiainetta ei voi merkitä valmiiksi')
    : keskeneräisiäSuorituksia
    ? t(
        'Ei voi merkitä valmiiksi, koska suorituksessa on keskeneräisiä tai arvioimattomia osasuorituksia.'
      )
    : ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu(model)
    ? t(
        'Ei voi merkitä valmiiksi, koska suoritukselta puuttuu ammatillisen tutkinnon osan suoritus...'
      )
    : ''

  return (
    <span>
      <button
        className="koski-button merkitse-valmiiksi"
        title={title}
        disabled={disabled}
        onClick={() => addingAtom.modify((x) => !x)}
        data-testid="merkitse-suoritus-valmiiksi"
      >
        {buttonText}
      </button>
      {addingAtom.map(
        (adding) =>
          adding && (
            <MerkitseSuoritusValmiiksiPopup
              suoritus={model}
              resultCallback={merkitseValmiiksiCallback}
            />
          )
      )}
    </span>
  )
}

export const eiTiedossaOppiaine = (suoritus) =>
  modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo') === 'XX'
