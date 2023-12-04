import React, { useCallback, useMemo, useState } from 'react'
import { ISO2FinnishDate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla } from '../../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'
import { Koulutustoimija } from '../../types/fi/oph/koski/schema/Koulutustoimija'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppilaitos } from '../../types/fi/oph/koski/schema/Oppilaitos'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import { Vahvistus } from '../../types/fi/oph/koski/schema/Vahvistus'
import {
  PäätasonSuoritusOf,
  isValmistuvaTerminaalitila
} from '../../util/opiskeluoikeus'
import {
  isHenkilövahvistus,
  viimeisinOpiskelujaksonTila
} from '../../util/schema'
import { ClassOf } from '../../util/types'
import { CommonProps, CommonPropsWithChildren, common } from '../CommonProps'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField
} from '../forms/FormField'
import { FormModel, FormOptic } from '../forms/FormModel'
import { Trans } from '../texts/Trans'
import { SuorituksenVahvistusModal } from './SuorituksenVahvistusModal'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'

// Suorituksen vahvitus field

export type SuorituksenVahvistusFieldProps<
  T extends Opiskeluoikeus,
  S extends PäätasonSuoritusOf<T> = PäätasonSuoritusOf<T>
> = CommonProps<{
  form: FormModel<T>
  suoritusPath: FormOptic<T, S>
  organisaatio?: Oppilaitos | Koulutustoimija
  disableAdd?: boolean
  disableRemoval?: boolean
}>

export const SuorituksenVahvistusField = <
  T extends Opiskeluoikeus,
  S extends PäätasonSuoritusOf<T> = PäätasonSuoritusOf<T>
>(
  props: SuorituksenVahvistusFieldProps<T, S>
): React.ReactElement => {
  const tila = viimeisinOpiskelujaksonTila(props.form.state.tila)
  const disableRemoval =
    props.disableRemoval ?? Boolean(tila && isValmistuvaTerminaalitila(tila))

  return (
    <FormField
      form={props.form}
      path={props.suoritusPath.prop('vahvistus')}
      optional
      view={SuorituksenVahvistusView}
      edit={SuorituksenVahvistusEdit}
      editProps={{
        organisaatio: props.organisaatio,
        vahvistusClass:
          HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla.className,
        disableAdd: props.disableAdd,
        disableRemoval
      }}
    />
  )
}

// Suorituksen vahvitus viewer

export type SuorituksenVahvistusViewProps<T extends Vahvistus> = CommonProps<
  FieldViewerProps<T | undefined, {}>
>

export const SuorituksenVahvistusView = <T extends Vahvistus>({
  value,
  ...rest
}: SuorituksenVahvistusViewProps<T>) => (
  <TestIdLayer id="suorituksenVahvistus.value">
    <SuorituksenVahvistus vahvistus={value} {...rest} />
  </TestIdLayer>
)

// Suorituksen vahvitus editor

export type SuorituksenVahvistusEditProps<T extends Vahvistus> = CommonProps<
  FieldEditorProps<
    T | undefined,
    {
      vahvistusClass: ClassOf<T>
      organisaatio?: Organisaatio
      disableAdd?: boolean
      disableRemoval?: boolean
    }
  >
>

export const SuorituksenVahvistusEdit = <T extends Vahvistus>({
  value,
  onChange,
  vahvistusClass,
  organisaatio,
  disableAdd,
  disableRemoval,
  ...rest
}: SuorituksenVahvistusEditProps<T>) => {
  const [modalVisible, setModalVisible] = useState(false)

  const onMerkitseValmiiksi = useCallback(() => {
    setModalVisible(true)
  }, [])

  const onMerkitseKeskeneräiseksi = useCallback(() => {
    onChange(undefined)
  }, [onChange])

  const onCancel = useCallback(() => setModalVisible(false), [])

  const onSubmit = useCallback(
    (vahvistus: T) => {
      onChange(vahvistus)
      setModalVisible(false)
    },
    [onChange]
  )

  return organisaatio ? (
    <TestIdLayer id="suorituksenVahvistus.edit">
      <SuorituksenVahvistus vahvistus={value} {...rest}>
        {value ? (
          <FlatButton
            onClick={onMerkitseKeskeneräiseksi}
            disabled={disableRemoval}
            testId="merkitseKeskeneräiseksi"
          >
            {'Merkitse keskeneräiseksi'}
          </FlatButton>
        ) : (
          <RaisedButton
            onClick={onMerkitseValmiiksi}
            disabled={disableAdd}
            testId="merkitseValmiiksi"
          >
            {'Merkitse valmiiksi'}
          </RaisedButton>
        )}
        {modalVisible && (
          <SuorituksenVahvistusModal
            organisaatio={organisaatio}
            vahvistusClass={vahvistusClass}
            onSubmit={onSubmit}
            onCancel={onCancel}
          />
        )}
      </SuorituksenVahvistus>
    </TestIdLayer>
  ) : null
}

type SuorituksenVahvistusProps = CommonPropsWithChildren<{
  vahvistus?: Vahvistus
}>

const SuorituksenVahvistus: React.FC<SuorituksenVahvistusProps> = (props) => {
  const { vahvistus } = props
  const myöntäjäHenkilöt = useMemo(
    () =>
      vahvistus && isHenkilövahvistus(vahvistus)
        ? vahvistus.myöntäjäHenkilöt.map(
            (h) => `${h.nimi}${t(h.titteli) ? ` (${t(h.titteli)})` : ''}`
          )
        : [],
    [vahvistus]
  )

  return (
    <div
      {...common(props, [
        'SuorituksenVahvistus',
        vahvistus && 'SuorituksenVahvistus--valmis'
      ])}
    >
      <div className="SuorituksenVahvistus__status">
        <TestIdText id="status">
          {vahvistus ? t('Suoritus valmis') : t('Suoritus kesken')}
        </TestIdText>
      </div>
      {vahvistus && (
        <>
          <div className="SuorituksenVahvistus__vahvistus">
            <TestIdText id="details">
              <Trans>{'Vahvistus'}</Trans>
              {': '}
              {ISO2FinnishDate(vahvistus.päivä)}{' '}
              {t(vahvistus.myöntäjäOrganisaatio.nimi)}
            </TestIdText>
          </div>
          <TestIdLayer id="henkilö">
            {myöntäjäHenkilöt.map((myöntäjä, i) => (
              <div key={i} className="SuorituksenVahvistus__myontaja">
                <TestIdText id={i}>{myöntäjä}</TestIdText>
              </div>
            ))}
          </TestIdLayer>
        </>
      )}
      {props.children}
    </div>
  )
}
