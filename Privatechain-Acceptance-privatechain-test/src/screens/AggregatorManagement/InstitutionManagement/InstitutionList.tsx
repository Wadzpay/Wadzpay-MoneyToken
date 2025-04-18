import React, { useState, useEffect, useRef, useMemo } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  Space,
  Table,
  Empty,
  Dropdown,
  Select,
  MenuProps,
  Input,
  Form,
  Button,
  notification,
  Typography,
  Popconfirm,
  UploadFile,
  message,
  Upload,
  TableColumnType,
  InputRef,
} from "antd";
import type { ColumnsType } from "antd/es/table";

import { RouteType } from "src/constants/routeTypes";
import AggregatorHierarchy from "./../AggregatorHierarchy";
import PageHeading from "src/components/ui/PageHeading";

import {
  useGetInstitutionList,
  useDeleteInstitution,
  useUpdateInstitutionDetails,
  SaveAggregatorFromFile,
  useAggregator,
  useUpdateAggregatorDetails,
  useDeleteAggregator,
} from "src/api/user";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import { UrlUpload } from "src/api/constants";
import { ExclamationCircleOutlined, SearchOutlined } from "@ant-design/icons";
import Highlighter from "react-highlight-words";
import { FilterDropdownProps } from "antd/es/table/interface";
import DOMPurify from "dompurify";

interface DataType {
  key: string;
  name: string;
  age: number;
  address: string;
  tags: string[];
}

const InstitutionList = (props: any) => {
  const [pageSize, setPageSize] = useState(10)
  const [totalItems, setTotalItems] = useState(0);
  const navigate = useNavigate();
  const location = useLocation();
  const childRef = useRef<any>();
  const [institutionDetails, setInstitutionDetails] = useState<any>();
  const [loading, setLoading] = useState<boolean>(true);
  const [institutions, setInstitutionList] = useState<any>([]);
  const [currentPage, setCurrentPage] = useState<number>();
  const [search, setSearch] = useState<string>("");
  const [days, setDays] = useState(0);
  const [searchBy, setSearchBy] = useState<string>("");
  const [filterBy, setFilterBy] = useState<string>("");
  const [reloadData, setReloadData] = useState<boolean>(false)
  const [uploadType, setUploadType] = useState('INSTITUTION_UPLOAD')
  const [aggregatorDetails, setAggregatorDetails] = useState<any>();

  // get Get Institution Details list API
  const { mutate: getInstiutionDetails, data, error } = useGetInstitutionList();
  const { mutate: getInstiutionDetailsNoPage, data:dataNoPage, error:errorNoPage } = useGetInstitutionList();
  const {
    data: aggData,
    error: aggError,
    isSuccess: aggSuccess,
    refetch
  } = useAggregator(location.state?.aggregatorId);
  
  const [searchedColumn, setSearchedColumn] = useState('');
  const [searchText, setSearchText] = useState('');
  const searchInput = useRef<InputRef>(null);
  const {
    mutate: updateAggregatorDetails,
    error: updateAggregatorDetailsError,
    isSuccess: isSuccessUpdateAggregator,
  } = useUpdateAggregatorDetails();
  // API Call Update Institution Details
  const {
    mutate: updateInstitution,
    error: updateInstitutionError,
    isSuccess: isSuccessUpdate,
  } = useUpdateInstitutionDetails();

  // API Call Delete Institution
  const {
    mutate: deleteInstitutionById,
    error: deleteInstitutionError,
    isSuccess,
  } = useDeleteInstitution();
  const {
    mutate: deleteAggregatorById,
    error: deleteAggregatorError,
    isSuccess:isSuccessAggregator,
  } = useDeleteAggregator();
  useEffect(() => {
    console.log("call 104")
    getInstitutionList();
  }, [searchBy]);

  useEffect(() => {
    if (data) {
      refetch()
      setInstitutionList(data);
      setLoading(false);
    }
  }, [data]);
  useEffect(() => {
    getInstitutionList();
  }, [currentPage, pageSize]);


  useEffect(() => {
    if (reloadData) {
      console.log("call 117")
      getInstitutionList()
      childRef.current?.getTreeList()
      setReloadData(false)
    }
  }, [reloadData])

  useEffect(() => {
    if (filterBy !== ""||days) {
      getInstitutionList();
    }
  }, [filterBy, searchBy,days]);

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Institution deleted successfully.",
      });

      getInstitutionList();
      childRef.current?.getTreeList();
    }
    if (deleteInstitutionError) {
      notification["error"]({
        message: "Notification",
        description: "Something went wrong.",
      });
    }
  }, [isSuccess, deleteInstitutionError]);
  useEffect(() => {    
    getInstitutionList()     
  }, [location])
  useEffect(() => {
    if(isSuccessUpdateAggregator||isSuccessAggregator)
    getInstitutionList() 
    childRef.current?.getTreeList();

  }, [isSuccessUpdateAggregator,isSuccessAggregator])
  const daysOptions=[{key:30,value:"30 days"},
  {key:90,value:"90 days"},
  {key:365,value:"1 year"},
{key:0,value:'Till Date'}]
 
  const getInstitutionList = () => {
    const requestParams: any = {
      page: currentPage || 1,
      duration:days,
      aggregatorPreferenceId: location.state?.aggregatorId,
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
    getInstiutionDetails(requestParams);
    let requestParamsNoPage:any
    if(requestParams){
       requestParamsNoPage= Object.assign({}, requestParams);
      requestParamsNoPage.page=0
      requestParamsNoPage.limit=0
  
  }

    getInstiutionDetailsNoPage(requestParamsNoPage);

  };
  const updateAggregatorRow = () => {
    navigate(RouteType.AGGREGATOR_UPDATE, {
      state: {
        ...aggregatorDetails,
      },
    });
    setAggregatorDetails({});
  }; 
  const deleteAggregator = () => {
    deleteAggregatorById({
      aggregatorId: aggregatorDetails.aggregatorPreferenceId,
    });
    setAggregatorDetails({});
  };
  const blockAggregator = () => {
    aggregatorDetails["aggregatorStatus"] =
      aggregatorDetails.aggregatorStatus == "pending" ||
        aggregatorDetails.aggregatorStatus == "active"
        ? "block"
        : "active";
    updateAggregatorDetails(aggregatorDetails);
   // setAggregatorDetails({});
  };

  const handleDropdownItemClick=(e:any)=>{
    if(e.key==1){
    downloadInstitutionsCSV()}
  else if(e.key==2){
    downloadInstitutionsPDF()
  }
  }
  const actionItemsAggrgator: any = [

    {
      key: "3",
      label: (
        <Popconfirm
          title="Sure to delete?"
          onConfirm={() => deleteAggregator()}
        >
          <span>Delete</span>
        </Popconfirm>
      ),
    },
  ];

  const deActivateAggregator = () => {
    aggregatorDetails["aggregatorStatus"] =
        aggregatorDetails.aggregatorStatus == "active"
        ? "de-active"
        : aggregatorDetails.aggregatorStatus;
    updateAggregatorDetails(aggregatorDetails);
    setAggregatorDetails({});
  };
  const deActivate = () => {
    institutionDetails["insitutionStatus"] ="de-active"
        ;
    updateInstitution(institutionDetails);
    setInstitutionDetails({});
  };
  const close = () => {
    institutionDetails["insitutionStatus"] =
    institutionDetails.insitutionStatus == "closed"
    updateInstitution(institutionDetails);
    setInstitutionDetails({});
  };

  const closeAggregator = () => {
    aggregatorDetails["aggregatorStatus"] = "closed"
    updateAggregatorDetails(aggregatorDetails);
    setAggregatorDetails({});
  };
  const actionItemsBlockAggreagator: any = [
    {
      key: "1",
      label: <a onClick={() => updateAggregatorRow()}>Update</a>,
    },
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockAggregator()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];

  
  const actionItemsActiveAgg: any = [
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

  const actionItemsActive: any = [
    {
      key: "1",
      label: <a onClick={() => updateInstitutionRow()}>Edit</a>,
    },
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockInstitution()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
    /* {
      key: "3",
      label: <a onClick={() => deActivate()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];
  
  const actionItemsPendingAgg: any = [
    {
      key: "1",
      label: <a onClick={() => updateAggregatorRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => closeAggregator()}>close</a>,
    },
  ];
  const actionItemsPending: any = [
    {
      key: "1",
      label: <a onClick={() => updateInstitutionRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];
  const actionItemsPendingAutoGen: any = [
    {
      key: "1",
      label: <a onClick={() => updateInstitutionRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];

  const actionItemsUnblockAgg: any = [
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
    /* {
      key: "3",
      label: <a onClick={() => deActivateAggregator()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeAggregator()}>close</a>,
    }
  ];
  const actionItemsUnblock: any = [
    {
      key: "2",
      label: (
        <Popconfirm
          title="Sure to unblock?"
          onConfirm={() => blockInstitution()}
        >
          <span>Unblock</span>
        </Popconfirm>
      ),
    },
    /* {
      key: "3",
      label: <a onClick={() => deActivate()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    }
  ];
  const downloadInstitutionsCSV = () => {
    JSONToCSVConvertor(dataNoPage, "Institutions", true)
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
    "insitutionName",
    "institutionId",
    "insitutionStatus"
     ];
  if (ShowLabel) {
    let row = "";
    for (const index in arrData?.institutionList[0]) {
      switch (index) {
        case "insitutionName":
          row += "Insitution Name" + ",";
          break;
        case "institutionId":
          row += "Insitution ID" + ",";

          break;
        case "insitutionStatus":
          row += "Status" + ",";
          break;
          default:
          break;
      }
    }
    row = row.slice(0, -1);
    CSV += row + "\r\n";
  }
  for (let i = 0; i < arrData?.institutionList.length; i++) {
    let row = "";
    for (const index in arrData.institutionList[i]) {
      if (headersRequried.includes(index)) {
          row += '"' + arrData?.institutionList[i][index] + '",';
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
const downloadInstitutionsPDF = () => {
  if (dataNoPage) {
    const currentData =typeof dataNoPage !== "object" ? JSON.parse(dataNoPage) : dataNoPage;
    const head = [
      [
        "Insitution Name",
        "Insitution ID",
        "Status"
      ]
    ]
    const finalData: any = []
    currentData?.institutionList?.map((item:any, index:any) => {
      const arr = []
      if (Object.prototype.hasOwnProperty.call(item, "insitutionName")) {
        arr.push(item.insitutionName)
      }
      if (Object.prototype.hasOwnProperty.call(item, "institutionId")) {
        arr.push(item.institutionId)
      }
      if (Object.prototype.hasOwnProperty.call(item, "insitutionStatus")) {
        arr.push(item.insitutionStatus)
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

    doc.save("myReports-Institutions.pdf")
  }}  
  type DataIndex = keyof any;
  const handleSearch = (
    selectedKeys: string[],
    confirm: FilterDropdownProps['confirm'],
    dataIndex: DataIndex,
  ) => {
    confirm();
    setSearchText(selectedKeys[0]);
    setSearchBy(selectedKeys[0])
    setSearchedColumn(dataIndex.toString());
    setFilterBy(dataIndex.toString());
  };

  const handleReset = (clearFilters: () => void) => {
    clearFilters();
    setSearchText('');
  };
  const getColumnSearchProps = (dataIndex: DataIndex): TableColumnType<any> => ({
    filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters, close }) => (
      <div style={{ padding: 8 }} onKeyDown={(e) => e.stopPropagation()}>
        <Input
          ref={searchInput}
          placeholder={`Search ${dataIndex.toString()}`}
          value={selectedKeys[0]}
          autoComplete="off"
          onChange={(e) => {
            const sanitizedVal=DOMPurify.sanitize(e.target.value)
            setSelectedKeys(sanitizedVal ? [sanitizedVal] : [])}}
          onPressEnter={() => handleSearch(selectedKeys as string[], confirm, dataIndex)}
          style={{ marginBottom: 8, display: 'block' }}
        />
        <Space>
          <Button
            type="primary"
            onClick={() => handleSearch(selectedKeys as string[], confirm, dataIndex)}
            icon={<SearchOutlined rev={1}  />}
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
              setSearchedColumn(dataIndex.toString());
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
      <SearchOutlined rev={1} style={{ color: filtered ? '#1677ff' : undefined }} />
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
    },
  
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
/*       dataIndex: "aggregatorName",
 */      key: "aggregatorName",
      render: (record: any) => (
        <span style={{ color: "#3080c5", fontWeight: "500" }}>
          {record?.aggregatorName}
        </span>
      ),
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
      render: (aggregatorStatus: any) =>  {   return    aggregatorStatus === "active" || aggregatorStatus === "pending"||aggregatorStatus==="draft" ? (
        aggregatorStatus === "active" ? (
          <>
            <img src={"/images/active-icon.svg"} /> {"Active"}
          </>
        ) : (
          aggregatorStatus === "pending" ? <>
          <img src={"/images/pendng-icon.svg"} /> {"Pending"}
        </>:
          <>
            <img src={"/images/pendng-icon.svg"} /> {"Draft"}
          </>
        )
      ) : (
        aggregatorStatus === "de-active" ? <>
        <img src={"/images/de-active.svg"} /> {"De-Active"}
      </>:(
        aggregatorStatus === "closed" ? <>
        <img src={"/images/closed-icon.svg"} /> {"Closed"}
      </>:
        <span style={{ color: "#D15241" }}>
          <img src={"/images/blocked-icon.svg"} /> {"Block"}
        </span>)
      )
    }
,
        ...getColumnSearchProps('aggregatorStatus')

    },
    {
      title: "",
      render: (record: any) => {
      return   <Dropdown
          menu={{
            items: [
              ...((record?.aggregatorStatus === "closed")?[]:(record?.aggregatorStatus === "active" ?actionItemsActiveAgg:
              record?.aggregatorStatus === "pending"||record?.aggregatorStatus === "de-active"||record?.aggregatorStatus === "draft" 
              ? actionItemsPendingAgg
              : actionItemsUnblockAgg))
            ],
          }}
          trigger={["click"]}
/*           disabled={record.isParentBlocked}
 */          arrow
        >
          <Typography.Link onClick={() => setAggregatorDetails(aggData)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      },
    },
    {
      title: "INSTITUTION NAME",
      key: "insitutionName",
/*       dataIndex: "insitutionName",
 */      render: (record: any) => {
       return <>
          <img
            src={record?.institutionLogo}
            style={{ height: "40px", width: "40px" }}
          />
          &nbsp;
          <span style={{ color: "#3080c5", fontWeight: "500" }}>
{/*             {record}
 */}            {record?.insitutionName?.charAt(0).toUpperCase() +
              record?.insitutionName?.slice(1)} 
          </span>
        </>
            },
      ...getColumnSearchProps('insitutionName')
    },
    {
      title: "INSTITUTION ID",
      dataIndex: "institutionId",
      key: "institutionId",
      ...getColumnSearchProps('institutionId')

    },
    {
      title: "STATUS",
       key: "insitutionStatus",
   //   render: (record: any) =>
      render: (record: any) =>  {   return    record.insitutionStatus === "active" || record.insitutionStatus === "pending"||record.insitutionStatus==="draft" ? (
        record.insitutionStatus === "active" ? (
          <>
            <img src={"/images/active-icon.svg"} /> {"Active"}
          </>
        ) : (
          record.insitutionStatus === "pending" ? <>
          <img src={"/images/pendng-icon.svg"} /> {"Pending"}
        </>:
          <>
            <img src={"/images/pendng-icon.svg"} /> {"Draft"}
          </>
        )
      ) : (
        record.insitutionStatus === "de-active" ? <>
        <img src={"/images/de-active.svg"} /> {"De-Active"}
      </>:(
        record.insitutionStatus === "closed" ? <>
        <img src={"/images/closed-icon.svg"} /> {"Closed"}
      </>:
        <span style={{ color: "#D15241" }}>
          <img src={"/images/blocked-icon.svg"} /> {"Block"}
        </span>)
      )
    }

    /*     ((record.insitutionStatus === "active" || record.insitutionStatus === "pending") && !record.isParentBlocked )? (
          record.insitutionStatus === "active" ? (
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
        ) */,
        ...getColumnSearchProps('insitutionStatus')
    },
    {
      title: "",
      render: (record: any) => {
     return   <Dropdown
          menu={{
            items: [              
              ...((record.systemGenerated||record?.insitutionStatus === "closed"||record?.aggregatorStatus!=="active")?[]:(record?.insitutionStatus === "active" ?actionItemsActive:
              record?.insitutionStatus === "pending"||record?.insitutionStatus === "de-active"||record?.insitutionStatus === "draft" 
              ? (actionItemsPending)

              : actionItemsUnblock))

            ],
          }}
          trigger={["click"]}
/*           disabled={record.isParentBlocked}
 */          arrow
        >
          <Typography.Link onClick={() => setInstitutionDetails(record)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      },
    }
  ];

  // Action items
  const actionItems: any = [
    
    {
      key: "2",
      label: (
        <Popconfirm
          title="Sure to delete?"
          onConfirm={() => deleteInstitution()}
        >
          <span>Delete</span>
        </Popconfirm>
      ),
    },
  ];

  const actionItemsBlock: any = [
    {
      key: "1",
      label: <a onClick={() => updateInstitutionRow()}>Update</a>,
    },
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockInstitution()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsBlockAutoGen: any = [
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockInstitution()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },

  ];
  

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

  const handleChange = (e: any, name: string) => {

    if (name === "pagination") {
      setCurrentPage(e);
    }
    if (name === "filterBy") {
      setFilterBy(e);
    }
    if (name === "searchBy") {
      const sanitizedInput = DOMPurify.sanitize(e.target.value)
      setSearchBy(sanitizedInput);
    }
  };
  const handleDaysChange = (value: string) => {
    setDays(Number.parseInt(value))
      };
  const updateInstitutionRow = () => {
    navigate(
      RouteType.INSTITUTION_REGISTER +
        "/" +
        location.state?.aggregatorId +
        "/" +
        location.state?.aggregatorName,
      {
        state: {
          ...institutionDetails,
        },
      }
    );
    setInstitutionDetails({});
  };
  const handlePaginationChange = (pagination:any) => {
    setCurrentPage(pagination);
    setPageSize(pageSize);
  };
  const blockInstitution = () => {
    institutionDetails["insitutionStatus"] =
      institutionDetails.insitutionStatus == "pending" ||
      institutionDetails.insitutionStatus == "active"
        ? "block"
        : "active";

    updateInstitution(institutionDetails);
    setInstitutionDetails({});
  };

  const deleteInstitution = () => {
    deleteInstitutionById({
      institutionId: institutionDetails.institutionId,
    });
    setInstitutionDetails({});
  };
 

return (
    <div>
      <PageHeading
        topTitle="Institution management"
        title="Acquirer Institution Management"
        linkData={{
          label: "Register New Institute",
          url:
            RouteType.INSTITUTION_REGISTER +
            "/" +
            location.state?.aggregatorId +
            "/" +
            location.state?.aggregatorName,
        }}
        uploadType={"INSTITUTION_UPLOAD"}
        parentRefetch={setReloadData}
        showRegister={institutions?.institutionList?.length!=0&&aggData?.aggregatorStatus==="active"}
      />
     
      {data !== undefined &&aggData?.aggregatorStatus==='active'&&
      days==0&&institutions?.institutionList?.length === 0 &&
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
              No Instutions are available yet to manage
            </h5>
            <p style={{ fontSize: "14px" }}>
              Want to create Institution? Please click on the below button
            </p>
            <Link
              to={RouteType.INSTITUTION_REGISTER+ "/" +
              location.state?.aggregatorId +
              "/" +
              location.state?.aggregatorName}
              title="Register New Instituion"
              
            >
              <Button
                style={{
                  background: "#26a6e0",
                  color: "#ffffff",
                }}
              >
                Register New Institution
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
                                  value: "institutionName",
                                  label: "Institution Name",
                                },
                                {
                                  value: "institutionId",
                                  label: "Institution ID",
                                },
                                { value: "status", label: "Status" },
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
                            autoComplete="off" 
                              onChange={(e) => handleChange(e, "searchBy")}
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
                      <Dropdown   menu={{
                          onClick: handleDropdownItemClick,
                          items: items,
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
                    scroll={{ x: "max-content" }}
                    dataSource={institutions?.institutionList ?.map((it:any)=>{
                      it.aggregatorName=aggData?.aggregatorName
                      it.aggregatorStatus=aggData?.aggregatorStatus
                      it.aggregatorLogo=aggData?.aggregatorLogo
                      return it
                    }) }
                    pagination={{
                      current: institutions?.pagination?.current_page,
                      pageSize: pageSize,
                      total: institutions?.pagination?.total_records,
                      onChange: handlePaginationChange,
                    }} 
                    size="middle"
                    className="table-custom"
                    loading={loading}
                    locale={{
                      emptyText: (
                        <Empty
                          image={Empty.PRESENTED_IMAGE_SIMPLE}
                          description="No institutions found"
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

export default InstitutionList;
function validateFileType(file: UploadFile<any>, arg1: string) {
  throw new Error("Function not implemented.");
}

