-- Kích hoạt Extension pgvector nếu chưa có
CREATE EXTENSION IF NOT EXISTS vector;

-- Bảng lưu trữ đoạn văn bản & Vector Embedding (768 chiều cho Google Gemini text-embedding-004)
CREATE TABLE IF NOT EXISTS tour_embeddings (
    id UUID PRIMARY KEY,
    tour_id UUID,
    chunk_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(768),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Đảm bảo cột embedding tồn tại nếu bảng đã được Hibernate tạo trước
ALTER TABLE tour_embeddings ADD COLUMN IF NOT EXISTS embedding vector(768);

-- Index lọc theo tour_id
CREATE INDEX IF NOT EXISTS idx_tour_embeddings_tour_id ON tour_embeddings(tour_id);

-- Chỉ mục HNSW cho Vector Cosine Similarity Search siêu tốc
CREATE INDEX IF NOT EXISTS idx_tour_embeddings_hnsw 
ON tour_embeddings USING hnsw (embedding vector_cosine_ops);
