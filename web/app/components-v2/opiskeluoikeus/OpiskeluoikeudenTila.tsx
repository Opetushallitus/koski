import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import React, { useCallback, useMemo, useState } from 'react'
import { useChildClassName } from '../../appstate/constraints'
import { addDaysISO, ISO2FinnishDate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { OpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/OpiskeluoikeudenTila'
import { Opiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/Opiskeluoikeusjakso'
import { isTerminaalitila } from '../../util/opiskeluoikeus'
import {
  OpiskeluoikeusjaksoOf,
  OpiskeluoikeusjaksoOrd
} from '../../util/schema'
import { CommonProps } from '../CommonProps'
import {
  KeyColumnedValuesRow,
  KeyValueRow,
  KeyValueTable
} from '../containers/KeyValueTable'
import { DateEdit } from '../controls/DateField'
import { FlatButton } from '../controls/FlatButton'
import { IconButton } from '../controls/IconButton'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'
import { isValidationError, ValidationError } from '../forms/validator'
import { CHARCODE_REMOVE } from '../texts/Icon'
import {
  OpiskeluoikeusjaksoForm,
  UusiOpiskeluoikeudenTilaModal
} from './UusiOpiskeluoikeudenTilaModal'

export type OpiskeluoikeudenTilaViewProps<T extends OpiskeluoikeudenTila> =
  CommonProps<FieldViewBaseProps<T>>

export const OpiskeluoikeudenTilaView = <T extends OpiskeluoikeudenTila>(
  props: OpiskeluoikeudenTilaViewProps<T>
) => {
  const sortedJaksot = useMemo(
    () =>
      pipe(
        props.value?.opiskeluoikeusjaksot || emptyOpiskeluoikeusjaksotArray,
        A.sort(Ord.reverse(OpiskeluoikeusjaksoOrd))
      ),
    [props.value]
  )

  return (
    <KeyValueTable>
      {sortedJaksot.map((jakso, index) => (
        <KeyColumnedValuesRow
          name={index === 0 ? 'Tila' : undefined}
          key={index}
          className={index === 0 ? 'OpiskeluoikeudenTila-viimeisin' : undefined}
          columnSpans={[2, '*']}
        >
          {[ISO2FinnishDate(jakso.alku), t(jakso.tila.nimi)]}
        </KeyColumnedValuesRow>
      ))}
    </KeyValueTable>
  )
}

export type OpiskeluoikeudenTilaEditProps<T extends OpiskeluoikeudenTila> =
  CommonProps<
    FieldEditBaseProps<
      T,
      {
        createJakso: (
          form: OpiskeluoikeusjaksoForm<OpiskeluoikeusjaksoOf<T>>
        ) => OpiskeluoikeusjaksoOf<T> | NonEmptyArray<ValidationError>
      }
    >
  >

export const OpiskeluoikeudenTilaEdit = <T extends OpiskeluoikeudenTila>(
  props: OpiskeluoikeudenTilaEditProps<T>
) => {
  const [isModalVisible, setModalVisible] = useState(false)

  const onChangeDate = (index: number) => (value?: string | null) => {
    if (props.value && value) {
      const opiskeluoikeusjaksot = O.toUndefined(
        A.updateAt(index, {
          ...props.value.opiskeluoikeusjaksot[index],
          alku: value!
        })(props.value.opiskeluoikeusjaksot)
      )
      if (opiskeluoikeusjaksot) {
        props.onChange({ ...props.value, opiskeluoikeusjaksot })
      }
    }
  }

  const onRemoveLatest = useCallback(() => {
    if (props.value) {
      const opiskeluoikeusjaksot = props.value.opiskeluoikeusjaksot.slice(0, -1)
      props.onChange({ ...props.value, opiskeluoikeusjaksot })
    }
  }, [props.value, props.onChange])

  const jaksot = useMemo(() => {
    const jaksot =
      props.value?.opiskeluoikeusjaksot || emptyOpiskeluoikeusjaksotArray
    return pipe(
      jaksot,
      A.mapWithIndex((index, jakso) => ({
        jakso,
        index,
        min: nextDay(jaksot[index - 1]?.alku),
        max: previousDay(jaksot[index + 1]?.alku),
        isLatest: index === jaksot.length - 1
      })),
      A.reverse
    )
  }, [props.value])

  const openModal = useCallback(() => {
    setModalVisible(true)
  }, [])

  const onAddNew = useCallback(
    (
      form: OpiskeluoikeusjaksoForm<OpiskeluoikeusjaksoOf<T>>
    ): NonEmptyArray<ValidationError> | undefined => {
      const result = props.createJakso(form)
      if (Array.isArray(result) && isValidationError(result[0])) {
        return result
      } else {
        if (props.value) {
          const opiskeluoikeusjaksot = [
            ...props.value.opiskeluoikeusjaksot,
            result
          ]
          props.onChange({ ...props.value, opiskeluoikeusjaksot })
          setModalVisible(false)
        }
      }
    },
    [props.value, props.onChange]
  )

  const closeModal = useCallback(() => {
    setModalVisible(false)
  }, [])

  const opiskeluoikeusjaksoClass = useChildClassName<OpiskeluoikeusjaksoOf<T>>(
    props.value?.$class,
    'opiskeluoikeusjaksot.[]'
  )

  const isTerminated = useMemo(
    () =>
      pipe(
        jaksot,
        A.head,
        O.map((jakso) => isTerminaalitila(jakso.jakso.tila)),
        O.getOrElse(() => false)
      ),
    [jaksot]
  )

  return (
    <>
      <KeyValueTable>
        {jaksot.map(({ jakso, index, min, max, isLatest }, arrIndex) => (
          <KeyColumnedValuesRow
            name={arrIndex === 0 ? 'Tila' : undefined}
            className={isLatest ? 'OpiskeluoikeudenTila-viimeisin' : undefined}
            columnSpans={[6, '*']}
            key={index}
          >
            {[
              <DateEdit
                key="date"
                value={jakso.alku}
                min={min}
                max={max}
                onChange={onChangeDate(index)}
              />,
              <div key="jakso">
                {t(jakso.tila.nimi)} {/* TODO Lisää rahoitusmuoto */}
                {isLatest && (
                  <IconButton
                    charCode={CHARCODE_REMOVE}
                    label={t('Poista')}
                    size="input"
                    onClick={onRemoveLatest}
                  />
                )}
              </div>
            ]}
          </KeyColumnedValuesRow>
        ))}
        {!isTerminated && (
          <KeyValueRow name={A.isEmpty(jaksot) ? 'Tila' : undefined}>
            <FlatButton onClick={openModal}>Lisää uusi</FlatButton>
          </KeyValueRow>
        )}
      </KeyValueTable>
      {isModalVisible && props.value && opiskeluoikeusjaksoClass && (
        <UusiOpiskeluoikeudenTilaModal
          onSubmit={onAddNew}
          onClose={closeModal}
          opiskeluoikeusjaksoClass={opiskeluoikeusjaksoClass}
        />
      )}
    </>
  )
}

// Utils

const emptyOpiskeluoikeusjaksotArray: Opiskeluoikeusjakso[] = []

const nextDay = addDaysISO(1)
const previousDay = addDaysISO(-1)
