import React from "react";
import { Link } from "react-router-dom";
import "./index.scss";
import { Col } from "antd";

const UserLevelModules = () => {
  const userLevelMenu = [
    {
      levelId: 1,
      levelName: "WADZPAY ADMIN",
      levelNumber: 0,
      imageUrl:
        "https://role-management-images.s3.me-south-1.amazonaws.com/ic_wadzpay_module_nav.svg",
      createdBy: 91,
      createdAt: "2024-06-11T07:56:53.094090Z",
      updatedBy: 91,
      updatedAt: "2024-06-18T04:59:42.528688Z",
      status: true,
    },
    {
      levelId: 2,
      levelName: "AGGREGATOR",
      levelNumber: 1,
      imageUrl:
        "https://role-management-images.s3.me-south-1.amazonaws.com/ic_aggregator.svg",
      createdBy: 91,
      createdAt: "2024-06-12T13:43:07.673843Z",
      updatedBy: 91,
      updatedAt: "2024-06-17T11:21:17.667008Z",
      status: true,
    },
    {
      levelId: 3,
      levelName: "INSTITUTION",
      levelNumber: 2,
      imageUrl:
        "https://role-management-images.s3.me-south-1.amazonaws.com/ic_institution.svg",
      createdBy: 91,
      createdAt: "2024-06-12T13:45:17.054188Z",
      updatedBy: 91,
      updatedAt: "2024-06-18T05:54:23.381124Z",
      status: true,
    },
    {
      levelId: 5,
      levelName: "MERCHANT GROUP",
      levelNumber: 3,
      imageUrl:
        "https://role-management-images.s3.me-south-1.amazonaws.com/ic_merchant_group.svg",
      createdBy: 91,
      createdAt: "2024-07-12T11:35:24.676474Z",
      updatedBy: 91,
      updatedAt: "2024-07-12T11:35:24.676474Z",
      status: true,
    },
    {
      levelId: 4,
      levelName: "MERCHANT",
      levelNumber: 4,
      imageUrl:
        "https://role-management-images.s3.me-south-1.amazonaws.com/ic_merchant.svg",
      createdBy: 91,
      createdAt: "2024-07-12T11:35:17.077281Z",
      updatedBy: 91,
      updatedAt: "2024-07-12T11:35:17.077281Z",
      status: true,
    },
    {
      levelId: 6,
      levelName: "SUB MERCHANT",
      levelNumber: 5,
      imageUrl:
        "https://role-management-images.s3.me-south-1.amazonaws.com/ic_sub_merchant.svg",
      createdBy: 91,
      createdAt: "2024-07-12T11:36:12.955096Z",
      updatedBy: 91,
      updatedAt: "2024-07-12T11:36:12.955096Z",
      status: true,
    },
    {
      levelId: 7,
      levelName: "OUTLET",
      levelNumber: 6,
      imageUrl:
        "https://role-management-images.s3.me-south-1.amazonaws.com/ic_outlet.svg",
      createdBy: 91,
      createdAt: "2024-07-12T11:36:32.307810Z",
      updatedBy: 91,
      updatedAt: "2024-07-12T11:36:32.307810Z",
      status: true,
    },
    {
      levelId: 8,
      levelName: "POS TERMINAL",
      levelNumber: 7,
      imageUrl:
        "https://role-management-images.s3.me-south-1.amazonaws.com/ic_pos_terminal.svg",
      createdBy: 91,
      createdAt: "2024-07-12T11:36:45.297087Z",
      updatedBy: 91,
      updatedAt: "2024-07-12T11:36:45.297087Z",
      status: true,
    },
  ];

  return (
    <>
      <div className="role-nav-col-title">
        <p
          style={{
            lineHeight: "64px",
            fontSize: "14px",
            marginLeft: "16px",
            fontWeight: 700,
            fontFamily: "Inter",
          }}
        >
          User Level Modules
        </p>
      </div>
      {userLevelMenu?.map((level: any, index: number) => {
        return (
          <div
            key={index}
            // className={
            //   currentRoleLevel.role === level.levelName
            //     ? "role-level-active"
            //     : "role-level"
            // }
          >
            <div
              className="level-container-role"
              // Uncomment the following style if needed
              // style={{ lineHeight: "40px", borderRadius: "1px", borderColor: "gray" }}
            >
              <img
                style={{ marginRight: "4px" }}
                src={level.imageUrl}
                height={20}
                width={20}
                alt={`Level ${index} icon`}
              />
              <span
                role="button"
                style={{
                  fontSize: "10px",
                  fontFamily: "Inter",
                  alignContent: "center",
                }}
              >
                L{index}.{level.levelName}
              </span>
            </div>
          </div>
        );
      })}
    </>
  );
};

export default UserLevelModules;
