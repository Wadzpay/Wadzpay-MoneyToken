export const mergeArraysAndRemoveDuplicates = (
  arr1: any[],
  arr2: any[],
  key: string
) => {
  const mergedArray = [...arr1, ...arr2]
  const uniqueItemsMap = new Map()

  mergedArray.forEach((item) => {
    uniqueItemsMap.set(item[key], item)
  })

  return Array.from(uniqueItemsMap.values())
}
