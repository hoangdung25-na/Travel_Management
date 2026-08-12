import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Calendar, Users, ArrowRight } from 'lucide-react';
import styles from './TourCard.module.css';

export default function TourCard({ tour, index }) {
  // Format currency
  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  };

  return (
    <motion.div 
      className={styles.card}
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: index * 0.1 }}
      whileHover={{ y: -8, transition: { duration: 0.2 } }}
    >
      <div className={styles.imageContainer}>
        {/* Placeholder gradient since we don't have images yet */}
        <div className={styles.imagePlaceholder} />
        <div className={styles.badge}>
          {tour.availableSeats > 0 ? 'Còn chỗ' : 'Hết chỗ'}
        </div>
      </div>
      
      <div className={styles.content}>
        <div className={styles.meta}>
          <span className={styles.code}>{tour.code}</span>
        </div>
        
        <h3 className={styles.title}>{tour.title}</h3>
        
        <div className={styles.details}>
          <div className={styles.detailItem}>
            <Users size={16} />
            <span>{tour.availableSeats} ghế trống</span>
          </div>
        </div>
        
        <div className={styles.footer}>
          <div className={styles.price}>
            <span className={styles.priceLabel}>Từ</span>
            <span className={styles.priceValue}>{formatPrice(tour.minPrice)}</span>
          </div>
          
          <Link to={`/tours/${tour.tourId}`} className={styles.btn}>
            <ArrowRight size={20} />
          </Link>
        </div>
      </div>
    </motion.div>
  );
}
