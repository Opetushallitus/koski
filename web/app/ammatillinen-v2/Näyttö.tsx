import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import { EmptyObject } from '../util/objects'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { localize, t } from '../i18n/i18n'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import {
  MultilineTextEdit,
  TextEdit
} from '../components-v2/controls/TextField'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { LocalizedTextEdit } from '../components-v2/controls/LocalizedTestField'
import { DateInput } from '../components-v2/controls/DateInput'
import { NäytönSuoritusaika } from '../types/fi/oph/koski/schema/NaytonSuoritusaika'
import { BooleanEdit } from '../components-v2/opiskeluoikeus/BooleanField'
import { IconButton } from '../components-v2/controls/IconButton'
import { Removable } from '../components-v2/controls/Removable'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import React from 'react'
import { NäytönArviointi } from '../types/fi/oph/koski/schema/NaytonArviointi'
import { ISO2FinnishDate, todayISODate } from '../date/date'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { NäytönArvioitsija } from '../types/fi/oph/koski/schema/NaytonArvioitsija'
import { CommonProps } from '../components-v2/CommonProps'
import { NäytönSuorituspaikka } from '../types/fi/oph/koski/schema/NaytonSuorituspaikka'
import { AmisArvosanaSelect } from './Arviointi'
import { NäyttöAmmatillinenOsittainen } from '../types/fi/oph/koski/schema/NayttoAmmatillinenOsittainen'
import { Näyttö } from '../types/fi/oph/koski/schema/Naytto'
import { hasErrorInField } from '../components-v2/forms/validator'

type AnyNäyttö = Näyttö | NäyttöAmmatillinenOsittainen

const EmptySuoritusaika = (): NäytönSuoritusaika =>
  NäytönSuoritusaika({
    alku: todayISODate(),
    loppu: todayISODate()
  })

// Suoritusaika on valinnainen, joten molempien päivien tyhjentäminen poistaa
// sen. Yksin tyhjennetty päivä jää tyhjäksi ja näkyy virheenä.
const suoritusaikaTaiTyhjä = (
  suoritusaika: NäytönSuoritusaika
): NäytönSuoritusaika | undefined =>
  suoritusaika.alku === '' && suoritusaika.loppu === ''
    ? undefined
    : suoritusaika

const EmptySuoritusPaikka = (): NäytönSuorituspaikka =>
  NäytönSuorituspaikka({
    tunniste: Koodistokoodiviite({
      koodistoUri: 'ammatillisennaytonsuorituspaikka',
      koodiarvo: 'työpaikka'
    }),
    kuvaus: localize('')
  })
type NäyttöEditBaseProps<T extends AnyNäyttö> = FieldEditorProps<
  T,
  EmptyObject
> & {
  EmptyNäyttö: () => T
}

export const NäyttöEdit = (props: FieldEditorProps<Näyttö, EmptyObject>) => (
  <NäyttöEditBase {...props} EmptyNäyttö={Näyttö} />
)

export const NäyttöAmmatillinenOsittainenEdit = (
  props: FieldEditorProps<NäyttöAmmatillinenOsittainen, EmptyObject>
) => <NäyttöEditBase {...props} EmptyNäyttö={NäyttöAmmatillinenOsittainen} />

export const NäyttöAmmatillinenOsittainenView = ({
  value
}: CommonProps<FieldViewerProps<NäyttöAmmatillinenOsittainen, EmptyObject>>) =>
  NäyttöViewBase(value)

const NäyttöViewBase = (value?: AnyNäyttö) => (
  <KeyValueTable>
    <KeyValueRow localizableLabel="Kuvaus">
      <span style={{ whiteSpace: 'pre-line' }}>{t(value?.kuvaus)}</span>
    </KeyValueRow>
    <KeyValueRow localizableLabel={'Suorituspaikka'}>
      {t(value?.suorituspaikka?.tunniste.nimi)}
      {': '}
      {t(value?.suorituspaikka?.kuvaus)}
    </KeyValueRow>
    <KeyValueRow
      localizableLabel="Suoritusaika"
      hideIfEmpty={value?.suoritusaika}
    >
      {value?.suoritusaika?.alku && ISO2FinnishDate(value.suoritusaika.alku)}
      {' — '}
      {value?.suoritusaika?.loppu && ISO2FinnishDate(value.suoritusaika.loppu)}
    </KeyValueRow>
    <NäytönArviointiView value={value?.arviointi} />
  </KeyValueTable>
)

export const NäyttöView = ({
  value
}: CommonProps<FieldViewerProps<Näyttö, EmptyObject>>) => NäyttöViewBase(value)

const NäyttöEditBase = <T extends AnyNäyttö>({
  value,
  onChange,
  errors,
  EmptyNäyttö
}: NäyttöEditBaseProps<T>) => {
  if (value === undefined) {
    return (
      <ButtonGroup>
        <FlatButton onClick={() => onChange(EmptyNäyttö())}>
          {t('Lisää ammattiosaamisen näyttö')}
        </FlatButton>
      </ButtonGroup>
    )
  }

  return (
    <Removable onClick={() => onChange(undefined)}>
      <KeyValueTable>
        <KeyValueRow localizableLabel="Kuvaus">
          <MultilineTextEdit
            value={t(value?.kuvaus)}
            testId="nayton-kuvaus"
            onChange={(kuvaus) =>
              onChange({
                ...value,
                kuvaus: kuvaus ? localize(kuvaus) : undefined
              })
            }
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel={'Suorituspaikka'}>
          <KoodistoSelect
            value={value?.suorituspaikka?.tunniste.koodiarvo}
            koodistoUri={'ammatillisennaytonsuorituspaikka'}
            onSelect={(tunniste) =>
              tunniste &&
              onChange({
                ...value,
                suorituspaikka: {
                  ...(value?.suorituspaikka ?? EmptySuoritusPaikka()),
                  tunniste
                }
              })
            }
            testId={'suorituspaikka'}
          />
          <LocalizedTextEdit
            value={value?.suorituspaikka?.kuvaus}
            placeholder={t('Kuvaus')}
            hasErrors={hasErrorInField(errors, 'suorituspaikka.kuvaus')}
            onChange={(kuvaus) =>
              onChange({
                ...value,
                suorituspaikka: {
                  ...(value?.suorituspaikka ?? EmptySuoritusPaikka()),
                  kuvaus: kuvaus ?? localize('')
                }
              })
            }
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel="Suoritusaika">
          <div className="AikajaksoEdit">
            <DateInput
              value={value?.suoritusaika?.alku}
              hasErrors={hasErrorInField(errors, 'suoritusaika.alku')}
              onChange={(alku?: string, rawAlku?: string) =>
                onChange({
                  ...value,
                  suoritusaika: suoritusaikaTaiTyhjä({
                    ...(value?.suoritusaika ?? EmptySuoritusaika()),
                    alku: alku ?? rawAlku ?? ''
                  })
                })
              }
              testId="alku"
            />
            <span className="AikajaksoEdit__separator"> {' — '}</span>
            <DateInput
              value={value?.suoritusaika?.loppu}
              hasErrors={hasErrorInField(errors, 'suoritusaika.loppu')}
              onChange={(loppu?: string, rawLoppu?: string) =>
                onChange({
                  ...value,
                  suoritusaika: suoritusaikaTaiTyhjä({
                    ...(value?.suoritusaika ?? EmptySuoritusaika()),
                    loppu: loppu ?? rawLoppu ?? ''
                  })
                })
              }
              testId="loppu"
            />
          </div>
        </KeyValueRow>
        <NäytönArviointiEdit
          value={value?.arviointi}
          errors={errors}
          onChange={(arviointi) =>
            arviointi && onChange({ ...value, arviointi })
          }
        />
      </KeyValueTable>
    </Removable>
  )
}
const NäytönArviointiView = ({
  value
}: CommonProps<FieldViewerProps<NäytönArviointi, EmptyObject>>) => {
  return (
    <>
      <KeyValueRow localizableLabel="Arvosana">
        {t(value?.arvosana.nimi)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointipäivä">
        {ISO2FinnishDate(value?.päivä)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioijat">
        {value?.arvioitsijat
          ?.map(
            (a) => a.nimi + (a.ntm ? ` (${t('näyttötutkintomestari')})` : '')
          )
          .join(', ')}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioinnista päättäneet">
        {value?.arvioinnistaPäättäneet?.map((a) => t(a.nimi)).join(', ')}
      </KeyValueRow>
    </>
  )
}
const emptyNäytönArviointi: NäytönArviointi = NäytönArviointi({
  päivä: todayISODate(),
  arvosana: Koodistokoodiviite({
    koodistoUri: 'arviointiasteikkoammatillinenhyvaksyttyhylatty',
    koodiarvo: 'Hyväksytty'
  })
})
const NäytönArviointiEdit = ({
  value,
  onChange,
  errors
}: FieldEditorProps<NäytönArviointi, EmptyObject>) => {
  return (
    <>
      <KeyValueRow localizableLabel="Arvosana">
        <AmisArvosanaSelect
          value={value?.arvosana}
          onChange={(arvosana) =>
            arvosana &&
            onChange({ ...(value ?? emptyNäytönArviointi), arvosana })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointipäivä">
        <DateInput
          value={value?.päivä}
          hasErrors={hasErrorInField(errors, 'arviointi.päivä')}
          onChange={(päivä, rawPäivä) =>
            onChange({
              ...(value ?? emptyNäytönArviointi),
              päivä: päivä ?? rawPäivä ?? ''
            })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioijat">
        {value?.arvioitsijat?.map((a, index) => (
          <div key={`nayton.arvioijat.${index}`}>
            <TextEdit
              value={a.nimi}
              hasErrors={hasErrorInField(errors, `arvioitsijat.${index}.nimi`)}
              onChange={(nimi) =>
                value.arvioitsijat &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arvioitsijat: [
                    ...value.arvioitsijat.slice(0, index),
                    NäytönArvioitsija({
                      nimi: nimi ?? '',
                      ntm: value.arvioitsijat[index].ntm
                    }),
                    ...value.arvioitsijat.slice(index + 1)
                  ]
                })
              }
            />
            <BooleanEdit
              label={t('Näyttötutkintomestari')}
              onChange={(ntm) =>
                ntm !== undefined &&
                value.arvioitsijat &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arvioitsijat: [
                    ...value.arvioitsijat.slice(0, index),
                    NäytönArvioitsija({
                      ntm,
                      nimi: value.arvioitsijat[index].nimi
                    }),
                    ...value.arvioitsijat.slice(index + 1)
                  ]
                })
              }
              value={a.ntm}
            />
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={() =>
                value.arvioitsijat &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arvioitsijat: [
                    ...value.arvioitsijat.slice(0, index),
                    ...value.arvioitsijat.slice(index + 1)
                  ]
                })
              }
              testId="delete"
            />
          </div>
        ))}
        <ButtonGroup>
          <FlatButton
            onClick={() =>
              onChange({
                ...emptyNäytönArviointi,
                ...value,
                arvioitsijat: [
                  ...(value?.arvioitsijat || []),
                  NäytönArvioitsija({ nimi: '', ntm: false })
                ]
              })
            }
          >
            {t('Lisää')}
          </FlatButton>
        </ButtonGroup>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioinnista päättäneet">
        {value?.arvioinnistaPäättäneet?.map((a, index) => (
          <div
            className={'AikajaksoEdit'}
            key={`nayton.arvioinnista.paattaneet.${a.nimi}`}
          >
            <KoodistoSelect
              koodistoUri={'ammatillisennaytonarvioinnistapaattaneet'}
              value={a.koodiarvo}
              onSelect={(val) =>
                val &&
                value.arvioinnistaPäättäneet &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arvioinnistaPäättäneet: [
                    ...value.arvioinnistaPäättäneet.slice(0, index),
                    val,
                    ...value.arvioinnistaPäättäneet.slice(index + 1)
                  ]
                })
              }
              testId={'ammatillisennaytonarvioinnistapaattaneet'}
            />
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={() =>
                value.arvioinnistaPäättäneet &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arvioinnistaPäättäneet: [
                    ...value.arvioinnistaPäättäneet.slice(0, index),
                    ...value.arvioinnistaPäättäneet.slice(index + 1)
                  ]
                })
              }
              testId="delete"
            />
          </div>
        ))}
        <KoodistoSelect
          koodistoUri={'ammatillisennaytonarvioinnistapaattaneet'}
          zeroValueOption
          onSelect={(val) =>
            val &&
            onChange({
              ...emptyNäytönArviointi,
              ...value,
              arvioinnistaPäättäneet: [
                ...(value?.arvioinnistaPäättäneet || []),
                val
              ]
            })
          }
          testId={'ammatillisennaytonarvioinnistapaattaneet-uusi'}
        />
      </KeyValueRow>
    </>
  )
}
