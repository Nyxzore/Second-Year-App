-- Run on PostgreSQL if habit_categories does not exist yet.
-- categories, goal_categories are assumed to exist per project ERD.

CREATE TABLE IF NOT EXISTS habit_categories (
    habit_id INTEGER NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (habit_id, category_id)
);

CREATE INDEX IF NOT EXISTS idx_habit_categories_category_id ON habit_categories(category_id);
