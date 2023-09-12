/* eslint-disable @typescript-eslint/no-unused-vars */
import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'
import React, { useCallback, useMemo } from 'react'
import { useAllowedStrings, useSchema } from '../../appstate/constraints'
import { useKoodisto } from '../../appstate/koodisto'
import { todayISODate } from '../../date/date'
import { t } from '../../i18n/i18n'
import {
  Koodistokoodiviite,
  isKoodistokoodiviite
} from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/Opiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { KoodiarvotOf } from '../../util/koodisto'
import { isValmistuvaTerminaalitila } from '../../util/opiskeluoikeus'
import { ClassOf } from '../../util/types'
import { common, CommonProps, subTestId } from '../CommonProps'
import { Label } from '../containers/Label'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { DateEdit, DateView } from '../controls/DateField'
import { FlatButton } from '../controls/FlatButton'
import { RadioButtonsEdit } from '../controls/RadioButtons'
import { RaisedButton } from '../controls/RaisedButton'
import { FormField, Nothing } from '../forms/FormField'
import { FormModel, useForm } from '../forms/FormModel'
import { ValidationError } from '../forms/validator'

export type UusiOpiskeluoikeudenTilaModalProps<T extends Opiskeluoikeusjakso> =
  CommonProps<{
    onSubmit: (
      form: UusiOpiskeluoikeusjakso<T>
    ) => NonEmptyArray<ValidationError> | undefined
    onClose: () => void
    opiskeluoikeusjaksoClass: ClassOf<T>
    enableValmistuminen: boolean
  }>

// TODO: Tyypitä tämä uudestaan tukemaan kaikkia Koskesta löytyviä opiskeluoikeusjaksoja.
export type UusiOpiskeluoikeusjakso<T extends Opiskeluoikeusjakso> = {
  alku: string
  tila: OpiskeluoikeudenTilakoodi<KoodiarvotOf<T['tila']>>
  // TODO: Vahvempi tyypitys
  opintojenRahoitus?: any
}

const KOODISTOURI_OPISKELUOIKEUDEN_TILA = 'koskiopiskeluoikeudentila' as const
const KOODISTOURI_OPINTOJENRAHOITUS = 'opintojenrahoitus' as const

type TilaKoodistoUri = typeof KOODISTOURI_OPISKELUOIKEUDEN_TILA
type OpintojenRahoitusUri = typeof KOODISTOURI_OPINTOJENRAHOITUS

type OpiskeluoikeudenTilakoodi<S extends string = string> = Koodistokoodiviite<
  TilaKoodistoUri,
  S
>

type OpiskeluoikeudenOpintojenrahoituskoodi<S extends string = string> =
  Koodistokoodiviite<OpintojenRahoitusUri, S>

const useInitialOpiskelujaksoForm = <T extends Opiskeluoikeusjakso>(
  opiskeluoikeusjaksoClass: ClassOf<T>
) =>
  useMemo<UusiOpiskeluoikeusjakso<T>>(() => {
    if (
      opiskeluoikeusjaksoClass ===
      VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
    ) {
      // VST JOTPA erikoistapaus
      // TODO: Tarkista, onko tämä ok
      return {
        alku: todayISODate(),
        tila: Koodistokoodiviite({
          koodistoUri: KOODISTOURI_OPISKELUOIKEUDEN_TILA,
          koodiarvo: defaultTila(opiskeluoikeusjaksoClass)
        }),
        opintojenRahoitus: Koodistokoodiviite({
          koodistoUri: KOODISTOURI_OPINTOJENRAHOITUS,
          koodiarvo: defaultOpintojenRahoitus(opiskeluoikeusjaksoClass)
        })
      }
    }
    return {
      alku: todayISODate(),
      tila: Koodistokoodiviite({
        koodistoUri: KOODISTOURI_OPISKELUOIKEUDEN_TILA,
        koodiarvo: defaultTila(opiskeluoikeusjaksoClass)
      })
    }
  }, [opiskeluoikeusjaksoClass])

function hasRahoitus(x: string) {
  switch (x) {
    case VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className:
      return true
    default:
      return false
  }
}

type P<T extends Opiskeluoikeusjakso> = {
  form: FormModel<UusiOpiskeluoikeusjakso<T>>
  opiskeluoikeusjaksoClass: ClassOf<T>
  enableValmistuminen: boolean
}

const OpiskeluoikeudenTilanRahoitusField = <T extends Opiskeluoikeusjakso>(
  props: P<T>
) => {
  const [opintojenRahoitusPath] = useMemo(
    () => [props.form.root.prop('opintojenRahoitus')],
    [props.form.root]
  )
  const rahoitusKoodiarvot = useAllowedStrings(
    props.opiskeluoikeusjaksoClass,
    'opintojenRahoitus.koodiarvo'
  )
  const rahoitus = useKoodisto(
    KOODISTOURI_OPINTOJENRAHOITUS,
    rahoitusKoodiarvot
  )?.map((k) => k.koodiviite)

  const opintojenRahoitusOptions = useMemo(
    () =>
      rahoitus?.map((r) => ({
        key: r.koodiarvo,
        label: t(r.nimi),
        value: r,
        disabled: !props.enableValmistuminen && isValmistuvaTerminaalitila(r)
      })),
    [props.enableValmistuminen, rahoitus]
  )

  return (
    <Label label="Opintojen rahoitus">
      <FormField
        form={props.form}
        path={opintojenRahoitusPath}
        view={Nothing}
        edit={RadioButtonsEdit}
        editProps={{
          getKey: (tila: unknown) => {
            if (isKoodistokoodiviite(tila)) {
              return tila.koodiarvo
            }
            throw new Error('getKey(): Unknown value for tila')
          },
          options: opintojenRahoitusOptions
        }}
        testId={subTestId(props, 'opintojenRahoitus')}
      />
    </Label>
  )
}

export const UusiOpiskeluoikeudenTilaModal = <T extends Opiskeluoikeusjakso>(
  props: UusiOpiskeluoikeudenTilaModalProps<T>
) => {
  const koodiarvot = useAllowedStrings(
    props.opiskeluoikeusjaksoClass,
    'tila.koodiarvo'
  )?.filter((k) => k !== 'mitatoity')

  const tilat = useKoodisto(KOODISTOURI_OPISKELUOIKEUDEN_TILA, koodiarvot)?.map(
    (k) => k.koodiviite
  )

  const tilaOptions = useMemo(
    () =>
      tilat?.map((tila) => ({
        key: tila.koodiarvo,
        label: t(tila.nimi),
        value: tila,
        disabled: !props.enableValmistuminen && isValmistuvaTerminaalitila(tila)
      })),
    [props.enableValmistuminen, tilat]
  )

  const initialState = useInitialOpiskelujaksoForm<T>(
    props.opiskeluoikeusjaksoClass
  )
  const opiskeluoikeusjaksoSchema = useSchema(props.opiskeluoikeusjaksoClass)
  const form = useForm(initialState, true, opiskeluoikeusjaksoSchema)
  const [päivämääräPath, tilaPath] = useMemo(
    () => [form.root.prop('alku'), form.root.prop('tila')],
    [form.root]
  )

  const onSubmit = useCallback(() => {
    const errors = props.onSubmit(form.state)
    if (errors) {
      // onSubmitista ei tällä implementointihetkellä pitäisi tulla virheitä,
      // joten virheiden näyttämisen voi toteuttaa myöhemmin vasta kun sitä tarvitaan.
      // eslint-disable-next-line no-console
      console.warn(
        'Käsittelemättömiä validointivirheitä UusiOpiskeluoikeudenTilaModalissa:',
        errors
      )
    }
  }, [form.state, props])

  return (
    <Modal
      {...common(props, ['UusiOpiskeluoikeudenTilaModal'])}
      onClose={props.onClose}
    >
      <ModalTitle>{'Uusi opiskeluoikeuden tila'}</ModalTitle>

      <ModalBody>
        <Label label="Päivämäärä">
          <FormField
            form={form}
            path={päivämääräPath}
            view={DateView}
            edit={DateEdit}
            testId={subTestId(props, 'date')}
          />
        </Label>

        <Label label="Tila">
          <FormField
            form={form}
            path={tilaPath}
            view={Nothing}
            edit={RadioButtonsEdit}
            editProps={{
              getKey: (tila: unknown) => {
                if (isKoodistokoodiviite(tila)) {
                  return tila.koodiarvo
                }
                throw new Error('getKey(): Unknown type for tila')
              },
              options: tilaOptions
            }}
            testId={subTestId(props, 'tila')}
          />
        </Label>

        {hasRahoitus(props.opiskeluoikeusjaksoClass) && (
          <OpiskeluoikeudenTilanRahoitusField
            form={form}
            enableValmistuminen={props.enableValmistuminen}
            opiskeluoikeusjaksoClass={props.opiskeluoikeusjaksoClass}
          />
        )}
      </ModalBody>

      <ModalFooter>
        <FlatButton onClick={props.onClose} testId={subTestId(props, 'cancel')}>
          {'Peruuta'}
        </FlatButton>
        <RaisedButton onClick={onSubmit} testId={subTestId(props, 'submit')}>
          {'Lisää'}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

// Utils

const defaultTila = (cn: ClassOf<Opiskeluoikeusjakso>): string => 'lasna'
const defaultOpintojenRahoitus = (cn: ClassOf<Opiskeluoikeusjakso>): string =>
  '14'
