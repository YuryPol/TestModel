cd C:\Users\ypolyako\workspace\TestModel\scripts
"C:\Program Files\MySQL\MySQL Server 5.6\bin\mysql.exe" "--defaults-file=C:\ProgramData\MySQL\MySQL Server 5.6\my.ini" "--verbose" "-uroot" "-pIraAnna12" < "C:\Users\ypolyako\workspace\TestModel\sql\InitDemo.sql"

java -cp "C:/Program Files (x86)/MySQL/MySQL Connector J/mysql-connector-java-5.1.36-bin.jar";C:/Users/ypolyako/workspace/TestModel/target/TestModel-0.0.1-SNAPSHOT-jar-with-dependencies.jar ProcessInputInc

"C:\Program Files\MySQL\MySQL Server 5.6\bin\mysql.exe" "--defaults-file=C:\ProgramData\MySQL\MySQL Server 5.6\my.ini" "--verbose" "-uroot" "-pIraAnna12" < "C:\Users\ypolyako\workspace\TestModel\sql\RunDemo.sql"
