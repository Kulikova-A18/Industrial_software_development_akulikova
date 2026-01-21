WITH client_txn_counts AS (
    SELECT c.client_id, c.full_name,
           COUNT(t.transaction_id) AS txn_count
    FROM clients c
    LEFT JOIN accounts a ON c.client_id = a.client_id
    LEFT JOIN transactions t ON a.account_id = t.account_id
        AND t.txn_date >= CURRENT_DATE - INTERVAL '90 days'
    GROUP BY c.client_id, c.full_name
)
SELECT full_name,
       CASE
           WHEN txn_count = 0 THEN 'inactive'
           WHEN txn_count BETWEEN 1 AND 5 THEN 'low'
           WHEN txn_count BETWEEN 6 AND 20 THEN 'medium'
           WHEN txn_count > 20 THEN 'high'
       END AS activity_level
FROM client_txn_counts;