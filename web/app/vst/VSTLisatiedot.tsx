import { append } from 'fp-ts/lib/Array'
import React, { useCallback } from 'react'
import { DateEdit } from '../components-v2/controls/DateField'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { FormModel, getValue } from '../components-v2/forms/FormModel'
import { t } from '../i18n/i18n'
import { Maksuttomuus } from '../types/fi/oph/koski/schema/Maksuttomuus'
import { OikeuttaMaksuttomuuteenPidennetty } from '../types/fi/oph/koski/schema/OikeuttaMaksuttomuuteenPidennetty'
import { VapaanSivistystyönOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeudenLisatiedot'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'

interface VSTLisatiedotProps {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
}

// TODO: Luo riveistä omat komponenttinsa
export const VSTLisatiedot: React.FC<VSTLisatiedotProps> = ({ form }) => {
  if (!form.editMode) {
    throw new Error('Oops')
  }
  const lisatiedotPath = form.root.prop('lisätiedot')
  const lisätiedot = getValue(lisatiedotPath)(form.state)

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
        maksuttomuus: append(newMaksuttomuus)(lisatiedot.maksuttomuus || [])
      }
    })
  }, [form, lisatiedotPath])
  const addOikeuttaMaksuttomuuteenPidennetty = useCallback(() => {
    const currentDate = new Date()
    const newOikeus = OikeuttaMaksuttomuuteenPidennetty({
      alku: `${currentDate.getFullYear()}-01-01`,
      loppu: `${currentDate.getFullYear()}-12-30`
    })
    form.updateAt(lisatiedotPath, (lisatiedot) => {
      if (lisatiedot === undefined) {
        return VapaanSivistystyönOpiskeluoikeudenLisätiedot({
          oikeuttaMaksuttomuuteenPidennetty: append(newOikeus)([])
        })
      }
      return {
        ...lisatiedot,
        oikeuttaMaksuttomuuteenPidennetty: append(newOikeus)(
          lisatiedot.oikeuttaMaksuttomuuteenPidennetty || []
        )
      }
    })
  }, [form, lisatiedotPath])

  return (
    <div className={'vst-lisatiedot'}>
      <div className="vst-lisatiedot__koulutuksen-maksuttomuus">
        <div>{t('Koulutuksen maksuttomuus')}</div>
        <div className="vst-lisatiedot__koulutuksen-maksuttomuus__inner-container">
          {lisätiedot?.maksuttomuus !== undefined &&
            lisätiedot?.maksuttomuus.length > 0 &&
            lisätiedot?.maksuttomuus.map((m, i) => {
              const maksuttomuusPath = lisatiedotPath
                .optional()
                .prop('maksuttomuus')
                .optional()
              return (
                <div
                  key={`maksuttomuus_${i}`}
                  className="vst-lisatiedot__koulutuksen-maksuttomuus__maksuttomuus-row"
                >
                  <div>
                    <DateEdit
                      onChange={(val) => {
                        form.updateAt(maksuttomuusPath.at(i), (_maksuttomuus) =>
                          Maksuttomuus({
                            alku: val || '',
                            maksuton: m.maksuton
                          })
                        )
                      }}
                      value={m.alku}
                    />
                  </div>
                  <div className="vst-lisatiedot__koulutuksen-maksuttomuus__maksuttomuus-row-checkbox-container">
                    <label htmlFor={`maksuton-${i}`}>{t('Maksuton')}</label>
                    <input
                      type="checkbox"
                      id={`maksuton-${i}`}
                      checked={m.maksuton === true}
                      onChange={(_e) => {
                        form.updateAt(
                          lisatiedotPath
                            .optional()
                            .prop('maksuttomuus')
                            .optional()
                            .at(i),
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
                    <RaisedButton
                      type="dangerzone"
                      fullWidth={false}
                      onClick={(e) => {
                        e.preventDefault()
                        form.updateAt(maksuttomuusPath, (maksuttomuudet) =>
                          maksuttomuudet.filter(
                            (_val, maksuttomuusIndex) => maksuttomuusIndex !== i
                          )
                        )
                      }}
                    >
                      {t('Poista')}
                    </RaisedButton>
                  </div>
                </div>
              )
            })}
          <div>
            <FlatButton
              fullWidth={false}
              onClick={(_e) => {
                // Lisää tyhjän koulutuksen maksuttomuus
                addKoulutuksenMaksuttomuus()
              }}
            >
              {t('lisatiedot:lisaa_uusi')}
            </FlatButton>
          </div>
        </div>
      </div>
      <div className="vst-lisatiedot__oikeutta-maksuttomuuteen-pidennetty">
        <div>{t('Oikeutta maksuttomuuteen pidennetty')}</div>
        <div className="vst-lisatiedot__oikeutta-maksuttomuuteen-pidennetty__inner-container">
          {lisätiedot?.oikeuttaMaksuttomuuteenPidennetty !== undefined &&
            lisätiedot?.oikeuttaMaksuttomuuteenPidennetty.length > 0 &&
            lisätiedot?.oikeuttaMaksuttomuuteenPidennetty.map((m, i) => {
              const oikeuttaMaksuttomuuteenPath = lisatiedotPath
                .optional()
                .prop('oikeuttaMaksuttomuuteenPidennetty')
                .optional()
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
                    <RaisedButton
                      fullWidth={false}
                      type={'dangerzone'}
                      onClick={(e) => {
                        e.preventDefault()
                        form.updateAt(oikeuttaMaksuttomuuteenPath, (omp) =>
                          omp.filter((_val, index) => index !== i)
                        )
                      }}
                    >
                      {t('Poista')}
                    </RaisedButton>
                  </div>
                </div>
              )
            })}
          <div>
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
          </div>
        </div>
      </div>
    </div>
  )
}
