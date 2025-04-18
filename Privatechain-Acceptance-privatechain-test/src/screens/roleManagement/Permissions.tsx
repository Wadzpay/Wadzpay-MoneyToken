import React, {
  Props,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useState,
} from "react";
import "./index.css";
import { Space, Switch, Table } from "antd";
import type { TableColumnsType, TableProps } from "antd";
import { useGetModuleListTree } from "src/api/user";

type TableRowSelection<T> = TableProps<T>["rowSelection"];

interface DataType {
  key: React.ReactNode;
  name: string;
  children?: DataType[];
}
interface DataType2 {
  key: React.ReactNode;
  name: string;
  moduleId: number;
  moduleName: string;
  moduleType: string;
  status: boolean;
  moduleUrl: string;
  imageUrl: string;
  createdBy: number;
  createdAt: string;
  updatedBy: number;
  updatedAt: string;
  children?: DataType2[];
}

const columns: TableColumnsType<DataType> = [
  Table.EXPAND_COLUMN,
  {
    title: "",
    dataIndex: "",
    key: "",
    /*     width:0,
    colSpan:0,
 */ className: "row-column-hide",
  },
  {
    title: "All Attributes",
    dataIndex: "name",
    key: "name",
    width: 200,
  },
  Table.SELECTION_COLUMN,
];

// rowSelection objects indicates the need for row selection

const Permssions: React.FC<any> = forwardRef((props: any, ref) => {
  const [checkStrictly, setCheckStrictly] = useState(false);
  const [order, setOrder] = useState(2);
  const [moduleList, setModuleList] = useState<any>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [currentPage, setCurrentPage] = useState<number>();
  const [pageSize, setPageSize] = useState(10);
  const [selectedRowKeys, setSelectedRowKeys] = useState<any[]>(
    props.editMode ? props.defaultData : []
  );
  const {
    data,
    mutate: getModuleListTree,
    isSuccess: isModuleDataSuccess,
  } = useGetModuleListTree();
  const fetchModuleList = () => {
    const requestParams: any = {
      page: currentPage || 1,

      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: pageSize,
    };
    getModuleListTree(requestParams);
  };
  useEffect(() => {
    if (data) {
      setModuleList(data);
      setLoading(false);
    }
  }, [data]);
  useEffect(() => {
    if (loading) {
      fetchModuleList();
      if (loading) setLoading(false);
    }
  }, [loading]);

  useImperativeHandle(ref, () => ({
    reset(permissions: any) {
      if (permissions) {
        copy(permissions);
      } else {
        reset();
      }
    },
  }));

  useEffect(
    () => {
      // Initialize selectedRowKeys with saved selection
      // if(reset){
      //setSelectedRowKeys(defaultData);
      //reset.current=reset
      //}
    },
    [
      //reset?.current
    ]
  );

  function reset() {
    setSelectedRowKeys(props.editMode?props.defaultData:[]);
  }
  function copy(permissions: any) {
    setSelectedRowKeys(permissions);
  }
  const rowSelection: TableRowSelection<DataType> = {
    onChange: (selectedRowKeys, selectedRows) => {
      props.currentRows(selectedRowKeys, true);
      setSelectedRowKeys(selectedRowKeys);
    },
    onSelect: (record, selected, selectedRows) => {},
    onSelectAll: (selected, selectedRows, changeRows) => {},
    getCheckboxProps: (record) => {
      if (props.readOnly) {
        return {
          disabled: props.readOnly 
        };
      }else{
        return {
          disabled: false 
        };
      }
    }

  };
  return (
    <>
      <Table
        columns={columns}
        className="tableRole"
        rowClassName={props.readOnly&&'disable-permissions'}      
        /*         expandable={{ defaultExpandAllRows:props.editMode,expandRowByClick:true,showExpandColumn:true}}
         */
        /*  expandable={{ expandedRowKeys:moduleList.map((it:any)=>it.key)}}
         */ rowSelection={{ selectedRowKeys, ...rowSelection, checkStrictly ,}}
        pagination={false}
        dataSource={moduleList.filter((module:any)=>module.status===true)}
        rowKey="key"
      
      />
    </>
  );
});

export default Permssions;
