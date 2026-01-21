SELECT a.account_id,
       COUNT(t.transaction_id) AS transaction_count,
       COALESCE(SUM(
           CASE
               WHEN t.txn_type IN ('withdrawal', 'transfer_out', 'fee') THEN t.amount
               ELSE 0
           END
       ), 0) AS total_debits
FROM accounts a
LEFT JOIN transactions t ON a.account_id = t.account_id
GROUP BY a.account_id;