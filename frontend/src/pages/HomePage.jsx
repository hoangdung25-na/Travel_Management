import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import TourCard from '../components/TourCard';
import { tourService } from '../services/tourService';
import styles from './HomePage.module.css';
import { Search } from 'lucide-react';

export default function HomePage() {
  const [tours, setTours] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchTours();
  }, []);

  const fetchTours = async (keyword = '') => {
    setLoading(true);
    try {
      // Fake delay for animation demo
      await new Promise(resolve => setTimeout(resolve, 800));
      
      const response = await tourService.getTours(keyword);
      // Assuming response is structured as ApiResponse<Page<TourVm>>
      setTours(response.data.data?.content || []);
    } catch (error) {
      console.error('Failed to fetch tours', error);
      // Mock data if backend is not running
      setTours([
        { tourId: '1', code: 'T001', title: 'Hành trình di sản Miền Trung', minPrice: 4500000, availableSeats: 12 },
        { tourId: '2', code: 'T002', title: 'Khám phá Vịnh Hạ Long 3N2Đ', minPrice: 3200000, availableSeats: 5 },
        { tourId: '3', code: 'T003', title: 'Nghỉ dưỡng Phú Quốc Resort 5 Sao', minPrice: 8900000, availableSeats: 0 },
        { tourId: '4', code: 'T004', title: 'Khám phá Tây Bắc Mùa Lúa Chín', minPrice: 5600000, availableSeats: 8 },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchTours(search);
  };

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroOverlay} />
        <div className={styles.heroContent}>
          <motion.h1 
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, ease: "easeOut" }}
            className={styles.title}
          >
            Trải nghiệm thế giới<br />theo cách của bạn
          </motion.h1>
          
          <motion.p 
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2, ease: "easeOut" }}
            className={styles.subtitle}
          >
            Khám phá những điểm đến tuyệt vời với dịch vụ cao cấp nhất
          </motion.p>
          
          <motion.form 
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className={`${styles.searchBox} glass`}
            onSubmit={handleSearch}
          >
            <Search className={styles.searchIcon} />
            <input 
              type="text" 
              placeholder="Tìm kiếm điểm đến, tên tour..." 
              className={styles.searchInput}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <button type="submit" className={styles.searchBtn}>Tìm kiếm</button>
          </motion.form>
        </div>
      </section>

      <section className={styles.toursSection}>
        <div className={styles.container}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>Tour nổi bật</h2>
            <p className={styles.sectionSubtitle}>Những lựa chọn hàng đầu cho chuyến đi tiếp theo của bạn</p>
          </div>

          {loading ? (
            <div className={styles.loadingContainer}>
              <div className={styles.spinner}></div>
            </div>
          ) : (
            <div className={styles.tourGrid}>
              {tours.map((tour, index) => (
                <TourCard key={tour.tourId} tour={tour} index={index} />
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
