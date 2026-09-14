import { provinceAdcode } from './provinceMap'

const provinceMapModules = import.meta.glob('/src/assets/maps/provinces/*.geo.json', {
  import: 'default',
}) as Record<string, () => Promise<unknown>>

/** Loads one locally bundled city-boundary map only when its province is opened. */
export async function loadProvinceCityGeoJson(provinceName: string): Promise<unknown | undefined> {
  const adcode = provinceAdcode(provinceName)
  const loadMap = adcode ? provinceMapModules[`/src/assets/maps/provinces/${adcode}.geo.json`] : undefined
  return loadMap?.()
}
