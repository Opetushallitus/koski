import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as Ord from 'fp-ts/Ord'
import React, { useMemo } from 'react'
import { ISO2FinnishDate } from '../../date/date'
import { t } from '../../i18n/i18n'
import {
  OpiskeluoikeudenJaksoLikeOrd,
  OpiskeluoikeudenTilaLike
} from '../../util/schema'
import { KeyMultiValueRow, KeyValueTable } from '../containers/KeyValueTable'

export type OpiskeluoikeudenTilaProps = {
  tila: OpiskeluoikeudenTilaLike
}

export const OpiskeluoikeudenTila = (props: OpiskeluoikeudenTilaProps) => {
  const sortedJaksot = useMemo(
    () =>
      pipe(
        props.tila.opiskeluoikeusjaksot || [],
        A.sort(Ord.reverse(OpiskeluoikeudenJaksoLikeOrd))
      ),
    [props.tila]
  )

  return (
    <KeyValueTable>
      {sortedJaksot.map((jakso, index) => (
        <KeyMultiValueRow
          name="Tila"
          key={index}
          className={
            index === 0
              ? 'OpiskeluoikeudenTila-viimeisin'
              : 'OpiskeluoikeudenTila-aiempi'
          }
          columnSpans={[2, 18]}
        >
          {[ISO2FinnishDate(jakso.alku), t(jakso.tila.nimi)]}
        </KeyMultiValueRow>
      ))}
    </KeyValueTable>
  )
}
