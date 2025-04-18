import { useGetAggregatorTree } from "src/api/user";
import { Popover, Tree } from "antd";
import React, { useEffect, useImperativeHandle, useState } from "react";
import {
  AggregatorTree,
  GetInstitutionList,
  GetMerchantGroupList,
  OutletOnly,
  Pos,
} from "src/api/models";
import "./AggregatorTree.scss";
import { Link } from "react-router-dom";

import { RouteType } from "src/constants/routeTypes";
import "./AggregatorTree.scss";

interface DataNode {
  title: string;
  key: string;
  isLeaf?: boolean;
  children?: DataNode[];
}

const initTreeData: DataNode[] = [
  { title: "Expand to load", key: "0" },
  { title: "Expand to load", key: "1" },
  { title: "Tree Node", key: "2", isLeaf: true },
];

type Props = {
  childRef?: any;
};

const AggregatorHierarchy: React.FC<Props> = ({ childRef }: Props) => {
  const [treeData, setTreeData] = useState(initTreeData);
  const { data, isFetching, error, refetch } = useGetAggregatorTree();
  const [aggregatorList, setAggregatorList] = useState<any>([]);

  useImperativeHandle(childRef, () => ({
    getTreeList() {
      refetch();
    },
  }));
  const InstitutionOptionsNested = (props: {
    aggregatorId: string;
    aggregatorName: string;
  }) => {
    let instId="INSTITUTION" + Date.now()
   /*  let mgId="MG" + Date.now()
/*     let merchantId= "MERCHANT" + Date.now()
     let subMechantId= "SUBMERCHANT" + Date.now() */
    return <div>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.INSTITUTION_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName
        }
        state={{"direct":true,"parentType":"aggregator"}}
          title="Register New Institute"
        >
          Institution
        </Link>
      </div>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.MERCHANT_GROUP_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props .aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName

        }
        title="Register New Merchant Group"
        state={{"direct":true,"parentType":"aggregator"}}
          >
          Merchant Group
        </Link>
      </div>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.MERCHANT_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName
        }
          title="Register New Merchant"
          state={{"direct":true,"parentType":"aggregator"}}

        >
          Merchant
        </Link>
      </div ><div className="aggregatorOptions">
        <Link to={
          RouteType.SUB_MERCHANT_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName
        }
          title="Register New Sub Merchant"
          state={{"direct":true,"parentType":"aggregator"}}
        >
          Sub Merchant
        </Link>
      </div>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.OUTLET_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName
        }
       state={{"direct":true,"parentType":"aggregator"}}
          title="Register New Outlet"
        >
          Outlet
        </Link>
      </div>
    </div>
  }

  const InstitutionOptions = (props: {
    aggregatorId: string;
    aggregatorName: string;
    isActive:boolean;
  }) => (
   <div className="aggregatoe-options-container">
      <Popover placement="right" content={<InstitutionOptionsNested aggregatorId={props.aggregatorId} aggregatorName={props.aggregatorName} />} trigger="hover">
       {props.isActive&& <div className="aggregatorOptions">
                   Create New
        </div>}
      </Popover>
      <div className="aggregatorOptions">
        <Link
          to={
            RouteType.INSTITUTION_LIST +
            "/" +
            props.aggregatorId +
            "/" +
            props.aggregatorName
          }
          state={{
            aggregatorId: props.aggregatorId,
            aggregatorName: props.aggregatorName,
          }}
          title="Institute Management"
          
        >
          View List
        </Link>
      </div>
    </div>
  );
  const MerchantGroupOptionsNested = (props: {
    aggregatorId: string;
    aggregatorName: string;
    institutionId: string;
    institutionName: string;
  }) => {
   /*  let mgId="MG" + Date.now()
    let merchantId= "MERCHANT" + Date.now()
    let subMechantId= "SUBMERCHANT" + Date.now() */
    return <>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.MERCHANT_GROUP_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName

        }
        state={{"direct":true,"parentType":"institution"}}
          title="Register New Merchant group"
        >
          Merchant Group
        </Link>
      </div>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.MERCHANT_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName

        }
        state={{"direct":true,"parentType":"institution"}}
          title="Register New Merchnat"
        >
          Merchant
        </Link>
      </div ><div className="aggregatorOptions">
        <Link to={
          RouteType.SUB_MERCHANT_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName
        }
        state={{"direct":true,"parentType":"institution"}}

          title="Register New Sub Merchant"
        >
          Sub Merchant
        </Link>
      </div>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.OUTLET_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.institutionId+
          "/" +
          props.institutionName
        }
        state={{"direct":true,"parentType":"institution"}}

          title="Register New Outlet"
        >
          Outlet
        </Link>
      </div></>
  }
  const MerchantGroupOptions = (props: {
    aggregatorId: string;
    aggregatorName: string;
    aggregatorLogo:string
    institutionId: string;
    institutionName: string;
    institutionLogo:string;
    isSystemGenerated:boolean;
    isActive:boolean;
  }) => (
    <div className="aggregatoe-options-container">
      {(!props.isSystemGenerated&&props.isActive)&&<Popover placement="right" content={<MerchantGroupOptionsNested aggregatorId={props.aggregatorId} aggregatorName={props.aggregatorName} institutionId={props.institutionId} institutionName={props.institutionName} />} trigger="hover">
        <div className="aggregatorOptions">
          {/*  <Link
          to={
            RouteType.MERCHANT_GROUP_REGISTER +
            "/" +
            props?.aggregatorId +
            "/" +
            props?.aggregatorName +
            "/" +
            props?.institutionId +
            "/" +
            props?.institutionName
          }
          title="Register New Mechant Group"
        > */}
          Create New
          {/*         </Link>
 */}      </div>
      </Popover>}
      <div className="aggregatorOptions">
        <Link
          to={
            RouteType.MERCHANT_GROUP_LIST +
            "/" +
            props?.aggregatorId +
            "/" +
            props?.aggregatorName +
            "/" +
            props?.institutionId +
            "/" +
            props?.institutionName
          }
          state={{
            aggregatorId: props.aggregatorId,
            aggregatorName: props.aggregatorName,
            aggregatorLogo:props.aggregatorLogo,
            institutionId: props.institutionId,
            institutionName: props.institutionName,

          }}
          title="Mechant Group Management"
        >
          View List
        </Link>
      </div>
    </div>
  );
  const MerchantOptionsNested = (props: {
    aggregatorId: string;
    aggregatorName: string;
    institutionId: string;
    institutionName: string;
    merchantGroupPreferenceId: string;
    merchantGroupName: string;

  }) => {
    /* let merchantId= "MERCHANT" + Date.now()
    let subMechantId= "SUBMERCHANT" + Date.now() */
    return <>             
    
      <div className="aggregatorOptions">
        <Link to={
          RouteType.MERCHANT_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.merchantGroupPreferenceId +
          "/" +
          props.merchantGroupName

        }
          title="Register New Merchant"
          state={{"direct":true,"parentType":"merchantGroup"}}
        >
          Merchant
        </Link>
      </div ><div className="aggregatorOptions">
        <Link to={
          RouteType.SUB_MERCHANT_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.merchantGroupPreferenceId +
          "/" +
          props.merchantGroupName +
          "/" +
          props.merchantGroupPreferenceId +
          "/" +
          props.merchantGroupName
        }
        state={{"direct":true,"parentType":"merchantGroup"}}

          title="Register Sub Merchant"
        >
          Sub Merchant
        </Link>
      </div>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.OUTLET_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.merchantGroupPreferenceId +
          "/" +
          props.merchantGroupName +
          "/" +
          props.merchantGroupPreferenceId +
          "/" +
          props.merchantGroupName +
          "/" +
          props.merchantGroupPreferenceId +
          "/" +
          props.merchantGroupName
        }
        state={{"direct":true,"parentType":"merchantGroup"}}

          title="Register New Outlet"
        >
          Outlet
        </Link></div>
    </>
  }
  const MerchantOptions = (props: {
    aggregatorId: string;
    aggregatorName: string;
    institutionId: string;
    institutionName: string;
    merchantGroupPreferenceId: string;
    merchantGroupName: string;
    isSystemGenerated:boolean;
    isActive:boolean
  }) => (
    <div className="aggregatoe-options-container">
     {(!props.isSystemGenerated&&props.isActive)&& <Popover placement="right" content={<MerchantOptionsNested aggregatorId={props.aggregatorId} aggregatorName={props.aggregatorName} institutionId={props.institutionId} institutionName={props.institutionName} merchantGroupPreferenceId={props.merchantGroupPreferenceId} merchantGroupName={props.merchantGroupName} />} trigger="hover">

        <div className="aggregatorOptions">

          {/* <Link
          to={
            RouteType.MERCHANT_REGISTER +
            "/" +
            props?.aggregatorId +
            "/" +
            props?.aggregatorName +
            "/" +
            props?.institutionId +
            "/" +
            props?.institutionName +
            "/" +
            props?.merchantGroupPreferenceId +
            "/" +
            props?.merchantGroupName
          }
          title="Register New Merchant"
        > */}
          Create New
          {/*         </Link>
 */}
        </div>
      </Popover>}
      <div className="aggregatorOptions">
        <Link
          to={
            RouteType.MERCHANT_LIST +
            "/" +
            props?.aggregatorId +
            "/" +
            props?.aggregatorName +
            "/" +
            props?.institutionId +
            "/" +
            props?.institutionName +
            "/" +
            props?.merchantGroupPreferenceId +
            "/" +
            props?.merchantGroupName
          }
          state={{
            aggregatorId: props?.aggregatorId,
            aggregatorName: props?.aggregatorName,
            institutionId: props?.institutionId,
            institutionName: props?.institutionName,
            merchantGroupPreferenceId: props?.merchantGroupPreferenceId,
            merchantGroupName: props?.merchantGroupName,
          }}
          title="Merchant Managemant"
        >
          View List
        </Link>
      </div>
    </div>
  );
const SubMerchantOptionsNested=(props: {
  aggregatorId: string;
  aggregatorName: string;
  institutionId: string;
  institutionName: string;
  merchantGroupPreferenceId: string;
  merchantGroupName: string;
  merchantAcquirerName:string;
  merchantAcquirerId:string;
})=>{
  //let subMechantId= "SUBMERCHANT" + Date.now()
  return <>
  <div className="aggregatorOptions">
        <Link to={
          RouteType.SUB_MERCHANT_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.merchantGroupPreferenceId +
          "/" +
          props.merchantGroupName +
          "/" +
          props.merchantAcquirerId +
          "/" +
          props.merchantAcquirerName
        }
          title="Register New Sub Merchant"
          state={{"direct":true,"parentType":"merchant"}}

        >
          Sub Merchant
        </Link>
      </div>
      <div className="aggregatorOptions">
        <Link to={
          RouteType.OUTLET_REGISTER +
          "/" +
          props.aggregatorId +
          "/" +
          props.aggregatorName +
          "/" +
          props.institutionId +
          "/" +
          props.institutionName +
          "/" +
          props.merchantGroupPreferenceId +
          "/" +
          props.merchantGroupName +
          "/" +
          props.merchantAcquirerId +
          "/" +
          props.merchantAcquirerName +
          "/" +
          props.merchantAcquirerId +
          "/" +
          props.merchantAcquirerName
        }
          title="Register New Outlet"
          state={{"direct":true,"parentType":"merchant"}}
        >
          Outlet
        </Link></div>
  </>
}
  const SubMerchantOptions = (props: {
    aggregatorId: string;
    aggregatorName: string;
    institutionId: string;
    institutionName: string;
    merchantGroupPreferenceId: string;
    merchantGroupName: string;
    merchantAcquirerName: string;
    merchantAcquirerId: string;
    isSystemGenerated:boolean;
    isActive:boolean;
  }) => (
    <div className="aggregatoe-options-container">
       { (!props?.isSystemGenerated&&props.isActive)&&    <Popover placement="right" content={<SubMerchantOptionsNested aggregatorId={props.aggregatorId} aggregatorName={props.aggregatorName} institutionId={props.institutionId} institutionName={props.institutionName} merchantGroupPreferenceId={props.merchantGroupPreferenceId} merchantGroupName={props.merchantGroupName} merchantAcquirerName={props.merchantAcquirerName} merchantAcquirerId={props.merchantAcquirerId} />} trigger="hover">
      <div className="aggregatorOptions">
        <Link
          to={
            RouteType.SUB_MERCHANT_REGISTER +
            "/" +
            props?.aggregatorId +
            "/" +
            props?.aggregatorName +
            "/" +
            props?.institutionId +
            "/" +
            props?.institutionName +
            "/" +
            props?.merchantGroupPreferenceId +
            "/" +
            props?.merchantGroupName +
            "/" +
            props?.merchantAcquirerId +
            "/" +
            props?.merchantAcquirerName
          }
          title="Register New Sub Merchant"
        >
          Create New
        </Link>
      </div>
      </Popover>}
      <div className="aggregatorOptions">
        <Link
          to={
            RouteType.SUB_MERCHANT_LIST +
            "/" +
            props?.aggregatorId +
            "/" +
            props?.aggregatorName +
            "/" +
            props?.institutionId +
            "/" +
            props?.institutionName +
            "/" +
            props?.merchantGroupPreferenceId +
            "/" +
            props?.merchantGroupName +
            "/" +
            props?.merchantAcquirerId +
            "/" +
            props?.merchantAcquirerName
          }
          state={{
            aggregatorId: props?.aggregatorId,
            aggregatorName: props?.aggregatorName,
            institutionId: props?.institutionId,
            institutionName: props?.institutionName,
            merchantGroupPreferenceId: props?.merchantGroupPreferenceId,
            merchantGroupName: props?.merchantGroupName,
            merchantAcquirerId: props?.merchantAcquirerId,
            merchantAcquirerName: props?.merchantAcquirerName,
          }}
          title="Sub Merchant Managemant"
        >
          View List
        </Link>
      </div>
    </div>
  );
  const OutletOptions = (props: {
    aggregatorId: string;
    aggregatorName: string;
    institutionId: string;
    institutionName: string;
    merchantGroupPreferenceId: string;
    merchantGroupName: string;
    merchantAcquirerName: string;
    merchantAcquirerId: string;
    subMerchantId: String;
    subMerchantName: String;
    isSystemGenerated:boolean;
    isActive:boolean;
  }) => {
    return (
      <div className="aggregatoe-options-container">
       {(!props.isSystemGenerated&&props.isActive) && <div className="aggregatorOptions">
          <Link
            to={
              RouteType.OUTLET_REGISTER +
              "/" +
              props?.aggregatorId +
              "/" +
              props?.aggregatorName +
              "/" +
              props?.institutionId +
              "/" +
              props?.institutionName +
              "/" +
              props?.merchantGroupPreferenceId +
              "/" +
              props?.merchantGroupName +
              "/" +
              props?.merchantAcquirerId +
              "/" +
              props?.merchantAcquirerName +
              "/" +
              props?.subMerchantId +
              "/" +
              props?.subMerchantName
            }
            title="Register New Outlet"
          >
            Create New
          </Link>
        </div>}
        <div className="aggregatorOptions">
          <Link
            to={
              RouteType.OUTLET_LIST +
              "/" +
              props?.aggregatorId +
              "/" +
              props?.aggregatorName +
              "/" +
              props?.institutionId +
              "/" +
              props?.institutionName +
              "/" +
              props?.merchantGroupPreferenceId +
              "/" +
              props?.merchantGroupName +
              "/" +
              props?.merchantAcquirerId +
              "/" +
              props?.merchantAcquirerName +
              "/" +
              props?.subMerchantId +
              "/" +
              props?.subMerchantName
            }
            state={{
              aggregatorId: props?.aggregatorId,
              aggregatorName: props?.aggregatorName,
              institutionId: props?.institutionId,
              institutionName: props?.institutionName,
              merchantGroupPreferenceId: props?.merchantGroupPreferenceId,
              merchantGroupName: props?.merchantGroupName,
              merchantAcquirerId: props?.merchantAcquirerId,
              merchantAcquirerName: props?.merchantAcquirerName,
              subMerchantId: props?.subMerchantId,
              subMerchantName: props?.subMerchantName,
            }}
            title="Outlet Managemant"
          >
            View List
          </Link>
        </div>
      </div>
    );
  };
  const PosOptions = (props: {
    aggregatorId: string;
    aggregatorName: string;
    institutionId: string;
    institutionName: string;
    merchantGroupPreferenceId: string;
    merchantGroupName: string;
    merchantAcquirerName: string;
    merchantAcquirerId: string;
    subMerchantId: String;
    subMerchantName: String;
    outletId: String;
    outletName: String;
    isActive:boolean;
  }) => {
    return (
      <div className="aggregatoe-options-container">
        {props.isActive&& <div className="aggregatorOptions">
         <Link
            to={
              RouteType.POS_REGISTER +
              "/" +
              props?.aggregatorId +
              "/" +
              props?.aggregatorName +
              "/" +
              props?.institutionId +
              "/" +
              props?.institutionName +
              "/" +
              props?.merchantGroupPreferenceId +
              "/" +
              props?.merchantGroupName +
              "/" +
              props?.merchantAcquirerId +
              "/" +
              props?.merchantAcquirerName +
              "/" +
              props?.subMerchantId +
              "/" +
              props?.subMerchantName+
              "/" +
              props?.outletId +
              "/" +
              props?.outletName
            }
            title="Register New Pos"
          >
            Create New
          </Link>
        </div>}
        <div className="aggregatorOptions">
          <Link
            to={
              RouteType.POS_LIST +
              "/" +
              props?.aggregatorId +
              "/" +
              props?.aggregatorName +
              "/" +
              props?.institutionId +
              "/" +
              props?.institutionName +
              "/" +
              props?.merchantGroupPreferenceId +
              "/" +
              props?.merchantGroupName +
              "/" +
              props?.merchantAcquirerId +
              "/" +
              props?.merchantAcquirerName +
              "/" +
              props?.subMerchantId +
              "/" +
              props?.subMerchantName+
              "/" +
              props?.outletId +
              "/" +
              props?.outletName
            }
            state={{
              aggregatorId: props?.aggregatorId,
              aggregatorName: props?.aggregatorName,
              institutionId: props?.institutionId,
              institutionName: props?.institutionName,
              merchantGroupPreferenceId: props?.merchantGroupPreferenceId,
              merchantGroupName: props?.merchantGroupName,
              merchantAcquirerId: props?.merchantAcquirerId,
              merchantAcquirerName: props?.merchantAcquirerName,
              subMerchantId: props?.subMerchantId,
              subMerchantName: props?.subMerchantName,
              outletId: props?.outletId,
              outletName: props?.outletName,

            }}
            title="Pos Managemant"
          >
            View List
          </Link>
        </div>
      </div>
    );
  };
  useEffect(() => {
    if (data) {
      setAggregatorList(data);
    }
  }, [data]);

  const aggregatorListTree = aggregatorList.map((element: AggregatorTree,index:number) => {
    return {
      title: (
        <div className="aggregator-node-container" key={index+element.aggregatorPreferenceId+(1000 + Math.random() * 9000).toFixed(0)}>
          <span className="aggregator-node-name">{element.aggregatorName}</span>
          {<Popover
            placement="bottomRight"
            content={
              <>
               <InstitutionOptions
                  aggregatorId={element.aggregatorPreferenceId}
                  aggregatorName={element.aggregatorName}
                  isActive={element.aggregatorStatus==='active'}
                />
              </>
            }
            trigger="hover"
          >
            <img
              className="aggregator-options-icon"
              // src="/images/white-logo.png"
              src="/images/moreOptions.svg"
              alt="More Options"
              title="More Options"
            />
          </Popover>}    
        </div>
      ),
      key: index+element.aggregatorPreferenceId+(1000 + Math.random() * 9000).toFixed(0),
      children: element.institutions?.map(
        (institution: GetInstitutionList,index:number) => {
          const isActive=(element.aggregatorStatus==='active'&&institution.institutionStatus==='active')
          return {
          title: (
            <div className="aggregator-node-container" key={index+institution.institutionId+(1000 + Math.random() * 9000).toFixed(0)}>
              <span className="aggregator-node-name">
                {institution.institutionName}
              </span>
           {   <Popover
                placement="rightBottom"
                content={
                  <MerchantGroupOptions
                    aggregatorId={element.aggregatorPreferenceId}
                    aggregatorName={element.aggregatorName}
                    aggregatorLogo={element.aggregatorLogo}
                    institutionId={institution.institutionId}
                    institutionName={institution.institutionName}
                    institutionLogo={institution.institutionLogo}
                    isSystemGenerated={institution.systemGenerated}
                    isActive={isActive}
                  />
                }
              >
                <img
                  className="aggregator-options-icon"
                  // src="/images/white-logo.png"
                  src="/images/moreOptions.svg"
                  alt="More options"
                  title="More Options"
                />
              </Popover>}
            </div>
          ),
          key: index+institution.institutionId+(1000 + Math.random() * 9000).toFixed(0),
          children: institution.merchantGroup?.map(
            (merchantGroup: GetMerchantGroupList,index) => {
             const isActive= element.aggregatorStatus==='active'&&institution.institutionStatus==='active'&& merchantGroup.merchantGroupStatus=="active"
              return {
              title: (
                <div className="aggregator-node-container" key={index+(1000 + Math.random() * 9000).toFixed(0)}>
                  <span className="aggregator-node-name">
                    {merchantGroup.merchantGroupName}
                  </span>
                 {<Popover
                    placement="rightBottom"
                    content={
                      <MerchantOptions
                        aggregatorId={element.aggregatorPreferenceId}
                        aggregatorName={element.aggregatorName}
                        institutionId={institution.institutionId}
                        institutionName={institution.institutionName}
                        merchantGroupPreferenceId={
                          merchantGroup.merchantGroupPreferenceId
                        }
                        merchantGroupName={merchantGroup.merchantGroupName}
                        isSystemGenerated={merchantGroup.systemGenerated}
                        isActive={isActive}
                      />
                    }
                  >
                    <img
                      className="aggregator-options-icon"
                      // src="/images/white-logo.png"
                      src="/images/moreOptions.svg"
                      alt="More Options"
                      title="More Options"
                    />
                  </Popover>}
                </div>
              ),
              key: index+institution.institutionId+(1000 + Math.random() * 9000).toFixed(0),
              children: merchantGroup.merchants?.map((merchant: any,index:number) =>{ 
                let isActive=(element.aggregatorStatus==='active'&&institution.institutionStatus==='active'&& merchantGroup.merchantGroupStatus=="active"&&merchant.merchantAcquirerStatus==='active')
                return{
                title: (
                  <div className="aggregator-node-container" key={index+merchant.merchantAcquirerId}>
                    <span className="aggregator-node-name">
                      {merchant.merchantAcquirerName}
                    </span>
                    {<Popover
                      placement="rightBottom"
                      content={
                        <SubMerchantOptions
                          aggregatorId={element.aggregatorPreferenceId}
                          aggregatorName={element.aggregatorName}
                          institutionId={institution.institutionId}
                          institutionName={institution.institutionName}
                          merchantGroupPreferenceId={
                            merchantGroup.merchantGroupPreferenceId
                          }
                          merchantGroupName={merchantGroup.merchantGroupName}
                          merchantAcquirerId={merchant.merchantAcquirerId}
                          merchantAcquirerName={merchant.merchantAcquirerName}
                          isSystemGenerated={merchant.systemGenerated}
                          isActive={isActive}
                          />
                      }
                    >
                      <img
                        className="aggregator-options-icon"
                        // src="/images/white-logo.png"
                        src="/images/moreOptions.svg"
                        alt="More Options"
                        title="More Options"
                      />
                    </Popover>}
                  </div>
                ),
                key: index+merchant.merchantAcquirerId+(1000 + Math.random() * 9000).toFixed(0),
                children: merchant.subMerchants?.map((subMerchant: any,index:number) => {
                  let isActive=(element.aggregatorStatus==='active'&&institution.institutionStatus==='active'&& merchantGroup.merchantGroupStatus=="active"&&merchant.merchantAcquirerStatus==='active'&&subMerchant.subMerchantAcquirerStatus==='active')
                  return {
                    title: (
                      <div className="aggregator-node-container" key={index+subMerchant.subMerchantAcquirerId}>
                        <span className="aggregator-node-name">
                          {subMerchant.subMerchantAcquirerName}
                        </span>
                        {<Popover
                          placement="rightBottom"
                          content={
                            <OutletOptions
                              aggregatorId={element.aggregatorPreferenceId}
                              aggregatorName={element.aggregatorName}
                              institutionId={institution.institutionId}
                              institutionName={institution.institutionName}
                              merchantGroupPreferenceId={
                                merchantGroup.merchantGroupPreferenceId
                              }
                              merchantGroupName={
                                merchantGroup.merchantGroupName
                              }
                              merchantAcquirerId={merchant.merchantAcquirerId}
                              merchantAcquirerName={
                                merchant.merchantAcquirerName
                              }
                              subMerchantId={subMerchant.subMerchantAcquirerId}
                              subMerchantName={
                                subMerchant.subMerchantAcquirerName
                              }
                              isSystemGenerated={subMerchant.systemGenerated}
                              isActive={isActive}
                            />
                          }
                        >
                          <img
                            className="aggregator-options-icon"
                            // src="/images/white-logo.png"
                            src="/images/moreOptions.svg"
                            alt="More Options"
                            title="More Options"
                          />
                        </Popover>}
                      </div>
                    ),
                    key: index+subMerchant.subMerchantAcquirerId+(1000 + Math.random() * 9000).toFixed(0),
                    children: subMerchant.outlets?.map((outlet: OutletOnly,index:number) => {
                      let isActive= (element.aggregatorStatus==='active'&&institution.institutionStatus==='active'&& merchantGroup.merchantGroupStatus=="active"&&merchant.merchantAcquirerStatus==='active'&&subMerchant.subMerchantAcquirerStatus==='active'&&outlet.outletStatus==='active')
                      return {
                        title: (
                          <div className="aggregator-node-container" key={index+outlet.outletId}>
                            <span className="aggregator-node-name">
                              {outlet.outletName}
                            </span>
                            {<Popover
                              placement="rightBottom"
                              content={
                                <PosOptions
                                  aggregatorId={element.aggregatorPreferenceId}
                                  aggregatorName={element.aggregatorName}
                                  institutionId={institution.institutionId}
                                  institutionName={institution.institutionName}
                                  merchantGroupPreferenceId={
                                    merchantGroup.merchantGroupPreferenceId
                                  }
                                  merchantGroupName={
                                    merchantGroup.merchantGroupName
                                  }
                                  merchantAcquirerId={
                                    merchant.merchantAcquirerId
                                  }
                                  merchantAcquirerName={
                                    merchant.merchantAcquirerName
                                  }
                                  subMerchantId={
                                    subMerchant.subMerchantAcquirerId
                                  }
                                  subMerchantName={
                                    subMerchant.subMerchantAcquirerName
                                  }
                                  outletId={
                                    outlet.outletId
                                  }
                                  outletName={
                                    outlet.outletName
                                  }
                                  isActive={isActive}
                                />
                              }
                            >
                              <img
                                className="aggregator-options-icon"
                                // src="/images/white-logo.png"
                                src="/images/moreOptions.svg"
                                alt="More Options"
                                title="More Options"
                              />
                            </Popover>}
                          </div>
                        ),
                        key: outlet.outletId,
                       children: outlet.posList?.map((pos: Pos,index:number) => {
                          return {
                            title: (
                              <div className="aggregator-node-container" key={index+outlet.outletId}>
                                <span className="aggregator-node-name">
                                  {pos.posId}
                                </span>
                                {/* {pos.status==='active'&&<Popover
                                  placement="rightBottom"
                                  content={
                                    <PosOptions
                                      aggregatorId={element.aggregatorPreferenceId}
                                      aggregatorName={element.aggregatorName}
                                      institutionId={institution.institutionId}
                                      institutionName={institution.institutionName}
                                      merchantGroupPreferenceId={
                                        merchantGroup.merchantGroupPreferenceId
                                      }
                                      merchantGroupName={
                                        merchantGroup.merchantGroupName
                                      }
                                      merchantAcquirerId={
                                        merchant.merchantAcquirerId
                                      }
                                      merchantAcquirerName={
                                        merchant.merchantAcquirerName
                                      }
                                      subMerchantId={
                                        subMerchant.subMerchantAcquirerId
                                      }
                                      subMerchantName={
                                        subMerchant.subMerchantAcquirerName
                                      }
                                      outletId={
                                        outlet.outletId
                                      }
                                      outletName={
                                        outlet.outletName
                                      }
    
                                    />
                                  }
                                >
                                  <img
                                    className="aggregator-options-icon"
                                    // src="/images/white-logo.png"
                                    src="/images/moreOptions.svg"
                                    alt="WadzPay Logo"
                                    title="WadzPay Logo"
                                  />
                                </Popover>} */}
                              </div>
                            ),
                            key: pos.posId,
                                }
                          }               
                    )}
                    }),
                  };
                }),
              }}),
            }}
          ),
        }}
      ),
    };
  });

  return <Tree treeData={aggregatorListTree} />;
};

export default AggregatorHierarchy;
