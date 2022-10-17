export const sortGrades = (grades) => {
  return grades.slice().sort(sortGradesF)
}

export const sortGradesF = (gradeX, gradeY) => {
  const xParts = gradeX.value.split('_')
  const yParts = gradeY.value.split('_')
  const xKoodisto = xParts[0]
  const yKoodisto = yParts[0]

  if (xKoodisto < yKoodisto) {
    return -1
  } else if (xKoodisto > yKoodisto) {
    return 1
  }

  const xAsFloat = parseFloat(xParts[1])
  const yAsFloat = parseFloat(yParts[1])
  if (isNaN(xAsFloat) && isNaN(yAsFloat)) {
    return xParts[1] < yParts[1] ? -1 : xParts[1] > yParts[1] ? 1 : 0
  }
  if (isNaN(xAsFloat)) {
    return 1
  }
  if (isNaN(yAsFloat)) {
    return -1
  }
  return xAsFloat - yAsFloat
}

// expects that the list is already sorted, just puts the preordered ones first
export const sortLanguages = (languages) => {
  const preordered = ['FI', 'SV', 'EN']

  return languages
    .filter((l) => preordered.includes(l.data.koodiarvo))
    .sort(
      (l1, l2) =>
        preordered.findIndex((v) => v === l1.data.koodiarvo) -
        preordered.findIndex((v) => v === l2.data.koodiarvo)
    )
    .concat(languages.filter((l) => !preordered.includes(l.data.koodiarvo)))
}
