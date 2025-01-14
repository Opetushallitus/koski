import React, { useCallback, useMemo } from 'react'
import {
  LaajuusEdit,
  LaajuusEditProps
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import { parseISODateNullable, today } from '../../date/date'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LaajuusOpintopisteissäTaiKursseissa } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissaTaiKursseissa'

const laajuusyksikönVaihtumispäivä = new Date(2025, 8 - 1, 1)

export type IBLaajuusEditProps = Omit<
  LaajuusEditProps<LaajuusOpintopisteissäTaiKursseissa> & {
    alkamispäivä?: string
  },
  'createLaajuus'
>

export const IBLaajuusEdit: React.FC<IBLaajuusEditProps> = (props) => {
  const yksikkö = useIBLaajuusyksikkö(props.value, props.alkamispäivä)
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

export const useIBLaajuusyksikkö = (
  value?: LaajuusOpintopisteissäTaiKursseissa,
  alkamispäivä?: string
): IBLaajuusYksikkö =>
  useMemo(
    () =>
      value
        ? value.yksikkö
        : laajuusyksikköPäivänPerusteella(
            (alkamispäivä && parseISODateNullable(alkamispäivä)) || today()
          ),
    [value, alkamispäivä]
  )

const laajuusyksikköPäivänPerusteella = (pvm: Date): IBLaajuusYksikkö =>
  Koodistokoodiviite({
    koodiarvo: pvm < laajuusyksikönVaihtumispäivä ? Kursseja : Opintopisteitä,
    koodistoUri: 'opintojenlaajuusyksikko'
  })
