-- Thêm hạn phản hồi cho lượt gán đơn: quá expires_at mà tài xế chưa phản hồi thì lượt gán hết hạn (EXPIRED)

ALTER TABLE assignments ADD COLUMN expires_at TIMESTAMPTZ;

ALTER TABLE assignments
    ADD CONSTRAINT chk_assignments_expires CHECK (expires_at > offered_at);

-- Dữ liệu đã có: coi như hạn phản hồi là 2 giờ sau thời điểm đề nghị
UPDATE assignments SET expires_at = offered_at + INTERVAL '2 hours';

ALTER TABLE assignments ALTER COLUMN expires_at SET NOT NULL;

-- Chỉnh lại index của assignments
DROP INDEX IF EXISTS idx_assignments_driver;
CREATE INDEX idx_assignments_shipment_id ON assignments (shipment_id);
CREATE INDEX idx_assignments_driver_status ON assignments (driver_id, status);
-- Index một phần: chỉ các lượt đang chờ phản hồi, phục vụ job quét lượt gán hết hạn
CREATE INDEX idx_assignments_offered_expires ON assignments (expires_at) WHERE status = 'OFFERED';
