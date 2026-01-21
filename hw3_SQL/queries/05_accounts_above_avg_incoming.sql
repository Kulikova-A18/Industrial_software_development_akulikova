WITH incoming_totals AS (
    SELECT account_id, SUM(amount) AS total_incoming
    FROM transactions
    WHERE txn_type IN ('deposit', 'transfer_in')
    GROUP BY account_id
),
bank_avg AS (
    SELECT AVG(total_incoming) AS avg_incoming
    FROM incoming_totals
)
SELECT i.account_id, i.total_incoming
FROM incoming_totals i
CROSS JOIN bank_avg b
WHERE i.total_incoming > b.avg_incoming;