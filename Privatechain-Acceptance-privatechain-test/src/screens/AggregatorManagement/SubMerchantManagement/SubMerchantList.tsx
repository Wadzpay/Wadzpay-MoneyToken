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
  useGetSubMerchantGroupList,
  useDeleteSubMerchant,
  useUpdateSubMerchantAcquirer,
  useAggregator,
  useInstitution,
  useDeleteAggregator,
  useUpdateInstitutionDetails,
  useMerchantGroup,
  useDeleteInstitution,
  useUpdateAggregatorDetails,
  useDeleteMerchantGroup,
  useUpdateMerchantGroup,
  useMerchantAcquirer,
} from "src/api/user";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import { FilterDropdownProps } from "antd/es/table/interface";
import { SearchOutlined } from "@ant-design/icons";

interface DataType {
  key: string;
  name: string;
  age: number;
  address: string;
  tags: string[];
}

const SubMerchantList = () => {
  const [pageSize, setPageSize] = useState(10)
  const location = useLocation();
  const navigate = useNavigate();
  const childRef = useRef<any>();
  const [merchantDetails, setMerchantDetails] = useState<any>();
  const [submerchantDetails, setSubMerchantDetails] = useState<any>();
  const [loading, setLoading] = useState<boolean>(true);
  const [merchants, setMerchantsList] = useState<any>([]);
  const [currentPage, setCurrentPage] = useState<number>();
  const [aggregatorDetails, setAggregatorDetails] = useState<any>();
  const [search, setSearch] = useState<string>("");
  const [searchText, setSearchText] = useState('');
  const [searchedColumn, setSearchedColumn] = useState('');
  const searchInput = useRef<InputRef>(null);
  const [days, setDays] = useState(0)
  const [searchBy, setSearchBy] = useState<string>("");
  const [filterBy, setFilterBy] = useState<string>("");
  const [reloadData, setReloadData] = useState<boolean>(false)
  const [institutionDetails, setInstitutionDetails] = useState<any>();
  const [merchantGroupDetails, setMerchantGroupDetails] = useState<any>();
  const {
    mutate: deleteAggregatorById,
    error: deleteAggregatorError,
    isSuccess:isSuccessAggregator,
  } = useDeleteAggregator();
  const {
    mutate: updateInstitution,
    error: updateInstitutionError,
    isSuccess: isSuccessUpdateInst,
  } = useUpdateInstitutionDetails();
  const {
    data: mcData,
    error: mcError,
    isSuccess: mcSuccess,
  } = useMerchantAcquirer(
location.state?.merchantAcquirerId,
    true
  );

  const {
    data: mgData,
    error: mgError,
    isSuccess: mgSuccess,
  } = useMerchantGroup(
    location.state?.merchantGroupPreferenceId,true
  );
  const {
    data: aggData,
    error: aggError,
    isSuccess: aggSuccess,
    refetch:refetchAggregator
  } = useAggregator(location.state?.aggregatorId);
    // API Call Delete Merchant 
    const {
      mutate: deleteMerchantById,
      error: deleteMerchantError,
      isSuccess:isSuccessDeleteMerchant,
    } = useDeleteMerchant();
  // API Call Save merchant Details
  const {
    mutate: updateMerchantDetails,
    error: updateMerchantDetailsError,
    isSuccess: isSuccessUpdateMerchant,
  } = useUpdateMerchantAcquirer();
  const {
    data: instData,
    error: instError,
    isSuccess: instSuccess,
    refetch:refetchInst
  } = useInstitution(
    location.state?.institutionId,location.state?.institutionId!="");
    const {
      mutate: deleteInstitutionById,
      error: deleteInstitutionError,
      isSuccess:isSuccessInst,
    } = useDeleteInstitution();
  // get Get Institution Details list API
  const {
    mutate: getMercahntDetails,
    data,
    error,
  } = useGetSubMerchantGroupList();
  const {
    mutate: getMercahntDetailsNoPage,
    data:dataNoPage,
    error:errorNoPage,    
  } = useGetSubMerchantGroupList();
  const {
    mutate: updateMerchantGroupDetails,
    error: updateMerchantGroupDetailsError,
    isSuccess: isSuccessUpdateMg,
  } = useUpdateMerchantGroup();
  // API Call Delete Merchant Group
  const {
    mutate: deleteMerchantGroupById,
    error: deleteMerchantGroupError,
    isSuccess:isSuccessMgDelete,
  } = useDeleteMerchantGroup();

  // API Call Save merchant Details
  const {
    mutate: updateSubMerchantDetails,
    error: updateSubMerchantDetailsError,
    isSuccess: isSuccessUpdate,
  } = useUpdateSubMerchantAcquirer();

  const {
    mutate: updateAggregatorDetails,
    error: updateAggregatorDetailsError,
    isSuccess: isSuccessUpdateAggregator,
  } = useUpdateAggregatorDetails();

  // API Call Delete Merchant Group
  const {
    mutate: deleteSubMerchantById,
    error: deleteSubMerchantError,
    isSuccess,
  } = useDeleteSubMerchant();
  const daysOptions=[{key:30,value:"30 days"},
  {key:90,value:"90 days"},
  {key:365,value:"1 year"},{key:0,value:'Till Date'}]
  useEffect(() => {
    getMerchantList();
  }, [searchBy]);

  useEffect(() => {
    if (data) {
      setMerchantsList(data);
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
    if (filterBy !== ""||days||loading) {
      getMerchantList();
    }
  }, [filterBy, searchBy,days]);

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Sub Merchant deleted successfully.",
      });

      getMerchantList();
      childRef.current?.getTreeList();
    }
    if (deleteSubMerchantError) {
      notification["error"]({
        message: "Notification",
        description: "Something went wrong.",
      });
    }
  }, [isSuccess, deleteSubMerchantError]);

  const getMerchantList = () => {
    const requestParams: any = {
      page: currentPage || 1,
      aggregatorPreferenceId: location.state?.aggregatorId,
      institutionPreferenceId: location.state?.institutionId,
      merchantGroupPreferenceId: location.state?.merchantGroupPreferenceId,
      merchantAcquirerPreferenceId:location.state?.merchantAcquirerId,
      // sortBy: "STATUS",
      // sortDirection: "DESC",
      duration:days,
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
tempInst.insitutionStatus ="de-active";
 let res=Object.assign (institutionDetails,{insitutionStatus:institutionDetails.insitutionStatus})
        updateInstitution(tempInst);


  };
  const deActivateMc = () => {
    merchantDetails["merchantAcquirerStatus"] ="de-active"        ;
      let request={merchant:merchantDetails}
  updateMerchantDetails(request);
  setMerchantDetails({});

  };
  const deActivate = () => {
    submerchantDetails["subMerchantAcquirerStatus"] ="de-active"
    let request={subMerchant:submerchantDetails}
    updateSubMerchantDetails(request);
    setSubMerchantDetails({});

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

  const closeMc = () => {
    merchantDetails["merchantAcquirerStatus"] = "closed"
    let request={merchant:merchantDetails}
updateMerchantDetails(request);
setMerchantDetails({});
};
  const close = () => {
    submerchantDetails["subMerchantAcquirerStatus"] ="closed"
    let request={subMerchant:submerchantDetails}
    updateSubMerchantDetails(request);
    setSubMerchantDetails({});
  };
  const closeInst = () => {
    let tempInst=Object.assign({},institutionDetails)
    tempInst.insitutionStatus ="closed";
     let res=Object.assign (institutionDetails,{insitutionStatus:institutionDetails.insitutionStatus})
            updateInstitution(tempInst);    
  };

  const handleDaysChange = (value: string) => {
    setDays(Number.parseInt(value))
      };
    
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
  const updateAggregatorRow = () => {
    navigate(RouteType.AGGREGATOR_UPDATE, {
      state: {
        ...aggregatorDetails,
      },
    });
    setAggregatorDetails({});
  };   

  useEffect(() => {
    getMerchantList();
  }, [currentPage, pageSize]); 
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
  const deleteMerchantGroup = () => {
    deleteMerchantGroupById({
      merchantGroupId: merchantGroupDetails.merchantGroupPreferenceId,
    });

    setMerchantGroupDetails({});
  };

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
   /*  {
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
  const blockMerchantGroup = () => {
    merchantGroupDetails["merchantGroupStatus"] =
      merchantGroupDetails.merchantGroupStatus == "pending" ||
      merchantGroupDetails.merchantGroupStatus == "active"
        ? "block"
        : "active";
        let request={merchantGroup:merchantGroupDetails}
    updateMerchantGroupDetails(request);
    setMerchantGroupDetails({});
  }
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
    }
  ];
  const deleteMerchant = () => {
    deleteMerchantById({
      merchantAcquirerId: merchantDetails.aggregatorPreferenceId,
    });

    setMerchantDetails({});
  };
  const actionItemsMerchant: any = [
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to delete?" onConfirm={() => deleteMerchant()}>
          <span>Delete</span>
        </Popconfirm>
      ),
    },
  ];
  const updateMerchantRow = () => {
    console.log(">>>>>> mohit merchantDetails", merchantDetails);
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
  const actionItemsBlockMerchant: any = [
    {
      key: "1",
      label: <a onClick={() => updateMerchantRow()}>Update</a>,
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
   /*  {
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
   /*  {
      key: "3",
      label: <a onClick={() => deActivateAggregator()}>De-Activate</a>,
    },
 */    {
      key: "4",
      label: <a onClick={() => closeAggregator()}>close</a>,
    },
  ];

  const actionItemsActive: any = [
    {
      key: "1",
      label: <a onClick={() => updateSubMerchantRow()}>Edit</a>,
    },
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockSubMerchant()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
   /*  {
      key: "3",
      label: <a onClick={() => deActivate()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];
  const actionItemsActiveMc: any = [
    {
      key: "1",
      label: <a onClick={() => updateMerchantRow()}>Edit</a>,
    },
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockMerchant()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
    /* {
      key: "3",
      label: <a onClick={() => deActivateMc()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeMc()}>close</a>,
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
  const actionItemsBlockMerchantAutoGen: any = [
   
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockMerchant()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsUnblockMerchant: any = [
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to unblock?" onConfirm={() => blockMerchant()}>
          <span>Unblock</span>
        </Popconfirm>
      ),
    },
  ];
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
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...(record?.aggregatorStatus ==="closed"?[]:(record?.aggregatorStatus === "active" ?actionItemsActiveAgg:
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
      ),
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
      dataIndex: "insitutionPreferenceId",
      key: "insitutionPreferenceId",
      ...getColumnSearchProps('insitutionPreferenceId')

    },
    {
      title: "STATUS",
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
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record?.institution?.systemGenerated||record?.insitutionStatus ==='closed'||record?.aggregator?.aggregatorStatus!=="active")?[]:(record?.insitutionStatus === "active" ?actionItemsActiveInst:
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
      dataIndex: "merchantGroupPreferenceId",
      key: "merchantGroupPreferenceId",
      ...getColumnSearchProps('merchantGroupPreferenceId')

    },
    {
      title: "STATUS",
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
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record?.merchantGroup?.systemGenerated||record?.merchantGroupStatus === "closed"||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active")?[]:(record?.merchantGroupStatus === "active" ?actionItemsActiveMg:
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
      dataIndex: "merchantAcquirerId",
      key: "merchantAcquirerId",
    },
    {
      title: "STATUS",
/*       dataIndex: "merchantAcquirerStatus", */
      key: "merchantAcquirerStatus",
      render: (record: any) =>{
        return    record.merchantAcquirerStatus === "active" || record.merchantAcquirerStatus === "pending"||record.merchantAcquirerStatus==="draft" ? (
          record.merchantAcquirerStatus === "active" ? (
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
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record?.merchantAcquirer?.systemGenerated||record?.merchantAcquirerStatus === "closed"||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active"||record?.merchantGroup?.merchantGroupStatus!=="active")?[]:(record?.merchantAcquirerStatus === "active" ?actionItemsActiveMc:
              record?.merchantAcquirerStatus === "pending"||record?.merchantAcquirerStatus === "de-active"||record?.merchantAcquirerStatus === "draft" 
              ? (actionItemsPendingMc)
              : actionItemsUnblockMc))            ],
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
    {
      title: "SUB MERCHANT NAME",
      key: "subMerchantAcquirerName",
      render: (record: any) => (
        <>
          <img
            src={record?.subMerchantAcquirerLogo}
            style={{ height: "40px", width: "40px" }}
          />
          &nbsp;
          <span style={{ color: "#3080c5", fontWeight: "500" }}>
            {record?.subMerchantAcquirerName === null
              ? "-"
              : record?.subMerchantAcquirerName?.charAt(0)?.toUpperCase() +
                record?.subMerchantAcquirerName?.slice(1)}
          </span>
        </>
      ),
      ...getColumnSearchProps('subMerchantAcquirerName')

    },
    {
      title: "SUB MERCHANT ID",
      dataIndex: "subMerchantAcquirerId",
      key: "subMerchantAcquirerId",
      ...getColumnSearchProps('subMerchantAcquirerId')

    },
    
    {
      title: "STATUS",
      /* dataIndex: "subMerchantAcquirerStatus", */
      key: "subMerchantAcquirerStatus",
      render: (record: any) =>
     { return record.subMerchantAcquirerStatus === "active" || record.subMerchantAcquirerStatus === "pending"||record.subMerchantAcquirerStatus==="draft" ? (
        record.subMerchantAcquirerStatus === "active" ? (
          <>
            <img src={"/images/active-icon.svg"} /> {"Active"}
          </>
        ) : (
          record.subMerchantAcquirerStatus === "pending" ? <>
          <img src={"/images/pendng-icon.svg"} /> {"Pending"}
        </>:
          <>
            <img src={"/images/pendng-icon.svg"} /> {"Draft"}
          </>
        )
      ) : (
        record.subMerchantAcquirerStatus === "de-active" ? <>
        <img src={"/images/de-active.svg"} /> {"De-Active"}
      </>:(
        record.subMerchantAcquirerStatus === "closed" ? <>
        <img src={"/images/closed-icon.svg"} /> {"Closed"}
      </>:
        <span style={{ color: "#D15241" }}>
          <img src={"/images/blocked-icon.svg"} /> {"Block"}
        </span>)
      ) },
        ...getColumnSearchProps('subMerchantAcquirerStatus')

    },
    {
      title: "",
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record?.subMerchantAcquirer.systemGenerated||record?.subMerchantAcquirerStatus === "closed"||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active"||record?.merchantGroup?.merchantGroupStatus!=="active"||record?.merchantAcquirer?.merchantAcquirerStatus!=="active")?[]:(record?.subMerchantAcquirerStatus === "active" ?actionItemsActive:
              record?.subMerchantAcquirerStatus === "pending"||record?.subMerchantAcquirerStatus === "de-active"||record?.subMerchantAcquirerStatus === "draft" 
              ? (actionItemsPending)
              : actionItemsUnblock))            ],
          }}              
              
            
          trigger={["click"]}
          arrow
/*           disabled={record.isParentBlocked}
 */        >
          <Typography.Link onClick={() => setSubMerchantDetails(record.subMerchantAcquirer)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
  ];

  // Action items
  const actionItems: any = [
   
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to delete?" onConfirm={() => deleteSubMerchant()}>
          <span>Delete</span>
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
      label: <a onClick={() => updateSubMerchantRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];
  const actionItemsPendingMc: any = [
    {
      key: "1",
      label: <a onClick={() => updateMerchantRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => closeMc()}>close</a>,
    },
  ];
  const actionItemsBlock: any = [
    {
      key: "1",
      label: <a onClick={() => updateSubMerchantRow()}>Update</a>,
    },
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockSubMerchant()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsBlockAutoGen: any = [
   
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockSubMerchant()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  
  const actionItemsUnblock: any = [
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to unblock?" onConfirm={() => blockSubMerchant()}>
          <span>Unblock</span>
        </Popconfirm>
      ),
    },
   /*  {
      key: "3",
      label: <a onClick={() => deActivate()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];
  const actionItemsUnblockMc: any = [
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
      label: <a onClick={() => deActivateMc()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeMc()}>close</a>,
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

  const updateSubMerchantRow = () => {
    navigate(
      RouteType.SUB_MERCHANT_REGISTER +
        "/" +
        location.state?.aggregatorId +
        "/" +
        location.state?.aggregatorName +
        "/" +
        location.state?.institutionId +
        "/" +
        location.state?.institutionName +
        "/" +
        location.state?.merchantGroupPreferenceId +
        "/" +
        location.state?.merchantGroupName+
        "/"+
        location.state?.merchantAcquirerId +
        "/" +
        location.state?.merchantAcquirerName,
      {
        state: {
          ...submerchantDetails,
        },
      }
    );
    setSubMerchantDetails({});
  };

  const blockSubMerchant = () => {
    submerchantDetails["subMerchantAcquirerStatus"] =
    submerchantDetails.subMerchantAcquirerStatus == "pending" ||
    submerchantDetails.subMerchantAcquirerStatus == "active"
        ? "block"
        : "active";
        let request={subMerchant:submerchantDetails}
    updateSubMerchantDetails(request);
    setSubMerchantDetails({});
  };

  const deleteSubMerchant = () => {
    deleteSubMerchantById({
       subMerchantGroupId: submerchantDetails.subMerchantAcquirerId,
    });

    setSubMerchantDetails({});
  };
  const handleDropdownItemClick=(e:any)=>{
    if(e.key==1){
    downloadSubMerchantCSV()}
  else if(e.key==2){
    downloadSubMerchantPDF()
  }
  }
  const downloadSubMerchantCSV = () => {
    JSONToCSVConvertor(dataNoPage, "SubMerchant", true)
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
      "subMerchantAcquirerName",
      "subMerchantAcquirerId",
      "subMerchantAcquirerStatus"
       ];
    if (ShowLabel) {
      let row = "";
      for (const index in arrData?.merchantList[0]?.subMerchantAcquirer) {
        switch (index) {
          case "subMerchantAcquirerName":
            row += "SubMerchant Name" + ",";
            break;
          case "subMerchantAcquirerId":
            row += "SubMerchant ID" + ",";
  
            break;
          case "subMerchantAcquirerStatus":
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
      for (const index in arrData.merchantList[i]?.subMerchantAcquirer) {
        if (headersRequried.includes(index)) {
            row += '"' + arrData?.merchantList[i]?.subMerchantAcquirer[index] + '",';
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
  const downloadSubMerchantPDF = () => {
    if (dataNoPage) {
      const currentData =typeof dataNoPage !== "object" ? JSON.parse(dataNoPage) : dataNoPage;
      const head = [
        [
          "Submerchant Name",
          "Submerchant ID",
          "Status"
        ]
      ]
      const finalData: any = []
      currentData?.merchantList?.map((item:any, index:any) => {
        const arr = []
        if (Object.prototype.hasOwnProperty.call(item.subMerchantAcquirer, "subMerchantAcquirerName")) {
          arr.push(item?.subMerchantAcquirer.subMerchantAcquirerName)
        }
        if (Object.prototype.hasOwnProperty.call(item.subMerchantAcquirer, "subMerchantAcquirerId")) {
          arr.push(item?.subMerchantAcquirer.subMerchantAcquirerId)
        }
        if (Object.prototype.hasOwnProperty.call(item.subMerchantAcquirer, "subMerchantAcquirerStatus")) {
          arr.push(item?.subMerchantAcquirer.subMerchantAcquirerStatus)
        }
        finalData.push(arr)
      })
      const doc = new jsPDF()
      autoTable(doc, {
        head: head,
        body: finalData,
        columnStyles: {
          0: { cellWidth: 45 },
          1: { cellWidth: 58 },
          5: { cellWidth: 20 }
        }
      })
  
      doc.save("myReports-submerchant.pdf")
    }}
  useEffect(() => {
    getMerchantList() 
  }, [location])
  useEffect(() => {
    if(isSuccessUpdateInst||isSuccessUpdateMg||isSuccessUpdateMerchant||isSuccessUpdate||isSuccessUpdateAggregator)
    {getMerchantList()
      childRef.current?.getTreeList();
    }
  }, [isSuccessUpdateInst,isSuccessUpdateMg,isSuccessUpdateMerchant,isSuccessUpdate,isSuccessUpdateAggregator])
  const handlePaginationChange = (pagination:any) => {
    setCurrentPage(pagination);
    setPageSize(pageSize);
  };
  return (
    <div>
     <PageHeading
        topTitle="Sub Merchant management"
        title="Acquirer Sub Merchant Management"
        parentRefetch={setReloadData}
        showRegister={merchants?.merchantList?.length !== 0&&(merchants&&merchants?.merchantList?.length >0&&merchants?.merchantList[0].aggregator
          ?.aggregatorStatus==="active"&&merchants?.merchantList[0].institution?.insitutionStatus==="active"&&merchants?.merchantList[0]?.merchantGroup?.merchantGroupStatus==="active"&&merchants?.merchantList[0]?.merchantAcquirer?.merchantAcquirerStatus==="active")}
        linkData={{
          label: "Register Sub Merchant",
          url:
            RouteType.SUB_MERCHANT_REGISTER +
            "/" +
            location.state?.aggregatorId +
            "/" +
            location.state?.aggregatorName +
            "/" +
            location.state?.institutionId +
            "/" +
            location.state?.institutionName +
            "/" +
            location.state?.merchantGroupPreferenceId +
            "/" +
            location.state?.merchantGroupName +
            "/" +
            location.state?.merchantAcquirerId +
            "/" +
            location.state?.merchantAcquirerName,
        }}
        uploadType={"SUBMERCHANT_UPLOAD"}

      />
      {data !== undefined &&(merchants?.merchantList?.length === 0&&(aggData?.aggregatorStatus==="active"&&instData?.insitutionStatus==="active"&&mgData?.merchantGroupStatus==="active"&&mcData?.merchantAcquirerStatus==="active"))&&      
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
              No Sub Merchant are available yet to manage
            </h5>
            <p style={{ fontSize: "14px" }}>
              Want to create Sub Merchant ? Please click on the below button
            </p>
            <Link
              to={
                RouteType.SUB_MERCHANT_REGISTER +
                "/" +
                location.state?.aggregatorId +
                "/" +
                location.state?.aggregatorName +
                "/" +
                location.state?.institutionId +
                "/" +
                location.state?.institutionName +
                "/" +
                location.state?.merchantGroupPreferenceId +
                "/" +
                location.state?.merchantGroupName +
                "/" +
                location.state?.merchantAcquirerId +
                "/" +
                location.state?.merchantAcquirerName              }
              title="Register New Sub Merchant "
            >
              <Button
                style={{
                  background: "#faad14",
                  color: "#ffffff",
                }}
              >
                Register New  Sub Merchant
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
                                  value: "subMerchantName",
                                  label: "Sub Merchant Name",
                                },
                                {
                                  value: "subMerchantId",
                                  label: "Sub Merchant ID",
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
                            <Input
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
                      <Dropdown menu={{
                          onClick: handleDropdownItemClick,
                          items: items,
                        }} trigger={["click"]} arrow>
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
                    dataSource={merchants?.merchantList?.map((it:any)=>{
                      let res=Object.assign(it,it.aggregator,it.institution,it.merchantGroup,it.merchantAcquirer,it.subMerchantAcquirer)
                      return res})}
                    size="middle"
                    className="table-custom"
                    scroll={{ x: "max-content" }}
                    pagination={{
                      current: merchants?.pagination?.current_page,
                      pageSize: pageSize,
                      total: merchants?.pagination?.total_records,
                      onChange: handlePaginationChange,
                    }}
                    loading={loading}
                    locale={{ emptyText: "No Submerchants found" }}
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

export default SubMerchantList;
