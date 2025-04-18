import React, { useState, useEffect, useRef } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  Space,
  Table,
  Dropdown,
  Select,
  MenuProps,
  Input,
  Form,
  Button,
  Popconfirm,
  notification,
  Typography,
  TableColumnType,
  InputRef,
} from "antd";

import { RouteType } from "src/constants/routeTypes";
import AggregatorHierarchy from "./../AggregatorHierarchy";
import PageHeading from "src/components/ui/PageHeading";

import {
  useGetMerchantAcquirerList,
  useDeleteMerchant,
  useUpdateMerchantAcquirer,
  useUpdateInstitutionDetails,
  useDeleteInstitution,
  useDeleteAggregator,
  useUpdateAggregatorDetails,
  useDeleteMerchantGroup,
  useAggregator,
  useInstitution,
  useUpdateMerchantGroup,
  useMerchantGroup,
} from "src/api/user";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import { SearchOutlined } from "@ant-design/icons";
import { FilterDropdownProps } from "antd/es/table/interface";
import daysToWeeks from "date-fns/daysToWeeks/index.js";
//import ResizeObserver from 'rc-resize-observer';

interface DataType {
  key: string;
  name: string;
  age: number;
  address: string;
  tags: string[];
}

const MerchantList = () => {
  const [pageSize, setPageSize] = useState(10)
  const location = useLocation();
  const navigate = useNavigate();
  const childRef = useRef<any>();
  const [canScroll, setCanScroll] = useState(false);
  const [merchantDetails, setMerchantDetails] = useState<any>();
  const [loading, setLoading] = useState<boolean>(true);
  const [merchants, setMerchantsList] = useState<any>([]);
  const [currentPage, setCurrentPage] = useState<number>();
  const [search, setSearch] = useState<string>("");
  const [searchBy, setSearchBy] = useState<string>("");
  const [filterBy, setFilterBy] = useState<string>("");
  const [tableWidth, setTableWidth] = useState(1500)
  const [reloadData, setReloadData] = useState<boolean>(false)
  const [aggregatorDetails, setAggregatorDetails] = useState<any>();
  const [searchText, setSearchText] = useState('');
  const [searchedColumn, setSearchedColumn] = useState('');
  const [merchantGroupDetails, setMerchantGroupDetails] = useState<any>();
  const [institutionDetails, setInstitutionDetails] = useState<any>();
  const searchInput = useRef<InputRef>(null);
  const [days, setDays] = useState(0)
  const {
    data: aggData,
    error: aggError,
    isSuccess: aggSuccess,
    refetch:refetchAggregator
  } = useAggregator(location.state?.aggregatorId);
  const {
    mutate: updateMerchantGroupDetails,
    error: updateMerchantGroupDetailsError,
    isSuccess: isSuccessUpdateMg,
  } = useUpdateMerchantGroup();
  const {
    data: instData,
    error: instError,
    isSuccess: instSuccess,
    refetch:refetchInst
  } = useInstitution(
    location.state?.institutionId,location.state?.institutionId!="");

  const {
    mutate: updateInstitution,
    error: updateInstitutionError,
    isSuccess: isSuccessUpdateInst,
  } = useUpdateInstitutionDetails();
  const {
    mutate: deleteAggregatorById,
    error: deleteAggregatorError,
    isSuccess:isSuccessAggregator,
  } = useDeleteAggregator();
  // get Get Merchant Details list API
  const {
    mutate: getMercahntDetails,
    data,
    error,
  } = useGetMerchantAcquirerList();
  const {
    mutate: getMercahntDetailsNoPage,
    data:dataNoPage,
    error:errorNoPage,
  } = useGetMerchantAcquirerList();

  // API Call Save merchant Details
  const {
    mutate: updateMerchantDetails,
    error: updateMerchantDetailsError,
    isSuccess: isSuccessUpdate,
  } = useUpdateMerchantAcquirer();
  const {
    data: mgData,
    error: mgError,
    isSuccess: mgSuccess,
    refetch:refetchMg
  } = useMerchantGroup(
    location.state?.merchantGroupPreferenceId,true
  );
  // API Call Delete Institution
  const {
    mutate: deleteInstitutionById,
    error: deleteInstitutionError,
    isSuccess:isSuccessInst,
  } = useDeleteInstitution();
  const {
    mutate: updateAggregatorDetails,
    error: updateAggregatorDetailsError,
    isSuccess: isSuccessUpdateAggregator,
  } = useUpdateAggregatorDetails();

  // API Call Delete Merchant Group
  const {
    mutate: deleteMerchantGroupById,
    error: deleteMerchantGroupError,
    isSuccess:isSuccessMgDelete,
  } = useDeleteMerchantGroup();
  // API Call Delete Merchant 
  const {
    mutate: deleteMerchantById,
    error: deleteMerchantError,
    isSuccess,
  } = useDeleteMerchant();
  const daysOptions=[{key:30,value:"30 days"},
  {key:90,value:"90 days"},
  {key:365,value:"1 year"},
  {key:0,value:'Till Date'}]
 
/* 
  useEffect(() => {
    console.log("usefeffect  121 searchby")
    getMerchantList();
  }, [searchBy]); */

  useEffect(() => {
    if (data) {
      setMerchantsList(data);
      setCanScroll(true)
      setLoading(false);
    }
  }, [data]);

  useEffect(() => {
    if (reloadData) {

      getMerchantList()
      childRef.current?.getTreeList()
      setReloadData(false)
    }
  }, [reloadData])
  useEffect(() => {
    //refetchInst()
    if(isSuccessUpdateInst||isSuccessUpdateMg||isSuccessUpdateAggregator)
   { getMerchantList()    
    childRef.current?.getTreeList();
   }

     }, [isSuccessUpdateInst,isSuccessUpdateMg,isSuccessUpdateAggregator,])
  
useEffect(() => {
  getMerchantList()
  childRef.current?.getTreeList();
},  [location])
useEffect(() => {  getMerchantList()
  childRef.current?.getTreeList();
}, [isSuccessInst,isSuccessMgDelete,isSuccessUpdate])

  useEffect(() => {
    if (filterBy !== ""||days) {
      getMerchantList();
    }
  }, [filterBy, searchBy,days]);

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Merchant deleted successfully.",
      });

      getMerchantList();
      childRef.current?.getTreeList();
    }
    if (deleteMerchantError) {
      notification["error"]({
        message: "Notification",
        description: "Something went wrong.",
      });
    }
  }, [isSuccess, deleteMerchantError]);

  const getMerchantList = () => {
    const requestParams: any = {
      page: currentPage || 1,
      aggregatorPreferenceId: location.state?.aggregatorId,
      institutionPreferenceId: location.state?.institutionId,
      merchantGroupPreferenceId: location.state?.merchantGroupPreferenceId,      
      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: pageSize,
      duration:days
    };

    if (filterBy != "" && searchBy != "") {
      requestParams[filterBy] = searchBy;
    } else {
      delete requestParams[filterBy];
    }

    // show loader
    setLoading(true);
    // API CALL
    getMercahntDetails(requestParams);
    let requestParamsNoPage= Object.assign({}, requestParams);
    requestParamsNoPage.page=0
    requestParamsNoPage.limit=0
    getMercahntDetailsNoPage(requestParamsNoPage);

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
  useEffect(() => { 
    getMerchantList();
  }, [currentPage, pageSize]);

  const blockInstitution = () => {
let tempInst=Object.assign({},institutionDetails)
tempInst.insitutionStatus =
tempInst.insitutionStatus == "pending" ||
tempInst.insitutionStatus == "active"
        ? "block"
        : "active";
        let res=Object.assign (institutionDetails,{insitutionStatus:institutionDetails.insitutionStatus})
        updateInstitution(tempInst);
    //setInstitutionDetails({});
  };

  const deleteInstitution = () => {
    deleteInstitutionById({
      institutionId: institutionDetails.institutionId,
    });
    setInstitutionDetails({});
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
  const deActivateInst = () => {
    let tempInst=Object.assign({},institutionDetails)
tempInst.insitutionStatus ="de-active"
   updateInstitution(tempInst);

  };
  const deActivate = () => {
    merchantDetails["merchantAcquirerStatus"] = "de-active";
    let request={merchant:merchantDetails}
  updateMerchantDetails(request);
  setMerchantDetails({});
  };
  const deActivateMg = () => {
    merchantGroupDetails["merchantGroupStatus"] ="de-active"        ;
    let request={merchantGroup:merchantGroupDetails}
    updateMerchantGroupDetails(request);
    setMerchantGroupDetails({});
  };
  const closeMg = () => {
    merchantGroupDetails["merchantGroupStatus"] = "closed"
    let request={merchantGroup:merchantGroupDetails}
    updateMerchantGroupDetails(request);
    setMerchantGroupDetails({});
  };

  const close = () => {
    merchantDetails["merchantAcquirerStatus"] = "closed";
    let request={merchant:merchantDetails}
  updateMerchantDetails(request);
  setMerchantDetails({});
  };
  const closeInst = () => {
    institutionDetails["insitutionStatus"] = "closed"
    updateInstitution(institutionDetails);
    setInstitutionDetails({});
  };

  const updateAggregatorRow = () => {
    navigate(RouteType.AGGREGATOR_UPDATE, {
      state: {
        ...aggregatorDetails,
      },
    });
    setAggregatorDetails({});
  };
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
      label: <a onClick={() => updateMarchantRow()}>Edit</a>,
    },
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockMerchant()}>
          <span>Block</span>
        </Popconfirm>
      ),
    } ,
   /* {
      key: "3",
      label: <a onClick={() => deActivate()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];
  const actionItemsActiveMg: any = [
    {
      key: "1",
      label: <a onClick={() => updateMarchantGroupRow()}>Edit</a>,
    },
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockMerchantGroup()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
    /* {
      key: "3",
      label: <a onClick={() => deActivateMg()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeMg()}>close</a>,
    },
  ];
  const actionItemsActiveInst: any = [
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
      label: <a onClick={() => deActivateInst()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeInst()}>close</a>,
    },
  ];

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

  const actionItemsUnblockAggregator: any = [
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
  ];
  // Action items Institution
  const actionItemsInst: any = [
    
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

  const actionItemsBlockInst: any = [
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
  const actionItemsBlockInstAutoGen: any = [
   
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockInstitution()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsPendingInst: any = [
    {
      key: "1",
      label: <a onClick={() => updateInstitutionRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => closeInst()}>close</a>,
    },
  ];
  const actionItemsUnblockInst: any = [
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
      label: <a onClick={() => deActivateInst()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeInst()}>close</a>,
    }
  ];
  type DataIndex = keyof any;
  const handleSearch = (
    selectedKeys: string[],
    confirm: FilterDropdownProps['confirm'],
    dataIndex: DataIndex,
  ) => {
    confirm();
    setSearchText(selectedKeys[0]);
    setSearchedColumn(dataIndex.toString());
  };

  const handleReset = (clearFilters: () => void) => {
    clearFilters();
    setSearchText('');
  };
  const getColumnSearchProps = (dataIndex: DataIndex): TableColumnType<any> => ({
    filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters, close }) => (
      <div style={{ padding: 8 }} onKeyDown={(e) => e.stopPropagation()}>
        <Input aria-autocomplete='both' aria-haspopup="false"
          ref={searchInput}
          autoComplete="off" 
          type="search"
          placeholder={`Search ${dataIndex.toString()}`}
          value={selectedKeys[0]}
          onChange={(e) => setSelectedKeys(e.target.value ? [e.target.value] : [])}
          onPressEnter={() => handleSearch(selectedKeys as string[], confirm, dataIndex)}
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
    },
  
  });
  const deleteMerchantGroup = () => {
    deleteMerchantGroupById({
      merchantGroupId: merchantGroupDetails.merchantGroupPreferenceId,
    });

    setMerchantGroupDetails({});
  };
  const actionItemsMg: any = [
   
    {
      key: "2",
      label: (
        <Popconfirm
          title="Sure to delete?"
          onConfirm={() => deleteMerchantGroup()}
        >
          <span>Delete</span>
        </Popconfirm>
      ),
    },
  ];
  const updateMarchantGroupRow = () => {
    navigate(
      RouteType.MERCHANT_GROUP_REGISTER +
        "/" +
        location.state?.aggregatorId +
        "/" +
        location.state?.aggregatorName +
        "/" +
        location.state?.institutionId +
        "/" +
        location.state?.institutionName,
      {
        state: {
          ...merchantGroupDetails,
        },
      }
    );
    setMerchantGroupDetails({});
  };
  const actionItemsBlockMg: any = [
    {
      key: "1",
      label: <a onClick={() => updateMarchantGroupRow()}>Update</a>,
    },
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockMerchantGroup()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsBlockMgAutoGen: any = [
      {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockMerchantGroup()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsUnblockMg: any = [
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to unblock?" onConfirm={() => blockMerchantGroup()}>
          <span>Unblock</span>
        </Popconfirm>
      ),
    },
    /* {
      key: "3",
      label: <a onClick={() => deActivateMg()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeMg()}>close</a>,
    },
  ];
  const scroll = canScroll
  ? {
      x: 'max-content',
    }
  : undefined;

  // Table columns
  const columns = [
    {
      title: "",
      //width: 100, // set a fixed width
      dataIndex: "aggregatorLogo",
      key: "aggregatorLogo",
      render: (aggregatorLogo: string) => (
        <img src={aggregatorLogo} style={{ height: "40px", width: "40px" }} />
      ),
    },
    {
      title: "AGGREGATOR NAME",
     // width: 200, // set a fixed width

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
      //width: 200, // set a fixed width

      dataIndex: "aggregatorPreferenceId",
      key: "aggregatorPreferenceId",
      ...getColumnSearchProps('aggregatorPreferenceId')

    },
    {
      title: "STATUS",
      //width: 200, // set a fixed width
      dataIndex: "aggregatorStatus",
      key: "aggregatorStatus",
      render: (aggregatorStatus: any) => aggregatorStatus === "active" || aggregatorStatus === "pending"||aggregatorStatus==="draft" ? (
        aggregatorStatus === "active"  ? (
            <>
              <img src={"/images/active-icon.svg"} /> {"Active"}
            </>
          ) : (
            aggregatorStatus === "pending" ?
            <>
              <img src={"/images/pendng-icon.svg"} /> {"Pending"}
            </>
            :
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
          </span>)        ),
        ...getColumnSearchProps('aggregatorStatus')

    },
    {
      title: "",
      //width: 100, // set a fixed width

      render: (record: any) => (
        <Dropdown
        menu={{
          items: [
            ...((record?.aggregatorStatus === "closed") ?[]:record?.aggregatorStatus === "active" ?actionItemsActiveAgg:

            record?.aggregatorStatus === "pending"||record?.aggregatorStatus === "de-active"||record?.aggregatorStatus === "draft" 
            ? actionItemsPendingAgg
            : actionItemsUnblockAgg)
          ],
        }}
          trigger={["click"]}
/*           disabled={record.isParentBlocked}
 */          arrow
        >
          <Typography.Link onClick={() => setAggregatorDetails(record?.aggregator)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
    {
      title: "INSTITUTION NAME",
      key: "insitutionName",
      //width: 200, // set a fixed width

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
      //width: 200, // set a fixed width

      dataIndex: "insitutionPreferenceId",
      key: "insitutionPreferenceId",
      ...getColumnSearchProps('insitutionPreferenceId')

    },
    {
      title: "STATUS",
      //width: 200, // set a fixed width

/*        dataIndex: "insitutionStatus", 
 */       key: "insitutionStatus",
 render: (record: any) => {
  return     record.insitutionStatus === "active" || record.insitutionStatus === "pending"||record.insitutionStatus==="draft" ? (
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
  ) },
        ...getColumnSearchProps('insitutionStatus')
    },
    {
      title: "",
     // width: 100, // set a fixed width

      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record.institution.systemGenerated||record.insitutionStatus==='closed'||record?.aggregator?.aggregatorStatus!=="active")?[]:(record?.insitutionStatus === "active" ?actionItemsActiveInst:
              record?.insitutionStatus === "pending"||record?.insitutionStatus === "de-active"||record?.insitutionStatus === "draft" 
              ? (actionItemsPendingInst)
              : actionItemsUnblockInst))
            ],
          }}
          trigger={["click"]}          /*  disabled={record.isParentBlockedInst} */
           arrow
        >
          <Typography.Link onClick={() =>{ 
            setInstitutionDetails(record?.institution)
          }}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
    {
      title: "MERCHANT GROUP NAME",
      //width: 200, // set a fixed width

      key: "merchantGroupName",
      render: (record: any) => (
        <>
          <img
            src={record?.merchantGroupLogo}
            style={{ height: "40px", width: "40px" }}
          />
          &nbsp;
          <span style={{ color: "#3080c5", fontWeight: "500" }}>
            {record?.merchantGroupName.charAt(0).toUpperCase() +
              record?.merchantGroupName.slice(1)}
          </span>
        </>
      ),
      ...getColumnSearchProps('merchantGroupName')

    },
    {
      title: "MERCHANT GROUP ID",
      //width: 200, // set a fixed width

      dataIndex: "merchantGroupPreferenceId",
      key: "merchantGroupPreferenceId",
      ...getColumnSearchProps('merchantGroupPreferenceId')

    },
    {
      title: "STATUS",
      //width: 200, // set a fixed width
/*       dataIndex: "merchantGroupStatus", */
      key: "merchantGroupStatus",
      render: (record: any) =>{

        return    record.merchantGroupStatus === "active" || record.merchantGroupStatus === "pending"||record.merchantGroupStatus==="draft" ? (
          record.merchantGroupStatus === "active" ? (
            <>
              <img src={"/images/active-icon.svg"} /> {"Active"}
            </>
          ) : (
            record.merchantGroupStatus === "pending" ? <>
            <img src={"/images/pendng-icon.svg"} /> {"Pending"}
          </>:
            <>
              <img src={"/images/pendng-icon.svg"} /> {"Draft"}
            </>
          )
        ) : (
          record.merchantGroupStatus === "de-active" ? <>
          <img src={"/images/de-active.svg"} /> {"De-Active"}
        </>:(
          record.merchantGroupStatus === "closed" ? <>
          <img src={"/images/closed-icon.svg"} /> {"Closed"}
        </>:
          <span style={{ color: "#D15241" }}>
            <img src={"/images/blocked-icon.svg"} /> {"Block"}
          </span>)
        )},
        ...getColumnSearchProps('merchantGroupStatus')

    },
    {
      title: "",
      //width: 100, // set a fixed width
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record?.merchantGroup?.systemGenerated||record?.merchantGroupStatus==='closed'||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active")?[]:(record?.merchantGroupStatus === "active" ?actionItemsActiveMg:
              record?.merchantGroupStatus === "pending"||record?.merchantGroupStatus === "de-active"||record?.merchantGroupStatus === "draft" 
              ? (actionItemsPendingMg)
              : actionItemsUnblockMg))            ],
          }}
          trigger={["click"]}
          arrow
/*           disabled={record.merchantGroup.isParentBlocked}
 */        >
          <Typography.Link onClick={() => setMerchantGroupDetails(record.merchantGroup)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
    {
      title: "MERCHANT NAME",
      //width: 200, // set a fixed width

      key: "merchantAcquirerName",
      render: (record: any) => (
        <>
          <img
            src={record?.merchantAcquirerLogo}
            style={{ height: "40px", width: "40px" }}
          />
          &nbsp;
          <span style={{ color: "#3080c5", fontWeight: "500" }}>
            {record?.merchantAcquirerName === null
              ? "-"
              : record?.merchantAcquirerName?.charAt(0)?.toUpperCase() +
                record?.merchantAcquirerName?.slice(1)}
          </span>
        </>
      ),
      ...getColumnSearchProps('merchantAcquirerName')

    },
    {
      title: "MERCHANT ID",
     // width: 200, // set a fixed width

      dataIndex: "merchantAcquirerId",
      key: "merchantAcquirerId",
      ...getColumnSearchProps('merchantAcquirerId')

    },
    {
      title: "STATUS",
      //width: 200, // set a fixed width

/*       dataIndex: "merchantAcquirerStatus", */
      key: "merchantAcquirerStatus",
      render: (record: any) =>{
        return    record.merchantAcquirerStatus === "active" || record.merchantAcquirerStatus === "pending"||record.merchantAcquirerStatus==="draft" ? (
          record.merchantAcquirerStatus === "active" ? (
            <>
              <img src={"/images/active-icon.svg"} /> {"Active"}
            </>
          ) : (
            record.merchantAcquirerStatus === "pending" ? <>
            <img src={"/images/pendng-icon.svg"} /> {"Pending"}
          </>:
            <>
              <img src={"/images/pendng-icon.svg"} /> {"Draft"}
            </>
          )
        ) : (
          record.merchantAcquirerStatus === "de-active" ? <>
          <img src={"/images/de-active.svg"} /> {"De-Active"}
        </>:(
          record.merchantAcquirerStatus === "closed" ? <>
          <img src={"/images/closed-icon.svg"} /> {"Closed"}
        </>:
          <span style={{ color: "#D15241" }}>
            <img src={"/images/blocked-icon.svg"} /> {"Block"}
          </span>)
        )},      
        ...getColumnSearchProps('merchantAcquirerStatus')

    },
    {
      title: "",
      //width: 100, // set a fixed width
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record.merchantAcquirer.systemGenerated||record?.merchantAcquirerStatus === "closed"||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active"||record?.merchantGroup?.merchantGroupStatus!=="active")?[]:(record?.merchantAcquirerStatus === "active" ?actionItemsActive:
              record?.merchantAcquirerStatus === "pending"||record?.merchantAcquirerStatus === "de-active"||record?.merchantAcquirerStatus === "draft" 
              ? (actionItemsPending)
              : actionItemsUnblock))            ],
          }}              
              
            
          trigger={["click"]}
          arrow
/*           disabled={record.isParentBlocked}
 */        >
          <Typography.Link onClick={() => setMerchantDetails(record.merchantAcquirer)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
  ];
  const handleDaysChange = (value: string) => {
    setDays(Number.parseInt(value))
      };
  const handleDropdownItemClick=(e:any)=>{
    if(e.key==1){
    downloadMerchantCSV()}
  else if(e.key==2){
    downloadMerchantPDF()
  }
  }
  const downloadMerchantCSV = () => {
    JSONToCSVConvertor(dataNoPage, "Merchant", true)
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
      "merchantAcquirerName",
      "merchantAcquirerId",
      "merchantAcquirerStatus"
       ];
    if (ShowLabel) {
      let row = "";
      for (const index in arrData?.merchantList[0]?.merchantAcquirer) {
        switch (index) {
          case "merchantAcquirerName":
            row += "Merchant Name" + ",";
            break;
          case "merchantAcquirerId":
            row += "Merchant ID" + ",";
  
            break;
          case "merchantAcquirerStatus":
            row += "Status" + ",";
            break;
            default:
            break;
        }
      }
      row = row.slice(0, -1);
      CSV += row + "\r\n";
    }
    for (let i = 0; i < arrData?.merchantList.length; i++) {
      let row = "";
      for (const index in arrData.merchantList[i]?.merchantAcquirer) {
        if (headersRequried.includes(index)) {
            row += '"' + arrData?.merchantList[i]?.merchantAcquirer[index] + '",';
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
  const downloadMerchantPDF = () => {
    if (dataNoPage) {
      const currentData =typeof dataNoPage !== "object" ? JSON.parse(dataNoPage) : dataNoPage;
      const head = [
        [
          "Merchant Name",
          "Merchant ID",
          "Status"
        ]
      ]
      const finalData: any = []
      currentData?.merchantList?.map((item:any, index:any) => {
        const arr = []
        if (Object.prototype.hasOwnProperty.call(item, "merchantAcquirerName")) {
          arr.push(item.merchantAcquirerName)
        }
        if (Object.prototype.hasOwnProperty.call(item, "merchantAcquirerId")) {
          arr.push(item.merchantAcquirerId)
        }
        if (Object.prototype.hasOwnProperty.call(item, "merchantAcquirerStatus")) {
          arr.push(item.merchantAcquirerStatus)
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
  
      doc.save("myReports-merchant.pdf")
    }}

  // Action items
  const actionItems: any = [
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to delete?" onConfirm={() => deleteMerchant()}>
          <span>Delete</span>
        </Popconfirm>
      ),
    },
  ];

  const actionItemsBlock: any = [
    {
      key: "1",
      label: <a onClick={() => updateMarchantRow()}>Update</a>,
    },
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockMerchant()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsBlockAutoGen: any = [
   
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockMerchant()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsPendingMg: any = [
    {
      key: "1",
      label: <a onClick={() => updateMarchantGroupRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => closeMg()}>close</a>,
    },
  ];
  const actionItemsPending: any = [
    {
      key: "1",
      label: <a onClick={() => updateMarchantRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];

  const actionItemsUnblock: any = [
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to unblock?" onConfirm={() => blockMerchant()}>
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
      setSearchBy(e.target.value);
    }
  };

  const updateMarchantRow = () => {
    navigate(
      RouteType.MERCHANT_REGISTER +
        "/" +
        location.state.aggregatorId +
        "/" +
        location.state.aggregatorName +
        "/" +
        location.state.institutionId +
        "/" +
        location.state.institutionName +
        "/" +
        location.state.merchantGroupPreferenceId +
        "/" +
        location.state.merchantGroupName,
      {
        state: {
          ...merchantDetails,
        },
      }
    );
    setMerchantDetails({});
  };
  const blockMerchantGroup = () => {
    merchantGroupDetails["merchantGroupStatus"] =
      merchantGroupDetails.merchantGroupStatus == "pending" ||
      merchantGroupDetails.merchantGroupStatus == "active"
        ? "block"
        : "active";
        let request={merchantGroup:merchantGroupDetails}
    updateMerchantGroupDetails(request);
    setMerchantGroupDetails({});
  };

  const blockMerchant = () => {
    merchantDetails["merchantAcquirerStatus"] =
      merchantDetails.merchantAcquirerStatus == "pending" ||
      merchantDetails.merchantAcquirerStatus == "active"
        ? "block"
        : "active";
        let request={merchant:merchantDetails}
    updateMerchantDetails(request);
    setMerchantDetails({});
  };

  const deleteMerchant = () => {
    deleteMerchantById({
      merchantAcquirerId: merchantDetails.aggregatorPreferenceId,
    });

    setMerchantDetails({});
  };
  const handlePaginationChange = (pagination:any) => {
    console.log(pagination)
    setCurrentPage(pagination);
    setPageSize(pageSize);
  };
  return (
    <div>
      <PageHeading
        topTitle="Merchant management"
        title="Acquirer Merchant Management"
        uploadType={"MERCHANT_UPLOAD"}
        parentRefetch={setReloadData}
        showRegister={merchants?.merchantList?.length > 0 &&(merchants?.merchantList[0]?.aggregator?.aggregatorStatus==="active"&&merchants?.merchantList[0].institution?.insitutionStatus==="active"&&merchants?.merchantList[0].merchantGroup?.merchantGroupStatus==="active")}
        linkData={{
          label: "Register Merchant",
          url:
            RouteType.MERCHANT_REGISTER +
            "/" +
            location.state.aggregatorId +
            "/" +
            location.state.aggregatorName +
            "/" +
            location.state.institutionId +
            "/" +
            location.state.institutionName +
            "/" +
            location.state.merchantGroupPreferenceId +
            "/" +
            location.state.merchantGroupName,
        }}
      />
      {data !== undefined && days==0 &&(merchants?.merchantList?.length === 0&&(aggData?.aggregatorStatus==="active"&&instData?.insitutionStatus==="active"&&mgData?.merchantGroupStatus==="active"))&&

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
              No Merchant are available yet to manage
            </h5>
            <p style={{ fontSize: "14px" }}>
              Want to create Merchant ? Please click on the below button
            </p>
            <Link
              to={
                RouteType.MERCHANT_REGISTER +
                "/" +
                location.state.aggregatorId +
                "/" +
                location.state.aggregatorName +
                "/" +
                location.state.institutionId +
                "/" +
                location.state.institutionName +
                "/" +
                location.state.merchantGroupPreferenceId +
                "/" +
                location.state.merchantGroupName
              }
              title="Register New Merchant "
            >
              <Button
                style={{
                  background: "#faad14",
                  color: "#ffffff",
                }}
              >
                Register New Merchant
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
                                value: "merchantName",
                                label: "Merchant Name",
                              },
                              {
                                value: "merchantId",
                                label: "Merchant ID",
                              },
                              { value: "status", label: "status" },
                            ]}
                            style={{ width: "50%" }}
                            suffixIcon={<img src={"/images/down-arrow.svg"} />}
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
                          <Input
                            aria-autocomplete="both"
                            aria-haspopup="false"
                            autoComplete="off"
                            type="search"
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
                      <Dropdown
                        menu={{
                          onClick: handleDropdownItemClick,
                          items: items,
                        }}
                        trigger={["click"]}
                        arrow
                      >
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
                <div
                  className="table-responsive "
                  style={{ overflowX: "auto" }}
                >
                  {/*                <ResizeObserver
  onResize={({ width }) => {
    setTableWidth(width);
  }}
> */}
                  <Table
                    columns={columns}
                    /*                      scroll={scroll}
                     */ scroll={{ x: "max-content" }}
                    pagination={{
                      current: merchants?.pagination?.current_page,
                      pageSize: pageSize,
                      total: merchants?.pagination?.total_records,
                      onChange: handlePaginationChange,
                    }}
                    dataSource={merchants?.merchantList?.map((it: any) => {
                      let res = Object.assign(
                        it,
                        it.aggregator,
                        it.institution,
                        it.merchantGroup,
                        it.merchantAcquirer
                      );
                      return res;
                    })}
                    size="middle"
                    className="table-custom"
                    loading={loading}
                    locale={{ emptyText: "No institution found" }}
                  />
                  {/*                   </ResizeObserver>
                   */}{" "}
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default MerchantList;
