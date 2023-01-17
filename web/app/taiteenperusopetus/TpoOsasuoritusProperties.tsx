import React, { useEffect } from 'react'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
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
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { lastElement } from '../util/optics'
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
  const viimeisinArviointiPath = props.osasuoritusPath
    .prop('arviointi')
    .optional()
    .compose(lastElement())

  return (
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
      <OsasuoritusSubproperty rowNumber={1} label="Päivämäärä" key="pvm">
        <FormField
          key="pvm"
          form={props.form}
          path={viimeisinArviointiPath.prop('päivä')}
          view={DateView}
          edit={DateEdit}
        />
      </OsasuoritusSubproperty>
      <OsasuoritusSubproperty rowNumber={2} label="Arvioitsijat">
        TODO: Arvioitsijaeditori
      </OsasuoritusSubproperty>
    </OsasuoritusProperty>
  )
}
