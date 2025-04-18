// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any
export const groupBy = <T, K extends keyof any>(
  array: T[],
  getKey: (item: T) => K
) =>
  array.reduce((previous, currentItem) => {
    const group = getKey(currentItem)
    if (!previous[group]) previous[group] = []
    previous[group].push(currentItem)
    return previous
  }, {} as Record<K, T[]>)

  const regexEmail = /\S+@\S+\.\S+/


export const getParamsFromObject: (obj: {}) => URLSearchParams = (obj) =>
  {
    // console.log("obj ", obj)
    return new URLSearchParams(
    Object.entries(obj).reduce(
      (newParams, [key, val]) =>
        val
          ? {
              ...newParams,
              [key]: val
               // [key]: val.toString().match(regexEmail) ?  val : typeof val === "string" ? encodeURIComponent(val) : val
            }
          : newParams,
      {}
    )
  )}

export const isConsideredPhoneNumber: (value: string) => boolean = (value) =>
  value?.startsWith("+") || /^\d/.test(value)
