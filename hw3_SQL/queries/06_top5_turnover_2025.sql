SELECT c.full_name, SUM(t.amount) AS total_turnover
FROM clients c
JOIN accounts a ON c.client_id = a.client_id
JOIN transactions t ON a.account_id = t.account_id
WHERE EXTRACT(YEAR FROM t.txn_date) = 2025
GROUP BY c.client_id, c.full_name
ORDER BY total_turnover DESC
LIMIT 5;