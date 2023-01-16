import React from 'react'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyLabel,
  OsasuoritusPropertyValue,
  OsasuoritusSubproperty
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { OSASUORITUSTABLE_DEPTH_KEY } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { useLayout } from '../util/useDepth'
import { createTpoArviointi } from './tpoCommon'

export type TpoOsasuoritusPropertiesProps = {
  form: FormModel<TaiteenPerusopetuksenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    TaiteenPerusopetuksenOpiskeluoikeus,
    TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus
  >
}

export const TpoOsasuoritusProperties: React.FC<
  TpoOsasuoritusPropertiesProps
> = (props) => {
  const [depth] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)

  return (
    <>
      <OsasuoritusProperty label="Arviointi">
        <OsasuoritusSubproperty label="Arvosana">
          <FormField
            form={props.form}
            path={props.osasuoritusPath.prop('arviointi')}
            view={(props) => <ArvosanaView {...props} />}
            edit={(props) => (
              <ArvosanaEdit {...props} createArviointi={createTpoArviointi} />
            )}
          />
        </OsasuoritusSubproperty>
        <OsasuoritusSubproperty rowNumber={1} label="Päivämäärä">
          TODO: Päivämääräeditori
        </OsasuoritusSubproperty>
        <OsasuoritusSubproperty rowNumber={2} label="Arvioitsijat">
          TODO: Arvioitsijaeditori
        </OsasuoritusSubproperty>
      </OsasuoritusProperty>
    </>
  )
}
