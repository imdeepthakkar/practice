CREATE TABLE job_audit (
    id INT PRIMARY KEY,
    job_name VARCHAR2(100),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR2(20),
    processed_count INT
);