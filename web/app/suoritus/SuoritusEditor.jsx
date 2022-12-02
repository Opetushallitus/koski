import {
  addContext,
  modelData,
  modelItems,
  modelLookup,
  removeCommonPath
} from '../editor/EditorModel'
import React from 'baret'
import { pathOr } from 'ramda'
import { t } from '../i18n/i18n'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { Editor } from '../editor/Editor'
import { TilaJaVahvistusEditor } from './TilaJaVahvistusEditor'
import {
  arviointiPuuttuu,
  osasuoritukset,
  osasuorituksetVahvistettu,
  suoritusKesken,
  suoritusValmis
} from './Suoritus'
import Text from '../i18n/Text'
import {
  resolveOsasuorituksetEditor,
  resolvePropertyEditor
} from './suoritusEditorMapping'
import { flatMapArray } from '../util/util'
import DeletePaatasonSuoritusButton from './DeletePaatasonSuoritusButton'
import { currentLocation } from '../util/location'
import {
  isOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus,
  isVälisuoritus
} from '../ammatillinen/TutkinnonOsa'
import {
  ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu,
  isOstettu
} from '../ammatillinen/AmmatillinenOsittainenTutkinto'
import { AmmatillinenArviointiasteikko } from '../ammatillinen/AmmatillinenArviointiasteikko'

export class SuoritusEditor extends React.Component {
  showDeleteButtonIfAllowed() {
    const { model } = this.props

    const editingAny = !!currentLocation().params.edit
    const showEditLink = model.context.opiskeluoikeus.editable && !editingAny
    const isSingleSuoritus =
      modelItems(model.context.opiskeluoikeus, 'suoritukset').length === 1
    const showDeleteLink =
      model.invalidatable && !showEditLink && !isSingleSuoritus

    return (
      showDeleteLink && (
        <DeletePaatasonSuoritusButton
          opiskeluoikeus={model.context.opiskeluoikeus}
          päätasonSuoritus={model}
        />
      )
    )
  }

  render() {
    let { model } = this.props
    const excludedProperties = [
      'osasuoritukset',
      'käyttäytymisenArvio',
      'vahvistus',
      'jääLuokalle',
      'pakollinen'
    ].filter(Boolean)

    model = addContext(model, {
      suoritus: model,
      toimipiste: modelLookup(model, 'toimipiste')
    })
    const osasuorituksetEditor = resolveOsasuorituksetEditor(model)

    const className = 'suoritus ' + model.value.classes.join(' ')

    return (
      <div className={className}>
        {this.showDeleteButtonIfAllowed()}
        <PropertiesEditor
          model={model}
          propertyFilter={(p) =>
            !excludedProperties.includes(p.key) &&
            (model.context.edit || modelData(p.model) !== false)
          }
          getValueEditor={(prop, getDefault) =>
            resolvePropertyEditor(prop, model) || getDefault()
          }
          className={model.context.kansalainen ? 'kansalainen' : ''}
        />
        <TilaJaVahvistusEditor model={model} />
        <AmmatillinenArviointiasteikko model={model} />
        <div className="osasuoritukset">{osasuorituksetEditor}</div>
      </div>
    )
  }

  shouldComponentUpdate(nextProps) {
    return Editor.shouldComponentUpdate.call(this, nextProps)
  }
}

SuoritusEditor.validateModel = (model) => {
  if (suoritusValmis(model) && arviointiPuuttuu(model)) {
    return [
      {
        key: 'missing',
        message: <Text name="Suoritus valmis, mutta arvosana puuttuu" />
      }
    ]
  }

  const validateSuoritus = (suoritus) =>
    flatMapArray(osasuoritukset(suoritus), (osasuoritus) => {
      if (
        suoritusValmis(suoritus) &&
        suoritusKesken(osasuoritus) &&
        !isVälisuoritus(suoritus)
      ) {
        return isOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus(
          osasuoritus
        )
          ? validateKeskeneräinenOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus(
              osasuoritus
            )
          : validationError(
              osasuoritus,
              'Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.'
            )
      } else {
        return validateSuoritus(osasuoritus)
      }
    })

  const validateKeskeneräinenOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus =
    (suoritus) =>
      osasuorituksetVahvistettu(suoritus)
        ? validateSuoritus(suoritus)
        : validationError(
            suoritus,
            'Keskeneräisen yhteisen tutkinnon osan pitää koostua valmiista osa-alueista, jotta suoritus voidaan merkitä valmiiksi'
          )

  const validateValmisOsittaisenAmmatillisenTutkinnonSuoritus = (suoritus) =>
    ammattillinenOsittainenTutkintoJaMuuAmmatillisenTutkinnonOsaPuuttuu(
      suoritus
    ) &&
    suoritusValmis(suoritus) &&
    !isOstettu(suoritus)
      ? [
          {
            key: 'missing',
            message: (
              <Text name="Ei voi merkitä valmiiksi, koska suoritukselta puuttuu ammatillinen tutkinnon osa..." />
            )
          }
        ]
      : []

  const validateLuokkaAsteSallittuVainErityiselleTutkinnolle = (suoritus) => {
    const luokkaAste = modelLookup(suoritus, 'luokkaAste')
    const suoritustapa = modelLookup(suoritus, 'suoritustapa')
    const isErityinenTutkinto =
      pathOr('', ['value', 'value'], suoritustapa) ===
      'perusopetuksensuoritustapa_erityinentutkinto'
    const isVuosiluokkaSelected =
      pathOr('eivalintaa', ['value', 'value'], luokkaAste) !== 'eivalintaa'
    if (!isErityinenTutkinto && isVuosiluokkaSelected) {
      return [
        {
          path: 'luokkaAste',
          key: 'luokkaAsteInvalid',
          message: (
            <Text name={t('luokkaAstettaEiVoiValitaKoulutusSuoritukselle')} />
          )
        }
      ]
    }
    return []
  }

  const validateEshKieliaineidenValinta = (suoritus) => {
    if (
      suoritus.value.classes.includes(
        'europeanschoolofhelsinkipaatasonsuoritus'
      )
    ) {
      const suorituskielellisetOsasuoritukset = osasuoritukset(suoritus).filter(
        (osasuoritus) => {
          const koulutusmoduuli = modelLookup(osasuoritus, 'koulutusmoduuli')
          return koulutusmoduuli.value.classes.includes(
            'europeanschoolofhelsinkikielioppiaine'
          )
        }
      )
      return suorituskielellisetOsasuoritukset.flatMap((osasuoritus) => {
        const koulutusmoduuli = modelLookup(osasuoritus, 'koulutusmoduuli')
        if (
          koulutusmoduuli.value.classes.includes(
            'europeanschoolofhelsinkikielioppiaine'
          )
        ) {
          if (modelData(koulutusmoduuli, 'kieli') === undefined) {
            return [
              {
                path: removeCommonPath(koulutusmoduuli.path, model.path).concat(
                  'kieli'
                ),
                key: 'eshKieliaineenKieli',
                message: (
                  <Text name="description:esh_kielioppiaine_kieli_vaadittu" />
                )
              }
            ]
          }
          return []
        }
      })
    }
    return []
  }

  const validateEshSuorituskielenValinta = (suoritus) => {
    if (
      suoritus.value.classes.includes(
        'europeanschoolofhelsinkipaatasonsuoritus'
      )
    ) {
      const kaikkiOsasuoritukset = osasuoritukset(suoritus)
      return kaikkiOsasuoritukset.flatMap((osasuoritus) => {
        const suorituskieliModel = modelLookup(osasuoritus, 'suorituskieli')
        if (
          suorituskieliModel !== undefined &&
          modelData(suorituskieliModel) === undefined
        ) {
          return [
            {
              path: removeCommonPath(osasuoritus.path, model.path).concat(
                'suorituskieli'
              ),
              key: 'eshSuorituskieli',
              message: <Text name="description:esh_suorituskieli_vaadittu" />
            }
          ]
        }
        return []
      })
    }
    return []
  }

  const validationError = (suoritus, virheviesti) => {
    const subPath = removeCommonPath(suoritus.path, model.path)
    return [
      {
        path: subPath.concat('arviointi'),
        key: 'osasuorituksenTila',
        message: <Text name={virheviesti} />
      }
    ]
  }

  return validateSuoritus(model)
    .concat(validateValmisOsittaisenAmmatillisenTutkinnonSuoritus(model))
    .concat(validateLuokkaAsteSallittuVainErityiselleTutkinnolle(model))
    .concat(validateEshKieliaineidenValinta(model))
    .concat(validateEshSuorituskielenValinta(model))
}
