CREATE EXTERNAL TABLE IF NOT EXISTS some_external_table (
	user_id string,
	male string,
	female string
) ROW FORMAT DELIMITED
FIELDS TERMINATED BY ',';

-- some comment

CREATE TABLE IF NOT EXISTS some_generated_data_table
ROW FORMAT DELIMITED 
FIELDS TERMINATED BY '\001' 
LOCATION '/tmp/some_datasource_export.csv' AS
SELECT b.user_id, b.age, count(distinct a.book_id) AS book_count,
		avg(a.price) AS average_price,
		avg(a.num_pages) AS average_num_pages
FROM some_external_table a JOIN some_other_external_table b ON a.book_id=b.book_id
GROUP BY b.user_id,b.age;

INSERT INTO TABLE some_generated_data_table
SELECT b.book_id, 'ALL' AS country, count(distinct a.book_id) AS books_count
FROM some_external_table a JOIN some_other_external_table b ON a.book_id=b.book_id
GROUP BY b.book_id;
