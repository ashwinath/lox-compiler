BUILD_FLAGS = -Wall -Werror -Wno-unused-parameter -Wextra

build:
	@mkdir -p bin
	@gcc $(BUILD_FLAGS) \
		-std=c99 \
		-o bin/compiler \
		src/*.c
