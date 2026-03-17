-- Очистка существующих таблиц (если есть)
DROP TABLE IF EXISTS loans CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS readers CASCADE;

-- Удаление индексов
DROP INDEX IF EXISTS idx_books_title;
DROP INDEX IF EXISTS idx_books_author;
DROP INDEX IF EXISTS idx_readers_email;
DROP INDEX IF EXISTS idx_loans_book_id;
DROP INDEX IF EXISTS idx_loans_reader_id;
DROP INDEX IF EXISTS idx_loans_dates;
DROP INDEX IF EXISTS idx_loans_return_date;