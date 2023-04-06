import React from 'react'
import { LukionOppiaineetEditor } from './LukionOppiaineetEditor'
import Text from '../i18n/Text'
import { modelItems, modelData } from '../editor/EditorModel'
import OmatTiedotLukionOppiaineet from './OmatTiedotLukionOppiaineet'

export const LuvaEditor = ({ suorituksetModel }) => {
  const { edit, kansalainen } = suorituksetModel.context

  const lukionkurssinsuorituksetFilter = (s) =>
    s.value.classes.includes(
      'lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa'
    )
  const lukio2019moduuliensuorituksetFilter = (s) =>
    s.value.classes.includes(
      'lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa2019'
    )
  const lukioonvalmistavankurssinsuorituksetFilter = (s) =>
    s.value.classes.includes('lukioonvalmistavankoulutuksenoppiaineensuoritus')

  const hasLukionKursseja =
    modelItems(suorituksetModel).filter(lukionkurssinsuorituksetFilter).length >
    0
  const hasLukion2019Moduuleja =
    modelItems(suorituksetModel).filter(lukio2019moduuliensuorituksetFilter)
      .length > 0
  const hasValmistaviaKursseja =
    modelItems(suorituksetModel).filter(
      lukioonvalmistavankurssinsuorituksetFilter
    ).length > 0

  const LukionOppiaineetComponent = kansalainen
    ? OmatTiedotLukionOppiaineet
    : LukionOppiaineetEditor

  const oppimaaraDiaarinumero = modelData(
    suorituksetModel.context.suoritus,
    'koulutusmoduuli.perusteenDiaarinumero'
  )

  return (
    <div>
      {(edit || hasValmistaviaKursseja) && (
        <div className="lukioon-valmistavat-opinnot">
          <h5>
            <Text name="Lukioon valmistavat opinnot" />
          </h5>
          <LukionOppiaineetComponent
            suorituksetModel={suorituksetModel}
            classesForUusiOppiaineenSuoritus={[
              'lukioonvalmistavankoulutuksenoppiaineensuoritus'
            ]}
            suoritusFilter={lukioonvalmistavankurssinsuorituksetFilter}
            forceLaajuusOpintopisteinä={
              oppimaaraDiaarinumero === 'OPH-4958-2020'
            }
          />
        </div>
      )}
      {oppimaaraDiaarinumero === '56/011/2015' &&
        (edit || hasLukionKursseja) && (
          <div className="valinnaisena-suoritetut-lukiokurssit">
            <h5>
              <Text name="Valinnaisena suoritetut lukiokurssit" />
            </h5>
            <LukionOppiaineetComponent
              suorituksetModel={suorituksetModel}
              classesForUusiOppiaineenSuoritus={[
                'lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa'
              ]}
              suoritusFilter={lukionkurssinsuorituksetFilter}
            />
          </div>
        )}
      {oppimaaraDiaarinumero === 'OPH-4958-2020' &&
        (edit || hasLukion2019Moduuleja) && (
          <div className="valinnaisena-suoritetut-lukiokurssit">
            <h5>
              <Text name="Valinnaisena suoritetut lukion moduulit" />
            </h5>
            <LukionOppiaineetComponent
              suorituksetModel={suorituksetModel}
              classesForUusiOppiaineenSuoritus={[
                'lukionoppiaineenopintojensuorituslukioonvalmistavassakoulutuksessa2019'
              ]}
              suoritusFilter={lukio2019moduuliensuorituksetFilter}
              useOppiaineLaajuus={true}
              showKeskiarvo={false}
              additionalOnlyEditableProperties={[
                'suorituskieli',
                'suoritettuErityisenäTutkintona'
              ]}
              additionalEditableKoulutusmoduuliProperties={[
                'pakollinen',
                'oppimäärä'
              ]}
              laajuusHeaderText={'Arvioitu'}
              showHyväksytystiArvioitujenLaajuus={true}
              useHylkäämättömätLaajuus={false}
              forceLaajuusOpintopisteinä={true}
            />
          </div>
        )}
    </div>
  )
}
