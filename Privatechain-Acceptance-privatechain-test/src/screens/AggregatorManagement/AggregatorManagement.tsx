import React, { useState, useEffect, useRef, useMemo } from "react";
import { Link, useNavigate } from "react-router-dom";
import PageHeading from "src/components/ui/PageHeading";
import { RouteType } from "src/constants/routeTypes";
import {
  useGetAggregatorDetails,
  useDeleteAggregator,
  useUpdateAggregatorDetails,
  SaveAggregatorFromFile,
} from "src/api/user";
import AggregatorHierarchy from "./AggregatorHierarchy";
import {
  Button,
  Table,
  Input,
  Form,
  Select,
  Dropdown,
  Space,
  MenuProps,
  Popconfirm,
  Typography,
  notification,
  Empty,
  TableColumnType,
  InputRef,
} from "antd";
import type { FilterDropdownProps } from 'antd/es/table/interface';

import "./AggregatorTree.scss";
import dayjs from "dayjs";
import autoTable from "jspdf-autotable";
import jsPDF from "jspdf";
import { UrlUpload } from "src/api/constants";
import { SearchOutlined } from "@ant-design/icons";
import { GetAggregatorList } from "src/api/models";
import Highlighter from "react-highlight-words";
import _ from "lodash";

const AggregatorManagement: React.FC = () => {
    
  const [pageSize, setPageSize] = useState(10)
  const [totalItems, setTotalItems] = useState(0);
  const navigate = useNavigate();
  const childRef = useRef<any>();
  const [loading, setLoading] = useState<boolean>(true);
  const [aggregator, setAggregatorList] = useState<any>([]);
  const [aggregatorDetails, setAggregatorDetails] = useState<any>();
  const [currentPage, setCurrentPage] = useState<number>();
  const [search, setSearch] = useState<string>("");
  const [days, setDays] = useState(0)
  const [searchBy, setSearchBy] = useState<string>("");
  const [filterBy, setFilterBy] = useState<string>("");
  const [reloadData, setReloadData] = useState<boolean>(false)
  const [uploadType, setUploadType] = useState('AGGREGATOR_UPLOAD')
  const [searchedColumn, setSearchedColumn] = useState('');
  const [searchText, setSearchText] = useState('');
  const searchInput = useRef<InputRef>(null);
  const [pageable, setPageable] = useState({
    current: 1,
    pageSize: 10,
    total: 0})
  

  // get Get Aggregator Details list API
  const {
    mutate: getAggregatorDetails,
    data,
    error,
  } = useGetAggregatorDetails();
  const {
    mutate: getAggregatorDetailsNoPage,
    data: dataNoPage,
    error: errorNoPage,
  } = useGetAggregatorDetails();


  // API Call Delete Aggregator
  const {
    mutate: deleteAggregatorById,
    error: deleteAggregatorError,
    isSuccess,
  } = useDeleteAggregator();

  // API Call Update Aggregator Details
  const {
    mutate: updateAggregatorDetails,
    error: updateAggregatorDetailsError,
    isSuccess: isSuccessUpdateAggregator,
  } = useUpdateAggregatorDetails();

  const [bulkFile, setBulkFile] = useState();
  const [bulkArray, setBulkArray] = useState([]);

  const bulkFileReader = new FileReader();

  const handleOnChange = (e: any) => {
    setBulkFile(e.target.files[0]);
  };

  const csvFileToArray = (inputString: string) => {
    const csvHeader = inputString.slice(0, inputString.indexOf("\n")).split(",");
    const csvRows = inputString.slice(inputString.indexOf("\n") + 1).split("\n");

    const array = csvRows.map(i => {
      const values = i.split(",");
      const obj = csvHeader.reduce((object: any, header, index) => {
        object[header] = values[index];
        return object;
      }, {});

      return obj;
    });
    setBulkArray(bulkArray);
  };

 const daysOptions=[{key:30,value:"30 days"},
 {key:90,value:"90 days"},
 {key:365,value:"1 year"},{key:0,value:'Till Date'}]
  const handleOnSubmit = (e: any) => {
    e.preventDefault();

    if (bulkFile) {
      bulkFileReader.onload = function (event) {
        if (event && event.target) {
          const text: any = event.target.result;
          if (text)
            csvFileToArray(text);

        }
      };

      bulkFileReader.readAsText(bulkFile);
    }
  };
  const bulkprops = {
    action: 'https://run.mocky.io/v3/435e224c-44fb-4773-9faf-380c5e6a2188',
    onChange: handleOnChange,
    multiple: true,
  };
  const headerKeys = Object.keys(Object.assign({}, ...bulkArray));

  useEffect(() => {
    if (data) {
      setAggregatorList(data);
      setLoading(false);
    }
  }, [data]);
useEffect(() => {
  if(isSuccessUpdateAggregator)
  {childRef.current?.getTreeList()}

}, [isSuccessUpdateAggregator])

  useEffect(() => {
    if (reloadData) {
      getAggregatorList()
      childRef.current?.getTreeList()
      setReloadData(false)
    }
  }, [reloadData])

  useEffect(() => {
    getAggregatorList();
  }, [searchBy]);

  useEffect(() => {
    if (filterBy !== ""||days) {
      getAggregatorList();
    }
  }, [filterBy, searchBy,days]);

  const getAggregatorList = () => {
    const requestParams: any = {
      page: currentPage || 1,
      duration:days,
      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: pageSize,
    };

    if (filterBy != "" && searchBy != "") {
      requestParams[filterBy] = searchBy;
    } else {
      delete requestParams[filterBy];
    }

    // show loader
    setLoading(true);
    // API CALL
    getAggregatorDetails(requestParams);
    let requestParamsNoPage = Object.assign({}, requestParams);
    requestParamsNoPage.page = 0
    requestParamsNoPage.limit = 0
    getAggregatorDetailsNoPage(requestParamsNoPage);
  };

  useEffect(() => {
    getAggregatorList();
  }, [currentPage, pageSize]);


  useEffect(() => {
    if (reloadData) {
      getAggregatorList()
      childRef.current?.getTreeList()
      setReloadData(false)
    }
  }, [reloadData])

  useEffect(() => {
    getAggregatorList();
  }, [searchBy]);

  useEffect(() => {
    if (filterBy !== "") {
      getAggregatorList();
    }
  }, [filterBy, searchBy]);  

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Aggregator deleted successfully.",
      });
      getAggregatorList();
      childRef.current?.getTreeList();
    }
    if (deleteAggregatorError || updateAggregatorDetailsError) {
      notification["error"]({
        message: "Notification",
        description: "Something went wrong.",
      });
    }
  }, [isSuccess, deleteAggregatorError, updateAggregatorDetailsError]);

  // Filter institutions
  const items: MenuProps["items"] = [
    {
      key: "1",
      label: <a>CSV</a>,
    },
    {
      key: "2",
      label: <a>PDF</a>,
    },
  ];

  // Action items
  const actionItems: any = [

    {
      key: "3",
      label: (
        <Popconfirm
          title="Sure to Close?"
          onConfirm={() => closeAggregator()}
        >
          <span>Close</span>
        </Popconfirm>
      ),
    },
  ];

  const actionItemsActive: any = [
    {
      key: "1",
      label: <a onClick={() => updateAggregatorRow()}>Edit</a>,
    },
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockAggregator()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
    /* {
      key: "3",
      label: <a onClick={() => deActivateAggregator()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeAggregator()}>close</a>,
    },
  ];
  
  const actionItemsPending: any = [
    {
      key: "1",
      label: <a onClick={() => updateAggregatorRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => closeAggregator()}>close</a>,
    },
  ];

  const actionItemsUnblock: any = [
    {
      key: "2",
      label: (
        <Popconfirm
          title="Sure to unblock?"
          onConfirm={() => blockAggregator()}
        >
          <span>Unblock</span>
        </Popconfirm>
      ),
    },
   /*  {
      key: "3",
      label: <a onClick={() => deActivateAggregator()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeAggregator()}>close</a>,
    }
  ];
  const getColumnSearchProps = (dataIndex: DataIndex): TableColumnType<GetAggregatorList> => ({
    filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters, close }) => (
      <div style={{ padding: 8 }} onKeyDown={(e) => e.stopPropagation()}>
        <Input aria-autocomplete='both' aria-haspopup="false"
          ref={searchInput}
          placeholder={`Search ${dataIndex}`}
          value={selectedKeys[0]}
/*           autoComplete="true" 
 */          onChange={(e) => {
           // setSearchBy(e.target.value)            
           // setFilterBy(dataIndex!=='aggregatorPreferenceId'?dataIndex:'aggregatorId')
            setSelectedKeys(e.target.value ? [e.target.value] : [])}}
          onPressEnter={() => {handleSearch(selectedKeys as string[], confirm, dataIndex)}}
          style={{ marginBottom: 8, display: 'block' }}
        />
        <Space>
          <Button
            type="primary"
            onClick={() => handleSearch(selectedKeys as string[], confirm, dataIndex)}
            icon={<SearchOutlined rev={1} />}
            size="small"
            style={{ width: 90 }}
          >
            Search
          </Button>
          <Button
            onClick={() => clearFilters && handleReset(clearFilters)}
            size="small"
            style={{ width: 90 }}
          >
            Reset
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => {
              confirm({ closeDropdown: false });
              setSearchText((selectedKeys as string[])[0]);
              setSearchedColumn(dataIndex);
            }}
          >
            Filter
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => {
              close();
            }}
          >
            close
          </Button>
        </Space>
      </div>
    ),
    filterIcon: (filtered: boolean) => (
      <SearchOutlined rev={1}style={{ color: filtered ? '#1677ff' : undefined }} />
    ),
    onFilter: (value, record) =>
      record[dataIndex]
        .toString()
        .toLowerCase()
        .includes((value as string).toLowerCase()),
    onFilterDropdownOpenChange: (visible) => {
      if (visible) {
        setTimeout(() => searchInput.current?.select(), 100);
      }
    } ,
    render: (text) =>{
      if(dataIndex=='aggregatorStatus'){
    return    text === "active" || text === "pending"||text==="draft" ? (
          text === "active" ? (
            <>
              <img src={"/images/active-icon.svg"} /> {"Active"}
            </>
          ) : (
            text === "pending" ? <>
            <img src={"/images/pendng-icon.svg"} /> {"Pending"}
          </>:
            <>
              <img src={"/images/pendng-icon.svg"} /> {"Draft"}
            </>
          )
        ) : (
          text === "de-active" ? <>
          <img src={"/images/de-active.svg"} /> {"De-Active"}
        </>:(
          text === "closed" ? <>
          <img src={"/images/closed-icon.svg"} /> {"Closed"}
        </>:
          <span style={{ color: "#D15241" }}>
            <img src={"/images/blocked-icon.svg"} /> {"Block"}
          </span>)
        )
      }
    return  searchedColumn === dataIndex ? (
        <Highlighter
          highlightStyle={{ backgroundColor: '#ffc069', padding: 0 }}
          searchWords={[searchText]}
          autoEscape
          textToHighlight={text ? text.toString() : ''}
        />
      ) : (
        text
      )}, 
  });
  // Table columns
  const columns = [
    {
      title: "",
      dataIndex: "aggregatorLogo",
      key: "aggregatorLogo",
      render: (aggregatorLogo: string) => (
        <img src={aggregatorLogo} style={{ height: "40px", width: "40px" }} />
      ),
    },
    {
      title: "AGGREGATOR NAME",
      dataIndex: "aggregatorName",
      key: "aggregatorName",
      render: (aggregatorName: string) => {
       return <span style={{ color: "#3080c5", fontWeight: "500" }}>
          {aggregatorName}
        </span>
      },
      ...getColumnSearchProps('aggregatorName')

    },
    {
      title: "AGGREGATOR ID",
      dataIndex: "aggregatorPreferenceId",
      key: "aggregatorPreferenceId",
      ...getColumnSearchProps('aggregatorPreferenceId')

    },
    {
      title: "STATUS",
       dataIndex: "aggregatorStatus",
      key: "aggregatorStatus",
      render: (aggregatorStatus: string) =>
      aggregatorStatus === "active" || aggregatorStatus === "pending" ? (
          aggregatorStatus === "active" ? (
            <>
              <img src={"/images/active-icon.svg"} /> {"Active"}
            </>
          ) : (
            <>
              <img src={"/images/pendng-icon.svg"} /> {"Pending"}
            </>
          )
        ) : (
          <span style={{ color: "#D15241" }}>
            <img src={"/images/blocked-icon.svg"} /> {"Block"}
          </span>
        ),
         ...getColumnSearchProps('aggregatorStatus')

    },
    {
      title: "",
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...(record?.aggregatorStatus ==="closed"?[]:(record?.aggregatorStatus === "active" ?actionItemsActive:
                record?.aggregatorStatus === "pending"||record?.aggregatorStatus === "de-active"||record?.aggregatorStatus === "draft" 
                ? actionItemsPending
                : actionItemsUnblock)),
            ],
          }}
          trigger={["click"]}
          arrow
        >
          <Typography.Link onClick={() => setAggregatorDetails(record)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
  ];
  type DataIndex = keyof GetAggregatorList;

  const handleChange = (e: any, name: string) => {
    if (name === "pagination") {
      setCurrentPage(e);
    }
    if (name === "filterBy") {
      setFilterBy(e);
    }
    if (name === "searchBy") {
      setSearchBy(e.target.value);
    }
  };

  const updateAggregatorRow = () => {
    navigate(RouteType.AGGREGATOR_UPDATE, {
      state: {
        ...aggregatorDetails,
      },
    });
    setAggregatorDetails({});
  };

  const blockAggregator = () => {
    aggregatorDetails["aggregatorStatus"] =
        aggregatorDetails.aggregatorStatus === "active"
        ? "block"
        : "active";
    updateAggregatorDetails(aggregatorDetails);
    setAggregatorDetails({});
  };

  const deActivateAggregator = () => {
    aggregatorDetails["aggregatorStatus"] = "de-active"
    updateAggregatorDetails(aggregatorDetails);
    setAggregatorDetails({});
  };
  const closeAggregator = () => {
    aggregatorDetails["aggregatorStatus"] = "closed"
    updateAggregatorDetails(aggregatorDetails);
    setAggregatorDetails({});
  };



  const deleteAggregator = () => {
    deleteAggregatorById({
      aggregatorId: aggregatorDetails.aggregatorPreferenceId,
    });
    setAggregatorDetails({});
  };
  const handleDropdownItemClick = (e: any) => {
    //getAggregatorReport(aggregator)
    if (e.key == 1) {
      downloadAggregatorsCSV()
    }
    else if (e.key == 2) {
      downloadTransactionsPDF()
    }
  }
  const handleSearch = (
    selectedKeys: string[],
    confirm: FilterDropdownProps['confirm'],
    dataIndex: DataIndex,
  ) => {
    confirm();
    setSearchBy(selectedKeys[0])
    setFilterBy(dataIndex)
    setSearchText(selectedKeys[0]);
    setSearchedColumn(dataIndex);
  };

  const handleReset = (clearFilters: () => void) => {
    setSearchBy('')
    setFilterBy('')
    clearFilters();
    setSearchText('');
  };

  
  const downloadAggregatorsCSV = () => {
    JSONToCSVConvertor(dataNoPage, "Aggregators", true)
  }
  const JSONToCSVConvertor = (
    JSONData: any,
    ReportTitle: string,
    ShowLabel: boolean
  ) => {
    const arrData =
      typeof JSONData !== "object" ? JSON.parse(JSONData) : JSONData;
    let CSV = "";
    const headersRequried = [
      "aggregatorName",
      "aggregatorPreferenceId",
      "aggregatorStatus"
    ];
    if (ShowLabel) {
      let row = "";
      for (const index in arrData?.aggregatorList[0]) {
        switch (index) {
          case "aggregatorName":
            row += "Aggregator Name" + ",";
            break;
          case "aggregatorPreferenceId":
            row += "Aggregator ID" + ",";

            break;
          case "aggregatorStatus":
            row += "Status" + ",";
            break;
          default:
            break;
        }
      }
      row = row.slice(0, -1);
      CSV += row + "\r\n";
    }
    for (let i = 0; i < arrData?.aggregatorList.length; i++) {
      let row = "";
      for (const index in arrData.aggregatorList[i]) {
        if (headersRequried.includes(index)) {
       if(index==="aggregatorStatus"){
        let status:string=arrData?.aggregatorList[i][index].toString()
        status=status.slice(0,1).toUpperCase()+status.substring(1)
        row += '"' + status + '",';

       }else{
          row += '"' + arrData?.aggregatorList[i][index] + '",';
       }
        }
      }
      row.slice(0, row.length - 1);
      CSV += row + "\r\n";
    }
    if (CSV === "") {
      return;
    }
    let fileName = "MyReport_";
    fileName += ReportTitle.replace(/ /g, "_");
    const uri = "data:text/csv;charset=utf-8,%EF%BB%BF" + encodeURI(CSV);
    const link = document.createElement("a");
    link.href = uri;
    link.style.visibility = "hidden";
    link.download = fileName + ".csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link); 
  };
  const downloadTransactionsPDF = () => {
    if (dataNoPage) {
      const currentData = typeof dataNoPage !== "object" ? JSON.parse(dataNoPage) : dataNoPage;
      const head = [
        [
          "Aggregator Name",
          "Aggregator ID",
          "Status"
        ]
      ]
      const finalData: any = []
      currentData?.aggregatorList?.map((item: any, index: any) => {
        const arr = []
        if (Object.prototype.hasOwnProperty.call(item, "aggregatorName")) {
          arr.push(item.aggregatorName)
        }
        if (Object.prototype.hasOwnProperty.call(item, "aggregatorPreferenceId")) {
          arr.push(item.aggregatorPreferenceId)
        }
        if (Object.prototype.hasOwnProperty.call(item, "aggregatorStatus")) {
          arr.push(_.camelCase(item.aggregatorStatus))
        }
        finalData.push(arr)
      })
      const doc = new jsPDF()
      autoTable(doc, {
        head: head,
        body: finalData,
        columnStyles: {
          0: { cellWidth: 35 },
          1: { cellWidth: 58 },
          5: { cellWidth: 20 }
        }
      })

      doc.save("myReports-Aggregators.pdf")
    }
  }

  const handleUpload = (info:any) => {
    console.log(typeof info.file)
    const reader = new FileReader()
    reader.onload = (e:any) => {
        csvFileToArray(e.target.result)
        if (info.file.status === 'uploading'){
          info.file.status = 'done'
        }

    };
    reader.readAsBinaryString(info.file.originFileObj);
    
    if (info.file.status === 'uploading') {
      // Handle successful upload (e.g., parse CSV data)
      const csvData = info.file.response; // Assuming server returns CSV data
      console.log('CSV data:', csvData);
    } else if (info.file.status === 'error') {
    }
  };
  const setRecords=(files:any)=>{
setBulkArray(files)
  }

  const handleDaysChange = (value: string) => {
setDays(Number.parseInt(value))
  };
  const handlePaginationChange = (pagination:any) => {
    setCurrentPage(pagination);
    setPageSize(pageSize);
  };

  
  return (
    <div style={{ width: "101%" }}>
      <PageHeading
        topTitle="Aggregator management"
        title="Acquirer Aggregator Management"
        linkData={{
          label: "Register New Aggregator",
          url: RouteType.AGGREGATOR_REGISTER,
        }}
         bulkUpload={handleUpload}
        bulkprops={bulkprops}
        uploadType={"AGGREGATOR_UPLOAD"}
        setFiles={setRecords}
        parentRefetch={setReloadData}
        showRegister={aggregator?.aggregatorList?.length!=0}

      />
      {data !== undefined &&
        aggregator?.aggregatorList?.length === 0 &&
        searchBy == "" ? (
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
                lineHeight: "21.33px",
              }}
            >
              No Aggregators are available yet to manage
            </h5>
            <p style={{ fontSize: "14px" }}>
              Want to create Aggregator? Please click on the below button
            </p>
           
            <Link
              to={RouteType.AGGREGATOR_REGISTER}
              title="Register New Aggregator"
            >
              <Button
                style={{
                  background: "#26a6e0",
                  color: "#ffffff",
                }}
              >
                Register New Aggregator
              </Button>
            </Link>
          </div>
        </>
      ) : (
        <>
          <div className="row bg-white mt-1">
            <div style={{ display: "flex" }}>
              <AggregatorHierarchy childRef={childRef} />
              <div style={{ width: "100%" }} className="ms-3">
                <div className="row bg-white rounded mt-3">
                  <div className="d-sm-flex align-items-center justify-content-between">
                    <div className="col-xl-3 col-lg-6 col-sm-6 mt-3 searchByselection">
                      <Form.Item>
                        <Input.Group compact>
                          <Select
                            placeholder="Select By"
                            onChange={(e) => handleChange(e, "filterBy")}
                            options={[
                              {
                                value: "aggregatorName",
                                label: "Aggregator Name",
                              },
                              {
                                value: "aggregatorId",
                                label: "Aggregator ID",
                              },
                              { value: "status", label: "status" },
                            ]}
                            style={{ width: "50%" }}
                            suffixIcon={
                              <img src={"/images/down-arrow.svg"} />
                            }
                          />
                          {searchBy?.length == 0 ? (
                            <span className="ant-picker-suffix">
                              <span
                                role="img"
                                aria-label="calendar"
                                className="anticon anticon-calendar"
                              >
                                <img
                                  className="searchIcon"
                                  src={"/images/search-icon.svg"}
                                />
                              </span>
                            </span>
                          ) : null}
                          <Input aria-autocomplete='both' aria-haspopup="false"
                          type="search"
/*                           autoComplete="true" 
 */                            onChange={(e) => handleChange(e, "searchBy")}
                            style={{ width: "50%" }}
                            allowClear
                          />
                        </Input.Group>
                      </Form.Item>
                    </div>
                    <div className="">
                      <Select
                      onChange={handleDaysChange}
                        placeholder="Please select"
                        defaultValue={days>0?`Last ${days.toString()} days`:'Till Date'}
                        value={days>0?`Last ${days.toString()} days`:'Till Date'}
                        style={{ width: "150%" }}
                        suffixIcon={<img src={"/images/down-arrow.svg"} />}
                      >
                       {daysOptions.map(e=> <Select.Option value={e.key}
                                               >
                          <img
                            style={{ marginTop: "-3px" }}
                            src={"/images/time-icon.svg"}
                          />{" "}
                          {Number.parseInt(e.value)>0?`Last ${e.value}`:e.value}
                        </Select.Option>)}
                      </Select>
                    </div>
                    &nbsp;
                    
                    <div className="">
                      <Dropdown
                        menu={{
                          onClick: handleDropdownItemClick,
                          items: items
                          
                        }}
                      
                        trigger={["click"]} arrow>
                        <Button
                          style={{
                            background: "rgb(250, 173, 20)",
                            color: "#000",
                            border: "none",
                            fontWeight: 400,
                          }}
                        >
                          <Space>Export to</Space>&nbsp;
                          <img src={"/images/down-arrow.svg"} />
                        </Button>
                      </Dropdown>
                    </div>
                  </div>
                </div>
                <div className="tableHeader more-screen-size-600"></div>
                <div className="table-responsive">
                  <Table
                    columns={columns}
                    dataSource={aggregator?.aggregatorList}
                    size="middle"
                    className="table-custom"
/*                     pagination={{ pageSize: 10 }}
 */                     pagination={{
                      current: aggregator?.pagination?.current_page,
                      pageSize: pageSize,
                      total: aggregator?.pagination?.total_records,
                      onChange: handlePaginationChange,
                      showTotal: (total, range) => {
                        // setTableCount(total); // store the total count in a hook
                         return `${range?.toString().replace(',','-')} of ${total}`; // return an empty string unless you want to show it next to the pagination
                       }
 
                    }} 
              
/*                     pagination={}
 */                    loading={loading}
                    locale={{
                      emptyText: (
                        <Empty
                          image={Empty.PRESENTED_IMAGE_SIMPLE}
                          description="No aggregators found"
                        />
                      ),
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default AggregatorManagement;
