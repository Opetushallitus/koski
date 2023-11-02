import React, { useCallback, useContext, useMemo } from 'react'
import { OpiskeluoikeusContext } from '../../appstate/opiskeluoikeus'
import { usePreferences } from '../../appstate/preferences'
import { CommonProps } from '../../components-v2/CommonProps'
import { Column, ColumnRow } from '../../components-v2/containers/Columns'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../components-v2/forms/FormModel'
import { PaikallinenOsasuoritusSelect } from '../../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import { t } from '../../i18n/i18n'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { createVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../VSTOsasuoritukset'
import { VSTOsasuoritus } from '../typeguards'

type AddJotpaOsasuoritusViewProps = CommonProps<{
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönOpiskeluoikeus, any>,
    osasuoritus: VSTOsasuoritus
  ) => void
  pathWithOsasuoritukset: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    | VapaanSivistystyönJotpaKoulutuksenSuoritus
    | VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
  >
}>

export const AddJotpaOsasuoritusView: React.FC<AddJotpaOsasuoritusViewProps> = (
  props
) => {
  const data = getValue(props.pathWithOsasuoritukset)(props.form.state)
  const { pathWithOsasuoritukset, createOsasuoritus } = props
  const { organisaatio } = useContext(OpiskeluoikeusContext)

  const osasuoritukset =
    usePreferences<VapaanSivistystyönJotpaKoulutuksenOsasuoritus>(
      organisaatio?.oid,
      'vapaansivistystyonjotpakoulutuksenosasuoritus'
    )

  const storedOsasuoritustunnisteet = useMemo(
    () => osasuoritukset.preferences.map((p) => p.tunniste),
    [osasuoritukset.preferences]
  )

  const onKoodistoSelect = useCallback(
    (
      osasuoritus: VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus,
      isNew = false
    ) => {
      createOsasuoritus(pathWithOsasuoritukset, osasuoritus)
      if (isNew) {
        osasuoritukset.store(
          osasuoritus.koulutusmoduuli.tunniste.koodiarvo,
          osasuoritus.koulutusmoduuli
        )
      }
    },
    [createOsasuoritus, pathWithOsasuoritukset, osasuoritukset]
  )

  const onRemovePaikallinenKoodisto = useCallback(
    (tunniste: PaikallinenKoodi) => {
      osasuoritukset.remove(tunniste.koodiarvo)
    },
    [osasuoritukset]
  )

  if (data === undefined) {
    console.warn('Data is undefined')
    return null
  }

  return (
    <ColumnRow indent={props.level + 1}>
      <Column span={10}>
        <PaikallinenOsasuoritusSelect
          addNewText={t('Lisää osasuoritus')}
          onSelect={(tunniste, isNew) =>
            onKoodistoSelect(
              createVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
                tunniste
              ),
              isNew
            )
          }
          onRemove={onRemovePaikallinenKoodisto}
          tunnisteet={storedOsasuoritustunnisteet}
          testId={props.testId}
        />
      </Column>
    </ColumnRow>
  )
}
