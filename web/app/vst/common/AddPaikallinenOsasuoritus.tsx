import React, { useCallback, useContext, useMemo } from 'react'
import { useKoodistoFiller } from '../../appstate/koodisto'
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
import { appendOptional } from '../../util/array'

type AddPaikallinenOsasuoritusProps<
  T extends VSTSuoritusPaikallisillaOsasuorituksilla
> = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  path: FormOptic<VapaanSivistystyönOpiskeluoikeus, T>
  createOsasuoritus: (tunniste: PaikallinenKoodi) => OsasuoritusOf<T>
  level: number
  preferenceStoreName: string
  placeholder?: string
}>

export const AddPaikallinenOsasuoritus = <
  T extends VSTSuoritusPaikallisillaOsasuorituksilla
>(
  props: AddPaikallinenOsasuoritusProps<T>
) => {
  const { createOsasuoritus, form, path } = props
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
      const osasuorituksetPath = path.prop('osasuoritukset')
      const osasuoritus = createOsasuoritus(tunniste)

      // Hae preferenceistä oletuslaajuus ja kuvaus jne.
      const koulutusmoduuli = osasuoritukset.preferences.find(
        (os) => os.tunniste.koodiarvo === tunniste.koodiarvo
      )
      if (koulutusmoduuli) {
        osasuoritus.koulutusmoduuli = koulutusmoduuli
      }

      const filledOsasuoritus = await fillKoodistot(osasuoritus)
      form.updateAt(
        osasuorituksetPath as any,
        appendOptional(filledOsasuoritus)
      )

      if (isNew) {
        osasuoritukset.store(
          filledOsasuoritus.koulutusmoduuli.tunniste.koodiarvo,
          filledOsasuoritus.koulutusmoduuli
        )
      }
    },
    [createOsasuoritus, fillKoodistot, form, osasuoritukset, path]
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
          addNewText={props.placeholder || t('Lisää osasuoritus')}
          onSelect={onSelect}
          onRemove={onRemovePaikallinenKoodisto}
          tunnisteet={storedOsasuoritustunnisteet}
        />
      </Column>
    </ColumnRow>
  )
}
