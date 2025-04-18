import React, { useState, useEffect, useRef } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  Space,
  Table,
  Tag,
  Dropdown,
  Select,
  MenuProps,
  Input,
  Form,
  Button,
  notification,
  Typography,
  Popconfirm,
  TableColumnType,
  InputRef,
} from "antd";
import type { ColumnsType } from "antd/es/table";

import { RouteType } from "src/constants/routeTypes";
import AggregatorHierarchy from "./../AggregatorHierarchy";
import PageHeading from "src/components/ui/PageHeading";

import {
  useGetMerchantGroupList,
  useDeleteMerchantGroup,
  useUpdateMerchantGroup,
  useAggregator,
  useInstitution,
  useUpdateAggregatorDetails,
  useDeleteAggregator,
  useDeleteInstitution,
  useUpdateInstitutionDetails,
  useMerchantGroup,
} from "src/api/user";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import { FilterDropdownProps } from "antd/es/table/interface";
import { SearchOutlined } from "@ant-design/icons";
import MerchantDetails from "src/screens/Onboarding/MerchantDetails";
interface DataType {
  key: string;
  name: string;
  age: number;
  address: string;
  tags: string[];
}
export type MerchantGroupAll = {
  merchantGroup: any;
  institution: any;
  aggregator: any;
};

const InstitutionList = (props: any) => {
  const [pageSize, setPageSize] = useState(10)
  const [totalItems, setTotalItems] = useState(0);
  const navigate = useNavigate();
  const location = useLocation();
  const childRef = useRef<any>();
  const [loading, setLoading] = useState<boolean>(true);
  const searchInput = useRef<InputRef>(null);
  const [institutionDetails, setInstitutionDetails] = useState<any>();
  const [merchantGroupDetails, setMerchantGroupDetails] = useState<any>();
  const {
    mutate: deleteAggregatorById,
    error: deleteAggregatorError,
    isSuccess: isSuccessAggregator,
  } = useDeleteAggregator();
  const {
    data: mgData,
    error: mgError,
    isSuccess: mgSuccess,
  } = useMerchantGroup(location.state?.merchantGroupId, true);
  const {
    data: aggData,
    error: aggError,
    isSuccess: aggSuccess,
    refetch: refetchAggregator,
  } = useAggregator(location.state?.aggregatorId);
  const {
    data: instData,
    error: instError,
    isSuccess: instSuccess,
    refetch: refetchInst,
  } = useInstitution(
    location.state?.institutionId,
    location.state?.institutionId != ""
  );
  const [merchants, setMerchantsList] = useState<any>([]);
  const [currentPage, setCurrentPage] = useState<number>();
  const [search, setSearch] = useState<string>("");
  const [searchBy, setSearchBy] = useState<string>("");
  const [searchedColumn, setSearchedColumn] = useState("");
  const [searchText, setSearchText] = useState("");
  const [filterBy, setFilterBy] = useState<string>("");
  const [days, setDays] = useState(0)
  const [reloadData, setReloadData] = useState<boolean>(false);
  const [aggregatorDetails, setAggregatorDetails] = useState<any>();
  const {
    mutate: updateAggregatorDetails,
    error: updateAggregatorDetailsError,
    isSuccess: isSuccessUpdateAggregator,
  } = useUpdateAggregatorDetails();
  // get Get Merchant Group Details list API
  const { mutate: getMercahntDetails, data, error } = useGetMerchantGroupList();
  const {
    mutate: getMercahntDetailsNoPage,
    data: dataNoPage,
    error: errorNoPage,
  } = useGetMerchantGroupList();

  // API Call Update Institution Details
  const {
    mutate: updateMerchantGroupDetails,
    error: updateMerchantGroupDetailsError,
    isSuccess: isSuccessUpdate,
  } = useUpdateMerchantGroup();
  
  // API Call Delete Institution
  const {
    mutate: deleteInstitutionById,
    error: deleteInstitutionError,
    isSuccess:isSuccessInst,
  } = useDeleteInstitution(); 

  // API Call Delete Merchant Group
  const {
    mutate: deleteMerchantGroupById,
    error: deleteMerchantGroupError,
    isSuccess,
  } = useDeleteMerchantGroup();
  const {
    mutate: updateInstitution,
    error: updateInstitutionError,
    isSuccess: isSuccessUpdateInst,
  } = useUpdateInstitutionDetails();

  useEffect(() => {
    getMerchantList();
  }, [searchBy]);
  useEffect(() => {
    refetchInst();
    getMerchantList()
    childRef.current?.getTreeList();
  }, [isSuccessUpdateInst, isSuccessUpdateAggregator, isSuccessUpdate]);

  useEffect(() => {
    refetchAggregator();
  }, [isSuccessUpdateAggregator]);

  useEffect(() => {
    if (data) {
      setMerchantsList(data);
      setLoading(false);
    }
  }, [data]);

  useEffect(() => {
    if (reloadData) {
      getMerchantList();
      childRef.current?.getTreeList();
      setReloadData(false);
    }
  }, [reloadData]);

  useEffect(() => {
    if (filterBy !== ""||days) {
      getMerchantList();
    }
  }, [filterBy, searchBy,days]);

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Merchant group deleted successfully.",
      });

      getMerchantList();
      childRef.current?.getTreeList();
    }
    if (deleteMerchantGroupError) {
      notification["error"]({
        message: "Notification",
        description: "Something went wrong.",
      });
    }
  }, [isSuccess, deleteMerchantGroupError]);
  useEffect(() => {
    getMerchantList();
    childRef.current?.getTreeList();
  }, [location, isSuccessUpdateInst, isSuccessUpdateAggregator,isSuccessUpdate]);
  const getMerchantList = () => {
    const requestParams: any = {
      page: currentPage || 1,
      aggregatorPreferenceId: location.state?.aggregatorId,
      institutionPreferenceId: location.state?.institutionId,
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
    getMercahntDetails(requestParams);
    let requestParamsNoPage = Object.assign({}, requestParams);
    requestParamsNoPage.page = 0;
    requestParamsNoPage.limit = 0;
    getMercahntDetailsNoPage(requestParamsNoPage);
  };
  type DataIndex = keyof any;
  const handleSearch = (
    selectedKeys: string[],
    confirm: FilterDropdownProps["confirm"],
    dataIndex: DataIndex
  ) => {
    confirm();
  if(dataIndex.toString().toLocaleLowerCase().includes('status')){
    setSearchText(selectedKeys[0]);
    setSearchedColumn("isParentBlocked");
  }
    setSearchText(selectedKeys[0]);
    setSearchedColumn(dataIndex.toString());
  };
/*   type DataIndex = keyof any;
  const handleSearch = (
    selectedKeys: string[],
    confirm: FilterDropdownProps['confirm'],
    dataIndex: DataIndex,
  ) => {
    confirm();
    setSearchText(selectedKeys[0]);
    setSearchedColumn(dataIndex.toString());
  };
 */
  const handleReset = (clearFilters: () => void) => {
    clearFilters();
    setSearchText("");
  };
  useEffect(() => {
    getMerchantList();
  }, [currentPage, pageSize]);

  const getColumnSearchProps = (
    dataIndex: DataIndex
  ): TableColumnType<any> => ({
    filterDropdown: ({
      setSelectedKeys,
      selectedKeys,
      confirm,
      clearFilters,
      close,
    }) => (
      <div style={{ padding: 8 }} onKeyDown={(e) => e.stopPropagation()}>
        <Input
          aria-autocomplete="both"
          aria-haspopup="false"
          ref={searchInput}
          placeholder={`Search ${dataIndex.toString()}`}
          value={selectedKeys[0]}
          autoComplete="off"
          type="search"
          onChange={(e) =>
            setSelectedKeys(e.target.value ? [e.target.value] : [])
          }
          onPressEnter={() =>
            handleSearch(selectedKeys as string[], confirm, dataIndex)
          }
          style={{ marginBottom: 8, display: "block" }}
        />
        <Space>
          <Button
            type="primary"
            onClick={() =>
              handleSearch(selectedKeys as string[], confirm, dataIndex)
            }
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
      <SearchOutlined
        rev={1}
        style={{ color: filtered ? "#1677ff" : undefined }}
      />
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

  const blockInstitution = () => {
    let tempInst = Object.assign({}, institutionDetails);
    tempInst.insitutionStatus =
      tempInst.insitutionStatus == "pending" ||
      tempInst.insitutionStatus == "active"
        ? "block"
        : "active";
    let res = Object.assign(institutionDetails, {
      insitutionStatus: institutionDetails.insitutionStatus,
    });
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
  const deleteAggregator = () => {
    deleteAggregatorById({
      aggregatorId: aggregatorDetails.aggregatorPreferenceId,
    });
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
  const deActivateInst = () => {
    institutionDetails["insitutionStatus"] ="de-active"
        ;
    updateInstitution(institutionDetails);
    setInstitutionDetails({});
  };
  const deActivate = () => {
    let tempmerchantGroupDetails=Object.assign({},merchantGroupDetails)
    tempmerchantGroupDetails["merchantGroupStatus"] ="de-active"
    let request = { merchantGroup: tempmerchantGroupDetails };
    updateMerchantGroupDetails(request);
    setMerchantGroupDetails({});
  };
  const close = () => {
    let tempmerchantGroupDetails=Object.assign({},merchantGroupDetails)
    tempmerchantGroupDetails["merchantGroupStatus"] ="closed"
    let request = { merchantGroup: tempmerchantGroupDetails };
    updateMerchantGroupDetails(request);
    setMerchantGroupDetails({});
  };
  const closeInst = () => {
    institutionDetails["insitutionStatus"] = "closed"
    updateInstitution(institutionDetails);
    setInstitutionDetails({});
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
    }, */
    {
      key: "4",
      label: <a onClick={() => closeAggregator()}>close</a>,
    },
  ];

  const actionItemsActive: any = [
    {
      key: "1",
      label: <a onClick={() => updateMarchantGroupRow()}>Edit</a>,
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
      label: <Popconfirm title="Sure to deactivate?" onConfirm={()  => deActivate()}><span>De-Activate  </span>       </Popconfirm>
      ,
    }, */
    {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
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
      label: <a onClick={() => updateMarchantGroupRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
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

  const actionItemsPendingAutoGen: any = [
    {
      key: "1",
      label: <a onClick={() => updateMarchantGroupRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => close()}>close</a>,
    },
  ];
  const actionItemsPendingAutoGenInst: any = [
    {
      key: "1",
      label: <a onClick={() => updateInstitutionRow()}>Edit</a>,
    },
    
       {
      key: "4",
      label: <a onClick={() => closeInst()}>close</a>,
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
    /* {
      key: "3",
      label: <a onClick={() => deActivateAggregator()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeAggregator()}>close</a>,
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
  const actionItemsBlockInstBlocked: any = [    
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
    /* {
      key: "3",
      label: <a onClick={() => deActivateInst()}>De-Activate</a>,
    }, */
    {
      key: "4",
      label: <a onClick={() => closeInst()}>close</a>,
    },
  ];
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
       */ key: "aggregatorName",
      render: (record: any) => (
        <span style={{ color: "#3080c5", fontWeight: "500" }}>
          {record?.aggregatorName}
        </span>
      ),
      ...getColumnSearchProps("aggregatorName"),
    },
    {
      title: "AGGREGATOR ID",
      dataIndex: "aggregatorPreferenceId",
      key: "aggregatorPreferenceId",
      ...getColumnSearchProps("aggregatorPreferenceId"),
    },
    {
      title: "STATUS",
      dataIndex: "aggregatorStatus",
      key: "aggregatorStatus",
      render: (aggregatorStatus: any) =>
      aggregatorStatus === "active" || aggregatorStatus === "pending"||aggregatorStatus==="draft" ? (
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
          <img src={"/images/closed.svg"} /> {"Closed"}
        </>:
          <span style={{ color: "#D15241" }}>
            <img src={"/images/blocked-icon.svg"} /> {"Block"}
          </span>)        ),
      ...getColumnSearchProps("aggregatorStatus"),
    },
    {
      title: "",
      render: (record: any) => (
        <Dropdown
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
           */ arrow
        >
          <Typography.Link
            onClick={() => setAggregatorDetails(record?.aggregator)}
          >
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
    {
      title: "INSTITUTION NAME",
      key: "insitutionName",
      /*       dataIndex: "insitutionName",
       */ render: (record: any) => {
        return (
          <>
            <img
              src={record?.institutionLogo}
              style={{ height: "40px", width: "40px" }}
            />
            &nbsp;
            <span style={{ color: "#3080c5", fontWeight: "500" }}>
              {/*             {record}
               */}{" "}
              {record?.insitutionName?.charAt(0).toUpperCase() +
                record?.insitutionName?.slice(1)}
            </span>
          </>
        );
      },
      ...getColumnSearchProps("insitutionName"),
    },
    {
      title: "INSTITUTION ID",
      dataIndex: "insitutionPreferenceId",
      key: "insitutionPreferenceId",
      ...getColumnSearchProps("insitutionPreferenceId"),
    },
    {
      title: "STATUS",
      /*        dataIndex: "insitutionStatus",
       */ key: "insitutionStatus",
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
      ...getColumnSearchProps("insitutionStatus"),
    },
    {
      title: "",
      render: (record: any) => {
  return      <Dropdown
          menu={{
            items: [
              ...((record?.institution?.systemGenerated||record?.insitutionStatus === "closed"||record?.aggregator?.aggregatorStatus!=="active")?[]:(record?.insitutionStatus === "active" ?actionItemsActiveInst:
              record?.insitutionStatus === "pending"||record?.insitutionStatus === "de-active"||record?.insitutionStatus === "draft" 
              ? (actionItemsPendingInst)
              : actionItemsUnblockInst))
            ],
          }}
          trigger={["click"]}
          /* disabled={record.isParentBlockedInst}
           */ arrow
        >
          <Typography.Link
            onClick={() => {
              setInstitutionDetails(instData);
            }}
          >
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      },
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
      ...getColumnSearchProps("merchantGroupName"),
    },
    {
      title: "MERCHANT GROUP ID",
      dataIndex: "merchantGroupPreferenceId",
      key: "merchantGroupPreferenceId",
      ...getColumnSearchProps("merchantGroupPreferenceId"),
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
      )}
       /*  (record.merchantGroupStatus === "active" ||
          record.merchantGroupStatus === "pending") &&
        !record.isParentBlocked ? (
          record.merchantGroupStatus === "active" ? (
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
      ...getColumnSearchProps("merchantGroupStatus"),
    },
    {
      title: "",
      render: (record: any) => (
        <Dropdown
          menu={{
            items: [
              ...((record.systemGenerated||record?.merchantGroupStatus === "closed"||record?.aggregator?.aggregatorStatus!=="active"||record?.institution?.insitutionStatus!=="active")?[]:(record?.merchantGroupStatus === "active" ?actionItemsActive:
              record?.merchantGroupStatus === "pending"||record?.merchantGroupStatus === "de-active"||record?.merchantGroupStatus === "draft" 
              ? (actionItemsPending)
              : actionItemsUnblock))            ],
          }}
          trigger={["click"]}
          arrow
          /*           disabled={record.isParentBlocked}
           */
        >
          <Typography.Link onClick={() => setMerchantGroupDetails(record)}>
            <img className="action-tab" src={"/images/moreOptions.svg"} />
          </Typography.Link>
        </Dropdown>
      ),
    },
  ];
  const handleDaysChange = (value: string) => {
    setDays(Number.parseInt(value))
      };
    
  const handleDropdownItemClick = (e: any) => {
    //getAggregatorReport(aggregator)
    if (e.key == 1) {
      downloadMerchantGroupCSV();
    } else if (e.key == 2) {
      downloadMerchantGroupPDF();
    }
  };
  const downloadMerchantGroupCSV = () => {
    JSONToCSVConvertor(dataNoPage, "MerchantGroup", true);
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
      "merchantGroupName",
      "merchantGroupPreferenceId",
      "merchantGroupStatus",
    ];
    if (ShowLabel) {
      let row = "";
      for (const index in arrData?.merchantGroupList[0]?.merchantGroup) {
        switch (index) {
          case "merchantGroupName":
            row += "MerchantGroup Name" + ",";
            break;
          case "merchantGroupPreferenceId":
            row += "MerchantGroup ID" + ",";

            break;
          case "merchantGroupStatus":
            row += "Status" + ",";
            break;
          default:
            break;
        }
      }
      row = row.slice(0, -1);
      CSV += row + "\r\n";
    }
    for (let i = 0; i < arrData?.merchantGroupList.length; i++) {
      let row = "";
      for (const index in arrData.merchantGroupList[i]?.merchantGroup) {
        if (headersRequried.includes(index)) {
          row +=
            '"' + arrData?.merchantGroupList[i]?.merchantGroup[index] + '",';
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
  const downloadMerchantGroupPDF = () => {
    if (dataNoPage) {
      const currentData =
        typeof dataNoPage !== "object" ? JSON.parse(dataNoPage) : dataNoPage;
      const head = [["MerchantGroup Name", "MerchantGroup ID", "Status"]];
      const finalData: any = [];
      currentData?.merchantGroupList?.map((item: any, index: any) => {
        const arr = [];
        if (
          Object.prototype.hasOwnProperty.call(
            item?.merchantGroup,
            "merchantGroupName"
          )
        ) {
          arr.push(item?.merchantGroup.merchantGroupName);
        }
        if (
          Object.prototype.hasOwnProperty.call(
            item?.merchantGroup,
            "merchantGroupPreferenceId"
          )
        ) {
          arr.push(item?.merchantGroup.merchantGroupPreferenceId);
        }
        if (
          Object.prototype.hasOwnProperty.call(
            item?.merchantGroup,
            "merchantGroupStatus"
          )
        ) {
          arr.push(item?.merchantGroup.merchantGroupStatus);
        }
        finalData.push(arr);
      });
      const doc = new jsPDF();
      autoTable(doc, {
        head: head,
        body: finalData,
        columnStyles: {
          0: { cellWidth: 35 },
          1: { cellWidth: 58 },
          5: { cellWidth: 20 },
        },
      });

      doc.save("myReports-merchantGroup.pdf");
    }
  };
  // Action items
  const actionItems: any = [
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
  const daysOptions=[{key:30,value:"30 days"},
  {key:90,value:"90 days"},
  {key:365,value:"1 year"},
  {key:0,value:'Till Date'}]
  const actionItemsBlock: any = [
    {
      key: "1",
      label: <a onClick={() => updateMarchantGroupRow()}>Update</a>,
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
      setSearchBy(e.target.value);
    }
  };

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
    // setMerchantGroupDetails({});
  };

  const blockMerchant = () => {
    let tempmerchantGroupDetails=Object.assign({},merchantGroupDetails)
    tempmerchantGroupDetails["merchantGroupStatus"] =
    tempmerchantGroupDetails.merchantGroupStatus == "pending" ||
    tempmerchantGroupDetails.merchantGroupStatus == "active"
        ? "block"
        : "active";
   // updateMerchantGroupDetails(tempmerchantGroupDetails);
   // setMerchantGroupDetails({});
    /* merchantGroupDetails["merchantGroupStatus"] =
      merchantGroupDetails.merchantGroupStatus == "pending" ||
      merchantGroupDetails.merchantGroupStatus == "active"
        ? "block"
        : "active";
   */  let request = { merchantGroup: tempmerchantGroupDetails };
    updateMerchantGroupDetails(request);
    setMerchantGroupDetails({});
  };

  const deleteMerchantGroup = () => {
    deleteMerchantGroupById({
      merchantGroupId: merchantGroupDetails.merchantGroupPreferenceId,
    });

    setMerchantGroupDetails({});
  };
  const handlePaginationChange = (pagination:any) => {
    console.log(pagination)
    setCurrentPage(pagination);
    setPageSize(pageSize);
  };
  return (
    <div>
      {data !== undefined &&
      merchants?.merchantGroupList?.length === 0 &&
      search == "" ? (
        <PageHeading
          title="Acquirer Merchant Group Management"
          parentRefetch={setReloadData}
          uploadType={"MERCHANTGROUP_UPLOAD"}
          showRegister={merchants?.merchantGroupList?.length == 0&&aggData?.aggregatorStatus==='active'&&instData?.insitutionStatus==='active'}
        />
      ) : (
        <PageHeading
          topTitle="Merchant group management"
          title="Acquirer Merchant Group Management"
          parentRefetch={setReloadData}
          showRegister={(merchants?.merchantGroupList?.length > 0 &&(merchants?.merchantGroupList[0].aggregator?.aggregatorStatus==='active'&&merchants?.merchantGroupList[0]?.institution?.insitutionStatus==='active'))}
          uploadType={"MERCHANTGROUP_UPLOAD"}
          linkData={{
            label: "Register Merchant Group",
            url:
              RouteType.MERCHANT_GROUP_REGISTER +
              "/" +
              location.state?.aggregatorId +
              "/" +
              location.state?.aggregatorName +
              "/" +
              location.state?.institutionId +
              "/" +
              location.state?.institutionName,
          }}
        />
      )}
      {data !== undefined && days==0&&(merchants?.merchantGroupList?.length === 0&&(aggData?.aggregatorStatus==="active"&&instData?.insitutionStatus==='active'))&&
      searchBy == "" ? (
        <>
          <hr
            style={{ height: "1px", background: "#ECECEC", border: "none" }}
          />
          <div className="text-center vertical-center mt-5">
            <h5
              style={{
                fontWeight: 600,
                fontSize: "19px",
                font: "Rubik",
                lineHeight: "21.33px",
              }}
            >
              No Merchant Group are available yet to manage
            </h5>
            <p style={{ fontSize: "14px" }}>
              Want to create Merchant Group? Please click on the below button
            </p>
            <Link
              to={
                RouteType.MERCHANT_GROUP_REGISTER +
                "/" +
                location.state?.aggregatorId +
                "/" +
                location.state?.aggregatorName +
                "/" +
                location.state?.institutionId +
                "/" +
                location.state?.institutionName
              }
              title="Register New Merchant Group"
            >
              <Button
                style={{
                  background: "#faad14",
                  color: "#ffffff",
                }}
              >
                Register New Merchant Group
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
                                value: "merchantGroupName",
                                label: "MerchantGroup Name",
                              },
                              {
                                value: "merchantGroupId",
                                label: "MerchantGroup ID",
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
                <div /* className="table-responsive" */>
                  <Table
                    columns={columns}
                    scroll={{ x: "max-content" }}
                    dataSource={merchants?.merchantGroupList?.map((it: any) => {
                      let res = Object.assign(
                        it,
                        it.aggregator,
                        it.institution,
                        it.merchantGroup
                      );
                      return res;
                    })}
                    pagination={{
                      current: merchants?.pagination?.current_page,
                      pageSize: pageSize,
                      total: merchants?.pagination?.total_records,
                      onChange: handlePaginationChange,
                    }}
                    size="middle"
                    className="table-custom"
                    loading={loading}
                    locale={{ emptyText: "No mercahnt group found" }}
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
