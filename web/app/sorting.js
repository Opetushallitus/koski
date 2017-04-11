export const sortGrades = grades => {
  let sort = (gradeX, gradeY) => {
    let x = gradeX.value
    let y = gradeY.value
    let xAsFloat = parseFloat(x)
    let yAsFloat = parseFloat(y)
    if (isNaN(xAsFloat) && isNaN(yAsFloat)) {
      return (x < y) ? -1 : (x > y) ? 1 : 0
    }
    if (isNaN(xAsFloat)) {
      return 1
    }
    if (isNaN(yAsFloat)) {
      return -1
    }
    return parseFloat(x) - parseFloat(y)
  }
  return grades.sort(sort)
}

// expects that the list is already sorted, just puts the preordered ones first
export const sortLanguages = languages => {
  let preordered = ['FI', 'SV', 'EN']
  return languages.reduce((acc, k) => {
    let preorderedIndex = preordered.findIndex(v => v === k.value)
    if (preorderedIndex > -1) {
      acc[preorderedIndex] = k
    } else {
      acc.push(k)
    }
    return acc
  }, [])
}