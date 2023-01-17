import React from 'react'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import {
  ArvioitsijatEdit,
  ArvioitsijatView
} from '../components-v2/opiskeluoikeus/ArvioitsijatField'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  OsasuoritusProperty,
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
          form={props.form}
          path={viimeisinArviointiPath.prop('päivä')}
          view={DateView}
          edit={DateEdit}
        />
      </OsasuoritusSubproperty>
      <OsasuoritusSubproperty rowNumber={2} label="Arvioitsijat">
        <FormField
          form={props.form}
          path={viimeisinArviointiPath.prop('arvioitsijat')}
          view={ArvioitsijatView}
          edit={ArvioitsijatEdit}
        />
      </OsasuoritusSubproperty>
    </OsasuoritusProperty>
  )
}
