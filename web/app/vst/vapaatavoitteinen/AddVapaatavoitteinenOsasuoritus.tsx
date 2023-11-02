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
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import { createVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../VSTOsasuoritukset'
import { VSTOsasuoritus } from '../typeguards'

type AddVapaatavoitteinenOsasuoritusProps = CommonProps<{
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönOpiskeluoikeus, any>,
    osasuoritus: VSTOsasuoritus
  ) => void
  pathWithOsasuoritukset: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
  >
}>

export const AddVapaatavoitteinenOsasuoritus: React.FC<
  AddVapaatavoitteinenOsasuoritusProps
> = (props) => {
  const data = useMemo(
    () => getValue(props.pathWithOsasuoritukset)(props.form.state),
    [props.form.state, props.pathWithOsasuoritukset]
  )
  const { pathWithOsasuoritukset, createOsasuoritus } = props
  const { organisaatio } = useContext(OpiskeluoikeusContext)

  const osasuoritukset =
    usePreferences<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus>(
      organisaatio?.oid,
      'vapaansivistystyonvapaatavoitteisenkoulutuksenosasuoritus'
    )

  const storedOsasuoritustunnisteet = useMemo(
    () => osasuoritukset.preferences.map((p) => p.tunniste),
    [osasuoritukset.preferences]
  )

  const onKoodistoSelect = useCallback(
    (
      osasuoritus: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus,
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
              createVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
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
