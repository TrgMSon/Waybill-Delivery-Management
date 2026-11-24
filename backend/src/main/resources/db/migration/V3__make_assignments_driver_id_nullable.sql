-- Cho phép lượt gán chưa chỉ định tài xế (driver_id NULL)
ALTER TABLE assignments ALTER COLUMN driver_id DROP NOT NULL;
