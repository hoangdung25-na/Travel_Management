import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { tourService } from '../services/tourService';
import styles from './TourDetailPage.module.css';
import { ArrowLeft, MapPin, Calendar, Clock, CheckCircle } from 'lucide-react';

export default function TourDetailPage() {
  const { id } = useParams();
  const [tour, setTour] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const response = await tourService.getTourById(id);
        setTour(response.data.data);
      } catch (error) {
        console.error('Failed to fetch tour details', error);
        // Mock data
        setTour({
          tourId: id,
          title: 'Khám phá Vịnh Hạ Long 3N2Đ',
          description: 'Hành trình tuyệt vời trên du thuyền 5 sao, tham quan hang Sửng Sốt, chèo kayak tại hang Luồn.',
          code: 'T002',
          schedules: [
            { scheduleId: 's1', departureTime: '2026-10-10T08:00:00Z', availableSeats: 5 }
          ],
          itineraries: [
            { dayNumber: 1, title: 'Hà Nội - Hạ Long', content: 'Xe đón đoàn khởi hành đi Hạ Long.' },
            { dayNumber: 2, title: 'Vịnh Hạ Long', content: 'Tham quan hang động, tắm biển.' },
            { dayNumber: 3, title: 'Hạ Long - Hà Nội', content: 'Mua sắm đặc sản và trở về.' }
          ]
        });
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  if (loading) {
    return <div className={styles.loading}><div className="spinner"></div></div>;
  }

  if (!tour) return <div className={styles.error}>Tour không tồn tại</div>;

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className={styles.page}
    >
      <div className={styles.container}>
        <Link to="/" className={styles.backBtn}>
          <ArrowLeft size={20} />
          <span>Quay lại</span>
        </Link>
        
        <div className={styles.header}>
          <div className={styles.meta}>
            <span className={styles.tag}>{tour.code}</span>
          </div>
          <h1 className={styles.title}>{tour.title}</h1>
        </div>

        <div className={styles.grid}>
          <div className={styles.mainContent}>
            <section className={styles.section}>
              <h2>Tổng quan</h2>
              <p className={styles.description}>{tour.description}</p>
            </section>

            <section className={styles.section}>
              <h2>Lịch trình</h2>
              <div className={styles.itineraryList}>
                {tour.itineraries?.map((itinerary) => (
                  <div key={itinerary.dayNumber} className={styles.itineraryItem}>
                    <div className={styles.dayIndicator}>
                      <div className={styles.dayDot}></div>
                      <div className={styles.dayLine}></div>
                    </div>
                    <div className={styles.dayContent}>
                      <h3>Ngày {itinerary.dayNumber}: {itinerary.title}</h3>
                      <p>{itinerary.content}</p>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </div>

          <div className={styles.sidebar}>
            <div className={`${styles.bookingCard} glass`}>
              <h3>Đặt chỗ ngay</h3>
              <p className={styles.bookingSub}>Chọn lịch khởi hành phù hợp</p>
              
              <div className={styles.schedules}>
                {tour.schedules?.map((schedule) => (
                  <div key={schedule.scheduleId} className={styles.scheduleOption}>
                    <div className={styles.scheduleInfo}>
                      <Calendar size={16} />
                      <span>{new Date(schedule.departureTime).toLocaleDateString('vi-VN')}</span>
                    </div>
                    <div className={styles.seatsInfo}>
                      <span>{schedule.availableSeats} chỗ trống</span>
                    </div>
                    <button 
                      className={styles.bookBtn} 
                      disabled={schedule.availableSeats === 0}
                    >
                      {schedule.availableSeats > 0 ? 'Chọn' : 'Hết chỗ'}
                    </button>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}
