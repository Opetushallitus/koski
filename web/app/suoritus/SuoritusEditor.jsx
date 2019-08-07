import {addContext, modelData, modelItems, modelLookup, removeCommonPath} from '../editor/EditorModel'
import React from 'baret'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {Editor} from '../editor/Editor'
import {TilaJaVahvistusEditor} from './TilaJaVahvistusEditor'
import {
  arviointiPuuttuu,
  osasuoritukset,
  osasuorituksetVahvistettu,
  suoritusKesken,
  suoritusValmis
} from './Suoritus'
import Text from '../i18n/Text'
import {resolveOsasuorituksetEditor, resolvePropertyEditor} from './suoritusEditorMapping'
import {flatMapArray} from '../util/util'
import DeletePaatasonSuoritusButton from './DeletePaatasonSuoritusButton'
import {currentLocation} from '../util/location'
import {
  isOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus,
  isVälisuoritus
} from '../ammatillinen/TutkinnonOsa'
import {ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu, isOstettu} from '../ammatillinen/AmmatillinenOsittainenTutkinto'

export class SuoritusEditor extends React.Component {
  showDeleteButtonIfAllowed() {
    const {model} = this.props

    const editingAny = !!currentLocation().params.edit
    const showEditLink = model.context.opiskeluoikeus.editable && !editingAny
    const isSingleSuoritus = modelItems(model.context.opiskeluoikeus, 'suoritukset').length == 1
    const showDeleteLink = model.invalidatable && !showEditLink && !isSingleSuoritus

    return showDeleteLink && (
      <DeletePaatasonSuoritusButton
        opiskeluoikeus={model.context.opiskeluoikeus}
        päätasonSuoritus={model}
      />
    )
  }

  render() {
    const excludedProperties = ['osasuoritukset', 'käyttäytymisenArvio', 'vahvistus', 'jääLuokalle', 'pakollinen']

    let {model} = this.props
    model = addContext(model, { suoritus: model, toimipiste: modelLookup(model, 'toimipiste')})
    const osasuorituksetEditor = resolveOsasuorituksetEditor(model)

    let className = 'suoritus ' + model.value.classes.join(' ')

    return (
      <div className={className}>
        {this.showDeleteButtonIfAllowed()}
        <TodistusLink suoritus={model} />
        <PropertiesEditor
          model={model}
          propertyFilter={p => !excludedProperties.includes(p.key) && (model.context.edit || modelData(p.model) !== false)}
          getValueEditor={(prop, getDefault) => resolvePropertyEditor(prop, model) || getDefault()}
          className={model.context.kansalainen ? 'kansalainen' : ''}
        />
        <TilaJaVahvistusEditor model={model} />
        <div className="osasuoritukset">{osasuorituksetEditor}</div>
      </div>
    )
  }

  shouldComponentUpdate(nextProps) {
    return Editor.shouldComponentUpdate.call(this, nextProps)
  }
}

SuoritusEditor.validateModel = model => {
  if (suoritusValmis(model) && arviointiPuuttuu(model)) {
    return [{key: 'missing', message: <Text name='Suoritus valmis, mutta arvosana puuttuu'/>}]
  }

  const validateSuoritus = suoritus => flatMapArray(osasuoritukset(suoritus), osasuoritus => {
    if (suoritusValmis(suoritus) && suoritusKesken(osasuoritus) && !isVälisuoritus(suoritus)) {
      return isOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus(osasuoritus)
        ? validateKeskeneräinenOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus(osasuoritus)
        : validationError(osasuoritus, 'Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.')
    } else {
      return validateSuoritus(osasuoritus)
    }
  })

  const validateKeskeneräinenOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus = suoritus => (
    osasuorituksetVahvistettu(suoritus)
      ? validateSuoritus(suoritus)
      : validationError(suoritus, 'Keskeneräisen yhteisen tutkinnon osan pitää koostua valmiista osa-alueista, jotta suoritus voidaan merkitä valmiiksi')
  )

  const validateValmisOsittaisenAmmatillisenTutkinnonSuoritus = suoritus => (
    ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu(suoritus) && suoritusValmis(suoritus) &&
    !isOstettu(suoritus)
      ? [{key: 'missing', message: <Text name='Ei voi merkitä valmiiksi, koska suoritukselta puuttuu ammatillinen tutkinnon osa...'/>}]
      : []
  )

  const validationError = (suoritus, virheviesti) => {
    const subPath = removeCommonPath(suoritus.path, model.path)
    return [{
      path: subPath.concat('arviointi'),
      key: 'osasuorituksenTila',
      message: <Text name={virheviesti}/>
    }]
  }

  return validateSuoritus(model).concat(validateValmisOsittaisenAmmatillisenTutkinnonSuoritus(model))
}

class TodistusLink extends React.Component {
  render() {
    let {suoritus} = this.props
    const {kansalainen} = suoritus.context
    let oppijaOid = suoritus.context.oppijaOid
    let suoritustyyppi = modelData(suoritus, 'tyyppi').koodiarvo
    let koulutusmoduuliKoodistoUri = modelData(suoritus, 'koulutusmoduuli').tunniste.koodistoUri
    let koulutusmoduuliKoodiarvo = modelData(suoritus, 'koulutusmoduuli').tunniste.koodiarvo
    let href = '/koski/todistus/' + oppijaOid + '?suoritustyyppi=' + suoritustyyppi + '&koulutusmoduuli=' + koulutusmoduuliKoodistoUri + '/' + koulutusmoduuliKoodiarvo
    return suoritusValmis(suoritus)
           && suoritustyyppi !== 'korkeakoulututkinto'
           && suoritustyyppi !== 'preiboppimaara'
           && suoritustyyppi !== 'esiopetuksensuoritus'
           && !(koulutusmoduuliKoodistoUri === 'perusopetuksenluokkaaste' && koulutusmoduuliKoodiarvo === '9')
           && !kansalainen
        ? <a className="todistus" href={href}><Text name="näytä todistus"/></a>
        : null
  }
}
