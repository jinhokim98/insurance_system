import { useEffect, useState } from "react";
import { Link, useOutletContext } from "react-router-dom";
import {
  getReceipts,
  receiptAmount,
  receiptTime,
  type Receipt,
} from "../api/receipts";
import type { CustomerSelection } from "./ProductLayout";
import styles from "./Products.module.css";

function CustomerReceipts({ customerId }: { customerId: number }) {
  const [receipts, setReceipts] = useState<Receipt[] | null>(null);
  const [error, setError] = useState(false);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    getReceipts(customerId, controller.signal)
      .then((data) => {
        if (!controller.signal.aborted) setReceipts(data);
      })
      .catch(() => {
        if (!controller.signal.aborted) setError(true);
      });
    return () => controller.abort();
  }, [customerId, attempt]);
  if (error)
    return (
      <section className={styles.state} role="alert">
        <p>접수 내역을 불러오지 못했습니다.</p>
        <button
          className={styles.button}
          onClick={() => {
            setError(false);
            setAttempt((value) => value + 1);
          }}
        >
          다시 시도
        </button>
      </section>
    );
  if (receipts === null)
    return <p role="status">접수 내역을 불러오는 중입니다…</p>;
  if (!receipts.length)
    return (
      <section className={styles.state}>
        <h2>접수 내역이 없습니다.</h2>
        <p>사고가 발생했다면 접수 작성에서 내용을 입력해 주세요.</p>
      </section>
    );
  return (
    <>
      <p className={styles.count}>전체 {receipts.length}건</p>
      <ul className={styles.grid}>
        {receipts.map((receipt) => (
          <li key={receipt.id} className={styles.card}>
            <h2>
              <Link to={`/receipts/${receipt.id}`}>접수 #{receipt.id}</Link>
            </h2>
            <dl className={styles.facts}>
              <div>
                <dt>계약</dt>
                <dd>#{receipt.contractId}</dd>
              </div>
              <div>
                <dt>사고 시각 (한국 시간)</dt>
                <dd>{receiptTime(receipt.crashTime)}</dd>
              </div>
              <div>
                <dt>사고 장소</dt>
                <dd>{receipt.crashPlace}</dd>
              </div>
              <div>
                <dt>예상 금액</dt>
                <dd>{receiptAmount(receipt.billAmount)}</dd>
              </div>
            </dl>
            <Link to={`/receipts/${receipt.id}`}>접수 상세 보기</Link>
          </li>
        ))}
      </ul>
    </>
  );
}
export default function ReceiptListPage() {
  const { customerId, customerName } = useOutletContext<CustomerSelection>();
  return (
    <>
      <div className={styles.intro}>
        <h1>내 접수</h1>
        <p>{customerName}님의 접수 내역을 확인하세요.</p>
        <Link className={styles.button} to="/receipts/new">
          접수 작성
        </Link>
      </div>
      <CustomerReceipts key={customerId} customerId={customerId} />
    </>
  );
}
