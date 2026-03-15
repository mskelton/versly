AVD_NAME ?= Pixel_9_Pro
PACKAGE = dev.mskelton.versly
ACTIVITY = .MainActivity
GRADLE = ./gradlew

.PHONY: build install run clean lint test uitest emulator logs

build:
	$(GRADLE) assembleDebug

install:
	$(GRADLE) installDebug

run:
	$(GRADLE) installDebug
	adb shell am start -n $(PACKAGE)/$(ACTIVITY)

clean:
	$(GRADLE) clean

lint:
	$(GRADLE) lint

test:
	$(GRADLE) test

uitest:
	$(GRADLE) pixel6api34DebugAndroidTest

emulator:
	emulator -avd $(AVD_NAME)

logs:
	adb logcat --pid=$$(adb shell pidof $(PACKAGE))
