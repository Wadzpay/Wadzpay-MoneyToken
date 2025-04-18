import { useSet } from ".";
import { EndpointKey, useApi } from "./constants";
import { RoleCreate } from "./models";

export const useCreateRole = () =>
  useSet<RoleCreate>(EndpointKey.CREATE_ROLE, useApi().createRole(), "POST", {});
  export const useGetRoleList = () =>
  useSet([EndpointKey.ROLE_LIST], useApi().roleList(), "POST");
  export const useGetUserRoleList = () =>
  useSet([EndpointKey.USER_ROLE_LIST], useApi().userRoleList(), "POST");

  export const useUpdateRole = () =>
  useSet<RoleCreate>(EndpointKey.UPDATE_ROLE, useApi().updateRole(), "POST", {});
