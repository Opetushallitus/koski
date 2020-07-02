import React from 'react'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {Editor} from '../editor/Editor'
import {t} from '../i18n/i18n.js'
import {suorituksenTyyppi} from './Suoritus'
import {buildClassNames} from '../components/classnames'
import {tutkinnonNimi} from './Koulutusmoduuli'
import {InternationalSchoolLevel} from '../internationalschool/InternationalSchoolLevel'
import {TunnisteenKoodiarvoEditor} from './TunnisteenKoodiarvoEditor'

export const KoulutusmoduuliEditor = ({model}) => {
  const propertyFilter = p => {
    const excludedProperties = ['tunniste', 'perusteenDiaarinumero', 'perusteenNimi', 'pakollinen', 'diplomaType']
    const esiopetusKuvaus = suorituksenTyyppi(model.context.suoritus) === 'esiopetuksensuoritus' && p.key === 'kuvaus'
    return !excludedProperties.includes(p.key) && !esiopetusKuvaus
  }
  return (
    <span className="koulutusmoduuli">
      <span className="tunniste">
        <TunnisteEditor model={model} />
      </span>
      <span className="tunniste-koodiarvo">
        <TunnisteenKoodiarvoEditor model={model} />
      </span>
      <span className="diaarinumero">
        <span className={buildClassNames(['value', !model.context.edit && 'label'])}>
          <Editor model={model} path="perusteenDiaarinumero" placeholder={t('Perusteen diaarinumero')}/>
        </span>
      </span>
      <PropertiesEditor model={model} propertyFilter={propertyFilter}/>
    </span>
  )
}

const TunnisteEditor = ({model}) => {
  const overrideEdit = model.context.editAll ? true : false
  const päätasonsuoritus = model.context.suoritus
  const tyyppi = suorituksenTyyppi(päätasonsuoritus)
  const käytäPäätasonSuoritusta =
    ['aikuistenperusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaaranalkuvaihe'].includes(tyyppi) || model.value.classes.includes('lukionoppiaineidenoppimaarat2019')

  return käytäPäätasonSuoritusta
    ? <Editor model={model.context.suoritus} path="tyyppi" edit={false}/>
    : <React.Fragment><Editor model={tutkinnonNimi(model)} edit={overrideEdit}/><InternationalSchoolLevel model={model} /></React.Fragment>
}
