import { MassaluovutusQueryParameters } from '../../types/fi/oph/koski/massaluovutus/MassaluovutusQueryParameters'
import {
  isMassaluovutusQueryAikuistenPerusopetuksenKurssikertyma,
  MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma'
import {
  isMassaluovutusQueryAmmatillinenOpiskelijavuositiedot,
  MassaluovutusQueryAmmatillinenOpiskelijavuositiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAmmatillinenOpiskelijavuositiedot'
import {
  isMassaluovutusQueryAmmatillinenOsittainenSuoritustiedot,
  MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot'
import {
  isMassaluovutusQueryAmmatillinenTutkintoSuoritustiedot,
  MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot'
import {
  isMassaluovutusQueryLukio2019Opintopistekertymat,
  MassaluovutusQueryLukio2019Opintopistekertymat
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryLukio2019Opintopistekertymat'
import {
  isMassaluovutusQueryLukio2019Suoritustiedot,
  MassaluovutusQueryLukio2019Suoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryLukio2019Suoritustiedot'
import {
  isMassaluovutusQueryLukioKurssikertymat,
  MassaluovutusQueryLukioKurssikertymat
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryLukioKurssikertymat'
import {
  isMassaluovutusQueryLukionSuoritustiedot,
  MassaluovutusQueryLukionSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryLukionSuoritustiedot'
import {
  isMassaluovutusQueryMuuAmmatillinen,
  MassaluovutusQueryMuuAmmatillinen
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryMuuAmmatillinen'
import {
  isMassaluovutusQueryMuuKuinSaanneltyKoulutus,
  MassaluovutusQueryMuuKuinSaanneltyKoulutus
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryMuuKuinSaanneltyKoulutus'
import {
  isMassaluovutusQueryPerusopetukseenValmistava,
  MassaluovutusQueryPerusopetukseenValmistava
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryPerusopetukseenValmistava'
import {
  isMassaluovutusQueryPerusopetuksenOppijamaaratRaportti,
  MassaluovutusQueryPerusopetuksenOppijamaaratRaportti
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryPerusopetuksenOppijamaaratRaportti'
import {
  isMassaluovutusQueryTOPKSAmmatillinen,
  MassaluovutusQueryTOPKSAmmatillinen
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryTOPKSAmmatillinen'
import {
  isMassaluovutusQueryTuvaSuoritustiedot,
  MassaluovutusQueryTuvaSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryTuvaSuoritustiedot'
import {
  isMassaluovutusQueryVSTKOPS,
  MassaluovutusQueryVSTKOPS
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryVSTKOPS'
import {
  isMassaluovutusQueryVSTOsaamismerkit,
  MassaluovutusQueryVSTOsaamismerkit
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryVSTOsaamismerkit'
import {
  isMassaluovutusQueryVSTVapaatavoitteinen,
  MassaluovutusQueryVSTVapaatavoitteinen
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryVSTVapaatavoitteinen'

export type AikajaksoraportinKyselynParametrit = {
  organisaatioOid: string
  alku: string
  loppu: string
  osasuoritustenAikarajaus?: boolean
  kotikuntaPvm?: string
  language: 'fi' | 'sv' | 'en'
}

export type PäiväraportinKyselynParametrit = {
  organisaatioOid: string
  paiva: string
  kotikuntaPvm?: string
  language: 'fi' | 'sv' | 'en'
}

export type RaporttiKyselynTiedot = {
  organisaatioOid?: string
  osasuoritustenAikarajaus?: boolean
  kotikuntaPvm?: string
} & ({ alku: string; loppu: string } | { paiva: string })

export type MassaluovutusKyselymalli<P> = {
  luo: (parametrit: P) => MassaluovutusQueryParameters
  // Palauttaa null, jos kysely ei ole tämän raportin kysely
  lue: (kysely: MassaluovutusQueryParameters) => RaporttiKyselynTiedot | null
  // Omien raporttien ajankohtasarakkeen otsikko, oletuksena Aikajakso
  ajankohdanOtsikko?: string
  // Annettuna omissa raporteissa näytetään osasuoritusten rajaus tällä otsikolla
  osasuoritustenRajausOtsikko?: string
  näytäKotikuntaPvm?: boolean
}

export const ammatillinenTutkintoSuoritustiedotKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu,
        osasuoritustenAikarajaus: p.osasuoritustenAikarajaus ?? false
      }),
    lue: (q) =>
      isMassaluovutusQueryAmmatillinenTutkintoSuoritustiedot(q)
        ? {
            organisaatioOid: q.organisaatioOid,
            alku: q.alku,
            loppu: q.loppu,
            osasuoritustenAikarajaus: q.osasuoritustenAikarajaus
          }
        : null,
    osasuoritustenRajausOtsikko: 'Tutkinnon osat'
  }

export const ammatillinenOsittainenSuoritustiedotKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu,
        osasuoritustenAikarajaus: p.osasuoritustenAikarajaus ?? false
      }),
    lue: (q) =>
      isMassaluovutusQueryAmmatillinenOsittainenSuoritustiedot(q)
        ? {
            organisaatioOid: q.organisaatioOid,
            alku: q.alku,
            loppu: q.loppu,
            osasuoritustenAikarajaus: q.osasuoritustenAikarajaus
          }
        : null,
    osasuoritustenRajausOtsikko: 'Tutkinnon osat'
  }

export const ammatillinenOpiskelijavuositiedotKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryAmmatillinenOpiskelijavuositiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryAmmatillinenOpiskelijavuositiedot(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const muuAmmatillinenKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryMuuAmmatillinen({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryMuuAmmatillinen(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const topksAmmatillinenKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryTOPKSAmmatillinen({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryTOPKSAmmatillinen(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const muuKuinSaanneltyKoulutusKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryMuuKuinSaanneltyKoulutus({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryMuuKuinSaanneltyKoulutus(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const tuvaSuoritustiedotKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryTuvaSuoritustiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryTuvaSuoritustiedot(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const aikuistenPerusopetuksenKurssikertymaKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryAikuistenPerusopetuksenKurssikertyma(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const lukioKurssikertymatKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryLukioKurssikertymat({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryLukioKurssikertymat(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const lukio2019OpintopistekertymatKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryLukio2019Opintopistekertymat({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryLukio2019Opintopistekertymat(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const perusopetukseenValmistavaKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryPerusopetukseenValmistava({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryPerusopetukseenValmistava(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const lukionSuoritustiedotKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryLukionSuoritustiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu,
        osasuoritustenAikarajaus: p.osasuoritustenAikarajaus ?? false,
        kotikuntaPvm: p.kotikuntaPvm
      }),
    lue: (q) =>
      isMassaluovutusQueryLukionSuoritustiedot(q)
        ? {
            organisaatioOid: q.organisaatioOid,
            alku: q.alku,
            loppu: q.loppu,
            osasuoritustenAikarajaus: q.osasuoritustenAikarajaus,
            kotikuntaPvm: q.kotikuntaPvm
          }
        : null,
    osasuoritustenRajausOtsikko: 'Kurssit',
    näytäKotikuntaPvm: true
  }

export const lukio2019SuoritustiedotKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryLukio2019Suoritustiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu,
        osasuoritustenAikarajaus: p.osasuoritustenAikarajaus ?? false,
        kotikuntaPvm: p.kotikuntaPvm
      }),
    lue: (q) =>
      isMassaluovutusQueryLukio2019Suoritustiedot(q)
        ? {
            organisaatioOid: q.organisaatioOid,
            alku: q.alku,
            loppu: q.loppu,
            osasuoritustenAikarajaus: q.osasuoritustenAikarajaus,
            kotikuntaPvm: q.kotikuntaPvm
          }
        : null,
    osasuoritustenRajausOtsikko: 'Kurssit',
    näytäKotikuntaPvm: true
  }

export const perusopetuksenOppijamaaratRaporttiKyselymalli: MassaluovutusKyselymalli<PäiväraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryPerusopetuksenOppijamaaratRaportti({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        paiva: p.paiva
      }),
    lue: (q) =>
      isMassaluovutusQueryPerusopetuksenOppijamaaratRaportti(q)
        ? { organisaatioOid: q.organisaatioOid, paiva: q.paiva }
        : null,
    ajankohdanOtsikko: 'Päivä'
  }

export const vstVapaatavoitteinenKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryVSTVapaatavoitteinen({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryVSTVapaatavoitteinen(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const vstKopsKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryVSTKOPS({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryVSTKOPS(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const vstOsaamismerkitKyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit> =
  {
    luo: (p) =>
      MassaluovutusQueryVSTOsaamismerkit({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryVSTOsaamismerkit(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }
