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
  InputRef,
  TableColumnType,
} from "antd";

import { RouteType } from "src/constants/routeTypes";
import AggregatorHierarchy from "./../AggregatorHierarchy";
import PageHeading from "src/components/ui/PageHeading";

import {
  useGetOutletList,
  useDeleteOutlet,
  useUpdateOutlet,
  useDeleteSubMerchant,
  useUpdateSubMerchantAcquirer,
  useUpdateMerchantAcquirer,
  useDeleteMerchantGroup,
  useUpdateMerchantGroup,
  useUpdateInstitutionDetails,
  useDeleteInstitution,
  useUpdateAggregatorDetails,
  useDeleteAggregator,
  useGetPosList,
  useGetPosListing,
  useDeletePos,
  useUpdatePos,
  useAggregator,
  useInstitution,
  useMerchantAcquirer,
  useMerchantGroup,
  useSubMerchantAcquirer,
  useOutlet,  
} from "src/api/user";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import { SearchOutlined } from "@ant-design/icons";
import { FilterDropdownProps } from "antd/es/table/interface";

interface DataType {
  key: string;
  name: string;
  age: number;
  address: string;
  tags: string[];
}

const PosList = () => {
  const [pageSize, setPageSize] = useState(10)
  const location = useLocation();
  const navigate = useNavigate();
  const childRef = useRef<any>();
  const [days, setDays] = useState(0)
  const [outletDetails, setOutletDetails] = useState<any>();
  const [posDetails, setPosDetails] = useState<any>();
  const [loading, setLoading] = useState<boolean>(true);
  const [pos, setPosList] = useState<any>([]);
  const [currentPage, setCurrentPage] = useState<number>();
  const [search, setSearch] = useState<string>("");
  const [searchBy, setSearchBy] = useState<string>("");
  const [filterBy, setFilterBy] = useState<string>("");
  const [reloadData, setReloadData] = useState<boolean>(false)
  const [subMerchantDetails, setSubMerchantDetails] = useState<any>();
  const [merchantDetails, setMerchantDetails] = useState<any>();
  const [institutionDetails, setInstitutionDetails] = useState<any>();
  const [merchantGroupDetails, setMerchantGroupDetails] = useState<any>();
  const [aggregatorDetails, setAggregatorDetails] = useState<any>();
  const [searchedColumn, setSearchedColumn] = useState('');
  const [searchText, setSearchText] = useState('');
  const searchInput = useRef<InputRef>(null);
  const {
    data: aggData,
    error: aggError,
    isSuccess: aggSuccess,
    refetch:refetchAggregator
  } = useAggregator(location.state?.aggregatorId);
  const {
    data: instData,
    error: instError,
    isSuccess: instSuccess,
  } = useInstitution(
    location.state?.institutionId,location.state?.institutionId!="");

  const {
    data: mcData,
    error: mcError,
    isSuccess: mcSuccess,
  } = useMerchantAcquirer(
location.state?.merchantAcquirerId,
    true
  );
  const {
    data: outletData,
    error: outletError,
    isSuccess: outletSuccess,
  } = useOutlet(
location.state?.outletId,
    true
  );
  const {
    data: subMcData,
    error: subMcError,
    isSuccess: subMcSuccess,
  } = useSubMerchantAcquirer(
location.state?.subMerchantId,
    true
  );


  const {
    data: mgData,
    error: mgError,
    isSuccess: mgSuccess,
  } = useMerchantGroup(
    location.state?.merchantGroupPreferenceId,true
  );
  // get Get outlet Details list API
  const {
    mutate: getPosDetails,
    data,
    error,
  } = useGetPosListing();
  const {
    mutate: getPosDetailsNoPage,
    data:dataNoPage,
    error:errorNoPage,
  } = useGetPosListing();

  // API Call Save merchant Details
  const {
    mutate: updatePosDetails,
    error: updatePosDetailsError,
    isSuccess: isSuccessUpdate,
  } = useUpdatePos();
  const {
    mutate: updateMerchantGroupDetails,
    error: updateMerchantGroupDetailsError,
    isSuccess: isSuccessUpdateMg,
  } = useUpdateMerchantGroup();

  const {
    mutate: deleteMerchantGroupById,
    error: deleteMerchantGroupError,
    isSuccess:isSuccessMgDelete,
  } = useDeleteMerchantGroup();
  const {
    mutate: deleteSubMerchantById,
    error: deleteSubMerchantError,
    isSuccess:isSuccessSubM,
  } = useDeleteSubMerchant();
  const {
    mutate: deleteOutletById,
    error: deleteOutletError,
    isSuccess:isSuccessOutlet,
  } = useDeleteOutlet();
  const {
    mutate: deleteInstitutionById,
    error: deleteInstitutionError,
    isSuccess:isSuccessInst,
  } = useDeleteInstitution();
  const {
    mutate: updateInstitution,
    error: updateInstitutionError,
    isSuccess: isSuccessUpdateInst,
  } = useUpdateInstitutionDetails();
  const {
    mutate: updateAggregatorDetails,
    error: updateAggregatorDetailsError,
    isSuccess: isSuccessUpdateAggregator,
  } = useUpdateAggregatorDetails();

  const {
    mutate: updateSubMerchantDetails,
    error: updateSubMerchantDetailsError,
    isSuccess: isSuccessUpdateSubM,
  } = useUpdateSubMerchantAcquirer();
  const {
    mutate: updateOutletDetails,
    error: updateOutletDetailsError,
    isSuccess: isSuccessUpdateOutlet,
  } = useUpdateOutlet();
// API Call Save merchant Details
const {
  mutate: updateMerchantDetails,
  error: updateMerchantDetailsError,
  isSuccess: isSuccessUpdateMerchant,
} = useUpdateMerchantAcquirer();
const daysOptions=[{key:30,value:"30 days"},
  {key:90,value:"90 days"},
  {key:365,value:"1 year"}]
  // API Call Delete Merchant Group
  const {
    mutate: deletePosById,
    error: deletePosError,
    isSuccess,
  } = useDeletePos();
  const {
    mutate: deleteAggregatorById,
    error: deleteAggregatorError,
    isSuccess:isSuccessAggregator,
  } = useDeleteAggregator();
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
    posDetails["status"] ="de-active"        ;
    updatePosDetails(posDetails);
    setPosDetails({});
  };
  const deActivateOutlet = () => {
    outletDetails["outletStatus"] ="de-active"        ;
    let request={outlet:outletDetails}
    updateOutletDetails(request);
    setOutletDetails({});
  };
  const deActivateSubMc = () => {
    subMerchantDetails["subMerchantAcquirerStatus"] ="de-active"        ;
    let request={subMerchant:subMerchantDetails}
    updateSubMerchantDetails(request);
    setSubMerchantDetails({});
  };

  const deActivateMg = () => {
    merchantGroupDetails["merchantGroupStatus"] ="de-active"        ;
    let request={merchantGroup:merchantGroupDetails}
    updateMerchantGroupDetails(request);
    setInstitutionDetails({});
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
    posDetails["status"] = "closed"
    updatePosDetails(posDetails);
    setPosDetails({});
  };
  const closeOutlet = () => {
    outletDetails["outletStatus"] = "closed"
    let request={outlet:outletDetails}
    updateOutletDetails(request);
    setOutletDetails({});
  };
  const closeSubMc = () => {
    subMerchantDetails["subMerchantAcquirerStatus"] = "closed"
    let request={subMerchant:subMerchantDetails}
    updateSubMerchantDetails(request);
    setSubMerchantDetails({});
  };

  const closeInst = () => {
    let tempInst=Object.assign({},institutionDetails)
tempInst.insitutionStatus ="closed"
        let res=Object.assign (institutionDetails,{insitutionStatus:institutionDetails.insitutionStatus})
        updateInstitution(tempInst);
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
      label: <a onClick={() => updatePosRow()}>Edit</a>,
    },
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockPos()}>
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
  const actionItemsActiveOutlet: any = [
    {
      key: "1",
      label: <a onClick={() => updateOutletRow()}>Edit</a>,
    },
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockOutlet()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
   /*  {
      key: "3",
      label: <a onClick={() => deActivateOutlet()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeOutlet()}>close</a>,
    },
  ];
  const actionItemsActiveSubMc: any = [
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
    /* {
      key: "3",
      label: <a onClick={() => deActivateSubMc()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeSubMc()}>close</a>,
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
   /*  {
      key: "3",
      label: <a onClick={() => deActivateInst()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeInst()}>close</a>,
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
      label: <a onClick={() => updatePosRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];
  const actionItemsPendingOutlet: any = [
    {
      key: "1",
      label: <a onClick={() => updateOutletRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => closeOutlet()}>close</a>,
    },
  ];
  const actionItemsUnblockSubMc: any = [
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
      label: <a onClick={() => deActivateSubMc()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeSubMc()}>close</a>,
    }
  ];
  const actionItemsPendingSubMc: any = [
    {
      key: "1",
      label: <a onClick={() => updateSubMerchantRow()}>Edit</a>,
    },
    
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


  /* useEffect(() => {
    getPosListByFilters();
  }, [searchBy]); */
  useEffect(() => {
   getPosListByFilters();

  }, [currentPage, pageSize]);
  useEffect(() => {
    if (data) {
      setPosList(data);
      setLoading(false);
    }
  }, [data]);

  useEffect(() => {
    if (reloadData) {
      getPosListByFilters()
      childRef.current?.getTreeList();
      setReloadData(false)
    }
  }, [reloadData])
  const handlePaginationChange = (pagination:any) => {
    setCurrentPage(pagination);
    setPageSize(pageSize);
  };
  useEffect(() => {
    if (filterBy !== ""&& searchBy||days) {
      getPosListByFilters();
    }
  }, [filterBy, searchBy,currentPage, pageSize,days]);

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Pos deleted successfully.",
      });

      getPosListByFilters();
      childRef.current?.getTreeList();
    }
    if (deletePosError) {
      notification["error"]({
        message: "Notification",
        description: "Something went wrong.",
      });
    }
  }, [isSuccess, deletePosError]);

  const getPosListByFilters = () => {
    const requestParams: any = {
      page: currentPage || 1,
      aggregatorPreferenceId: location.state?.aggregatorId,
      institutionPreferenceId: location.state?.institutionId,
      merchantGroupPreferenceId: location.state?.merchantGroupPreferenceId,
      merchantAcquirerPreferenceId:location.state?.merchantAcquirerId,
      subMerchantPreferenceId:location.state?.subMerchantId,
      outletPreferenceId: location.state?.outletId,

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
    getPosDetails(requestParams);
    let requestParamsNoPage= Object.assign({}, requestParams);
    requestParamsNoPage.page=0
    requestParamsNoPage.limit=0
    getPosDetailsNoPage(requestParamsNoPage);

  };
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
  const getColumnSearchProps = (dataIndex: DataIndex): TableColumnType<any> => ({
    filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters, close }) => {
  return    <div style={{ padding: 8 }} onKeyDown={(e) => e.stopPropagation()}>
        <Input
          ref={searchInput}
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
    },
    filterIcon: (filtered: boolean) => (
      <SearchOutlined rev={1} style={{ color: filtered ? '#1677ff' : undefined }} />
    ),
    onFilter: (value, record) =>{
     return record[dataIndex]
        .toString()
        .toLowerCase()
        .includes((value as string).toLowerCase())},
    onFilterDropdownOpenChange: (visible) => {
      if (visible) {
        setTimeout(() => searchInput.current?.select(), 100);
      }
    },
   /*  render: (text) =>
    {
      if(dataIndex=='aggregatorStatus'||dataIndex===){
        return    text === "active" || text === "pending" ? (
              text === "active" ? (
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
                <img src={"/images/blocked-icon.svg"} /> {"Blocked"}
              </span>
            )
          }
    
    
      return searchedColumn === dataIndex ? (
        <Highlighter
          highlightStyle={{ backgroundColor: '#ffc069', padding: 0 }}
          searchWords={[searchText]}
          autoEscape
          textToHighlight={text ? text.toString() : ''}
        />
      ) : (
        text
      )} ,*/
  });
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
  const actionItemsBlockSubMerchantAutoGen: any = [
   
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockSubMerchant()}>
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
              ...(record?.aggregatorStatus === "closed"?[]:(record?.aggregatorStatus === "active" ?actionItemsActiveAgg:
              record?.aggregatorStatus === "pending"||record?.aggregatorStatus === "de-active"||record?.aggregatorStatus === "draft" 
              ? actionItemsPendingAgg
              : actionItemsUnblockAgg))
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
      /*
      (record?.institution?.systemGenerated&&!record.isParentBlocked)? actionItemsBlockInstAutoGen:record.isParentBlocked?[]: actionItemsBlockInst)
                : actionItemsUnblockInst),
                ...actionItemsInst
      */

      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record.institution.systemGenerated||record?.insitutionStatus === "closed"||record?.aggregator?.aggregatorStatus!=="active")?[]:((record?.insitutionStatus === "active" ?actionItemsActiveInst:
              record?.insitutionStatus === "pending"||record?.insitutionStatus === "de-active"||record?.insitutionStatus === "draft" 
              ? (actionItemsPendingInst)
              : actionItemsUnblockInst)))
            ],

          }}
          trigger={["click"]}           /* disabled={record.isParentBlockedInst} */
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
              ...((record?.merchantGroup.systemGenerated||record?.merchantGroupStatus === "closed"||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active")?[]:(record?.merchantGroupStatus === "active" ?actionItemsActiveMg:
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
      ...getColumnSearchProps('merchantAcquirerId')

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
              ...((record?.merchantAcquirer?.systemGenerated||record?.merchantAcquirerStatus === 'closed'||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active"||record?.merchantGroup?.merchantGroupStatus!=="active")?[]:(record?.merchantAcquirerStatus === "active" ?actionItemsActiveMc:
              record?.merchantAcquirerStatus === "pending"||record?.merchantAcquirerStatus === "de-active"||record?.merchantAcquirerStatus === "draft" 
              ? (actionItemsPendingMc)
              : actionItemsUnblockMc))            ],
          }}
          trigger={["click"]}
          arrow
          /* disabled={record.isParentBlocked} */
        >
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
      render: (record: any) => {

     return   <Dropdown
          menu={{
            items: [
              ...((record.systemGenerated||record?.subMerchantAcquirerStatus === 'closed'||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active"||record?.merchantGroup?.merchantGroupStatus!=="active"||record?.merchantAcquirer?.merchantAcquirerStatus!=="active")?[]:(record?.subMerchantAcquirerStatus === "active" ?actionItemsActiveSubMc:
              record?.subMerchantAcquirer.subMerchantAcquirerStatus === "pending"||record?.subMerchantAcquirerStatus === "de-active"||record?.subMerchantAcquirerStatus === "draft" 
              ? (actionItemsPendingSubMc)
              : actionItemsUnblockSubMc))            ],
          }}              

          trigger={["click"]}
/*           disabled={record?.subMerchantAcquirer.isParentBlocked}
 */          arrow
        >
          <Typography.Link onClick={() => setSubMerchantDetails(record?.subMerchantAcquirer)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      },
    }, 
    
    {
      title: "OUTLET NAME",
      key: "outletName",
      //  dataIndex:"outletName",
      render: (record: any) => (
        <>
          <img
            src={record?.outletLogo}
            style={{ height: "40px", width: "40px" }}
          />
          &nbsp;
          <span style={{ color: "#3080c5", fontWeight: "500" }}>
            {record?.outlet?.outletName === null
              ? "-"
              : record?.outlet?.outletName?.charAt(0)?.toUpperCase() +
                record?.outlet?.outletName?.slice(1)}
          </span>
        </>
      ),
      ...getColumnSearchProps('outletName')

    },
    {
      title: "OUTLET ID",
       dataIndex: "outletId",
      key: "outletId",
      ...getColumnSearchProps('outletId')

    },
    {
      title: "STATUS",
/*       dataIndex: "outletStatus",
 */      key: "outletStatus",
 render: (record: any) =>
 { return record.outletStatus === "active" || record.outletStatus === "pending"||record.outletStatus==="draft" ? (
    record.outletStatus === "active" ? (
      <>
        <img src={"/images/active-icon.svg"} /> {"Active"}
      </>
    ) : (
      record.outletStatus === "pending" ? <>
      <img src={"/images/pendng-icon.svg"} /> {"Pending"}
    </>:
      <>
        <img src={"/images/pendng-icon.svg"} /> {"Draft"}
      </>
    )
  ) : (
    record.outletStatus === "de-active" ? <>
    <img src={"/images/de-active.svg"} /> {"De-Active"}
  </>:(
    record.outletStatus === "closed" ? <>
    <img src={"/images/closed-icon.svg"} /> {"Closed"}
  </>:
    <span style={{ color: "#D15241" }}>
      <img src={"/images/blocked-icon.svg"} /> {"Block"}
    </span>)
  ) }, ...getColumnSearchProps('outletStatus')

    },
    {
      title: "",
      render: (record: any) => {
       return <Dropdown
          menu={{
            items: [
              ...((record?.outlet?.outletStatus ==="closed"||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active"||record?.merchantGroup?.merchantGroupStatus!=="active"||record?.merchantAcquirer?.merchantAcquirerStatus!=="active"||record?.subMerchantAcquirer?.subMerchantAcquirerStatus!=="active")?[]:(record?.outletStatus === "active" ?actionItemsActiveOutlet:
              record?.outletStatus === "pending"||record?.outletStatus === "de-active"||record?.outletStatus === "draft" 
              ? (actionItemsPendingOutlet)
              : actionItemsUnblockOutlet))            ],
        }}
          trigger={["click"]}
/*           disabled={record.isParentBlocked}
 */          arrow
        >
          <Typography.Link onClick={() => setOutletDetails(record.outlet)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      },
    },
    {
      title: "POS ID",
      key: "posId",
      //  dataIndex:"outletName",
      render: (record: any) => (
        <>
          <img
            src={record?.outletLogo}
            style={{ height: "40px", width: "40px" }}
          />
          &nbsp;
          <span style={{ color: "#3080c5", fontWeight: "500" }}>
            {record?.pos?.posId === null
              ? "-"
              : record?.pos?.posId?.charAt(0)?.toUpperCase() +
                record?.pos?.posId?.slice(1)}
          </span>
        </>
      ),
      ...getColumnSearchProps('posId')

    },
    {
      title: "POS MAC",
       dataIndex: "posMacAddress",
      key: "posMacAddress",
      ...getColumnSearchProps('posMacAddress')

    },
    {
      title: "STATUS",
/*       dataIndex: "outletStatus",
 */      key: "status",
      render: (record: any) =>{
      return  (record.status === "active" || record.status === "pending"||record.status==="draft" ? (
        record.status === "active" ? (
          <>
            <img src={"/images/active-icon.svg"} /> {"Active"}
          </>
        ) : (
          record.status === "pending" ? <>
          <img src={"/images/pendng-icon.svg"} /> {"Pending"}
        </>:
          <>
            <img src={"/images/pendng-icon.svg"} /> {"Draft"}
          </>
        )
      ) : (
        record.status === "de-active" ? <>
        <img src={"/images/de-active.svg"} /> {"De-Active"}
      </>:(
        record.status === "closed" ? <>
        <img src={"/images/closed-icon.svg"} /> {"Closed"}
      </>:
        <span style={{ color: "#D15241" }}>
          <img src={"/images/blocked-icon.svg"} /> {"Block"}
        </span>)
      ))},
        ...getColumnSearchProps('status')

    },
    {
      title: "",
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record?.pos.status ==="closed"||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active"||record?.merchantGroup?.merchantGroupStatus!=="active"||record?.merchantAcquirer?.merchantAcquirerStatus!=="active"||record?.subMerchantAcquirer?.subMerchantAcquirerStatus!=="active"||record?.outlet?.outletStatus!=="active") ?[]:(record?.pos.status === "active" ?actionItemsActive:
              record?.pos.status === "pending"||record?.pos.status === "de-active"||record?.pos.status === "draft" 
              ? (actionItemsPending)
              : actionItemsUnblock))   
            ],
          }}
          trigger={["click"]}
/*           disabled={record.isParentBlocked}
 */          arrow
        >
          <Typography.Link onClick={() => setPosDetails(record.pos)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
  ];

  // Action items
  const actionItemsOutlet: any = [
        {
      key: "2",
      label: (
        <Popconfirm title="Sure to delete?" onConfirm={() => deleteOutlet()}>
          <span>Delete</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItems: any = [
    {
  key: "2",
  label: (
    <Popconfirm title="Sure to delete?" onConfirm={() => deletePos()}>
      <span>Delete</span>
    </Popconfirm>
  ),
},
];

  const handleDropdownItemClick=(e:any)=>{
    if(e.key==1){
    downloadOutletCSV()}
  else if(e.key==2){
    downloadOutletPDF()
  }
  }
  const downloadOutletCSV = () => {
    JSONToCSVConvertor(dataNoPage, "Pos", true)
    }
    type DataIndex = keyof any;
    const handleSearch = (
      selectedKeys: string[],
      confirm: FilterDropdownProps['confirm'],
      dataIndex: DataIndex,
    ) => {
      confirm();
      setSearchText(selectedKeys[0]);
      setSearchBy(selectedKeys[0])
      setFilterBy(dataIndex.toString())
      setSearchedColumn(dataIndex.toString());
    };
  
    const handleReset = (clearFilters: () => void) => {
      clearFilters();
      setSearchText('');
    };
   
  const JSONToCSVConvertor = (
    JSONData: any,
    ReportTitle: string,
    ShowLabel: boolean
  ) => {
    const arrData =
      typeof JSONData !== "object" ? JSON.parse(JSONData) : JSONData;
    let CSV = "";   
    const headersRequried = [
      "posId",
      "posMacAddress",
      "status"
       ];
    if (ShowLabel) {
      let row = "";
      for (const index in arrData?.posList[0].pos) {
        switch (index) {
          case "posId":
            row += "Pos ID" + ",";
            break;

            case "posMacAddress":
            row += "Pos Mac Address" + ",";
            break;

          case "status":
            row += "Status" + ",";
            break;
            default:
            break;
        }
      }
      row = row.slice(0, -1);
      CSV += row + "\r\n";
    }
    for (let i = 0; i < arrData?.posList.length; i++) {
      let row = "";
        for (const index in arrData.posList[i].pos) {
        if (headersRequried.includes(index)) {
            row += '"' + arrData?.posList[i].pos[index] + '",';
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
  const downloadOutletPDF = () => {
    if (dataNoPage) {
      const currentData =typeof dataNoPage !== "object" ? JSON.parse(dataNoPage) : dataNoPage;
      const head = [
        [
          "Pos ID",
          "Mac Address",
          "Status"
        ]
      ]
      const finalData: any = []
      currentData?.posList?.map((item:any, index:any) => {
        const arr = []
        if (Object.prototype.hasOwnProperty.call(item?.pos, "posId")) {
          arr.push(item?.pos?.posId)
        }
        if (Object.prototype.hasOwnProperty.call(item?.pos, "posMacAddress")) {
          arr.push(item?.pos?.posMacAddress)
        }
        if (Object.prototype.hasOwnProperty.call(item?.pos, "status")) {
          arr.push(item?.pos?.status)
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
  
      doc.save("myReports-pos.pdf")
    }}
    const deleteSubMerchant = () => {
      deleteSubMerchantById({
         subMerchantGroupId: subMerchantDetails.subMerchantAcquirerId,
      });
  
      subMerchantDetails({});
    };
  const actionItemsBlockOutlet: any = [
    {
      key: "1",
      label: <a onClick={() => updateOutletRow()}>Update</a>,
    },
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockOutlet()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsBlock: any = [
    {
      key: "1",
      label: <a onClick={() => updatePosRow()}>Update</a>,
    },
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to block?" onConfirm={() => blockPos()}>
          <span>Block</span>
        </Popconfirm>
      ),
    },
  ];
  const actionItemsSubMerchant: any = [
   
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to delete?" onConfirm={() => deleteSubMerchant()}>
          <span>Delete</span>
        </Popconfirm>
      ),
    },
  ];
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
          ...subMerchantDetails,
        },
      }
    );
    setSubMerchantDetails({});
  };
  const actionItemsBlockSubMerchant: any = [
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
  const deleteInstitution = () => {
    deleteInstitutionById({
      institutionId: institutionDetails.institutionId,
    });
    setInstitutionDetails({});
  };
  const deleteMerchantGroup = () => {
    deleteMerchantGroupById({
      merchantGroupId: merchantGroupDetails.merchantGroupPreferenceId,
    });

    setMerchantGroupDetails({});
  };
  const blockSubMerchant = () => {
    subMerchantDetails["subMerchantAcquirerStatus"] =
    subMerchantDetails.subMerchantAcquirerStatus == "pending" ||
    subMerchantDetails.subMerchantAcquirerStatus == "active"
        ? "block"
        : "active";
        let request={subMerchant:subMerchantDetails}
    updateSubMerchantDetails(request);
    setSubMerchantDetails({});
  };
  const actionItemsUnblocksubMerchant: any = [
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to unblock?" onConfirm={() => blockSubMerchant()}>
          <span>Unblock</span>
        </Popconfirm>
      ),
    },
  ];

  const actionItemsUnblockOutlet: any = [
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to unblock?" onConfirm={() => blockOutlet()}>
          <span>Unblock</span>
        </Popconfirm>
      ),
    },
   /*  {
      key: "3",
      label: <a onClick={() => deActivateOutlet()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeOutlet()}>close</a>,
    },
  ];
  const actionItemsUnblock: any = [
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to unblock?" onConfirm={() => blockPos()}>
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
  const updateMerchantRow = () => {
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
  const actionItemsMerchant: any = [
    
    {
      key: "2",
      label: (
        <Popconfirm title="Sure to delete?" onConfirm={() => deleteSubMerchant()}>
          <span>Delete</span>
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

  const updateOutletRow = () => {
    let update={update:true}
    navigate(
      RouteType.OUTLET_REGISTER +
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
        location.state.merchantGroupName+
         "/"+
        location.state.merchantAcquirerId +
        "/" +
        location.state.merchantAcquirerName+
        "/" +        
        location.state.subMerchantId +
        "/" +
        location.state.subMerchantName,
      {
        state: {
          ...outletDetails,update
        },
      }
    );
    setPosDetails({});
  };

  const updatePosRow = () => {
    let update={update:true}
    let showRegister={register:pos?.posList?.length !== 0&&(pos?.posList?.length>0 &&pos?.posList[0]?.aggregator?.aggregatorStatus==="active"&&pos?.posList[0]?.institution?.insitutionStatus==="active"&& pos?.posList[0]?.merchantGroup?.merchantGroupStatus==="active"&&pos?.posList[0]?.merchantAcquirer?.merchantAcquirerStatus==="active"&&pos?.posList[0]?.subMerchantAcquirer?.subMerchantAcquirerStatus==="active"&&pos?.posList[0]?.outlet?.outletStatus==="active")}
    navigate(
      RouteType.POS_REGISTER +
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
        location.state.merchantGroupName+
         "/"+
        location.state.merchantAcquirerId +
        "/" +
        location.state.merchantAcquirerName+
        "/" +        
        location.state.subMerchantId +
        "/" +
        location.state.subMerchantName+ 
        "/" +        
        location.state.outletId +
        "/" +
        location.state.outletName,
  
      {
        state: {
          ...posDetails,update,showRegister
        },
      }
    );
    setPosDetails({});
  };
  const blockOutlet = () => {
    outletDetails["outletStatus"] =
    outletDetails.outletStatus == "pending" ||
    outletDetails.outletStatus == "active"
        ? "block"
        : "active";
    let request={outlet:outletDetails}
    updateOutletDetails(request);
    setOutletDetails({});
  };
 const blockPos = () => {
  const posObj=Object.assign({},posDetails)
  posObj["status"] =
  posObj.status == "pending" ||
  posObj.status == "active"
        ? "block"
        : "active";
    let request={pos:posObj}    
    updatePosDetails(request.pos);
    setPosDetails({});
  };
  const deletePos= () => {
    deletePosById(posDetails);
    setPosDetails({});
  };
  const deleteOutlet= () => {
    deleteOutletById(outletDetails);
    setOutletDetails({});
  };

  useEffect(() => {
    getPosListByFilters() 
  }, [location])
  useEffect(() => {
    if(isSuccessUpdateInst||isSuccessUpdate||isSuccessUpdateAggregator||isSuccessUpdateMerchant||isSuccessUpdateSubM||isSuccessUpdateMg||isSuccessOutlet||isSuccessUpdateOutlet)
   { getPosListByFilters()
    childRef.current?.getTreeList();
  }
  }, [isSuccessUpdateInst,isSuccessUpdate,isSuccessUpdateAggregator,isSuccessUpdateMerchant,isSuccessUpdateSubM,isSuccessUpdateMg,isSuccessOutlet,isSuccessUpdateOutlet])
console.log(location.state)
console.log(pos?.posList)
/* console.log(pos?.posList[0])
console.log(pos?.posList[0]?.aggregator)
 */  return (
    <div>
     <PageHeading
        topTitle="Pos management"
        title="Acquirer Pos Management"
        parentRefetch={setReloadData}
        showRegister={pos?.posList?.length !== 0&&(pos?.posList?.length>0 &&pos?.posList[0]?.aggregator?.aggregatorStatus==="active"&&pos?.posList[0]?.institution?.insitutionStatus==="active"&& pos?.posList[0]?.merchantGroup?.merchantGroupStatus==="active"&&pos?.posList[0]?.merchantAcquirer?.merchantAcquirerStatus==="active"&&pos?.posList[0]?.subMerchantAcquirer?.subMerchantAcquirerStatus==="active"&&pos?.posList[0]?.outlet?.outletStatus==="active")}
        linkData={{
          label: "Register Pos",
          url:
            RouteType.POS_REGISTER +
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
            location.state.merchantGroupName+
            "/"+
            location.state.merchantAcquirerId +
            "/" +
            location.state.merchantAcquirerName+
            "/"+
            location.state.subMerchantId +
            "/" +
            location.state.subMerchantName+
            "/"+
            location.state.outletId +
            "/" +
            location.state.outletName ,
        }}
        uploadType={"OUTLET_UPLOAD"}

      />
      {data !== undefined &&
      (pos?.posList?.length === 0&&aggData?.aggregatorStatus==="active"&&instData?.insitutionStatus==="active"&&mgData?.merchantGroupStatus==="active"&&mcData?.merchantAcquirerStatus==="active"&&subMcData?.subMerchantAcquirerStatus==="active"&&outletData?.outletStatus==="active") &&

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
              No pos are available yet to manage
            </h5>
            <p style={{ fontSize: "14px" }}>
              Want to create Pos ? Please click on the below button
            </p>
            <Link
              to={
                RouteType.POS_REGISTER +
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
                location.state.merchantGroupName+
                "/"+
                location.state.merchantAcquirerId +
                "/" +
                location.state.merchantAcquirerName+
                "/"+
                location.state.subMerchantId +
                "/" +
                location.state.subMerchantName +
                "/"+
                location.state.outletId +
                "/" +
                location.state.outletName              }
              title="Register New POS "
            >
              <Button
                style={{
                  background: "#faad14",
                  color: "#ffffff",
                }}
              >
                Register New  Pos
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
                                  value: "outletName",
                                  label: "Outlet Name",
                                },
                                {
                                  value: "outletId",
                                  label: "Outlet ID",
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
                      <Dropdown  menu={{
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
                    scroll={{"x":"max-content"}}
                    dataSource={pos?.posList?.map((it:any)=>
                      {
                        return Object.assign(it,it.aggregator,it.institution,it.merchantGroup,it.merchantAcquirer,it.subMerchantAcquirer,it.outlet,it.pos)
                      })}
                      pagination={{
                        current: pos?.pagination?.current_page,
                        pageSize: pageSize,
                        total: pos?.pagination?.total_records,
                        onChange: handlePaginationChange,
                      }}
                    size="middle"
                    className="table-custom"
                    loading={loading}
                    locale={{ emptyText: "No pos found" }}
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

export default PosList;


