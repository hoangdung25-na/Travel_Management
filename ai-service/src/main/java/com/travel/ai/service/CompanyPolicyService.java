package com.travel.ai.service;

/**
 * Service quản lý nạp và đồng bộ Tri thức Chính sách của Doanh nghiệp vào Vector DB
 */
public interface CompanyPolicyService {
    /**
     * Nạp toàn bộ chính sách doanh nghiệp (hoàn hủy vé, đặt cọc, thanh toán, thủ tục...) vào Vector DB
     * @return Số lượng chính sách đã nạp
     */
    int initCompanyPolicies();
}
