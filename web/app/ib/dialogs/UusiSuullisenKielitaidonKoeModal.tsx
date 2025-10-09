import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { useChildSchema } from '../../appstate/constraints'
import { SuullisenKielitaidonKoe2019 } from '../../types/fi/oph/koski/schema/SuullisenKielitaidonKoe2019'
import React, { useState } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { t } from '../../i18n/i18n'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { DateInput } from '../../components-v2/controls/DateInput'
import { Spacer } from '../../components-v2/layout/Spacer'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import {
  PreIBSuullisenKielitaidonKoe2019Arvosana,
  PreIBSuullisenKielitaidonKoe2019Taitotaso
} from '../PreIB2019KieliJaViestintäRows'

export type UusiSuullisenKielitaidonKoeModalProps = {
  onClose: () => void
  onSubmit: (
    kieli: Koodistokoodiviite<'kielivalikoima'>,
    arvosana: PreIBSuullisenKielitaidonKoe2019Arvosana,
    taitotaso: PreIBSuullisenKielitaidonKoe2019Taitotaso,
    päivä: string
  ) => void
}

export const UusiSuullisenKielitaidonKoeModal = ({
  onClose,
  onSubmit
}: UusiSuullisenKielitaidonKoeModalProps) => {
  const taitotasot =
    useKoodistoOfConstraint(
      useChildSchema(SuullisenKielitaidonKoe2019.className, 'taitotaso')
    ) || []
  const arvosanat =
    useKoodistoOfConstraint(
      useChildSchema(SuullisenKielitaidonKoe2019.className, 'arvosana')
    ) || []

  const [kieli, setKieli] = useState<
    Koodistokoodiviite<'kielivalikoima'> | undefined
  >(undefined)
  const [arvosana, setArvosana] = useState<
    PreIBSuullisenKielitaidonKoe2019Arvosana | undefined
  >(undefined)
  const [taitotaso, setTaitotaso] = useState<
    PreIBSuullisenKielitaidonKoe2019Taitotaso | undefined
  >(undefined)
  const [päivä, setPäivä] = useState<string | undefined>(undefined)

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Suullisen kielitaidon kokeen lisäys')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Kieli')}
          <KoodistoSelect
            koodistoUri={'kielivalikoima'}
            onSelect={(koodiviite) => {
              koodiviite && setKieli(koodiviite)
            }}
            value={kieli ? kieli.koodiarvo : undefined}
            testId={'suullisenKielitaidonKokeet.modal.kieli'}
          />
        </label>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            koodiarvot={arvosanat.map((a) => a.koodiviite.koodiarvo)}
            onSelect={(koodiviite) => {
              koodiviite &&
                setArvosana(
                  koodiviite as PreIBSuullisenKielitaidonKoe2019Arvosana
                )
            }}
            value={arvosana ? arvosana.koodiarvo : undefined}
            testId={'suullisenKielitaidonKokeet.modal.arvosana'}
          />
        </label>
        <label>
          {t('Taitotaso')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkokehittyvankielitaidontasot'}
            koodiarvot={taitotasot.map((tt) => tt.koodiviite.koodiarvo)}
            onSelect={(koodiviite) => {
              koodiviite &&
                setTaitotaso(
                  koodiviite as PreIBSuullisenKielitaidonKoe2019Taitotaso
                )
            }}
            value={taitotaso ? taitotaso.koodiarvo : undefined}
            testId={'suullisenKielitaidonKokeet.modal.taitotaso'}
          />
        </label>
        <label>
          {t('Päivä')}
          <DateInput
            value={päivä}
            onChange={(date) => setPäivä(date)}
            testId={'suullisenKielitaidonKokeet.modal.päivä'}
          />
        </label>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={[kieli, arvosana, taitotaso, päivä].includes(undefined)}
          onClick={() => {
            if (
              kieli !== undefined &&
              arvosana !== undefined &&
              taitotaso !== undefined &&
              päivä !== undefined
            ) {
              onSubmit(kieli, arvosana, taitotaso, päivä)
              onClose()
            }
          }}
          testId="confirm"
        >
          {t('Lisää suullisen kielitaidon koe')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
