# --- !Ups
CREATE TABLE IF NOT EXISTS todo_comments (
  id      SERIAL PRIMARY KEY,
  text    TEXT NOT NULL,
  todo_id INTEGER REFERENCES todos (id)
);


INSERT INTO todo_comments (id, text, todo_id)
VALUES
  (DEFAULT, 'Foo Comment 1', 1),
  (DEFAULT, 'Foo Comment 2', 1),
  (DEFAULT, 'Bar Comment 1', 2),
  (DEFAULT, 'Baz Comment 1', 3);

# --- !Downs
DROP TABLE todo_comments;
