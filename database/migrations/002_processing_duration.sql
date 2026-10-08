-- VIVA GUIDE: Adds measured processing duration and test-clock offset fields used by completed-service ETA samples.
USE campus_queue;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='queue' AND column_name='processing_seconds')=0,'ALTER TABLE queue ADD COLUMN processing_seconds DOUBLE NULL','SELECT 1');
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='queue' AND column_name='processing_clock_offset_seconds')=0,'ALTER TABLE queue ADD COLUMN processing_clock_offset_seconds BIGINT NULL','SELECT 1');
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;
