import React from 'react'
import { Column, ColumnGrid } from '../components-v2/containers/ColumnGrid'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import {
  ArviointiEdit,
  ArviointiView
} from '../components-v2/opiskeluoikeus/ArviointiField'
import { OSASUORITUSTABLE_DEPTH_KEY } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { useDepth } from '../util/useDepth'
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
  const [depth] = useDepth(OSASUORITUSTABLE_DEPTH_KEY)

  return (
    <div className="TpoOsasuoritusProperties">
      <ColumnGrid>
        <Column span={depth} />
        <Column span={3} className="TpoOsasuoritusProperties__label">
          Arviointi
        </Column>
        <Column span={4} className="TpoOsasuoritusProperties__label">
          Arvosana
        </Column>
        <Column span={24 - depth - 7}>
          <FormField
            form={props.form}
            path={props.osasuoritusPath.prop('arviointi')}
            view={(props) => <ArviointiView {...props} />}
            edit={(props) => (
              <ArviointiEdit {...props} createArviointi={createTpoArviointi} />
            )}
          />
        </Column>
      </ColumnGrid>
      <ColumnGrid>
        <Column span={depth + 3} />
        <Column span={4} className="TpoOsasuoritusProperties__label">
          Päivämäärä
        </Column>
        <Column span={24 - depth - 7}>
          TODO: Päivämääräeditori (ArviointiDateEdit ja DateEdit)
        </Column>
      </ColumnGrid>
      <ColumnGrid>
        <Column span={depth + 3} />
        <Column span={4} className="TpoOsasuoritusProperties__label">
          Arvioijat
        </Column>
        <Column span={24 - depth - 7}>TODO: Arvioitsijaeditori</Column>
      </ColumnGrid>
    </div>
  )
}
