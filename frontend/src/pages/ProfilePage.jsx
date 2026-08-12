import { useState, useContext, useEffect } from 'react';
import { AuthContext } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import styles from './ProfilePage.module.css';

const ProfilePage = () => {
  const { user, setUser } = useContext(AuthContext);
  const [formData, setFormData] = useState({
    fullName: '',
    phoneNumber: '',
    dateOfBirth: '',
    emergencyContact: ''
  });
  const [message, setMessage] = useState({ text: '', type: '' });

  useEffect(() => {
    if (user) {
      setFormData({
        fullName: user.fullName || '',
        phoneNumber: user.phoneNumber || '',
        dateOfBirth: user.dateOfBirth || '',
        emergencyContact: user.emergencyContact || ''
      });
    }
  }, [user]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Need to use FormData because of potential multipart/form-data for avatar
      const formPayload = new FormData();
      // Since backend expects request part 'profile' as JSON string or individual fields, 
      // let's construct it. According to AuthController: @RequestPart("profile") UpdateProfileRequest
      formPayload.append('profile', new Blob([JSON.stringify(formData)], { type: 'application/json' }));
      
      const response = await apiClient.put('/auth/me', formPayload, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      
      setUser(response.data.data);
      setMessage({ text: 'Cập nhật hồ sơ thành công!', type: 'success' });
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật.', type: 'error' });
    }
  };

  if (!user) return <div className={styles.container}>Vui lòng đăng nhập...</div>;

  return (
    <div className={styles.container}>
      <div className={styles.profileCard}>
        <h2>Hồ Sơ Cá Nhân</h2>
        {message.text && (
          <div className={`${styles.alert} ${styles[message.type]}`}>
            {message.text}
          </div>
        )}
        <div className={styles.userInfo}>
          <div className={styles.avatarPlaceholder}>
            {user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
          </div>
          <h3>{user.email}</h3>
          <p>Loại tài khoản: {user.accountType}</p>
        </div>
        
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.formGroup}>
            <label htmlFor="fullName">Họ và Tên</label>
            <input
              type="text"
              id="fullName"
              value={formData.fullName}
              onChange={handleChange}
            />
          </div>
          <div className={styles.formGroup}>
            <label htmlFor="phoneNumber">Số điện thoại</label>
            <input
              type="tel"
              id="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
            />
          </div>
          <div className={styles.formGroup}>
            <label htmlFor="dateOfBirth">Ngày sinh</label>
            <input
              type="date"
              id="dateOfBirth"
              value={formData.dateOfBirth}
              onChange={handleChange}
            />
          </div>
          <div className={styles.formGroup}>
            <label htmlFor="emergencyContact">Liên hệ khẩn cấp</label>
            <input
              type="text"
              id="emergencyContact"
              value={formData.emergencyContact}
              onChange={handleChange}
            />
          </div>
          <button type="submit" className={styles.saveBtn}>Lưu Thay Đổi</button>
        </form>
      </div>
    </div>
  );
};

export default ProfilePage;
