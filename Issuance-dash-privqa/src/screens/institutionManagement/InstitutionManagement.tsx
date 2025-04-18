import React, { useState, useEffect } from "react"
import { Link } from "react-router-dom"
import { Button, Table, Input, Select, Dropdown, Space, MenuProps } from "antd"
import PageHeading from "src/components/ui/PageHeading"
import { RouteType } from "src/constants/routeTypes"
import { useGetInstitutionDetails } from "src/api/user"
import { useSelector } from "react-redux"
import { ROOTSTATE } from "src/utils/modules"

const InstitutionManagement = () => {
  const [loading, setLoading] = useState<boolean>(true)
  const [institutions, setInstitutionList] = useState<any>([])
  const [search, setSearch] = useState<boolean>(false)

  // get Get Institution Details list API
  const { data, isFetching, error, refetch } = useGetInstitutionDetails()

  useEffect(() => {
    if (data) {
      setInstitutionList(data)
      setLoading(false)
    } else {
      setLoading(false)
    }
  }, [data])

  // Table columns
  const columns = [
    {
      title: "Institution Name",
      dataIndex: "institutionId",
      render: (data: any) => <>{`${data.value}`}</>
    },
    {
      title: "ID",
      dataIndex: "id"
    },
    {
      title: "Type",
      dataIndex: "type",
      render: (data: any) => (data == null ? "-" : data?.value)
    },
    {
      title: "Super Admin",
      dataIndex: "superAdmin",
      render: (data: any) => (data == null ? "-" : data?.value)
    },
    {
      title: "Contact Person",
      dataIndex: "contactPerson",
      render: (data: any) => (data == null ? "-" : data?.value)
    },
    {
      title: "Fiat Currency",
      dataIndex: "fiatCurrency",
      render: (data: any) => (data == null ? "-" : data?.value)
    },
    {
      title: "Country",
      dataIndex: "country",
      render: (data: any) => (data == null ? "-" : data?.value)
    },
    {
      title: "Created On",
      dataIndex: "createdOn",
      render: (data: any) => (data == null ? "-" : data?.value)
    },
    {
      title: "Created By",
      dataIndex: "createdBy",
      render: (data: any) => (data == null ? "-" : data?.value)
    },
    {
      title: "Status",
      dataIndex: "status",
      render: (data: any) => (data == null ? "-" : data?.value)
    },
    {
      title: "",
      dataIndex: "action",
      render: (data: any) => (
        <>
          <img src={"/images/configurations/action-icon.svg"} />
        </>
      )
    }
  ]

  // Search institutions
  const handleChange = (e: any, name: string) => {
    if (name === "search") {
      setSearch(e.target.value === "" ? false : true)
    }
  }

  // Filter institutions
  const items: MenuProps["items"] = [
    {
      key: "1",
      label: <a>CSV</a>
    },
    {
      key: "2",
      label: <a>PDF</a>
    }
  ]

  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  return (
    <>
      <PageHeading
        title="Institution Management"
        linkData={
          data !== undefined && institutions.length === 0
            ? ""
            : {
                label: "Register New Institute",
                url: RouteType.INSTITUTION_REGISTER
              }
        }
      />
      <div className="p-2 ms-1">
        {data !== undefined && institutions.length === 0 ? (
          <>
            <hr
              style={{ height: "1px", background: "#ECECEC", border: "none" }}
            />
            <div className="text-center vertical-center">
              <h5
                style={{
                  fontWeight: 600,
                  fontSize: "19px",
                  font: "Rubik",
                  lineHeight: "21.33px"
                }}
              >
                No institutions are available yet to manage
              </h5>
              <p style={{ fontSize: "14px" }}>
                Want to create institute? Please click on the below button
              </p>
              <Link
                to={RouteType.INSTITUTION_REGISTER}
                title="Register New Institute"
              >
                <Button
                  style={{
                    background: "#26a6e0",
                    color: "#ffffff"
                  }}
                >
                  Register New Institute
                </Button>
              </Link>
            </div>
          </>
        ) : (
          <>
            <div className="row bg-white rounded mt-2">
              <div className="d-sm-flex align-items-center justify-content-between">
                <div className="col-xl-8 col-lg-6 col-sm-12 mt-2 mb-2">
                  <Input
                    type="search"
                    placeholder="Search"
                    onChange={(e) => handleChange(e, "search")}
                    style={{ width: "30%" }}
                    suffix={
                      !search ? (
                        <img src={"/images/wallets/search-icon.svg"} />
                      ) : (
                        <span style={{ display: "none" }} />
                      )
                    }
                  />
                </div>
                <div className="">
                  <Select
                    placeholder="Please select"
                    defaultValue={null}
                    value={null}
                    onChange={(e) => handleChange(e, "walletBalanceBy")}
                    style={{ width: "100%" }}
                    suffixIcon={<img src={"/images/down-arrow.svg"} />}
                  >
                    <Select.Option value={null}>
                      <img
                        style={{ marginTop: "-3px" }}
                        src={"/images/time-icon.svg"}
                      />{" "}
                      Last 30 days
                    </Select.Option>
                  </Select>
                </div>
                <div className="">
                  <Dropdown menu={{ items }} trigger={["click"]} arrow>
                    <Button>
                      <img
                        style={{ marginTop: "-2x" }}
                        src={"/images/filter-icon.svg"}
                      />
                      &nbsp;&nbsp;
                      <Space>Add Filter</Space>
                    </Button>
                  </Dropdown>
                </div>
                <div className="">
                  <Dropdown menu={{ items }} trigger={["click"]} arrow>
                    <Button
                      style={{
                        background: buttonColor,
                        color: "#000",
                        border: "none",
                        fontWeight: 400
                      }}
                    >
                      <Space>Export to</Space>&nbsp;
                      <img src={"/images/down-arrow.svg"} />
                    </Button>
                  </Dropdown>
                </div>
              </div>
            </div>
            <div className="row bg-white mt-1">
              <>
                <div className="table-responsive">
                  <Table
                    columns={columns}
                    dataSource={institutions}
                    size="middle"
                    className="table-custom table-common"
                    loading={loading}
                    pagination={false}
                    locale={{ emptyText: "No institution found" }}
                  />
                  {/* {institutions?.pagination.total_records > 0 ? (
                <Pagination
                  pageSize={pageSize}
                  current={WalletUser?.pagination.current_page}
                  total={WalletUser?.pagination.total_records}
                  onChange={(e) => handleChange(e, "pagination")}
                  prevIcon={
                    <>
                      <img src={"/images/wallets/arrow-previous.svg"} />
                      <img src={"/images/wallets/arrow-previous.svg"} />
                    </>
                  }
                  nextIcon={
                    <>
                      <img src={"/images/wallets/arrow-next.svg"} />
                      <img src={"/images/wallets/arrow-next.svg"} />
                    </>
                  }
                  className={"pagination-custom"}
                />
              ) : null} */}
                </div>
              </>
            </div>
          </>
        )}
      </div>
    </>
  )
}

export default InstitutionManagement
