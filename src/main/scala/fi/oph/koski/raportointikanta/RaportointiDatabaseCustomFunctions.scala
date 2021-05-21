package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation

object RaportointiDatabaseCustomFunctions {
  def vuodenViimeinenPäivämäärä(s: Schema) = {
    sqlu"""
      create or replace function #${s.name}.vuodenViimeinenPaivamaara(date) returns date
        as 'select to_date(concat(
              extract(year from ($$1 + interval ''20 year'')::date)::text,
              ''-12-31''
            ),
          ''YYYY-MM-DD'')'
      language sql
      immutable
      returns null on null input;
      """
  }
}
