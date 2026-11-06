import { useEffect, useRef, useState, type FormEvent } from 'react'
import axios from 'axios'
import { useNavigate, useOutletContext } from 'react-router-dom'
import { getCustomers, type Customer } from '../api/customers'
import { createContract } from '../api/contracts'
import type { CustomerSelection } from './ProductLayout'
import styles from './Products.module.css'

function tomorrowInSeoul() {
  const parts = new Intl.DateTimeFormat('en-US', {
    timeZone: 'Asia/Seoul', year: 'numeric', month: '2-digit', day: '2-digit',
  }).formatToParts(new Date())
  const part = (name: string) => Number(parts.find(value => value.type === name)?.value)
  return new Date(Date.UTC(part('year'), part('month') - 1, part('day') + 1)).toISOString().slice(0, 10)
}

export default function EnrollmentForm({ productId }: { productId: number }) {
  const selection = useOutletContext<CustomerSelection>()
  const navigate = useNavigate()
  const [customerId, setCustomerId] = useState(selection.customerId)
  const [customers, setCustomers] = useState<Customer[] | null>(null)
  const [customerError, setCustomerError] = useState(false)
  const [attempt, setAttempt] = useState(0)
  const [vehicleNumber, setVehicleNumber] = useState('')
  const [coverageStartDate, setCoverageStartDate] = useState('')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')
  const submitting = useRef(false)
  const minimumDate = tomorrowInSeoul()

  useEffect(() => {
    const controller = new AbortController()
    getCustomers(controller.signal).then(data => {
      if (!controller.signal.aborted) setCustomers(data)
    }).catch(() => { if (!controller.signal.aborted) setCustomerError(true) })
    return () => controller.abort()
  }, [attempt])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (submitting.current) return
    if (!customers?.some(customer => customer.id === customerId)) {
      setError('가입할 고객을 선택해 주세요.')
      return
    }
    if (!vehicleNumber.trim() || coverageStartDate < tomorrowInSeoul()) {
      setError('차량번호를 입력하고 보장 시작일을 내일 이후로 선택해 주세요.')
      return
    }
    submitting.current = true
    setPending(true)
    setError('')
    try {
      const id = await createContract({ customerId, productId, vehicleNumber: vehicleNumber.trim(), coverageStartDate })
      selection.selectCustomer(customerId)
      await navigate(`/contracts/${id}`)
    } catch (error) {
      const status = axios.isAxiosError(error) ? error.response?.status : undefined
      setError(status === 400 ? '입력값을 확인해 주세요. 차량번호는 1~20자, 보장 시작일은 내일 이후여야 합니다.'
        : status === 404 ? '선택한 고객 또는 상품을 찾을 수 없습니다.'
        : '가입 결과를 확인하지 못했습니다. 내 계약에서 생성 여부를 확인한 후 다시 시도해 주세요.')
    } finally {
      submitting.current = false
      setPending(false)
    }
  }

  return <section className={styles.enrollment} aria-labelledby="enrollment-title">
    <h2 id="enrollment-title">보험 가입 신청</h2>
    <p>보장 시작일은 한국 시간 기준 내일부터 선택할 수 있습니다.</p>
    {customerError ? <div role="alert">고객 목록을 불러오지 못했습니다. <button type="button" onClick={() => { setCustomerError(false); setAttempt(value => value + 1) }}>다시 시도</button></div>
      : customers === null ? <p role="status">고객 목록을 불러오는 중입니다…</p>
      : customers.length === 0 ? <p>선택 가능한 샘플 고객이 없습니다.</p> : null}
    <form onSubmit={submit} aria-busy={pending}>
      <fieldset disabled={pending} className={styles.formFields}>
        <label htmlFor="customer">고객</label>
        <select id="customer" required value={customerId} onChange={event => setCustomerId(Number(event.target.value))} disabled={!customers?.length}>
          <option value="">고객을 선택해 주세요</option>
          {customers?.map(customer => <option key={customer.id} value={customer.id}>{customer.name} (#{customer.id})</option>)}
        </select>
        <label htmlFor="vehicle">차량번호</label>
        <input id="vehicle" required maxLength={20} placeholder="예: 11가1111" value={vehicleNumber} onChange={event => setVehicleNumber(event.target.value)} />
        <label htmlFor="coverage-start">보장 시작일</label>
        <input id="coverage-start" type="date" required min={minimumDate} value={coverageStartDate} onChange={event => setCoverageStartDate(event.target.value)} />
        {error && <p role="alert" className={styles.formError}>{error}</p>}
        <button className={styles.button} type="submit" disabled={pending || !customers?.length || customerError}>{pending ? '가입 신청 중…' : '가입 신청하기'}</button>
      </fieldset>
    </form>
  </section>
}
