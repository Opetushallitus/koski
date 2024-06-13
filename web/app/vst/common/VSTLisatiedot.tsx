import { append, isNonEmpty } from 'fp-ts/lib/Array'
import React, { useCallback, useMemo } from 'react'
import { DateEdit } from '../../components-v2/controls/DateField'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import { FormModel, getValue } from '../../components-v2/forms/FormModel'
import { t } from '../../i18n/i18n'
import { Maksuttomuus } from '../../types/fi/oph/koski/schema/Maksuttomuus'
import { OikeuttaMaksuttomuuteenPidennetty } from '../../types/fi/oph/koski/schema/OikeuttaMaksuttomuuteenPidennetty'
import { VapaanSivistystyönOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeudenLisatiedot'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { deleteAt } from '../../util/fp/arrays'
import { TestIdLayer } from '../../appstate/useTestId'
import { ISO2FinnishDate } from '../../date/date'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { Finnish } from '../../types/fi/oph/koski/schema/Finnish'

interface VSTLisatiedotProps {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
}

export const VSTLisatiedot: React.FC<VSTLisatiedotProps> = ({ form }) => {
  const lisatiedotPath = form.root.prop('lisätiedot')
  const lisätiedot = getValue(lisatiedotPath)(form.state)
  const maksuttomuus = useMemo(
    () => lisätiedot?.maksuttomuus || [],
    [lisätiedot?.maksuttomuus]
  )
  const oikeuttaMaksuttomuuteenPidennetty = useMemo(
    () => lisätiedot?.oikeuttaMaksuttomuuteenPidennetty || [],
    [lisätiedot?.oikeuttaMaksuttomuuteenPidennetty]
  )

  const addKoulutuksenMaksuttomuus = useCallback(() => {
    const currentDate = new Date()
    const newMaksuttomuus = Maksuttomuus({
      alku: `${currentDate.getFullYear()}-01-01`,
      maksuton: false
    })
    form.updateAt(lisatiedotPath, (lisatiedot) => {
      if (lisatiedot === undefined) {
        return VapaanSivistystyönOpiskeluoikeudenLisätiedot({
          maksuttomuus: append(newMaksuttomuus)([])
        })
      }
      return {
        ...lisatiedot,
        maksuttomuus: append(newMaksuttomuus)(maksuttomuus)
      }
    })
  }, [form, lisatiedotPath, maksuttomuus])

  const addOikeuttaMaksuttomuuteenPidennetty = useCallback(() => {
    const currentDate = new Date()
    const newOikeuttaMaksuttomuuteen = OikeuttaMaksuttomuuteenPidennetty({
      alku: `${currentDate.getFullYear()}-01-01`,
      loppu: `${currentDate.getFullYear()}-12-30`
    })
    form.updateAt(lisatiedotPath, (lisatiedot) => {
      if (lisatiedot === undefined) {
        return VapaanSivistystyönOpiskeluoikeudenLisätiedot({
          oikeuttaMaksuttomuuteenPidennetty: append(newOikeuttaMaksuttomuuteen)(
            oikeuttaMaksuttomuuteenPidennetty
          )
        })
      }
      return {
        ...lisatiedot,
        oikeuttaMaksuttomuuteenPidennetty: append(newOikeuttaMaksuttomuuteen)(
          oikeuttaMaksuttomuuteenPidennetty
        )
      }
    })
  }, [form, lisatiedotPath, oikeuttaMaksuttomuuteenPidennetty])

  const maksuttomuusPath = lisatiedotPath
    .optional()
    .prop('maksuttomuus')
    .optional()
  const oikeuttaMaksuttomuuteenPath = lisatiedotPath
    .optional()
    .prop('oikeuttaMaksuttomuuteenPidennetty')
    .optional()

  const jotpaPath = lisatiedotPath.optional().prop('jotpaAsianumero').optional()

  return (
    <div className={'vst-lisatiedot'}>
      <div className="vst-lisatiedot__koulutuksen-maksuttomuus">
        <div>{t('Koulutuksen maksuttomuus')}</div>
        <div className="vst-lisatiedot__koulutuksen-maksuttomuus__inner-container">
          {isNonEmpty(maksuttomuus || []) &&
            maksuttomuus.map((m, i) => {
              return (
                <TestIdLayer id={`maksuttomuudet.${i}`} key={i}>
                  <div
                    key={`maksuttomuus_${i}`}
                    className="vst-lisatiedot__koulutuksen-maksuttomuus__maksuttomuus-row"
                  >
                    <div>
                      {form.editMode ? (
                        <DateEdit
                          onChange={(val) => {
                            form.updateAt(
                              maksuttomuusPath.at(i),
                              (_maksuttomuus) =>
                                Maksuttomuus({
                                  alku: val || '',
                                  maksuton: m.maksuton
                                })
                            )
                          }}
                          value={m.alku}
                        />
                      ) : (
                        <>{ISO2FinnishDate(m.alku)}</>
                      )}
                    </div>
                    <div className="vst-lisatiedot__koulutuksen-maksuttomuus__maksuttomuus-row-checkbox-container">
                      <label htmlFor={`maksuton-${i}`}>{t('Maksuton')}</label>
                      <input
                        type="checkbox"
                        disabled={!form.editMode}
                        id={`maksuton-${i}`}
                        checked={m.maksuton === true}
                        onChange={(_e) => {
                          form.updateAt(
                            lisatiedotPath
                              .optional()
                              .prop('maksuttomuus')
                              .optional()
                              .at(i),
                            // eslint-disable-next-line @typescript-eslint/no-shadow
                            (maksuttomuus) =>
                              Maksuttomuus({
                                alku: maksuttomuus.alku,
                                maksuton: !maksuttomuus.maksuton
                              })
                          )
                        }}
                      />
                    </div>
                    <div>
                      {form.editMode && (
                        <RaisedButton
                          type="dangerzone"
                          fullWidth={false}
                          testId="remove"
                          onClick={(e) => {
                            e.preventDefault()
                            form.updateAt(lisatiedotPath.optional(), (l) => ({
                              ...l,
                              maksuttomuus:
                                deleteAt(l?.maksuttomuus || [], i) || []
                            }))
                          }}
                        >
                          {t(`Poista`)}
                        </RaisedButton>
                      )}
                    </div>
                  </div>
                </TestIdLayer>
              )
            })}
          <div>
            {form.editMode && (
              <FlatButton
                fullWidth={false}
                onClick={(_e) => {
                  addKoulutuksenMaksuttomuus()
                }}
              >
                {t('lisatiedot:lisaa_uusi')}
              </FlatButton>
            )}
          </div>
        </div>
      </div>
      <div className="vst-lisatiedot__oikeutta-maksuttomuuteen-pidennetty">
        <div>{t('Oikeutta maksuttomuuteen pidennetty')}</div>
        <div className="vst-lisatiedot__oikeutta-maksuttomuuteen-pidennetty__inner-container">
          {isNonEmpty(oikeuttaMaksuttomuuteenPidennetty || []) &&
            oikeuttaMaksuttomuuteenPidennetty.map((m, i) => {
              return (
                <div
                  className="vst-lisatiedot__oikeutta-maksuttomuuteen-pidennetty__row"
                  key={`oikeuttamaksuttomuuteen_${i}`}
                >
                  <div>
                    <DateEdit
                      onChange={(val) => {
                        form.updateAt(oikeuttaMaksuttomuuteenPath.at(i), (om) =>
                          OikeuttaMaksuttomuuteenPidennetty({
                            alku: val || '',
                            loppu: om.loppu
                          })
                        )
                      }}
                      value={m.alku}
                    />
                  </div>
                  <div className="vst-lisatiedot__oikeutta-maksuttomuuteen-pidennetty__range-spacer">
                    {'-'}
                  </div>
                  <div>
                    <DateEdit
                      onChange={(val) => {
                        form.updateAt(oikeuttaMaksuttomuuteenPath.at(i), (om) =>
                          OikeuttaMaksuttomuuteenPidennetty({
                            alku: om.alku,
                            loppu: val || ''
                          })
                        )
                      }}
                      value={m.loppu}
                    />
                  </div>
                  <div>
                    {form.editMode && (
                      <RaisedButton
                        fullWidth={false}
                        type={'dangerzone'}
                        onClick={(e) => {
                          e.preventDefault()
                          form.updateAt(oikeuttaMaksuttomuuteenPath, (omp) =>
                            deleteAt(omp, i)
                          )
                        }}
                      >
                        {t(`Poista`)}
                      </RaisedButton>
                    )}
                  </div>
                </div>
              )
            })}
          <div>
            {form.editMode && (
              <FlatButton
                fullWidth={false}
                onClick={(e) => {
                  e.preventDefault()
                  // Lisää tyhjän oikeutta maksuttomuuteen pidennetty
                  addOikeuttaMaksuttomuuteenPidennetty()
                }}
              >
                {t('Lisää uusi')}
              </FlatButton>
            )}
          </div>
        </div>
      </div>
      <div className="vst-lisatiedot__jotpaasianumero">
        <div>{t('JOTPA asianumero')}</div>
        {form.editMode ? (
          <KoodistoSelect
            koodistoUri="jotpaasianumero"
            addNewText={t('Ei valintaa')}
            zeroValueOption
            onSelect={(koodiviite) => {
              form.updateAt(lisatiedotPath, (lisatiedot) => {
                return lisatiedot === undefined
                  ? VapaanSivistystyönOpiskeluoikeudenLisätiedot({
                      jotpaAsianumero: koodiviite
                    })
                  : { ...lisatiedot, jotpaAsianumero: koodiviite }
              })
            }}
            value={getValue(jotpaPath)(form.state)?.koodiarvo}
            testId="jotpaasianumero"
          />
        ) : (
          (getValue(jotpaPath)(form.state)?.nimi as Finnish | undefined)?.fi
        )}
      </div>
    </div>
  )
}
