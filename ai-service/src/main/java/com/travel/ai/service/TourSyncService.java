package com.travel.ai.service;

public interface TourSyncService {
    /**
     * Xóa các bản ghi mock/giả lập cũ và đồng bộ toàn bộ Tour thật từ tour-service sang Vector DB của AI
     * @return Số lượng tour thật đã đồng bộ thành công
     */
    int syncAllToursFromTourService();
}
