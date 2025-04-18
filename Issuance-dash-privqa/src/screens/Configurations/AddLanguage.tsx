import React, { ChangeEvent, useEffect, useState, useRef } from "react"
import PageHeading from "src/components/ui/PageHeading"
import { Form, Input, Select, Upload, Button, notification } from "antd"
import type { FormInstance } from "antd/es/form"
import { useSelector } from "react-redux"
import s3Client from "src/s3Config"
import { ROOTSTATE } from "src/utils/modules"
import {
  addLanguage as addLanguageType,
  updateLanguage as updateLanguageType
} from "src/api/models"
import {
  useGetCountryList,
  useAddLanguage,
  useUpdateLanguage
} from "src/api/user"
import { useLocation, useNavigate } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import isProduction from "src/utils/environmentUtils"
const languageIcon = "/images/languageIcon.svg"

const productionMode = isProduction()

const AddLanguage: React.FC = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const formRef = useRef<FormInstance>(null)
  const [form] = Form.useForm()
  const [countryList, setCountryList] = useState<any>([])
  const [isButtonDisabled, setIsButtonDisabled] = useState(true)
  const [editLanguageId, setEditLanguageId] = useState<number | null>(
    location?.state?.data?.id || null
  )
  const [isEnglish, setIsEnglish] = useState<boolean>(false)
  const [fromObj, setFromObj] = useState<addLanguageType | updateLanguageType>({
    languageName: "",
    languageDisplayName: "",
    countryId: null,
    resourceFileUrl: "",
    isActive: true
  })
  const [countryFlag, setCountryFlag] = useState<string | null>(null)
  const [oldFileUrl, setOldFileUrl] = useState<string | null>(null)

  // Get Country List API
  const {
    data: getCountryList,
    isFetching: isFetchingGetCountryList,
    error: getCountryListError
  } = useGetCountryList()

  // API Call Add Language
  const {
    mutate: addLanguage,
    error: addLanguageError,
    isSuccess: isSuccess
  } = useAddLanguage()

  // API Call Add Language
  const {
    mutate: updateLanguage,
    error: updateLanguageError,
    isSuccess: isSuccessUpdateLanguage
  } = useUpdateLanguage()

  useEffect(() => {
    if (getCountryList) {
      setCountryList(getCountryList)
    }

    if (getCountryListError) {
      notification["error"]({
        message: "Error",
        description: getCountryListError.message
      })
    }

    if (isSuccess) {
      const { languageDisplayName } = fromObj
      notification["success"]({
        message: "Notification",
        description: `You have added ${languageDisplayName} language successfully.`
      })

      // clear the from
      clearForm()

      navigate(RouteType.LANGUAGES)
    }

    if (isSuccessUpdateLanguage) {
      const { languageDisplayName } = fromObj
      notification["success"]({
        message: "Notification",
        description: `You have updated the Languages successfully.`
      })

      // clear the from
      clearForm()

      navigate(RouteType.LANGUAGES)
    }

    if (addLanguageError) {
      notification["error"]({
        message: "Notification",
        description: addLanguageError.message
      })

      // clear the from
      clearForm()
    }

    if (updateLanguageError) {
      notification["error"]({
        message: "Notification",
        description: updateLanguageError.message
      })
    }
  }, [
    getCountryList,
    getCountryListError,
    addLanguageError,
    isSuccess,
    isSuccessUpdateLanguage
  ])

  useEffect(() => {
    const { languageName, languageDisplayName, countryId, resourceFileUrl } =
      fromObj
    if (
      languageName !== "" &&
      languageDisplayName !== "" &&
      countryId !== null &&
      resourceFileUrl !== ""
    ) {
      setIsButtonDisabled(false)
    } else {
      setIsButtonDisabled(true)
    }
  }, [fromObj])

  useEffect(() => {
    const setInitialValues = async () => {
      const data = location?.state?.data

      if (data) {
        const {
          id,
          languageName,
          languageDisplayName,
          countryName,
          resourceFileUrl,
          countryImageUrl,
          isActive
        } = await data

        // set initial value
        form.setFieldsValue({
          languageName,
          languageDisplayName,
          countryName
        })

        const filterValue: any = await countryList?.find((obj: any) => {
          return countryName === obj.countryName ? obj.countryId : null
        })

        const { countryId } = filterValue

        // set from object state
        setFromObj({
          languageName,
          languageDisplayName,
          countryId,
          resourceFileUrl,
          isActive
        })

        // setIsEnglish
        if (languageName === "English") {
          setIsEnglish(true)
        } else {
          setIsEnglish(false)
        }

        //set Country Flag
        setCountryFlag(countryImageUrl)

        setEditLanguageId(id)
      }
    }
    setInitialValues()
  }, [location, countryList])

  const handleUpload = async (
    file: any,
    fileType: string,
    oldFileUrl: string | null = ""
  ) => {
    try {
      const newFileName = `${Date.now()}_${"translation"}`

      // Delete the old file if the URL is provided
      if (oldFileUrl !== null) {
        const oldFileName: any = oldFileUrl.split("/").pop() // Extract the file name from the URL
        await s3Client.deleteFile(oldFileName)
      }
      // return
      const data = await s3Client.uploadFile(file, newFileName)
      if (data.status === 204) {
        const obj: any = { ...fromObj }

        if (fileType === "langugageFile") {
          obj["resourceFileUrl"] = data.location
          setFromObj(obj)
        }
      } else {
        notification["success"]({
          message: "Error",
          description: `File upload failed.`
        })
      }
    } catch (error) {
      notification["error"]({
        message: "Error",
        description: `Error while uploading file`
      })
    }
  }

  const removeLanguageFile = () => {
    const obj: any = { ...fromObj }
    // set old url
    setOldFileUrl(obj.resourceFileUrl)
    obj["resourceFileUrl"] = ""
    setFromObj(obj)
  }

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target

    // Shallow copy of the object with the updated field value
    const updatedObj: addLanguageType = { ...fromObj, [name]: value }

    // Set the updated object
    setFromObj(updatedObj)
  }

  const handleSelect = async (value: string) => {
    const filterValue: any = await getCountryList?.find((obj) => {
      return value === obj.countryName ? obj.countryId : null
    })

    const { countryId, countryImageUrl } = filterValue

    //  set setCountry Flag based on the selected country
    setCountryFlag(countryImageUrl)

    // Shallow copy of the object with the updated field value
    const updatedObj: addLanguageType = {
      ...fromObj,
      countryId: countryId
    }

    // Set the updated object
    setFromObj(updatedObj)
  }

  const handleAddLanguage = () => {
    // add and update language
    if (editLanguageId) {
      updateLanguage({ ...fromObj, ["id"]: editLanguageId })
    } else {
      addLanguage(fromObj)
    }
  }

  const clearForm = () => {
    if (formRef.current) {
      formRef.current.resetFields()
      setFromObj({
        languageName: "",
        languageDisplayName: "",
        countryId: null,
        resourceFileUrl: "",
        isActive: true
      })
      setIsButtonDisabled(true)
    }
  }

  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  const beforeUpload = (file: any) => {
    const isJson = file.type === "application/json"
    if (!isJson) {
      notification["error"]({
        message: "Error",
        description: "File format not supported!"
      })
    }
    return isJson
  }

  if (productionMode) {
    navigate(RouteType.HOME)
  }

  console.log(">>>>>>>>>>>> mohit editLanguageId", editLanguageId)

  return productionMode ? null : (
    <div>
      <PageHeading
        title={editLanguageId ? "Edit Language" : "Add Language"}
        backIcon={true}
      />
      <div
        className="row bg-white institution-div p-4"
        style={{ minHeight: 500 }}
      >
        <div className="col-lg-3">
          <Form form={form} layout="vertical" ref={formRef}>
            <Form.Item
              label="Language Name"
              name="languageName"
              initialValue={fromObj.languageName}
            >
              <Input
                placeholder="Enter language name"
                name="languageName"
                onChange={handleChange}
                disabled={
                  isEnglish && fromObj.languageName === "English" ? true : false
                }
              />
            </Form.Item>
            <Form.Item label="Display Language" name="languageDisplayName">
              <Input
                placeholder="Enter display language"
                name="languageDisplayName"
                onChange={handleChange}
              />
            </Form.Item>
            <Form.Item label="Country Name" name="countryName">
              <Select
                showSearch
                placeholder="Select"
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                disabled={isFetchingGetCountryList}
                onChange={handleSelect}
              >
                {getCountryList?.map((obj, key) => {
                  const { countryName } = obj

                  return (
                    <Select.Option key={key} value={countryName}>
                      {countryName}
                    </Select.Option>
                  )
                })}
              </Select>
            </Form.Item>
            {countryFlag !== null && (
              <Form.Item label="Country Flag">
                <img
                  width={50}
                  src={countryFlag}
                  alt="Country Flag"
                  style={{ border: "1px solid #E0E0E0" }}
                />
              </Form.Item>
            )}
            <Form.Item label="PO (Language) File">
              {fromObj.resourceFileUrl !== "" ? (
                <>
                  <Button>
                    {" "}
                    <img
                      style={{ verticalAlign: "text-top", marginTop: "2px" }}
                      src={languageIcon}
                      className="flagIcon"
                    />
                    &nbsp; Language.po
                  </Button>
                  <Button
                    onClick={removeLanguageFile}
                    type="link"
                    style={{ color: "#197EDC" }}
                  >
                    Remove
                  </Button>
                </>
              ) : (
                <Upload
                  accept=".json"
                  customRequest={({ file }) =>
                    handleUpload(file, "langugageFile", oldFileUrl)
                  }
                  showUploadList={false}
                  beforeUpload={beforeUpload}
                >
                  <Button
                    icon={
                      <img
                        style={{ verticalAlign: "text-top", marginTop: "2px" }}
                        src={languageIcon}
                        className="flagIcon"
                      />
                    }
                  >
                    &nbsp;<span style={{ color: "#006BE2" }}>Upload</span>
                  </Button>
                </Upload>
              )}
            </Form.Item>
            <Button
              size="large"
              style={{
                background: isButtonDisabled ? "rgb(255 217 128)" : buttonColor,
                color: isButtonDisabled ? "rgb(108 108 108)" : "#000",
                fontWeight: "500",
                border: isButtonDisabled ? "#fff" : ""
              }}
              onClick={handleAddLanguage}
              disabled={isButtonDisabled}
            >
              {editLanguageId ? "Save Language" : "Add Language"}
            </Button>
          </Form>
        </div>
      </div>
    </div>
  )
}

export default AddLanguage
