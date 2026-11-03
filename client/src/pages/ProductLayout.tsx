import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useNavigation } from 'react-router-dom'
import { getCustomers, type Customer } from '../api/customers'
import styles from './Products.module.css'

export interface CustomerSelection { customerId: number; customerName: string }

export default function ProductLayout() {
  const navigation = useNavigation()
  const [customerId, setCustomerId] = useState(() => {
    try {
      const saved = Number(localStorage.getItem('selectedCustomerId'))
      return [1, 2, 3].includes(saved) ? saved : 1
    } catch { return 1 }
  })
  const [open, setOpen] = useState(false)
  const [customers, setCustomers] = useState<Customer[]>([])
  const [customerError, setCustomerError] = useState(false)
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    getCustomers(controller.signal).then(data => {
      setCustomers(data)
      setCustomerError(false)
    }).catch(() => { if (!controller.signal.aborted) setCustomerError(true) })
    return () => controller.abort()
  }, [attempt])
  const customerName = customers.find(customer => customer.id === customerId)?.name || `고객 ${customerId}`
  const loading = navigation.state === 'loading'

  function selectCustomer(id: number) {
    setCustomerId(id)
    try { localStorage.setItem('selectedCustomerId', String(id)) } catch { /* Selection still works without storage. */ }
    setOpen(false)
  }

  return (
    <div className={styles.app}>
      <header className={styles.header}>
        <Link to="/products" className={styles.brand}>보험 상품 안내</Link>
        <div className={styles.headerActions}>
          <nav aria-label="주 메뉴" className={styles.nav}>
            <NavLink to="/products">상품 목록</NavLink>
            <NavLink to="/contracts">내 계약</NavLink>
          </nav>
          <div className={styles.profile} onKeyDown={event => { if (event.key === 'Escape') setOpen(false) }} onBlur={event => { if (!event.currentTarget.contains(event.relatedTarget)) setOpen(false) }}>
            <button type="button" className={styles.profileButton} aria-expanded={open} aria-controls="profile-panel" onClick={() => setOpen(!open)}>프로필 · {customerName} ▾</button>
            {open && <div id="profile-panel" className={styles.profilePanel}>
              <p>조회할 고객 선택</p>
              {[1, 2, 3].map(id => <button type="button" key={id} aria-pressed={id === customerId} onClick={() => selectCustomer(id)}>
                {customers.find(customer => customer.id === id)?.name || `고객 ${id}`} <span>#{id}{id === customerId ? ' · 선택됨' : ''}</span>
              </button>)}
              {customerError && <div role="status">고객 이름을 불러오지 못했습니다.<button type="button" onClick={() => setAttempt(value => value + 1)}>다시 시도</button></div>}
            </div>}
          </div>
        </div>
      </header>
      <main className={styles.main} aria-busy={loading}>
        {loading && <p role="status" className={styles.loading}>불러오는 중입니다…</p>}
        <Outlet context={{ customerId, customerName } satisfies CustomerSelection} />
      </main>
    </div>
  )
}
