import React, { useCallback, useState } from 'react'
import { useApiWithParams, useOnApiSuccess } from '../../api-fetch'
import { t } from '../../i18n/i18n'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
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
> = (props) => {
  const { opiskeluoikeus } = props

  const [peruuttamassaSuostumusta, setPeruuttamassaSuostumusta] =
    useState(false)
  const [suostumuksenPerumisenInfo, setSuostumuksenPerumisenInfo] =
    useState(false)
  const [suoritusjakoTehty, setSuoritusjakoTehty] = useState(true)

  const opiskeluoikeusOid = getOpiskeluoikeusOid(opiskeluoikeus)
  const opiskeluoikeudenNimi = t(opiskeluoikeus.tyyppi.nimi)

  const peruutus = useApiWithParams(
    fetchSuoritusjakoTehty,
    opiskeluoikeusOid !== undefined ? [opiskeluoikeusOid] : undefined
  )

  useOnApiSuccess(peruutus, (response) =>
    setSuoritusjakoTehty(response.data.tehty)
  )

  return (
    <div {...common(props, ['OpiskeluoikeudenSuostumuksenPeruminen'])}>
      <b>
        <Trans>
          {
            'Tämän opiskeluoikeuden tiedot näytetään antamasi suostumuksen perusteella.'
          }
        </Trans>
      </b>
      <span className="infobox">
        <span
          className="info-icon"
          onClick={() =>
            setSuostumuksenPerumisenInfo(!suostumuksenPerumisenInfo)
          }
          onMouseEnter={() => setSuostumuksenPerumisenInfo(true)}
          onMouseLeave={() => setSuostumuksenPerumisenInfo(false)}
        />
      </span>
      {!suoritusjakoTehty && (
        <a
          className="peru-suostumus-linkki"
          onClick={() => setPeruuttamassaSuostumusta(!peruuttamassaSuostumusta)}
        >
          {'Peruuta suostumus'}
        </a>
      )}
      {peruuttamassaSuostumusta && opiskeluoikeusOid && (
        <SuostumuksenPeruutusPopup
          opiskeluoikeudenNimi={opiskeluoikeudenNimi}
          opiskeluoikeusOid={opiskeluoikeusOid}
          onDismiss={() => setPeruuttamassaSuostumusta(false)}
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
  onDismiss: () => void
}

const SuostumuksenPeruutusPopup: React.FC<SuostumuksenPeruutusPopupProps> = ({
  opiskeluoikeudenNimi,
  opiskeluoikeusOid,
  onDismiss
}) => {
  const [accepted, setAccepted] = useState(false)

  const onSubmit = useCallback(async () => {
    if (accepted) {
      await peruutaSuostumus(opiskeluoikeusOid)
      window.location.reload()
    }
  }, [accepted, opiskeluoikeusOid])

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
