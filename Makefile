all: clean compile
	@echo -e '[INFO] Done!'
clean:
	@echo -e '[INFO] Cleaning Up..'	
	@-rm -rf bin/proj/**/**/*.class

compile: 
	@echo -e '[INFO] Compiling the Source..'
	@mkdir -p bin
	@javac -d bin src/proj/**/**/*.java
	
	
