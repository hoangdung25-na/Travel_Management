import { Link } from 'react-router-dom';
import { Compass, Globe, MessageCircle, Camera, Mail } from 'lucide-react';
import styles from './Footer.module.css';

export default function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.grid}>
          <div className={styles.brandSection}>
            <Link to="/" className={styles.logo}>
              <Compass className={styles.logoIcon} />
              <span>LuminaTravel</span>
            </Link>
            <p className={styles.description}>
              Mang đến những trải nghiệm du lịch cao cấp và độc bản, thiết kế riêng cho từng bước chân khám phá của bạn.
            </p>
            <div className={styles.socialLinks}>
              <a href="#" className={styles.socialBtn}><Globe size={20} /></a>
              <a href="#" className={styles.socialBtn}><MessageCircle size={20} /></a>
              <a href="#" className={styles.socialBtn}><Camera size={20} /></a>
            </div>
          </div>

          <div className={styles.linkSection}>
            <h4 className={styles.title}>Khám phá</h4>
            <ul className={styles.list}>
              <li><Link to="/tours" className={styles.link}>Danh sách Tour</Link></li>
              <li><Link to="/destinations" className={styles.link}>Điểm đến</Link></li>
              <li><Link to="/offers" className={styles.link}>Ưu đãi</Link></li>
              <li><Link to="/blog" className={styles.link}>Cẩm nang du lịch</Link></li>
            </ul>
          </div>

          <div className={styles.linkSection}>
            <h4 className={styles.title}>Hỗ trợ</h4>
            <ul className={styles.list}>
              <li><Link to="/contact" className={styles.link}>Liên hệ</Link></li>
              <li><Link to="/faq" className={styles.link}>Câu hỏi thường gặp</Link></li>
              <li><Link to="/terms" className={styles.link}>Điều khoản dịch vụ</Link></li>
              <li><Link to="/privacy" className={styles.link}>Bảo mật thông tin</Link></li>
            </ul>
          </div>

          <div className={styles.linkSection}>
            <h4 className={styles.title}>Đăng ký nhận tin</h4>
            <p className={styles.newsletterText}>Nhận những thông tin ưu đãi mới nhất từ chúng tôi.</p>
            <form className={styles.newsletterForm} onSubmit={(e) => e.preventDefault()}>
              <div className={styles.inputWrapper}>
                <Mail size={20} className={styles.inputIcon} />
                <input type="email" placeholder="Email của bạn" className={styles.input} />
              </div>
              <button type="submit" className={styles.submitBtn}>Gửi</button>
            </form>
          </div>
        </div>

        <div className={styles.bottom}>
          <p>&copy; {new Date().getFullYear()} LuminaTravel. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
