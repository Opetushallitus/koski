import React from 'react'
import { CommonProps } from '../components-v2/CommonProps'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Trans } from '../components-v2/texts/Trans'
import { t } from '../i18n/i18n'
import { TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenLaajanOppimaaranPerusopintojenSuoritus'
import { TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenLaajanOppimaaranSyventavienOpintojenSuoritus'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenYleisenOppimaaranTeemaopintojenSuoritus'
import { TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenYleisenOppimaaranYhteistenOpintojenSuoritus'
import { KoodiarvotOf } from '../util/koodisto'

export type UusiTaiteenPerusopetuksenPäätasonSuoritusModalProps = CommonProps<{
  opiskeluoikeus: TaiteenPerusopetuksenOpiskeluoikeus
  suoritus: TaiteenPerusopetuksenPäätasonSuoritus
  onDismiss: () => void
  onCreate: () => void
}>

export const UusiTaiteenPerusopetuksenPäätasonSuoritusModal: React.FC<
  UusiTaiteenPerusopetuksenPäätasonSuoritusModalProps
> = (props) => {
  return (
    <Modal>
      <ModalTitle>{t('Uusi taiteen perusopetuksen suoritus')}</ModalTitle>
      <ModalBody>
        <KeyValueTable>
          <KeyValueRow label="Taiteenala">
            <Trans>{props.suoritus.koulutusmoduuli.taiteenala.nimi}</Trans>
          </KeyValueRow>
          <KeyValueRow label="Oppimäärä">
            <Trans>{props.opiskeluoikeus.oppimäärä.nimi}</Trans>
          </KeyValueRow>
          <KeyValueRow label="Oppilaitos">
            <Trans>{props.opiskeluoikeus.oppilaitos?.nimi}</Trans>
          </KeyValueRow>
        </KeyValueTable>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onDismiss}>{t('Peruuta')}</FlatButton>
        <RaisedButton onClick={props.onCreate}>
          {t('Lisää suoritus')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

export const createCompanionSuoritus = (
  suoritus: TaiteenPerusopetuksenPäätasonSuoritus
): TaiteenPerusopetuksenPäätasonSuoritus => {
  const seed = {
    koulutusmoduuli: suoritus.koulutusmoduuli,
    toimipiste: suoritus.toimipiste
  }
  const uudetSuoritukset: Record<
    KoodiarvotOf<TaiteenPerusopetuksenPäätasonSuoritus['tyyppi']>,
    () => TaiteenPerusopetuksenPäätasonSuoritus
  > = {
    taiteenperusopetuksenlaajanoppimaaranperusopinnot: () =>
      TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus(seed),
    taiteenperusopetuksenlaajanoppimaaransyventavatopinnot: () =>
      TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus(seed),
    taiteenperusopetuksenyleisenoppimaaranteemaopinnot: () =>
      TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus(seed),
    taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot: () =>
      TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus(seed)
  }

  return uudetSuoritukset[suoritus.tyyppi.koodiarvo]()
}
