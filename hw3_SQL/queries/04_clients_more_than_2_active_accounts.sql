SELECT c.full_name
FROM clients c
JOIN accounts a ON c.client_id = a.client_id
WHERE a.status = 'active'
GROUP BY c.client_id, c.full_name
HAVING COUNT(a.account_id) > 2;