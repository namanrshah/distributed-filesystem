all: compile
	@echo -e '[INFO] Done!'
clean:
	@echo -e '[INFO] Cleaning Up..'	
	@-rm -rf bin/proj/dfs/**/*.class

compile: 
	@echo -e '[INFO] Compiling the Source..'
	@mkdir -p bin
	@rm -rf bin/*
	@javac -d bin/ src/proj/dfs/**/*.java
	
	
