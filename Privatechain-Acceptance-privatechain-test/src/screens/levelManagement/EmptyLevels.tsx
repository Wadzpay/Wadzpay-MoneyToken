import { Button } from "antd";
import React from "react";

function EmptyLevels(props: any) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        alignContent: "center",
        marginTop: "16px",
      }}
    >
      <div
        style={{
          display: "flex",
          flexDirection: "row",
          justifyContent: "center",
        }}
      >
        {" "}
        <img src="/images/ic_noroles.svg"></img>
      </div>
      <p
        style={{
          display: "flex",
          flexDirection: "row",
          justifyContent: "center",
          fontWeight: 600,
        }}
      >
        It seems to be no Levels to manage
      </p>
      <div
        style={{
          display: "flex",
          flexDirection: "row",
          justifyContent: "center",
        }}
      >
        {" "}
        <Button
          className="role-empty-btn"
          onClick={() => {
            props.onCreateLevel();
            /* let temp=currentRoleLevel
temp.showCreate=true
              setCurrentRoleLevel(temp)
              setShowCreate(true)
 */
          }}
        >
          Create Level
        </Button>
      </div>
    </div>
  );
}

export default EmptyLevels;
