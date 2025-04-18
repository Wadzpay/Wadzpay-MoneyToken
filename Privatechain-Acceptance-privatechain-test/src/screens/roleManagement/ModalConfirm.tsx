//import { Modal }  from 'antd'
import { t } from 'i18next'
import React, { useEffect } from 'react'
import { Modal } from 'react-bootstrap'
import BlockUI from 'src/helpers/BlockUI'

function ModalConfirm(props:any) {
    
  return (
    <div>  
        <Modal centered={true} style={{width:"543px",height:"fit-content",top:"25%",left:"30%"}} show={props.deactivateConfirm} onHide={()=>props.setShowModalDeActive(false)}/* onHide={props.setShowModalDeActive(false)} */ backdrop="static">
{/*         <BlockUI blocking={props.deactivateConfirm} title="submitting" />
 */}        <Modal.Header closeButton>
          <Modal.Title style={{fontSize:"18px",fontFamily:"Inter",'fontWeight': '600'}}>{t("Role-Management.deActivate.header")}</Modal.Title>
        </Modal.Header> 
        <Modal.Body>

        <p style={{fontSize:"14px",fontFamily:"Inter",'fontWeight': '600' }}>
        {t("Role-Management.deActivate.title",{role:`"${props.role?.roleName}" (${props?.role?.roleId})`}) }   </p>
       <p style={{fontSize:"12px",fontFamily:"Inter" }}> {t("Role-Management.deActivate.body")}
      </p>
  
          </Modal.Body>
          <Modal.Footer style={{fontSize:"14px",fontFamily:"Inter",height:"fit-content" }}>
              <button
                type="submit"
                className="btn btn-outline-secondary btn-sm"
            
                onClick={() =>
                     props.setShowModalDeActive(true)}
                data-testid="confirmButton"              >
                <p style={{fontFamily:"rubik",margin:"3px 0px 3px 0px",paddingBottom:"0px"}}>{t("Confirm")}</p>
              </button>
              <button
                className="btn btn-warning"
                style={{ marginLeft: "2%" }}
                onClick={() => props.setShowModalDeActive(false)}
                data-testid="cancelConfirmButton"
              >
                {t("Cancel")}
              </button>
            </Modal.Footer>

          </Modal>
      
  </div>
  )
}

export default ModalConfirm