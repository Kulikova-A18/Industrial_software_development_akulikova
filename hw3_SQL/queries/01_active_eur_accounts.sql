SELECT *
FROM accounts
WHERE status = 'active'
  AND currency = 'EUR'
  AND opened_at > '2024-01-01'
ORDER BY opened_at;