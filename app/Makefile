.DEFAULT_GOAL := build-run

clean:
	./gradlew clean
        
build:
	./gradlew clean build
        
install:
	./gradlew clean install
        
test:
	./gradlew test
        
report:
	./gradlew jacocoTestReport
        
lint:
	./gradlew checkstyleMain checkstyleTest
        
update-deps:
	./gradlew useLatestVersions
        
build-run: build run
	
	
.PHONY: build
