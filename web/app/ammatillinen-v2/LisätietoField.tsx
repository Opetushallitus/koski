import { CommonProps } from '../components-v2/CommonProps'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import { AmmatillisenTutkinnonOsanLisätieto } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsanLisatieto'
import { EmptyObject } from '../util/objects'
import { localize, t } from '../i18n/i18n'
import React from 'react'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { MultilineTextEdit } from '../components-v2/controls/TextField'

export const LisätietoView = ({
  value
}: CommonProps<
  FieldViewerProps<AmmatillisenTutkinnonOsanLisätieto, EmptyObject>
>) => {
  return (
    <>
      <div>{t(value?.tunniste.nimi)}</div>
      <div className={'LisätietoKuvaus'}>{t(value?.kuvaus)}</div>
    </>
  )
}
export const emptyAmmatillisenTutkinnonOsanLisätieto =
  AmmatillisenTutkinnonOsanLisätieto({
    tunniste: Koodistokoodiviite({
      koodistoUri: 'ammatillisentutkinnonosanlisatieto',
      koodiarvo: 'mukautettu'
    }),
    kuvaus: localize('')
  })
export const LisätietoEdit = ({
  value,
  onChange
}: FieldEditorProps<AmmatillisenTutkinnonOsanLisätieto, EmptyObject>) => {
  return (
    <>
      <KoodistoSelect
        koodistoUri={'ammatillisentutkinnonosanlisatieto'}
        value={value?.tunniste.koodiarvo}
        onSelect={(tunniste) =>
          tunniste &&
          onChange({
            ...emptyAmmatillisenTutkinnonOsanLisätieto,
            ...value,
            tunniste
          })
        }
        testId={'tunniste'}
      />
      <MultilineTextEdit
        value={t(value?.kuvaus)}
        onChange={(kuvaus) =>
          kuvaus &&
          onChange({
            ...emptyAmmatillisenTutkinnonOsanLisätieto,
            ...value,
            kuvaus: localize(kuvaus)
          })
        }
      />
    </>
  )
}
