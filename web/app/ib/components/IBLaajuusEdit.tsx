import React, { useCallback, useMemo } from 'react'
import {
  LaajuusEdit,
  LaajuusEditProps
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import { todayISODate } from '../../date/date'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LaajuusOpintopisteissäTaiKursseissa } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissaTaiKursseissa'
import { config } from '../../util/config'

const laajuusyksikönVaihtumispäivä =
  config().rajapäivät.ibLaajuusOpintopisteinäAlkaen

export type IBLaajuusEditProps = Omit<
  LaajuusEditProps<LaajuusOpintopisteissäTaiKursseissa> & {
    alkamispäivä?: string
  },
  'createLaajuus'
>

export const IBLaajuusEdit: React.FC<IBLaajuusEditProps> = ({
  alkamispäivä,
  ...props
}) => {
  const yksikkö = useMemo(
    () => createIBLaajuusyksikkö(props.value, alkamispäivä),
    [props.value, alkamispäivä]
  )

  const createLaajuus = useCallback(
    (arvo: number) => {
      return createIBLaajuus(arvo, yksikkö!)
    },
    [yksikkö]
  )

  return yksikkö ? (
    <>
      <LaajuusEdit
        {...props}
        createLaajuus={createLaajuus}
        testId={props.testId || 'laajuus'}
      />
    </>
  ) : null
}

export const createIBLaajuus = (
  arvo: number,
  yksikkö: Koodistokoodiviite
): LaajuusOpintopisteissäTaiKursseissa =>
  yksikkö.koodiarvo === Kursseja
    ? LaajuusKursseissa({ arvo })
    : LaajuusOpintopisteissä({ arvo })

export const Kursseja = '4'
export const Opintopisteitä = '2'
type IBLaajuusYksikkö = Koodistokoodiviite<
  'opintojenlaajuusyksikko',
  typeof Kursseja | typeof Opintopisteitä
>

export const createIBLaajuusyksikkö = (
  value?: LaajuusOpintopisteissäTaiKursseissa,
  alkamispäivä?: string
): IBLaajuusYksikkö =>
  value
    ? value.yksikkö
    : laajuusyksikköPäivänPerusteella(alkamispäivä || todayISODate())

const laajuusyksikköPäivänPerusteella = (pvm: string): IBLaajuusYksikkö =>
  Koodistokoodiviite({
    koodiarvo: pvm < laajuusyksikönVaihtumispäivä ? Kursseja : Opintopisteitä,
    koodistoUri: 'opintojenlaajuusyksikko'
  })
