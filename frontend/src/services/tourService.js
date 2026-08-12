import apiClient from './apiClient';

export const tourService = {
  getTours: (keyword = '') => {
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    return apiClient.get(`/tours?${params.toString()}`);
  },
  
  getTourById: (id) => {
    return apiClient.get(`/tours/${id}`);
  }
};
