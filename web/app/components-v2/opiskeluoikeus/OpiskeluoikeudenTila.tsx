import * as A from 'fp-ts/Array'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'
import { pipe } from 'fp-ts/lib/function'
import React, { useCallback, useMemo, useState } from 'react'
import { useChildClassNames } from '../../appstate/constraints'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { ISO2FinnishDate, addDaysISO } from '../../date/date'
import { t } from '../../i18n/i18n'
import { isAikuistenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeusjakso'
import { isAmmatillinenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeusjakso'
import { isDIAOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/DIAOpiskeluoikeusjakso'
import { isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'
import { isInternationalSchoolOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/InternationalSchoolOpiskeluoikeusjakso'
import { isLukionOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso } from '../../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenOpiskeluoikeudenJakso'
import { OpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/OpiskeluoikeudenTila'
import { Opiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/Opiskeluoikeusjakso'
import { isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
import { isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { isVapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { isTerminaalitila } from '../../util/opiskeluoikeus'
import {
  OpiskeluoikeusjaksoOf,
  OpiskeluoikeusjaksoOrd
} from '../../util/schema'
import { ClassOf } from '../../util/types'
import { CommonProps } from '../CommonProps'
import {
  KeyColumnedValuesRow,
  KeyValueTable
} from '../containers/KeyValueTable'
import { DateEdit } from '../controls/DateField'
import { IconButton } from '../controls/IconButton'
import { RaisedButton } from '../controls/RaisedButton'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { ValidationError, isValidationError } from '../forms/validator'
import { CHARCODE_REMOVE } from '../texts/Icon'
import {
  UusiOpiskeluoikeudenTilaModal,
  UusiOpiskeluoikeusjakso
} from './UusiOpiskeluoikeudenTilaModal'

type RahoituksellinenOpiskeluoikeusjakso = Extract<
  Opiskeluoikeusjakso,
  { opintojenRahoitus?: any }
>
function isRahoituksellinenOpiskeluoikeusjakso(
  x: Opiskeluoikeusjakso
): x is RahoituksellinenOpiskeluoikeusjakso {
  return (
    isAikuistenPerusopetuksenOpiskeluoikeusjakso(x) ||
    isAmmatillinenOpiskeluoikeusjakso(x) ||
    isDIAOpiskeluoikeusjakso(x) ||
    isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(x) ||
    isInternationalSchoolOpiskeluoikeusjakso(x) ||
    isLukionOpiskeluoikeusjakso(x) ||
    isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso(x) ||
    isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso(x) ||
    isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(x)
  )
}

// Opiskeluoikeuden tila viewer

export type OpiskeluoikeudenTilaViewProps<T extends OpiskeluoikeudenTila> =
  CommonProps<FieldViewerProps<T, {}>>

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
    <TestIdLayer id="tila.value">
      <KeyValueTable>
        <TestIdLayer id="items">
          {sortedJaksot.map((jakso, index) => (
            <TestIdLayer key={index} id={index}>
              <KeyColumnedValuesRow
                name={index === 0 ? 'Tila' : undefined}
                className={
                  index === 0 ? 'OpiskeluoikeudenTila-viimeisin' : undefined
                }
                columnSpans={{ default: [2, '*'], phone: [4, '*'] }}
              >
                {[
                  <time key={`jakso_time_${index}`} dateTime={jakso.alku}>
                    <TestIdText id="date">
                      {' '}
                      {ISO2FinnishDate(jakso.alku)}
                    </TestIdText>
                  </time>,
                  <React.Fragment key={`jakso_tila_lisatiedot_${index}`}>
                    <TestIdText id="tila">
                      <span>{t(jakso.tila.nimi)}</span>
                      <span>
                        {isVapaanSivistystyönOpiskeluoikeusjakso(jakso) &&
                          isRahoituksellinenOpiskeluoikeusjakso(jakso) &&
                          ` (${t(jakso.opintojenRahoitus?.nimi)})`}
                      </span>
                    </TestIdText>
                  </React.Fragment>
                ]}
              </KeyColumnedValuesRow>
            </TestIdLayer>
          ))}
        </TestIdLayer>
      </KeyValueTable>
    </TestIdLayer>
  )
}

// Opiskeluoikeuden tila editor
export type OpiskeluoikeudenTilaEditProps<T extends OpiskeluoikeudenTila> =
  CommonProps<
    FieldEditorProps<
      T,
      {
        enableValmistuminen: boolean
        createJakso: (
          form: UusiOpiskeluoikeusjakso<OpiskeluoikeusjaksoOf<T>>
        ) => OpiskeluoikeusjaksoOf<T> | NonEmptyArray<ValidationError>
        opiskeluoikeusJaksoClassName?: ClassOf<OpiskeluoikeusjaksoOf<T>>
      }
    >
  >

export const OpiskeluoikeudenTilaEdit = <T extends OpiskeluoikeudenTila>(
  props: OpiskeluoikeudenTilaEditProps<T>
) => {
  const oo = useOpiskeluoikeudenTilaState(props)

  return (
    <TestIdLayer id="tila.edit">
      <KeyValueTable>
        <TestIdLayer id="items">
          {oo.jaksot.map(({ jakso, index, min, max, isLatest }, arrIndex) => (
            <TestIdLayer key={`${index}_${arrIndex}`} id={index}>
              <KeyColumnedValuesRow
                name={arrIndex === 0 ? 'Tila' : undefined}
                className={
                  isLatest ? 'OpiskeluoikeudenTila-viimeisin' : undefined
                }
                columnSpans={{
                  default: [6, '*'],
                  small: [8, '*'],
                  phone: [24, '*']
                }}
              >
                {[
                  <DateEdit
                    key={`date_${index}_${arrIndex}`}
                    value={jakso.alku}
                    min={min}
                    max={max}
                    onChange={oo.onChangeDate(index)}
                  />,
                  <div key="jakso">
                    <TestIdText id="tila">{t(jakso.tila.nimi)}</TestIdText>
                    {isVapaanSivistystyönOpiskeluoikeusjakso(jakso) &&
                      isRahoituksellinenOpiskeluoikeusjakso(jakso) && (
                        <>
                          {' '}
                          <TestIdText id="rahoitus">
                            {'('}
                            {t(jakso.opintojenRahoitus?.nimi)}
                            {')'}
                          </TestIdText>
                        </>
                      )}
                    {isLatest && (
                      <IconButton
                        charCode={CHARCODE_REMOVE}
                        label={t('Poista')}
                        size="input"
                        onClick={oo.onRemoveLatest}
                        testId="remove"
                        key={`IconButton_${index}_${arrIndex}`}
                      />
                    )}
                  </div>
                ]}
              </KeyColumnedValuesRow>
            </TestIdLayer>
          ))}
        </TestIdLayer>

        {!oo.isTerminated && (
          <KeyColumnedValuesRow
            name={A.isEmpty(oo.jaksot) ? 'Tila' : undefined}
            columnSpans={{
              default: [6, '*'],
              small: [8, '*'],
              phone: [24, '*']
            }}
          >
            {[
              <RaisedButton
                key="RaisedButton"
                onClick={oo.openModal}
                testId="add"
              >
                {'Lisää uusi'}
              </RaisedButton>
            ]}
          </KeyColumnedValuesRow>
        )}
      </KeyValueTable>
      {oo.isModalVisible && props.value && oo.opiskeluoikeusjaksoClass && (
        <UusiOpiskeluoikeudenTilaModal
          onSubmit={oo.onAddNew}
          onClose={oo.closeModal}
          opiskeluoikeusjaksoClass={oo.opiskeluoikeusjaksoClass}
          enableValmistuminen={props.enableValmistuminen}
        />
      )}
      <FieldErrors errors={props.errors} />
    </TestIdLayer>
  )
}

// State

const useOpiskeluoikeudenTilaState = <T extends OpiskeluoikeudenTila>(
  props: OpiskeluoikeudenTilaEditProps<T>
) => {
  const [isModalVisible, setModalVisible] = useState(false)

  const onChangeDate = (index: number) => (value?: string | null) => {
    if (props.value && value) {
      const opiskeluoikeusjaksot = O.toUndefined(
        A.updateAt(index, {
          ...props.value.opiskeluoikeusjaksot[index],
          alku: value
        })(props.value.opiskeluoikeusjaksot)
      )
      if (opiskeluoikeusjaksot) {
        props.onChange({ ...props.value, opiskeluoikeusjaksot })
      }
    }
  }

  const { onChange, value, createJakso } = props

  const onRemoveLatest = useCallback(() => {
    if (value) {
      const opiskeluoikeusjaksot = value.opiskeluoikeusjaksot.slice(0, -1)
      onChange({ ...value, opiskeluoikeusjaksot })
    }
  }, [onChange, value])

  const jaksot = useMemo(() => {
    const opiskeluoikeusjaksot =
      props.value?.opiskeluoikeusjaksot || emptyOpiskeluoikeusjaksotArray
    return pipe(
      opiskeluoikeusjaksot,
      A.mapWithIndex((index, jakso) => ({
        jakso,
        index,
        min: nextDay(opiskeluoikeusjaksot[index - 1]?.alku),
        max: previousDay(opiskeluoikeusjaksot[index + 1]?.alku),
        isLatest: index === opiskeluoikeusjaksot.length - 1
      })),
      A.reverse
    )
  }, [props.value])

  const openModal = useCallback(() => {
    setModalVisible(true)
  }, [])

  const onAddNew = useCallback(
    (
      form: UusiOpiskeluoikeusjakso<OpiskeluoikeusjaksoOf<T>>
    ): NonEmptyArray<ValidationError> | undefined => {
      const result = createJakso(form)
      if (Array.isArray(result) && isValidationError(result[0])) {
        return result
      } else {
        if (value) {
          const opiskeluoikeusjaksot = [...value.opiskeluoikeusjaksot, result]
          onChange({ ...value, opiskeluoikeusjaksot })
          setModalVisible(false)
        }
      }
    },
    [createJakso, onChange, value]
  )

  const closeModal = useCallback(() => {
    setModalVisible(false)
  }, [])

  // Joissain tapauksissa opiskeluoikeusjaksolla on useampi luokkanimi.
  // Näille tapauksille käytetään komponentista syötettävää resolveria, jolla määritellään käytettävä luokka manuaalisesti.
  // Ennen kuin Scala-Scheman OnlyWhen ja NotWhen saadaan uuteen käliin mukaan linsseillä, niin tämä toteutus on väliaikainen.
  const opiskeluoikeusjaksoClassNames = useChildClassNames<
    OpiskeluoikeusjaksoOf<T>
  >(props.value?.$class, 'opiskeluoikeusjaksot.[]')

  if (
    props.opiskeluoikeusJaksoClassName === undefined &&
    opiskeluoikeusjaksoClassNames &&
    opiskeluoikeusjaksoClassNames.length > 1
  ) {
    throw new Error(
      "More than one possible className detected and no className resolver was supplied. To suppress this error, please use 'classNameResolver' property in your data model."
    )
  }

  if (
    props.opiskeluoikeusJaksoClassName !== undefined &&
    opiskeluoikeusjaksoClassNames &&
    opiskeluoikeusjaksoClassNames.length > 1 &&
    !opiskeluoikeusjaksoClassNames.includes(props.opiskeluoikeusJaksoClassName)
  ) {
    throw new Error(
      `opiskeluoikeusjaksoClassNames does not include ${
        props.opiskeluoikeusJaksoClassName
      }. Valid options are: ${opiskeluoikeusjaksoClassNames.join(', ')}`
    )
  }

  const opiskeluoikeusjaksoClass =
    opiskeluoikeusjaksoClassNames !== null
      ? opiskeluoikeusjaksoClassNames.length === 1
        ? opiskeluoikeusjaksoClassNames[0]
        : props.opiskeluoikeusJaksoClassName !== undefined
        ? props.opiskeluoikeusJaksoClassName
        : null
      : null

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

  return {
    jaksot,
    isTerminated,
    opiskeluoikeusjaksoClass,
    isModalVisible,
    onChangeDate,
    openModal,
    closeModal,
    onAddNew,
    onRemoveLatest
  }
}

// Utils

const emptyOpiskeluoikeusjaksotArray: Opiskeluoikeusjakso[] = []

const nextDay = addDaysISO(1)
const previousDay = addDaysISO(-1)
