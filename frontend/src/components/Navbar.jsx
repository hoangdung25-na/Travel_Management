import { Link, useNavigate } from 'react-router-dom';
import { Compass, User, Menu, LogOut } from 'lucide-react';
import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import styles from './Navbar.module.css';

export default function Navbar() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className={`${styles.navbar} glass`}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>
          <Compass className={styles.logoIcon} />
          <span>LuminaTravel</span>
        </Link>
        
        <nav className={styles.navLinks}>
          <Link to="/" className={styles.link}>Khám phá</Link>
          <Link to="/destinations" className={styles.link}>Điểm đến</Link>
          <Link to="/about" className={styles.link}>Về chúng tôi</Link>
        </nav>

        <div className={styles.actions}>
          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <Link to="/profile" className={styles.link} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <User size={20} />
                <span style={{ fontSize: '0.9rem' }}>{user.fullName || user.email}</span>
              </Link>
              <button onClick={handleLogout} className={styles.iconBtn} title="Đăng xuất">
                <LogOut size={20} />
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <Link to="/login" className={styles.link}>Đăng nhập</Link>
              <Link to="/register" className={styles.link} style={{ fontWeight: 'bold' }}>Đăng ký</Link>
            </div>
          )}
          <button className={styles.menuBtn}>
            <Menu size={24} />
          </button>
        </div>
      </div>
    </header>
  );
}
