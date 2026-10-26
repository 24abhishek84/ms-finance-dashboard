ALTER TABLE categories ADD COLUMN parent_category_id BIGINT;
ALTER TABLE categories ADD CONSTRAINT fk_categories_parent_category_id FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE SET NULL;
CREATE INDEX idx_categories_parent_category_id ON categories(parent_category_id);
