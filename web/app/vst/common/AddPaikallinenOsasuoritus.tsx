import React, { useCallback, useContext, useMemo } from 'react'
import { OpiskeluoikeusContext } from '../../appstate/opiskeluoikeus'
import { usePreferences } from '../../appstate/preferences'
import { CommonProps } from '../../components-v2/CommonProps'
import { Column, ColumnRow } from '../../components-v2/containers/Columns'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { PaikallinenOsasuoritusSelect } from '../../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import { t } from '../../i18n/i18n'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { KoulutusmoduuliOf, OsasuoritusOf } from '../../util/schema'
import { VSTSuoritusPaikallisillaOsasuorituksilla } from './types'
import { useKoodistoFiller } from '../../appstate/koodisto'

type AddPaikallinenOsasuoritusProps<
  T extends VSTSuoritusPaikallisillaOsasuorituksilla
> = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<VapaanSivistystyönOpiskeluoikeus, T>
  createOsasuoritus: (tunniste: PaikallinenKoodi) => OsasuoritusOf<T>
  level: number
  preferenceStoreName: string
}>

export const AddPaikallinenOsasuoritus = <
  T extends VSTSuoritusPaikallisillaOsasuorituksilla
>(
  props: AddPaikallinenOsasuoritusProps<T>
) => {
  const { createOsasuoritus, form, suoritusPath } = props
  const { organisaatio } = useContext(OpiskeluoikeusContext)
  const fillKoodistot = useKoodistoFiller()

  const osasuoritukset = usePreferences<KoulutusmoduuliOf<OsasuoritusOf<T>>>(
    organisaatio?.oid,
    props.preferenceStoreName
  )

  const storedOsasuoritustunnisteet = useMemo(
    () => osasuoritukset.preferences.map((p) => p.tunniste),
    [osasuoritukset.preferences]
  )

  const onSelect = useCallback(
    async (tunniste: PaikallinenKoodi, isNew = false) => {
      const osasuorituksetPath = suoritusPath
        .prop('osasuoritukset')
        .optional() as any as FormOptic<
        VapaanSivistystyönOpiskeluoikeus,
        OsasuoritusOf<T>[]
      >
      const osasuoritus = await fillKoodistot(createOsasuoritus(tunniste))
      form.updateAt(osasuorituksetPath, (os: OsasuoritusOf<T>[]) => [
        ...os,
        osasuoritus
      ])
      if (isNew) {
        osasuoritukset.store(
          osasuoritus.koulutusmoduuli.tunniste.koodiarvo,
          osasuoritus.koulutusmoduuli
        )
      }
    },
    [createOsasuoritus, fillKoodistot, form, osasuoritukset, suoritusPath]
  )

  const onRemovePaikallinenKoodisto = useCallback(
    (tunniste: PaikallinenKoodi) => {
      osasuoritukset.remove(tunniste.koodiarvo)
    },
    [osasuoritukset]
  )

  return (
    <ColumnRow indent={props.level + 1}>
      <Column span={10}>
        <PaikallinenOsasuoritusSelect
          addNewText={t('Lisää osasuoritus')}
          onSelect={onSelect}
          onRemove={onRemovePaikallinenKoodisto}
          tunnisteet={storedOsasuoritustunnisteet}
          testId={props.testId}
        />
      </Column>
    </ColumnRow>
  )
}
