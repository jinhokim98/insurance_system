import { Link, Outlet, useNavigation } from 'react-router-dom'
import styles from './Products.module.css'

export default function ProductLayout() {
  const navigation = useNavigation()
  const loading = navigation.state === 'loading'

  return (
    <div className={styles.app}>
      <header className={styles.header}>
        <Link to="/products" className={styles.brand}>보험 상품 안내</Link>
        <nav aria-label="주 메뉴"><Link to="/products">상품 목록</Link></nav>
      </header>
      <main className={styles.main} aria-busy={loading}>
        {loading && <p role="status" className={styles.loading}>상품을 불러오는 중입니다…</p>}
        <Outlet />
      </main>
    </div>
  )
}
