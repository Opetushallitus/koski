import React from 'react'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { Editor } from '../editor/Editor'
import { EnumEditor, zeroValue } from '../editor/EnumEditor'
import {
  modelData,
  modelEmpty,
  modelLookup,
  modelProperty
} from '../editor/EditorModel'
import { suorituksenTyyppi } from './Suoritus'
import { tutkinnonNimi } from './Koulutusmoduuli'
import { InternationalSchoolLevel } from '../internationalschool/InternationalSchoolLevel'
import { isMuutaAmmatillistaPäätasonSuoritus } from '../muuammatillinen/MuuAmmatillinen'
import { PropertyInfo } from '../editor/PropertyInfo'
import { t } from '../i18n/i18n'
import { TunnisteenKoodiarvoEditor } from './TunnisteenKoodiarvoEditor'
import { buildClassNames } from '../components/classnames'

const isZeroValue = (option) => option.value === zeroValue.value
const hasKoodiarvo = (data) => data !== undefined && 'koodiarvo' in data
const formatOpintokokonaisuusDisplayValue = (option) => {
  if(option === undefined) {
    return zeroValue.title
  }
  if(isZeroValue(option)) {
    return option.title
  }
  if(!hasKoodiarvo(option.data)) {
    return option.title
  }
  return `${option.data.koodiarvo} ${option.title}`
}
const formatOpintokokonaisuusTitle = (option) => {
  if(option === undefined) {
    return zeroValue.title
  }
  if(isZeroValue(option)) {
    return option.title
  }
  if(!hasKoodiarvo(option.data)) {
    return option.title
  }
  return `${option.data.koodiarvo} ${option.title}`
}

const asHyperLink = (model) => {
  const data = modelData(model)
  if(!hasKoodiarvo(data)) {
    return {
      url: '#',
      target: '_self'
    }
  }
  return {
    url: `${window['ePerusteetBaseUrl']}${t(
      'eperusteet_opintopolku_url_fragment'
    )}${data.koodiarvo}`,
    target: '_blank'
  }
}

export const VstVapaaTavoitteinenKoulutusmoduuliEditor = ({ model }) => {
  const propertyFilter = (p) => {
    const excludedProperties = [
      'tunniste',
      'tunniste-koodiarvo',
      'perusteenNimi',
      'opintokokonaisuus'
    ]
    return !excludedProperties.includes(p.key)
  }

  const valueClass = modelEmpty(modelProperty(model, 'opintokokonaisuus').model)
    ? 'value empty'
    : 'value'

  return (
    <table className="koulutusmoduuli">
      <tbody>
        <tr>
          <td colSpan={2}>
            <span className="tunniste">
              <TunnisteEditor model={model} />
            </span>
            <span className="tunniste-koodiarvo">
              <TunnisteenKoodiarvoEditor model={model} />
            </span>
            <span className="diaarinumero">
              <span
                className={buildClassNames([
                  'value',
                  !model.context.edit && 'label'
                ])}
              >
                <Editor
                  model={model}
                  path="perusteenDiaarinumero"
                  placeholder={t('Perusteen diaarinumero')}
                />
              </span>
            </span>
          </td>
        </tr>
        <tr className="opintokokonaisuus">
          <td className={'label'}>
            <PropertyInfo
              property={modelProperty(model, 'opintokokonaisuus')}
            />
            {t('Opintokokonaisuus')}
          </td>
          <td className={valueClass}>
            <EnumEditor
              showEmptyOption={true}
              model={modelLookup(model, 'opintokokonaisuus')}
              asHyperlink={asHyperLink}
              titleFormatter={(mdl) => formatOpintokokonaisuusTitle(mdl.value)}
              displayValue={formatOpintokokonaisuusDisplayValue}
            />
          </td>
        </tr>
        <tr>
          <td colSpan={2}>
            <PropertiesEditor model={model} propertyFilter={propertyFilter} />
          </td>
        </tr>
      </tbody>
    </table>
  )
}

const TunnisteEditor = ({ model }) => {
  const overrideEdit =
    model.context.editAll ||
    (model.context.edit &&
      isMuutaAmmatillistaPäätasonSuoritus(model.context.suoritus))
      ? true
      : false
  const päätasonsuoritus = model.context.suoritus
  const tyyppi = suorituksenTyyppi(päätasonsuoritus)
  const käytäPäätasonSuoritusta =
    [
      'aikuistenperusopetuksenoppimaara',
      'aikuistenperusopetuksenoppimaaranalkuvaihe'
    ].includes(tyyppi) ||
    model.value.classes.includes('lukionoppiaineidenoppimaarat2019')
  let tutkinnonNimiModel = tutkinnonNimi(model)
  const excludedProperties = ['koodistoUri']

  if (
    tutkinnonNimiModel.value.properties &&
    isMuutaAmmatillistaPäätasonSuoritus(päätasonsuoritus)
  ) {
    tutkinnonNimiModel.value.properties =
      tutkinnonNimiModel.value.properties.filter((p) => {
        return !excludedProperties.includes(p.key)
      })
  }

  return käytäPäätasonSuoritusta ? (
    <Editor model={model.context.suoritus} path="tyyppi" edit={false} />
  ) : (
    <React.Fragment>
      <Editor model={tutkinnonNimiModel} edit={overrideEdit} />
      <InternationalSchoolLevel model={model} />
    </React.Fragment>
  )
}
