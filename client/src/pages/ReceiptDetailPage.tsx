import { Link, useLoaderData } from 'react-router-dom'
import { type getReceipt, receiptAmount, receiptTime } from '../api/receipts'
import styles from './Products.module.css'

export default function ReceiptDetailPage() {
  const receipt = useLoaderData<typeof getReceipt>()
  return <><Link to="/receipts" className={styles.back}>← 내 접수 목록</Link>
    <article className={styles.detail}>
      <p className={styles.eyebrow}>사고 접수 상세</p><h1>접수 #{receipt.id}</h1>
      <dl className={styles.facts}>
        <div><dt>계약</dt><dd><Link to={`/contracts/${receipt.contractId}`}>계약 #{receipt.contractId}</Link></dd></div>
        <div><dt>사고 시각 (한국 시간)</dt><dd>{receiptTime(receipt.crashTime)}</dd></div>
        <div><dt>사고 장소</dt><dd>{receipt.crashPlace}</dd></div>
        <div><dt>예상 금액</dt><dd>{receiptAmount(receipt.billAmount)}</dd></div>
      </dl>
      <section className={styles.description}><h2>사고 경위</h2><p>{receipt.crashDetail}</p></section>
    </article></>
}
