# --- !Ups

CREATE TABLE IF NOT EXISTS todos (
  id   SERIAL PRIMARY KEY,
  text TEXT    NOT NULL,
  done BOOLEAN NOT NULL
);


INSERT INTO todos (id, text, done)
VALUES
  (DEFAULT, 'Do Foo', FALSE),
  (DEFAULT, 'Do Bar', FALSE),
  (DEFAULT, 'Do Baz', TRUE),
  (DEFAULT, 'Do Quix', TRUE);

# --- !Downs

DROP TABLE todos;

