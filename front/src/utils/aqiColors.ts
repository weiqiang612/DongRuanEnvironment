const MIN_AQI_GRADE = 1
const MAX_AQI_GRADE = 6

function getAqiGradeToken(grade: number, token: 'bg' | 'color' | 'chart'): string {
  const safeGrade = Math.min(MAX_AQI_GRADE, Math.max(MIN_AQI_GRADE, Math.trunc(grade)))
  return getComputedStyle(document.documentElement)
    .getPropertyValue(`--aqi-grade-${safeGrade}-${token}`)
    .trim()
}

/** Returns the foreground color used by AQI text and icons. */
export function getAqiGradeColor(grade: number): string {
  return getAqiGradeToken(grade, 'color')
}

/** Returns the stronger AQI fill color used by ECharts and its legends. */
export function getAqiGradeChartColor(grade: number): string {
  return getAqiGradeToken(grade, 'chart')
}
