import {addContext, modelData, modelLookup, removeCommonPath} from '../editor/EditorModel'
import React from 'baret'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {Editor} from '../editor/Editor'
import {TilaJaVahvistusEditor} from './TilaJaVahvistusEditor'
import {arviointiPuuttuu, osasuoritukset, suoritusKesken, suoritusValmis} from './Suoritus'
import Text from '../i18n/Text'
import {resolveOsasuorituksetEditor, resolvePropertyEditor} from './suoritusEditorMapping'
import {flatMapArray} from '../util/util'
import DeletePaatasonSuoritusButton from './DeletePaatasonSuoritusButton'
import {currentLocation} from '../util/location'

export class SuoritusEditor extends React.Component {
  showDeleteButtonIfAllowed() {
    const {model} = this.props

    const editingAny = !!currentLocation().params.edit
    const showEditLink = model.editable && !editingAny
    const showDeleteLink = model.invalidatable && !showEditLink

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

SuoritusEditor.validateModel = (m) => {
  if (suoritusValmis(m) && arviointiPuuttuu(m)) {
    return [{key: 'missing', message: <Text name='Suoritus valmis, mutta arvosana puuttuu'/>}]
  }

  const validateSuoritus = (s) =>
    flatMapArray(osasuoritukset(s),
      osasuoritus => {
        if (suoritusValmis(s) && suoritusKesken(osasuoritus)) {
          let subPath = removeCommonPath(osasuoritus.path, m.path)
          return [{
            path: subPath.concat('arviointi'),
            key: 'osasuorituksenTila',
            message: <Text name='Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.'/>
          }]
        } else {
          return validateSuoritus(osasuoritus)
        }
      })

  return validateSuoritus(m)
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
