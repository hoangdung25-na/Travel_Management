import { apiClient, unwrap } from "../api-client";
import { UserVm, ApiResponse } from "../types";

export const adminService = {
  getUsers: (): Promise<UserVm[]> =>
    unwrap<UserVm[]>(
      apiClient.get<ApiResponse<UserVm[]>>("/api/v1/auth/admin/users")
    ),

  approveGuide: (userId: string): Promise<UserVm> =>
    unwrap<UserVm>(
      apiClient.put<ApiResponse<UserVm>>(
        `/api/v1/auth/admin/users/${userId}/status`,
        null,
        { params: { status: "ACTIVE" } }
      )
    ),
};
