The empty lib directory exists to document that the ScalaPersistenceFramework doesn't have any external dependencies 
besides:
1) Java 1.6 or greater
2) Scala 2.10


GitHib will not allow empty directories and I wanted to use a common build file that
would allow ScalaPersistenceFramework users to add other dependencies and build against 
the examples and/or tests.
