build:
	find . -name "*.java" -print | xargs javac -d make_out
gen-ast:
	javac -d make_out src/com/ashwinchat/tool/GenerateAst.java
	cd make_out && java com/ashwinchat/tool/GenerateAst "../src/com/ashwinchat/jlox"
run:
	cd make_out && java com/ashwinchat/jlox/Lox $(file)
