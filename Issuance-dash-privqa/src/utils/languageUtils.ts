export const sortLanguagesByDefault = (languages: any[]) => {
  return languages.sort((a: any, b: any) => {
    const aIsDefault = a.mappingData?.isDefault || false
    const bIsDefault = b.mappingData?.isDefault || false

    if (aIsDefault === bIsDefault) {
      return 0
    }
    return aIsDefault ? -1 : 1
  })
}
