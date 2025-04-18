import React, { useEffect, useState } from "react"
import { Link, useLocation, useNavigate } from "react-router-dom"
import {
  Button,
  Table,
  Popconfirm,
  Switch,
  notification,
  Radio,
  Tooltip
} from "antd"
import { ThreeDots } from "react-loader-spinner"
import PageHeading from "src/components/ui/PageHeading"
import { RouteType } from "src/constants/routeTypes"
import { useSelector } from "react-redux"
import { ROOTSTATE } from "src/utils/modules"
import isProduction from "src/utils/environmentUtils"
import {
  useGetAllLanguages,
  useSaveLanguage,
  useMappedLanguageInstitution,
  useMakeLanguageDefault
} from "src/api/user"

import DisableLanguageInfo from "./DisableLanguageInfo"

const showNotification = (
  type: "success" | "error",
  message: string,
  description: string
) => {
  notification[type]({
    message,
    description
  })
}

// Main Component
const Languages = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const [languages, setLanguages] = useState<any>([])
  const [isInstitution, setIsInstitution] = useState<boolean>(false)
  const [mappedLanguages, setMappedLanguages] = useState<any>([])
  const [selectedRowKeys, setselectedRowKeys] = useState<React.Key[]>([])
  const [isLanguages, setIsLanguages] = useState<boolean>(false)
  const [selectedDefaultLanguage, setSelectedDefaultLanguage] = useState<
    number | null
  >(null)
  const [isButtonDisabled, setIsButtonDisabled] = useState<boolean>(true)
  const [loading, setLoading] = useState<boolean>(false)
  const [isModal, setIsModal] = useState(false)
  const [institutionsList, setInstitutionsList] = useState<any>()
  const [languageName, setLanguageName] = useState<string>("")

  // get all languages API
  const {
    data: getAllLanguages,
    isFetching: isFetchingAllLanguages,
    error: AllLanguagesError,
    refetch
  } = useGetAllLanguages()

  // API Call Save Language
  const {
    mutate: saveLanguage,
    error: saveLanguageError,
    isSuccess: isSuccess
  } = useSaveLanguage()

  // API Call Mapped Language
  const {
    mutate: mappedLanguage,
    error: mappedLanguageError,
    isSuccess: isSuccessMappedLanguage
  } = useMappedLanguageInstitution()

  // API Call make Language default
  const {
    mutate: makeLanguageDefault,
    error: makeLanguageDefaultError,
    isSuccess: isSuccessMakeLanguageDefault
  } = useMakeLanguageDefault()

  const getColumns = (
    isInstitution: boolean,
    confirm: (isActive: boolean, id: number, languageName: string) => void,
    openFile: (file: string) => void,
    editLanguage: (data: any) => void
  ) => [
    {
      title: "Language Name",
      className: "first-column-margin",
      render: (data: any) => (
        <div
          style={{
            marginLeft: "16px",
            color:
              !isInstitution && data.languageName === "English" ? "#00599D" : ""
          }}
        >
          {`${data.languageName} ${
            !isInstitution && data.languageName === "English" ? "(Default)" : ""
          }`}
        </div>
      )
    },
    {
      title: "Display Language",
      render: (data: any) => (
        <span
          style={{
            color:
              !isInstitution && data.languageName === "English" ? "#00599D" : ""
          }}
        >
          {data.languageDisplayName}
        </span>
      )
    },
    {
      title: "Country Name",
      render: (data: any) => (
        <span
          style={{
            color:
              !isInstitution && data.languageName === "English" ? "#00599D" : ""
          }}
        >
          {data.countryName}
        </span>
      )
    },
    {
      title: "Country Flag",
      dataIndex: "countryImageUrl",
      render: (countryImageUrl: string) => (
        <>
          <img
            width={50}
            src={countryImageUrl}
            style={{ border: "1px solid #E0E0E0", marginLeft: "16px" }}
          />
        </>
      )
    },
    {
      title: "Status",
      render: (data: any) => (
        <>
          {!isInstitution && data.languageName === "English" ? null : (
            <Popconfirm
              title={
                !data.isActive
                  ? "Are you sure to enable this language?"
                  : "Are you sure to disable this language?"
              }
              onConfirm={() =>
                confirm(!data.isActive, data.id, data.languageName)
              }
              okText="Yes"
              cancelText="No"
              disabled={
                !isInstitution && data.languageName === "English" ? true : false
              }
            >
              <Switch
                className="custom-switch"
                style={{
                  backgroundColor: data.isActive ? "#26A6E0" : "#B6B6B6"
                }}
                checked={data.isActive}
                disabled={
                  !isInstitution && data.languageName === "English"
                    ? true
                    : false
                }
                size="small"
              />
            </Popconfirm>
          )}
        </>
      )
    },
    {
      title: "Institution Count",
      render: (data: any) => (
        <>
          {!isInstitution && data.languageName === "English" ? null : (
            <span style={{ marginLeft: "65px" }}>{data?.institutionCount}</span>
          )}
        </>
      )
    },
    {
      title: isInstitution ? "Default Language" : "",
      render: (data: any) =>
        isInstitution ? (
          mappedLanguages.some((item: any) => item.languageId === data.id) &&
          (selectedDefaultLanguage === data.id ? (
            <Radio
              checked={selectedDefaultLanguage === data.id ? true : false}
              onChange={() => onChangeDefaultLanguage(data.id)}
              className="language-radio"
            />
          ) : (
            <Tooltip
              overlayClassName="multilingual-tooltip"
              title={"Set Default Language"}
              placement="bottom"
            >
              <Radio
                checked={selectedDefaultLanguage === data.id ? true : false}
                onChange={() => onChangeDefaultLanguage(data.id)}
                className="language-radio"
              />
            </Tooltip>
          ))
        ) : (
          <div style={{ display: "flex" }}>
            <Button
              type="link"
              onClick={() => openFile(data.resourceFileUrl)}
              style={{ color: "#006BE2" }}
            >
              View Language File
            </Button>
            <Button
              onClick={() => editLanguage(data)}
              type="link"
              style={{ color: "#006BE2" }}
            >
              Edit
            </Button>
          </div>
        )
    }
  ]

  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  useEffect(() => {
    if (location.pathname === RouteType.INSTITUTION_LANGUAGES) {
      setIsInstitution(true)
    } else {
      // Reset mapped languages state
      setMappedLanguages([])
      setIsInstitution(false)
      resetSelectedRows()
    }
  }, [location])

  function resetSelectedRows() {
    setselectedRowKeys(
      languages
        .filter((language: any) => language.mappingData && language.id)
        .map(({ id }: any) => id)
    )

    const mappedLanguages = languages
      .filter((lang: any) => lang.mappingData !== null)
      .map((lang: any) => ({
        id: lang.mappingData.mappingId,
        isActive: lang.isActive,
        languageId: lang.id
      }))

    setMappedLanguages(mappedLanguages)
  }

  useEffect(() => {
    if (AllLanguagesError) {
      showNotification("error", "Notification", AllLanguagesError.message)
    }
    if (getAllLanguages) {
      const allLanguages = getAllLanguages.map((obj) => ({
        ...obj,
        key: obj.id
      }))

      setselectedRowKeys(
        getAllLanguages
          .filter((language: any) => language.mappingData && language.id)
          .map(({ id }: any) => id)
      )

      // Set default language id
      const defaultLanguageId =
        (allLanguages.find((lang: any) => lang.mappingData?.isDefault) || {})
          .id || null
      setSelectedDefaultLanguage(defaultLanguageId)

      const mappedLanguages = allLanguages
        .filter((lang: any) => lang.mappingData !== null)
        .map((lang: any) => ({
          id: lang.mappingData.mappingId,
          isActive: lang.isActive,
          languageId: lang.id
        }))

      setMappedLanguages(mappedLanguages)

      allLanguages.sort((a: any, b: any) =>
        a.languageName.localeCompare(b.languageName)
      )

      if (isInstitution) {
        allLanguages.sort((a: any, b: any) => {
          if (a.mappingData === null && b.mappingData !== null) {
            return 1
          }
          if (a.mappingData !== null && b.mappingData === null) {
            return -1
          }
          return 0
        })
      }

      // set languages
      setLanguages(allLanguages)

      setIsLanguages(true)
    }
  }, [getAllLanguages, AllLanguagesError, isInstitution])

  // Table columns
  const columns = getColumns(
    isInstitution,
    (isActive, id, languageName) => confirm(isActive, id, languageName),
    (file) => openFile(file),
    (data) => editLanguage(data)
  )

  // Conditionally remove columns based on the props
  const filteredColumns = columns.filter(
    (column) =>
      (isInstitution && column.title) !== "Status" &&
      (isInstitution && column.title) !== "Institution Count"
  )

  useEffect(() => {
    setselectedRowKeys(
      languages
        .filter((language: any) => language.mappingData && language.id)
        .map(({ id }: any) => id)
    )
  }, [languages])

  // rowSelection object indicates the need for row selection
  const rowSelection = {
    selectedRowKeys,
    onChange: (selectedRowKeys: React.Key[], selectedRows: any) => {
      setselectedRowKeys(selectedRowKeys)
      const removeColumns = (data: any, columnsToRemove: any) => {
        return data.map((item: any) => {
          const newItem = { ...item, languageId: item.id }
          if (item.mappingData !== null) {
            newItem["id"] = item.mappingData?.mappingId
          }

          const updatedColumns =
            item.mappingData !== null
              ? columnsToRemove.filter((column: any) => column !== "id")
              : columnsToRemove

          updatedColumns.forEach((column: string) => delete newItem[column])

          return newItem
        })
      }

      const filteredData = removeColumns(selectedRows, [
        "id",
        "key",
        "countryId",
        "countryName",
        "languageDisplayName",
        "languageName",
        "mappingData",
        "countryImageUrl",
        "resourceFileUrl"
      ])

      setMappedLanguages(filteredData)

      // set button state
      setIsButtonDisabled(false)
    }
    // ,
    // getCheckboxProps: (record: any) => ({
    //   disabled: record.mappingData?.isDefault
    // })
  }

  const confirm = (isActive: boolean, id: number, languageName: string) => {
    setLanguageName(languageName)

    saveLanguage({ isActive, id, languageName })
  }

  useEffect(() => {
    if (isSuccess) {
      showNotification(
        "success",
        "Notification",
        "You have updated the Languages successfully."
      )
      refetch()
    }
  }, [isSuccess])

  useEffect(() => {
    if (isSuccessMappedLanguage) {
      if (selectedDefaultLanguage !== null) {
        // Call make as default language
        makeDefault(selectedDefaultLanguage)
      } else {
        showNotification(
          "success",
          "Notification",
          "You have updated the Languages successfully."
        )

        // Reset mapped languages state
        setMappedLanguages([])

        // Disabled button
        setIsButtonDisabled(true)

        // hide loader
        setLoading(false)

        // fetch languages
        refetch()
      }
    }
  }, [isSuccessMappedLanguage])

  useEffect(() => {
    if (isSuccessMakeLanguageDefault) {
      showNotification(
        "success",
        "Notification",
        "You have updated the Languages successfully."
      )

      // Set null default language of state
      setSelectedDefaultLanguage(null)

      // Reset mapped languages state
      setMappedLanguages([])

      // Disabled button
      setIsButtonDisabled(true)

      // hide loader
      setLoading(false)

      refetch()
    }
  }, [isSuccessMakeLanguageDefault])

  useEffect(() => {
    if (saveLanguageError || mappedLanguageError || makeLanguageDefaultError) {
      if (saveLanguageError) {
        const data = JSON.parse(saveLanguageError?.message)
        if (data.message === "LANGUAGE_MAPPED_WITH_INSTITUTIONS") {
          setIsModal(true)

          setInstitutionsList(data.list)
        } else {
          showNotification(
            "error",
            "Notification",
            "Unable to update the Language try again later!"
          )
        }
      } else {
        showNotification(
          "error",
          "Notification",
          "Unable to update the Language try again later!"
        )
      }
    }
  }, [saveLanguageError, mappedLanguageError, makeLanguageDefaultError])

  useEffect(() => {
    if (
      selectedDefaultLanguage !== null &&
      !selectedRowKeys.includes(selectedDefaultLanguage)
    ) {
      setSelectedDefaultLanguage(null)
    }
  }, [mappedLanguages])

  const openFile = (file: string) => {
    window.open(file, "_blank")
  }

  const editLanguage = (data: any) => {
    navigate(RouteType.EDIT_LANGUAGE, { state: { data } })
  }

  const handleMappedLanguage = () => {
    const allMappedLanguages = languages
      .filter((item: any) => item.mappingData)
      .map((item: any) => {
        const obj = {
          id: item.mappingData?.mappingId,
          isActive: false,
          languageId: item.id
        }
        return obj
      })

    // Check for default language
    if (selectedDefaultLanguage === null) {
      showNotification(
        "error",
        "Notification",
        "Please select a default language to proceed"
      )
      return
    }

    // Disabled button
    setIsButtonDisabled(true)

    // set loader
    setLoading(true)

    const result: any = mergeArraysAndRemoveDuplicates(
      allMappedLanguages,
      mappedLanguages
    )
    mappedLanguage(result)
  }

  const mergeArraysAndRemoveDuplicates = (arr1: any[], arr2: any[]) => {
    const mergedArray = [...arr2, ...arr1]

    const removeDuplicates = (arr: any) => {
      const seen = new Set()
      return arr.filter((item: any) => {
        const identifier = item.id
          ? `${item.id}-${item.languageId}`
          : `${item.languageId}`
        if (seen.has(identifier)) {
          return false
        } else {
          seen.add(identifier)
          return true
        }
      })
    }

    const uniqueItemsMap = removeDuplicates(mergedArray)

    return Array.from(uniqueItemsMap.values())
  }

  const onChangeDefaultLanguage = (languageId: number) => {
    // update default language id
    setSelectedDefaultLanguage(languageId)

    // Enable the button to set language
    setIsButtonDisabled(false)
  }

  const makeDefault = (languageId: number) => {
    const payload = { languageId, isDefault: true }
    makeLanguageDefault(payload)
  }

  // Utility Functions
  const sortLanguagesByDefault = (languages: any[]) => {
    if (!isInstitution) {
      return languages
    }

    return languages.sort((a: any, b: any) => {
      const aIsDefault = a.mappingData?.isDefault || false
      const bIsDefault = b.mappingData?.isDefault || false

      if (aIsDefault === bIsDefault) {
        return 0
      }
      return aIsDefault ? -1 : 1
    })
  }

  if (isProduction()) {
    navigate(RouteType.HOME)
  }

  const handleModal = () => {
    setIsModal(false)
  }

  return isProduction() ? null : (
    <div>
      <PageHeading
        title="Languages"
        linkData={
          isInstitution || (languages && languages.length === 0)
            ? undefined
            : {
                label: "Add Language",
                url: RouteType.ADD_LANGUAGE
              }
        }
        buttonTitle={!isInstitution ? undefined : "Set Languages"}
        buttonDisabled={isInstitution && isButtonDisabled ? true : false}
        setMappedLanguage={handleMappedLanguage}
        loading={
          <ThreeDots
            height="25"
            width="25"
            color="#000000"
            ariaLabel="three-dots-loading"
            visible={loading}
          />
        }
      />
      <hr className="middle-line" />
      {!isLanguages && isFetchingAllLanguages ? (
        <div className="loader">
          <ThreeDots
            height="40"
            width="40"
            color="#26a6e0"
            ariaLabel="three-dots-loading"
            visible={true}
          />
        </div>
      ) : (
        <div className="ms-1">
          {!isFetchingAllLanguages &&
          languages !== undefined &&
          languages.length === 0 &&
          !isInstitution ? (
            <div className="text-center vertical-center">
              <img className="mb-3" src="/images/language.svg" />
              <h5
                style={{
                  fontWeight: 600,
                  fontSize: "19px",
                  font: "Rubik",
                  lineHeight: "21.33px"
                }}
              >
                No languages added yet
              </h5>
              {/* <p style={{ fontSize: "14px" }}>
                Write here lorem ipsum text - dummy text
              </p> */}
              <Link to={RouteType.ADD_LANGUAGE}>
                <Button
                  style={{
                    background: buttonColor,
                    color: "#000",
                    fontWeight: "500"
                  }}
                >
                  Add Language
                </Button>
              </Link>
            </div>
          ) : (
            <div className="row bg-white mt-1">
              <div className="table-responsive" style={{ marginTop: "8px" }}>
                <Table
                  columns={filteredColumns}
                  dataSource={sortLanguagesByDefault(
                    isInstitution
                      ? languages.filter((element: any) => element.isActive)
                      : languages
                  )}
                  size="middle"
                  className={
                    isInstitution ? `language languageOnly` : "language"
                  }
                  loading={{
                    indicator: (
                      <div className="loader" style={{ width: "40px" }}>
                        <ThreeDots
                          color="#26a6e0"
                          ariaLabel="three-dots-loading"
                          visible={true}
                        />
                      </div>
                    ),
                    spinning: isFetchingAllLanguages
                  }}
                  pagination={false}
                  locale={{ emptyText: "No language found!" }}
                  rowClassName={(record) =>
                    isInstitution && record.mappingData?.isDefault
                      ? "first-row"
                      : "default-row"
                  } // Apply custom class to row
                  rowSelection={isInstitution ? rowSelection : undefined}
                />
              </div>
            </div>
          )}
        </div>
      )}
      {/* Disable Language Info Modal */}
      {isModal && (
        <DisableLanguageInfo
          isModal={isModal}
          setIsModal={handleModal}
          institutionsList={institutionsList}
          languageName={languageName}
        />
      )}
    </div>
  )
}

export default Languages
