import React from 'react'
import Bacon from 'baconjs'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'

export const tiedonsiirtolokiContentP = tiedonsiirrotContentP(Bacon.constant(<span>Viimeisimmät KOSKI-palveluun siirtyneet opiskelijatiedot</span>))