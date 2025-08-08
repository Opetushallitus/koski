import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import { Näyttö } from '../types/fi/oph/koski/schema/Naytto'
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
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import { IconButton } from '../components-v2/controls/IconButton'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import React from 'react'
import { NäytönArviointi } from '../types/fi/oph/koski/schema/NaytonArviointi'
import { ISO2FinnishDate, todayISODate } from '../date/date'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { NäytönArvioitsija } from '../types/fi/oph/koski/schema/NaytonArvioitsija'
import { CommonProps } from '../components-v2/CommonProps'
import { NäytönSuorituspaikka } from '../types/fi/oph/koski/schema/NaytonSuorituspaikka'
import { AmisArvosanaSelect } from './Arviointi'

export const NäyttöView = ({
  value
}: CommonProps<FieldViewerProps<Näyttö, EmptyObject>>) => {
  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Kuvaus">{t(value?.kuvaus)}</KeyValueRow>
      <KeyValueRow localizableLabel={'Suorituspaikka'}>
        {t(value?.suorituspaikka?.tunniste.nimi)}
        {': '}
        {t(value?.suorituspaikka?.kuvaus)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Suoritusaika">
        {value?.suoritusaika?.alku && ISO2FinnishDate(value.suoritusaika.alku)}
        {' - '}
        {value?.suoritusaika?.loppu &&
          ISO2FinnishDate(value.suoritusaika.loppu)}
      </KeyValueRow>
      <NäytönArviointiView value={value?.arviointi} />
    </KeyValueTable>
  )
}
const emptyNäyttö: Näyttö = Näyttö({})
const emptySuoritusaika: NäytönSuoritusaika = NäytönSuoritusaika({
  alku: todayISODate(),
  loppu: todayISODate()
})
const emptySuoritusPaikka: NäytönSuorituspaikka = NäytönSuorituspaikka({
  tunniste: Koodistokoodiviite({
    koodistoUri: 'ammatillisennaytonsuorituspaikka',
    koodiarvo: 'työpaikka'
  }),
  kuvaus: localize('')
})
export const NäyttöEdit = ({
  value,
  onChange
}: FieldEditorProps<Näyttö, EmptyObject>) => {
  if (value === undefined) {
    return (
      <ButtonGroup>
        <FlatButton onClick={() => onChange(emptyNäyttö)}>
          {t('Lisää ammattiosaamisen näyttö')}
        </FlatButton>
      </ButtonGroup>
    )
  }

  return (
    <>
      <KeyValueTable>
        <KeyValueRow localizableLabel="Kuvaus">
          <MultilineTextEdit
            value={t(value?.kuvaus)}
            onChange={(kuvaus) =>
              kuvaus &&
              onChange({ ...emptyNäyttö, ...value, kuvaus: localize(kuvaus) })
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
                ...emptyNäyttö,
                ...value,
                suorituspaikka: {
                  ...emptySuoritusPaikka,
                  ...value?.suorituspaikka,
                  tunniste
                }
              })
            }
            testId={'suorituspaikka'}
          />
          <LocalizedTextEdit
            value={value?.suorituspaikka?.kuvaus}
            onChange={(kuvaus) =>
              kuvaus &&
              onChange({
                ...emptyNäyttö,
                ...value,
                suorituspaikka: {
                  ...emptySuoritusPaikka,
                  ...value?.suorituspaikka,
                  kuvaus
                }
              })
            }
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel="Suoritusaika">
          <div className="AikajaksoEdit">
            <DateInput
              value={value?.suoritusaika?.alku}
              onChange={(alku?: string) => {
                alku &&
                  onChange({
                    ...emptyNäyttö,
                    ...value,
                    suoritusaika: NäytönSuoritusaika({
                      ...emptySuoritusaika,
                      ...value?.suoritusaika,
                      alku
                    })
                  })
              }}
              testId="alku"
            />
            <span className="AikajaksoEdit__separator"> {' - '}</span>
            <DateInput
              value={value?.suoritusaika?.loppu}
              onChange={(loppu?: string) => {
                loppu &&
                  onChange({
                    ...emptyNäyttö,
                    ...value,
                    suoritusaika: NäytönSuoritusaika({
                      ...emptySuoritusaika,
                      ...value?.suoritusaika,
                      loppu
                    })
                  })
              }}
              testId="loppu"
            />
          </div>
        </KeyValueRow>
        <NäytönArviointiEdit
          value={value?.arviointi}
          onChange={(arviointi) =>
            arviointi && onChange({ ...emptyNäyttö, ...value, arviointi })
          }
        />
      </KeyValueTable>
      <IconButton
        charCode={CHARCODE_REMOVE}
        label={t('Poista')}
        size="input"
        onClick={() => onChange(undefined)}
        testId="delete"
      />
    </>
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
  onChange
}: FieldEditorProps<NäytönArviointi, EmptyObject>) => {
  return (
    <>
      <KeyValueRow localizableLabel="Arvosana">
        <AmisArvosanaSelect
          value={value?.arvosana}
          onChange={(arvosana) =>
            arvosana &&
            onChange({ ...emptyNäytönArviointi, ...value, arvosana })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointipäivä">
        <DateInput
          value={value?.päivä}
          onChange={(päivä) =>
            päivä && onChange({ ...emptyNäytönArviointi, ...value, päivä })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioijat">
        {value?.arvioitsijat?.map((a, index) => (
          <>
            <TextEdit
              value={a.nimi}
              onChange={(nimi) =>
                nimi &&
                value.arvioitsijat &&
                onChange({
                  ...emptyNäytönArviointi,
                  ...value,
                  arvioitsijat: [
                    ...value.arvioitsijat.slice(0, index),
                    NäytönArvioitsija({
                      nimi,
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
          </>
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
          <div className={'AikajaksoEdit'}>
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
