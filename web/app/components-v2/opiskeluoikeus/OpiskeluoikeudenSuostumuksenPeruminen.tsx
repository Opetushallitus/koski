import React, { useCallback, useRef, useState } from 'react'
import {
  createPreferLocalCache,
  useApiWithParams,
  useOnApiSuccess
} from '../../api-fetch'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Suoritus } from '../../types/fi/oph/koski/schema/Suoritus'
import { fetchSuoritusjakoTehty, peruutaSuostumus } from '../../util/koskiApi'
import { getOpiskeluoikeusOid } from '../../util/opiskeluoikeus'
import { common, CommonProps } from '../CommonProps'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { Checkbox } from '../controls/Checkbox'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { Trans } from '../texts/Trans'

export type OpiskeluoikeudenSuostumuksenPeruminenProps = CommonProps<{
  opiskeluoikeus: Opiskeluoikeus
}>

export const OpiskeluoikeudenSuostumuksenPeruminen: React.FC<
  OpiskeluoikeudenSuostumuksenPeruminenProps
> = ({ opiskeluoikeus, ...rest }) => (
  <SuostumuksenPeruminen
    {...common(rest, ['OpiskeluoikeudenSuostumuksenPeruminen'])}
    opiskeluoikeusOid={getOpiskeluoikeusOid(opiskeluoikeus)}
    nimi={t(opiskeluoikeus.tyyppi.nimi)}
    text="Tämän opiskeluoikeuden tiedot näytetään antamasi suostumuksen perusteella."
  />
)

export type PäätasonSuorituksenSuostumuksenPeruminenProps = CommonProps<{
  opiskeluoikeus: Opiskeluoikeus
  suoritus: Suoritus
}>

export const PäätasonSuorituksenSuostumuksenPeruminen: React.FC<
  PäätasonSuorituksenSuostumuksenPeruminenProps
> = ({ opiskeluoikeus, suoritus, ...rest }) => (
  <SuostumuksenPeruminen
    {...common(rest, ['PäätasonSuorituksenSuostumuksenPeruminen'])}
    opiskeluoikeusOid={getOpiskeluoikeusOid(opiskeluoikeus)}
    suorituksenTyyppi={suoritus.tyyppi}
    nimi={t(suoritus.tyyppi.nimi)}
    text="Tämän suorituksen tiedot näytetään antamasi suostumuksen perusteella."
  />
)

export type SuostumuksenPeruminenProps = CommonProps<{
  opiskeluoikeusOid?: string
  suorituksenTyyppi?: Koodistokoodiviite<'suorituksentyyppi'>
  nimi: string
  text: string
}>

const suoritusjakoTehtyCache = createPreferLocalCache(fetchSuoritusjakoTehty)

const SuostumuksenPeruminen: React.FC<SuostumuksenPeruminenProps> = (props) => {
  const peruutaSuostumusBtn = useRef<HTMLButtonElement>(null)
  const [peruuttamassaSuostumusta, setPeruuttamassaSuostumusta] =
    useState(false)
  const [suostumuksenPerumisenInfo, setSuostumuksenPerumisenInfo] =
    useState(false)
  const [suoritusjakoTehty, setSuoritusjakoTehty] = useState(true)

  const suoritusjaonTekemisenHaku = useApiWithParams(
    fetchSuoritusjakoTehty,
    props.opiskeluoikeusOid !== undefined
      ? [props.opiskeluoikeusOid, props.suorituksenTyyppi?.koodiarvo]
      : undefined,
    suoritusjakoTehtyCache
  )

  useOnApiSuccess(suoritusjaonTekemisenHaku, (response) =>
    setSuoritusjakoTehty(response.data.tehty)
  )

  const dismissPopup = useCallback(() => {
    setPeruuttamassaSuostumusta(false)
    setTimeout(() => peruutaSuostumusBtn.current?.focus(), 0)
  }, [])

  return (
    <div {...common(props, ['SuostumuksenPeruminen'])}>
      <b>
        <Trans>{props.text}</Trans>
      </b>
      <span className="infobox">
        <button
          className="info-icon"
          onClick={() =>
            setSuostumuksenPerumisenInfo(!suostumuksenPerumisenInfo)
          }
          onMouseEnter={() => setSuostumuksenPerumisenInfo(true)}
          onMouseLeave={() => setSuostumuksenPerumisenInfo(false)}
          onKeyDown={() =>
            setSuostumuksenPerumisenInfo(!suostumuksenPerumisenInfo)
          }
          onFocus={() => setSuostumuksenPerumisenInfo(true)}
          onBlur={() => setSuostumuksenPerumisenInfo(false)}
          aria-label={t('tooltip:Suostumuksen selitys')}
        />
      </span>
      {!suoritusjakoTehty && (
        <FlatButton
          className="peru-suostumus-linkki"
          onClick={() => setPeruuttamassaSuostumusta(!peruuttamassaSuostumusta)}
          compact
          buttonRef={peruutaSuostumusBtn}
        >
          {'Peruuta suostumus'}
        </FlatButton>
      )}
      {peruuttamassaSuostumusta && props.opiskeluoikeusOid && (
        <SuostumuksenPeruutusPopup
          opiskeluoikeudenNimi={props.nimi}
          opiskeluoikeusOid={props.opiskeluoikeusOid}
          suorituksenTyyppi={props.suorituksenTyyppi}
          onDismiss={dismissPopup}
        />
      )}
      {suostumuksenPerumisenInfo && (
        <div
          className={'suostumuksen-perumisen-info modal'}
          role="dialog"
          aria-modal={true}
          aria-describedby="modal-main-content"
        >
          <div className="modal-content">
            <div className="modal-main-content">
              <Trans>{'tooltip:Suostumuksen selitys'}</Trans>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

type SuostumuksenPeruutusPopupProps = {
  opiskeluoikeudenNimi: string
  opiskeluoikeusOid: string
  suorituksenTyyppi?: Koodistokoodiviite<'suorituksentyyppi'>
  onDismiss: () => void
}

const SuostumuksenPeruutusPopup: React.FC<SuostumuksenPeruutusPopupProps> = ({
  opiskeluoikeudenNimi,
  opiskeluoikeusOid,
  suorituksenTyyppi,
  onDismiss
}) => {
  const [accepted, setAccepted] = useState(false)

  const onSubmit = useCallback(async () => {
    if (accepted) {
      await peruutaSuostumus(opiskeluoikeusOid, suorituksenTyyppi?.koodiarvo)
      window.location.reload()
    }
  }, [accepted, opiskeluoikeusOid, suorituksenTyyppi?.koodiarvo])

  return (
    <Modal>
      <ModalTitle>{`${t(
        'Suostumuksen peruminen'
      )}: ${opiskeluoikeudenNimi}`}</ModalTitle>
      <ModalBody>
        <p>{t('Kun peruutat antamasi suostumuksen...')}</p>
        <Checkbox
          label="Ymmärrän, että suostumuksen peruutus on lopullinen. Kyseisen koulutuksen tiedot poistuvat Opintopolun suoritustiedoistani välittömästi."
          checked={accepted}
          onChange={setAccepted}
        />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onDismiss}>{t('Takaisin')}</FlatButton>
        <RaisedButton disabled={!accepted} onClick={onSubmit}>
          {t('Kyllä, peru suostumus')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
